import * as SecureStore from 'expo-secure-store';

import type { StoredAuthSession } from '@/features/auth/types';

const AUTH_SESSION_KEY = 'healthtracker.auth.session';

export async function loadStoredSession(): Promise<StoredAuthSession | null> {
  const raw = await SecureStore.getItemAsync(AUTH_SESSION_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredAuthSession;
  } catch {
    await SecureStore.deleteItemAsync(AUTH_SESSION_KEY);
    return null;
  }
}

export async function saveStoredSession(session: StoredAuthSession): Promise<void> {
  await SecureStore.setItemAsync(AUTH_SESSION_KEY, JSON.stringify(session));
}

export async function clearStoredSession(): Promise<void> {
  await SecureStore.deleteItemAsync(AUTH_SESSION_KEY);
}
