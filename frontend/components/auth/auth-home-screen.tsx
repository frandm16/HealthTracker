import {
  ActivityIndicator,
  Pressable,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import type { StoredAuthSession } from '@/features/auth/types';

type AuthHomeScreenProps = {
  message: string;
  authBusy: boolean;
  authEnabled: boolean;
  missingConfig: string[];
  requestReady: boolean;
  status: 'loading' | 'authenticated' | 'unauthenticated';
  session: StoredAuthSession | null;
  onGoogleSignIn: () => void;
  onRefreshUser: () => void;
  onLogout: () => void;
};

export function AuthHomeScreen({
  message,
  authBusy,
  authEnabled,
  missingConfig,
  requestReady,
  status,
  session,
  onGoogleSignIn,
  onRefreshUser,
  onLogout,
}: AuthHomeScreenProps) {
  return (
    <SafeAreaView style={styles.safeArea}>
      <ScrollView contentContainerStyle={styles.container}>
        <Text >Health Tracker Auth</Text>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Estado</Text>
          <Text style={styles.message}>{message}</Text>
          {status === 'loading' ? <ActivityIndicator color="#0f766e" /> : null}
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Configuracion requerida</Text>
          <Text style={styles.configLine}>Scheme app: personalapp</Text>
          <Text style={styles.configLine}>
            EXPO_PUBLIC_API_BASE_URL: {authEnabled ? 'configurada' : 'pendiente'}
          </Text>
          <Text style={styles.configLine}>
            EXPO_PUBLIC_GOOGLE_CLIENT_ID: {authEnabled ? 'configurada' : 'pendiente'}
          </Text>
          {missingConfig.length > 0 ? (
            <Text style={styles.warning}>Faltan: {missingConfig.join(', ')}</Text>
          ) : null}
        </View>

        <View style={styles.card}>
          <Text style={styles.cardTitle}>Sesion local</Text>
          {session ? (
            <>
              <Text style={styles.sessionLabel}>Usuario</Text>
              <Text style={styles.sessionValue}>{session.user.name}</Text>
              <Text style={styles.sessionValue}>{session.user.email ?? 'Sin email'}</Text>
              <Text style={styles.sessionLabel}>Expira</Text>
              <Text style={styles.sessionValue}>{session.expiresAt}</Text>
              <Text style={styles.sessionLabel}>Access token</Text>
              <Text style={styles.tokenPreview}>{session.accessToken.slice(0, 48)}...</Text>
            </>
          ) : (
            <Text style={styles.sessionValue}>Todavia no hay sesion guardada.</Text>
          )}
        </View>

        <View style={styles.actions}>
          <Pressable
            style={[styles.button, (!authEnabled || authBusy || !requestReady) && styles.buttonDisabled]}
            disabled={!authEnabled || authBusy || !requestReady}
            onPress={onGoogleSignIn}
          >
            <Text style={styles.buttonText}>{authBusy ? 'Procesando...' : 'Entrar con Google'}</Text>
          </Pressable>

          <Pressable
            style={[styles.secondaryButton, (!session || authBusy) && styles.buttonDisabled]}
            disabled={!session || authBusy}
            onPress={onRefreshUser}
          >
            <Text style={styles.secondaryButtonText}>Sincronizar /api/auth/me</Text>
          </Pressable>

          <Pressable
            style={[styles.ghostButton, (!session || authBusy) && styles.buttonDisabled]}
            disabled={!session || authBusy}
            onPress={onLogout}
          >
            <Text style={styles.ghostButtonText}>Cerrar sesion</Text>
          </Pressable>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flex: 1,
    backgroundColor: '#f4efe6',
  },
  container: {
    paddingHorizontal: 20,
    paddingVertical: 32,
    gap: 16,
  },
  eyebrow: {
    color: '#ff8800',
    fontSize: 14,
    fontWeight: '700',
    letterSpacing: 1,
    textTransform: 'uppercase',
  },
  subtitle: {
    color: '#4b5563',
    fontSize: 16,
    lineHeight: 24,
  },
  card: {
    backgroundColor: '#fffaf2',
    borderColor: '#eadfce',
    borderRadius: 20,
    borderWidth: 1,
    gap: 10,
    padding: 18,
  },
  cardTitle: {
    color: '#111827',
    fontSize: 18,
    fontWeight: '700',
  },
  message: {
    color: '#334155',
    fontSize: 15,
    lineHeight: 22,
  },
  configLine: {
    color: '#475569',
    fontSize: 14,
  },
  warning: {
    color: '#b45309',
    fontSize: 14,
    fontWeight: '600',
  },
  sessionLabel: {
    color: '#ff8800',
    fontSize: 13,
    fontWeight: '700',
    textTransform: 'uppercase',
  },
  sessionValue: {
    color: '#1f2937',
    fontSize: 15,
  },
  tokenPreview: {
    color: '#475569',
    fontSize: 12,
    lineHeight: 18,
  },
  actions: {
    gap: 12,
  },
  button: {
    alignItems: 'center',
    backgroundColor: '#ff8800',
    borderRadius: 16,
    paddingHorizontal: 18,
    paddingVertical: 16,
  },
  buttonDisabled: {
    opacity: 0.55,
  },
  buttonText: {
    color: '#f8fafc',
    fontSize: 16,
    fontWeight: '700',
  },
  secondaryButton: {
    alignItems: 'center',
    backgroundColor: '#dbeafe',
    borderRadius: 16,
    paddingHorizontal: 18,
    paddingVertical: 14,
  },
  secondaryButtonText: {
    color: '#1d4ed8',
    fontSize: 15,
    fontWeight: '700',
  },
  ghostButton: {
    alignItems: 'center',
    borderColor: '#d6c9b3',
    borderRadius: 16,
    borderWidth: 1,
    paddingHorizontal: 18,
    paddingVertical: 14,
  },
  ghostButtonText: {
    color: '#7c2d12',
    fontSize: 15,
    fontWeight: '700',
  },
});
