import { createContext, useCallback, useEffect, useState, type PropsWithChildren } from 'react';

import { fetchMe, logout, refreshSession, signInWithGoogle, type GoogleIdTokenSignInPayload } from '@/features/auth/api/auth-api';
import { signOutFromGoogleNative } from '@/features/auth/native/google-sign-in';
import { clearStoredSession, loadStoredSession, saveStoredSession } from '@/features/auth/storage/auth-storage';
import { isAccessTokenExpired, type StoredAuthSession } from '@/features/auth/types';
import { ApiError } from '@/lib/api-client';

type AuthStatus = 'loading' | 'authenticated' | 'unauthenticated';

type AuthContextValue = {
  status: AuthStatus;
  session: StoredAuthSession | null;
  getAccessToken: (forceRefresh?: boolean) => Promise<string>;
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

  const signInWithGoogleIdToken = useCallback(async (payload: GoogleIdTokenSignInPayload): Promise<StoredAuthSession> => {
    const nextSession = await signInWithGoogle(payload);
    await saveStoredSession(nextSession);
    setSession(nextSession);
    setStatus('authenticated');
    return nextSession;
  }, []);

  const expireSession = useCallback(async () => {
    await clearStoredSession();
    setSession(null);
    setStatus('unauthenticated');
  }, []);

  const renewSession = useCallback(async (currentSession: StoredAuthSession): Promise<StoredAuthSession> => {
    try {
      const nextSession = await refreshSession(currentSession.refreshToken);
      await saveStoredSession(nextSession);
      setSession(nextSession);
      setStatus('authenticated');
      return nextSession;
    } catch (error) {
      await expireSession();
      throw error;
    }
  }, [expireSession]);

  const getAccessToken = useCallback(async (forceRefresh = false): Promise<string> => {
    if (!session) {
      throw new Error('Please sign in.');
    }

    const nextSession = forceRefresh || isAccessTokenExpired(session) ? await renewSession(session) : session;
    return nextSession.accessToken;
  }, [renewSession, session]);

  const refreshCurrentUser = useCallback(async (): Promise<void> => {
    if (!session) {
      throw new Error('Please sign in.');
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
  }, [renewSession, session]);

  const signOut = useCallback(async (): Promise<void> => {
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
  }, [session?.refreshToken]);

  return (
    <AuthContext.Provider
      value={{
        status,
        session,
        getAccessToken,
        signInWithGoogleIdToken,
        refreshCurrentUser,
        signOut,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
}
