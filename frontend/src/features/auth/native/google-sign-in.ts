import {
  GoogleSignin,
  isCancelledResponse,
  isErrorWithCode,
  isSuccessResponse,
  statusCodes,
} from '@react-native-google-signin/google-signin';
import { Platform } from 'react-native';

import { getGoogleWebClientId } from '@/lib/config';

let configured = false;

function ensureConfigured() {
  if (configured) {
    return;
  }

  GoogleSignin.configure({
    webClientId: getGoogleWebClientId(),
    offlineAccess: false,
    scopes: ['email', 'profile'],
  });

  configured = true;
}

export async function signInWithGoogleNative(): Promise<string> {
  if (Platform.OS !== 'android') {
    throw new Error('Not supported.');
  }

  ensureConfigured();
  await GoogleSignin.hasPlayServices({ showPlayServicesUpdateDialog: true });

  const response = await GoogleSignin.signIn();

  if (isCancelledResponse(response)) {
    throw new Error('Canceled.');
  }

  if (!isSuccessResponse(response) || !response.data.idToken) {
    throw new Error('Sign-in failed.');
  }

  return response.data.idToken;
}

export async function signOutFromGoogleNative(): Promise<void> {
  ensureConfigured();

  try {
    await GoogleSignin.signOut();
  } catch (error) {
    if (
      isErrorWithCode(error) &&
      (error.code === statusCodes.SIGN_IN_REQUIRED || error.code === statusCodes.PLAY_SERVICES_NOT_AVAILABLE)
    ) {
      return;
    }

    throw error;
  }
}

export function toGoogleAuthError(error: unknown): Error {
  if (!isErrorWithCode(error)) {
    return error instanceof Error ? error : new Error('Sign-in failed.');
  }

  switch (error.code) {
    case statusCodes.IN_PROGRESS:
      return new Error('Please wait.');
    case statusCodes.PLAY_SERVICES_NOT_AVAILABLE:
      return new Error('Sign-in failed.');
    case statusCodes.SIGN_IN_CANCELLED:
      return new Error('Canceled.');
    case statusCodes.SIGN_IN_REQUIRED:
      return new Error('Please sign in.');
    default:
      return new Error('Sign-in failed.');
  }
}
