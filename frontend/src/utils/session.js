import { ROLE_LABELS } from "../config/roles";

export function clearAuth() {
  for (const key of ["token", "role", "user"]) localStorage.removeItem(key);
}

// Only remove retired demo keys. Preserve all unrelated browser data.
export function removeLegacyDemoData() {
  for (const storage of [localStorage, sessionStorage]) {
    storage.removeItem("unicare.demo-session.v1");
    storage.removeItem("unicare.admin-demo.v1");
  }
  if (localStorage.getItem("token") && !readSession()) clearAuth();
}

// Client-side expiry/shape checks are not signature verification.
// The backend remains responsible for JWT signature and authorization checks.
export function sessionFromToken(token) {
  try {
    const parts = token.split(".");
    if (parts.length !== 3 || !parts[2]) return null;
    const payload = parts[1].replace(/-/g, "+").replace(/_/g, "/");
    const bytes = Uint8Array.from(
      atob(payload.padEnd(Math.ceil(payload.length / 4) * 4, "=")),
      (char) => char.charCodeAt(0),
    );
    const claims = JSON.parse(new TextDecoder().decode(bytes));
    if (
      !Number.isFinite(claims.exp) ||
      claims.exp * 1000 <= Date.now() ||
      !claims.sub ||
      !Object.hasOwn(ROLE_LABELS, claims.role)
    )
      return null;
    return {
      username: claims.sub,
      fullName: claims.fullName,
      role: claims.role,
      expiresAt: claims.exp * 1000,
    };
  } catch {
    return null;
  }
}

export function readSession() {
  return sessionFromToken(localStorage.getItem("token") || "");
}
