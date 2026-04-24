const apiBaseUrl = process.env.EXPO_PUBLIC_API_BASE_URL?.trim();
const googleWebClientId =
  process.env.EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID?.trim() ??
  process.env.EXPO_PUBLIC_GOOGLE_CLIENT_ID?.trim();

export function getApiBaseUrl(): string {
  if (!apiBaseUrl) {
    throw new Error('EXPO_PUBLIC_API_BASE_URL is missing.');
  }

  return apiBaseUrl.replace(/\/+$/, '');
}

export function getGoogleWebClientId(): string {
  if (!googleWebClientId) {
    throw new Error('EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID is missing.');
  }

  return googleWebClientId;
}

export function getMissingAuthConfig(): string[] {
  const missing: string[] = [];

  if (!apiBaseUrl) {
    missing.push('EXPO_PUBLIC_API_BASE_URL');
  }

  if (!googleWebClientId) {
    missing.push('EXPO_PUBLIC_GOOGLE_WEB_CLIENT_ID');
  }

  return missing;
}
