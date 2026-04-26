import { Link, type Href } from 'expo-router';
import { Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';

export default function HomeScreen() {
  return (
    <ScrollView contentInsetAdjustmentBehavior="automatic" contentContainerStyle={styles.safeArea}>
      <View style={styles.container}>
        <View style={styles.header}>
          <Text style={styles.eyebrow}>PersonalApp</Text>
          <Text style={styles.title}>Health tracker</Text>
          <Text style={styles.subtitle}>Open authentication and the nutrition dashboard.</Text>
        </View>
        <View style={styles.actions}>
          <Link href={'/nutrition' as Href} asChild>
            <Pressable style={[styles.button, styles.secondaryButton]}>
              <Text style={styles.secondaryButtonText}>View nutrition</Text>
            </Pressable>
          </Link>
          <Link href="/auth" asChild>
            <Pressable style={[styles.button, styles.secondaryButton]}>
              <Text style={styles.secondaryButtonText}>Go to auth</Text>
            </Pressable>
          </Link>
        </View>
      </View>
    </ScrollView>
  );
}

const styles = StyleSheet.create({
  safeArea: {
    flexGrow: 1,
    backgroundColor: '#f8fafc',
    padding: 24,
  },
  container: {
    flexGrow: 1,
    justifyContent: 'center',
    gap: 28,
  },
  header: {
    gap: 10,
  },
  eyebrow: {
    color: '#2563eb',
    fontSize: 13,
    fontWeight: '800',
    textTransform: 'uppercase',
  },
  title: {
    color: '#0f172a',
    fontSize: 36,
    fontWeight: '800',
  },
  subtitle: {
    color: '#475569',
    fontSize: 16,
    lineHeight: 23,
  },
  actions: {
    gap: 12,
  },
  button: {
    alignItems: 'center',
    borderCurve: 'continuous',
    borderRadius: 12,
    padding: 16,
  },
  primaryButton: {
    backgroundColor: '#f97316',
  },
  secondaryButton: {
    backgroundColor: '#e2e8f0',
  },
  primaryButtonText: {
    color: '#ffffff',
    fontSize: 16,
    fontWeight: '800',
  },
  secondaryButtonText: {
    color: '#0f172a',
    fontSize: 16,
    fontWeight: '800',
  },
});
