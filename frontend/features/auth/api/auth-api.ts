import { apiRequest } from '@/lib/api-client';
import type { AuthUser, StoredAuthSession } from '@/features/auth/types';

type AuthResponse = {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: AuthUser;
};

export type GoogleIdTokenSignInPayload = {
  idToken: string;
};

function toStoredSession(response: AuthResponse): StoredAuthSession {
  return {
    accessToken: response.accessToken,
    refreshToken: response.refreshToken,
    expiresAt: new Date(Date.now() + response.expiresIn * 1000).toISOString(),
    user: response.user,
  };
}

export async function signInWithGoogle(payload: GoogleIdTokenSignInPayload): Promise<StoredAuthSession> {
  const response = await apiRequest<AuthResponse>('/api/auth/google/sign-in', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  return toStoredSession(response);
}

export async function refreshSession(refreshToken: string): Promise<StoredAuthSession> {
  const response = await apiRequest<AuthResponse>('/api/auth/refresh', {
    method: 'POST',
    body: JSON.stringify({ refreshToken }),
  });

  return toStoredSession(response);
}

export async function fetchMe(accessToken: string): Promise<AuthUser> {
  return apiRequest<AuthUser>('/api/auth/me', {
    method: 'GET',
    accessToken,
  });
}

export async function logout(refreshToken: string): Promise<void> {
  await apiRequest<void>('/api/auth/logout', {
    method: 'POST',
    body: JSON.stringify({ refreshToken }),
  });
}
