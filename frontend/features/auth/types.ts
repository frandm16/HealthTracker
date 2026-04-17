export type AuthUser = {
  id: string;
  email: string | null;
  name: string;
};

export type StoredAuthSession = {
  accessToken: string;
  refreshToken: string;
  expiresAt: string;
  user: AuthUser;
};

export function isAccessTokenExpired(session: StoredAuthSession, now = new Date()): boolean {
  const expiresAt = Date.parse(session.expiresAt);
  return Number.isNaN(expiresAt) || expiresAt <= now.getTime() + 30_000;
}
