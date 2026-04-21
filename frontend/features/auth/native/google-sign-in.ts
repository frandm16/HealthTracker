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
    throw new Error('El login nativo de Google esta preparado solo para Android en esta fase.');
  }

  ensureConfigured();
  await GoogleSignin.hasPlayServices({ showPlayServicesUpdateDialog: true });

  const response = await GoogleSignin.signIn();

  if (isCancelledResponse(response)) {
    throw new Error('Inicio de sesion cancelado.');
  }

  if (!isSuccessResponse(response) || !response.data.idToken) {
    throw new Error('Google no devolvio un idToken valido.');
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
    return error instanceof Error ? error : new Error('No se pudo iniciar sesion con Google.');
  }

  switch (error.code) {
    case statusCodes.IN_PROGRESS:
      return new Error('Ya hay un inicio de sesion en curso.');
    case statusCodes.PLAY_SERVICES_NOT_AVAILABLE:
      return new Error('Google Play Services no esta disponible en este dispositivo.');
    case statusCodes.SIGN_IN_CANCELLED:
      return new Error('Inicio de sesion cancelado.');
    case statusCodes.SIGN_IN_REQUIRED:
      return new Error('Debes volver a seleccionar una cuenta de Google.');
    default:
      return new Error(error.message || 'No se pudo iniciar sesion con Google.');
  }
}
