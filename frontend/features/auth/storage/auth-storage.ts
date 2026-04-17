import AsyncStorage from '@react-native-async-storage/async-storage';

import type { StoredAuthSession } from '@/features/auth/types';

const AUTH_SESSION_KEY = 'healthtracker.auth.session';

export async function loadStoredSession(): Promise<StoredAuthSession | null> {
  const raw = await AsyncStorage.getItem(AUTH_SESSION_KEY);

  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as StoredAuthSession;
  } catch {
    await AsyncStorage.removeItem(AUTH_SESSION_KEY);
    return null;
  }
}

export async function saveStoredSession(session: StoredAuthSession): Promise<void> {
  await AsyncStorage.setItem(AUTH_SESSION_KEY, JSON.stringify(session));
}

export async function clearStoredSession(): Promise<void> {
  await AsyncStorage.removeItem(AUTH_SESSION_KEY);
}
