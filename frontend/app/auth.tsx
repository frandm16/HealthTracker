import * as AuthSession from 'expo-auth-session';
import * as WebBrowser from 'expo-web-browser';
import { useEffect, useState } from 'react';

import { AuthHomeScreen } from '@/components/auth/auth-home-screen';
import { useAuth } from '@/hooks/use-auth';
import { getGoogleClientId, getMissingAuthConfig } from '@/lib/config';

WebBrowser.maybeCompleteAuthSession();

const discovery = {
  authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth',
};

export default function AuthScreen() {
  const { session, signInWithGoogleIdToken, refreshCurrentUser, signOut, status } = useAuth();
  const [authBusy, setAuthBusy] = useState(false);
  const [message, setMessage] = useState<string>('Configura el login y valida el flujo completo con el backend.');

  const missingConfig = getMissingAuthConfig();
  const authEnabled = missingConfig.length === 0;
  const googleClientId = authEnabled ? getGoogleClientId() : 'missing-google-client-id';

  const [request, response, promptAsync] = AuthSession.useAuthRequest(
    {
      clientId: googleClientId,
      responseType: AuthSession.ResponseType.IdToken,
      scopes: ['openid', 'profile', 'email'],
      usePKCE: false,
      redirectUri: AuthSession.makeRedirectUri({
        scheme: 'personalapp',
        path: 'oauthredirect',
      }),
      extraParams: {
        nonce: 'healthtracker-nonce',
        prompt: 'select_account',
      },
    },
    discovery,
  );

  useEffect(() => {
    if (!response) {
      return;
    }

    if (response.type === 'error') {
      setAuthBusy(false);
      setMessage(`Google devolvio un error: ${response.error?.message ?? 'error desconocido'}.`);
      return;
    }

    if (response.type !== 'success') {
      setAuthBusy(false);
      return;
    }

    const idToken = response.params.id_token;
    if (!idToken) {
      setAuthBusy(false);
      setMessage('Google no devolvio un idToken. Revisa el client ID y el flujo de Expo.');
      return;
    }

    void (async () => {
      try {
        const backendSession = await signInWithGoogleIdToken(idToken);
        setMessage(`Sesion iniciada como ${backendSession.user.email ?? backendSession.user.name}.`);
      } catch (error) {
        setMessage(error instanceof Error ? error.message : 'No se pudo iniciar sesion contra el backend.');
      } finally {
        setAuthBusy(false);
      }
    })();
  }, [response, signInWithGoogleIdToken]);

  async function handleGoogleSignIn() {
    if (!authEnabled || !request) {
      setMessage(`Faltan variables: ${missingConfig.join(', ')}.`);
      return;
    }

    setAuthBusy(true);
    setMessage('Abriendo Google Sign-In...');

    try {
      await promptAsync();
    } catch (error) {
      setAuthBusy(false);
      setMessage(error instanceof Error ? error.message : 'No se pudo abrir el flujo de Google.');
    }
  }

  async function handleRefreshUser() {
    setAuthBusy(true);
    try {
      await refreshCurrentUser();
      setMessage('Backend verificado. Sesion y perfil sincronizados.');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo cargar /api/auth/me.');
    } finally {
      setAuthBusy(false);
    }
  }

  async function handleLogout() {
    setAuthBusy(true);
    try {
      await signOut();
      setMessage('Sesion eliminada en la app.');
    } catch (error) {
      setMessage(error instanceof Error ? error.message : 'No se pudo cerrar la sesion.');
    } finally {
      setAuthBusy(false);
    }
  }

  return (
    <AuthHomeScreen
      authBusy={authBusy}
      authEnabled={authEnabled}
      message={message}
      missingConfig={missingConfig}
      onGoogleSignIn={handleGoogleSignIn}
      onLogout={handleLogout}
      onRefreshUser={handleRefreshUser}
      requestReady={Boolean(request)}
      session={session}
      status={status}
    />
  );
}
