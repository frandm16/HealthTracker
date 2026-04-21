import { createContext, useEffect, useState, type PropsWithChildren } from 'react';

import { fetchMe, logout, refreshSession, signInWithGoogle, type GoogleIdTokenSignInPayload } from '@/features/auth/api/auth-api';
import { signOutFromGoogleNative } from '@/features/auth/native/google-sign-in';
import { clearStoredSession, loadStoredSession, saveStoredSession } from '@/features/auth/storage/auth-storage';
import { isAccessTokenExpired, type StoredAuthSession } from '@/features/auth/types';
import { ApiError } from '@/lib/api-client';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  status: AuthStatus;
  session: StoredAuthSession | null;
  signInWithGoogleIdToken: (payload: GoogleIdTokenSignInPayload) => Promise<StoredAuthSession>;
  refreshCurrentUser: () => Promise<void>;
  signOut: () => Promise<void>;
};

export const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [status, setStatus] = useState<AuthStatus>('loading');
  const [session, setSession] = useState<StoredAuthSession | null>(null);

  useEffect(() => {
    let active = true;

    async function hydrateSession() {
      try {
        const storedSession = await loadStoredSession();

        if (!storedSession) {
          if (active) {
            setSession(null);
            setStatus('unauthenticated');
          }
          return;
        }

        const nextSession = isAccessTokenExpired(storedSession)
          ? await refreshSession(storedSession.refreshToken)
          : storedSession;

        if (!active) {
          return;
        }

        setSession(nextSession);
        setStatus('authenticated');
        await saveStoredSession(nextSession);
      } catch {
        await clearStoredSession();
        if (active) {
          setSession(null);
          setStatus('unauthenticated');
        }
      }
    }

    void hydrateSession();

    return () => {
      active = false;
    };
  }, []);

  async function signInWithGoogleIdToken(payload: GoogleIdTokenSignInPayload): Promise<StoredAuthSession> {
    const nextSession = await signInWithGoogle(payload);
    await saveStoredSession(nextSession);
    setSession(nextSession);
    setStatus('authenticated');
    return nextSession;
  }

  async function renewSession(currentSession: StoredAuthSession): Promise<StoredAuthSession> {
    const nextSession = await refreshSession(currentSession.refreshToken);
    await saveStoredSession(nextSession);
    setSession(nextSession);
    setStatus('authenticated');
    return nextSession;
  }

  async function refreshCurrentUser(): Promise<void> {
    if (!session) {
      throw new Error('No hay ninguna sesion iniciada.');
    }

    try {
      const nextSession = isAccessTokenExpired(session) ? await renewSession(session) : session;
      const user = await fetchMe(nextSession.accessToken);
      const updatedSession = { ...nextSession, user };

      await saveStoredSession(updatedSession);
      setSession(updatedSession);
      setStatus('authenticated');
    } catch (error) {
      if (error instanceof ApiError && error.status === 401 && !isAccessTokenExpired(session)) {
        const nextSession = await renewSession(session);
        const user = await fetchMe(nextSession.accessToken);
        const updatedSession = { ...nextSession, user };

        await saveStoredSession(updatedSession);
        setSession(updatedSession);
        setStatus('authenticated');
        return;
      }

      throw error;
    }
  }

  async function signOut(): Promise<void> {
    const refreshToken = session?.refreshToken;

    try {
      if (refreshToken) {
        await logout(refreshToken);
      }

      await signOutFromGoogleNative();
    } finally {
      await clearStoredSession();
      setSession(null);
      setStatus('unauthenticated');
    }
  }

  return (
    <AuthContext.Provider
      value={{
        status,
        session,
        signInWithGoogleIdToken,
        refreshCurrentUser,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
