import { createContext, useEffect, useState, type PropsWithChildren } from 'react';

import { fetchMe, logout, refreshSession, signInWithGoogle } from '@/features/auth/api/auth-api';
import { clearStoredSession, loadStoredSession, saveStoredSession } from '@/features/auth/storage/auth-storage';
import { isAccessTokenExpired, type StoredAuthSession } from '@/features/auth/types';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  status: AuthStatus;
  session: StoredAuthSession | null;
  signInWithGoogleIdToken: (idToken: string) => Promise<StoredAuthSession>;
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

  async function signInWithGoogleIdToken(idToken: string): Promise<StoredAuthSession> {
    const nextSession = await signInWithGoogle(idToken);
    await saveStoredSession(nextSession);
    setSession(nextSession);
    setStatus('authenticated');
    return nextSession;
  }

  async function refreshCurrentUser(): Promise<void> {
    if (!session) {
      throw new Error('No hay ninguna sesion iniciada.');
    }

    let nextSession = session;

    if (isAccessTokenExpired(session)) {
      nextSession = await refreshSession(session.refreshToken);
    }

    const user = await fetchMe(nextSession.accessToken);
    const updatedSession = { ...nextSession, user };

    await saveStoredSession(updatedSession);
    setSession(updatedSession);
    setStatus('authenticated');
  }

  async function signOut(): Promise<void> {
    const refreshToken = session?.refreshToken;

    try {
      if (refreshToken) {
        await logout(refreshToken);
      }
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
