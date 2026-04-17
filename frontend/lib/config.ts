const apiBaseUrl = process.env.EXPO_PUBLIC_API_BASE_URL?.trim();
const googleClientId = process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID?.trim();

export function getApiBaseUrl(): string {
  if (!apiBaseUrl) {
    throw new Error('EXPO_PUBLIC_API_BASE_URL is missing.');
  }

  return apiBaseUrl.replace(/\/+$/, '');
}

export function getGoogleClientId(): string {
  if (!googleClientId) {
    throw new Error('EXPO_PUBLIC_GOOGLE_CLIENT_ID is missing.');
  }

  return googleClientId;
}

export function getMissingAuthConfig(): string[] {
  const missing: string[] = [];

  if (!apiBaseUrl) {
    missing.push('EXPO_PUBLIC_API_BASE_URL');
  }

  if (!googleClientId) {
    missing.push('EXPO_PUBLIC_GOOGLE_CLIENT_ID');
  }

  return missing;
}
