import { Stack } from 'expo-router';
import * as AuthSession from 'expo-auth-session';
import * as React from 'react';
import { useEffect, useState } from 'react';
import { ActivityIndicator, Pressable, ScrollView, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { getGoogleClientId, getMissingAuthConfig } from '@/lib/config';
import { AuthContext } from '@/providers/auth-provider';

const discovery = {
  authorizationEndpoint: 'https://accounts.google.com/o/oauth2/v2/auth',
};

export default function AuthScreen() {
  const insets = useSafeAreaInsets();
  const auth = React.use(AuthContext);
  const [authBusy, setAuthBusy] = useState(false);
  const [message, setMessage] = useState('Inicia sesion con Google y valida el backend.');

  if (!auth) {
    throw new Error('Auth context is missing.');
  }

  const { session, signInWithGoogleIdToken, refreshCurrentUser, signOut, status } = auth;
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
    <>
      <Stack.Screen options={{ title: 'Auth' }} />
      <ScrollView
        contentInsetAdjustmentBehavior="automatic"
        contentContainerStyle={{
          paddingTop: 24,
          paddingRight: 20,
          paddingBottom: Math.max(insets.bottom, 24),
          paddingLeft: 20,
          gap: 16,
        }}
      >
        <View
          style={{
            gap: 6,
            padding: 20,
            borderRadius: 24,
            borderCurve: 'continuous',
            backgroundColor: '#fff7ed',
          }}
        >
          <Text selectable style={{ fontSize: 28, fontWeight: '700', color: '#111827' }}>
            Login simple
          </Text>
          <Text selectable style={{ fontSize: 15, lineHeight: 22, color: '#4b5563' }}>
            {message}
          </Text>
          {status === 'loading' ? <ActivityIndicator color="#ea580c" /> : null}
        </View>

        <View
          style={{
            gap: 8,
            padding: 20,
            borderRadius: 24,
            borderCurve: 'continuous',
            backgroundColor: '#ffffff',
          }}
        >
          <Text selectable style={{ fontSize: 16, fontWeight: '600', color: '#111827' }}>
            Estado
          </Text>
          <Text selectable style={{ color: '#374151' }}>
            {session ? `${session.user.name} | ${session.user.email ?? 'sin email'}` : 'Sin sesion'}
          </Text>
          <Text selectable style={{ color: '#6b7280' }}>
            {authEnabled ? 'Configuracion lista' : `Faltan: ${missingConfig.join(', ')}`}
          </Text>
        </View>

        <View style={{ gap: 12 }}>
          <Pressable
            style={{
              alignItems: 'center',
              padding: 16,
              borderRadius: 18,
              borderCurve: 'continuous',
              backgroundColor: !authEnabled || authBusy || !request ? '#fed7aa' : '#f97316',
            }}
            disabled={!authEnabled || authBusy || !request}
            onPress={handleGoogleSignIn}
          >
            <Text style={{ color: '#fff', fontSize: 16, fontWeight: '700' }}>
              {authBusy ? 'Procesando...' : 'Entrar con Google'}
            </Text>
          </Pressable>

          <Pressable
            style={{
              alignItems: 'center',
              padding: 16,
              borderRadius: 18,
              borderCurve: 'continuous',
              backgroundColor: session && !authBusy ? '#e5e7eb' : '#f3f4f6',
            }}
            disabled={!session || authBusy}
            onPress={handleRefreshUser}
          >
            <Text style={{ color: '#111827', fontSize: 15, fontWeight: '600' }}>Actualizar perfil</Text>
          </Pressable>

          <Pressable
            style={{
              alignItems: 'center',
              padding: 16,
              borderRadius: 18,
              borderCurve: 'continuous',
              backgroundColor: session && !authBusy ? '#fee2e2' : '#fef2f2',
            }}
            disabled={!session || authBusy}
            onPress={handleLogout}
          >
            <Text style={{ color: '#b91c1c', fontSize: 15, fontWeight: '600' }}>Cerrar sesion</Text>
          </Pressable>
        </View>
      </ScrollView>
    </>
  );
}
