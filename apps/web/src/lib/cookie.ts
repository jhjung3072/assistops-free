export const ACCESS_TOKEN_COOKIE_NAME = "assistops_access_token";

export type CookieOptions = {
  path?: string;
  sameSite?: "Lax" | "Strict" | "None";
  secure?: boolean;
  maxAge?: number;
  expires?: Date;
  domain?: string;
};

const DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS = 60 * 60;

const DEFAULT_COOKIE_OPTIONS: Required<
  Pick<CookieOptions, "path" | "sameSite" | "secure" | "maxAge">
> = {
  path: "/",
  sameSite: "Lax",
  secure: false,
  maxAge: DEFAULT_ACCESS_TOKEN_MAX_AGE_SECONDS,
};

function canUseDocumentCookie() {
  return typeof document !== "undefined";
}

export function getCookie(name: string): string | null {
  if (!canUseDocumentCookie()) {
    return null;
  }

  const encodedName = `${encodeURIComponent(name)}=`;
  const cookie = document.cookie
    .split("; ")
    .find((row) => row.startsWith(encodedName));

  if (!cookie) {
    return null;
  }

  return decodeURIComponent(cookie.slice(encodedName.length));
}

export function setCookie(
  name: string,
  value: string,
  options: CookieOptions = {},
) {
  if (!canUseDocumentCookie()) {
    return;
  }

  const cookieOptions = { ...DEFAULT_COOKIE_OPTIONS, ...options };
  const segments = [
    `${encodeURIComponent(name)}=${encodeURIComponent(value)}`,
    `Path=${cookieOptions.path}`,
    `SameSite=${cookieOptions.sameSite}`,
    `Max-Age=${cookieOptions.maxAge}`,
  ];

  if (cookieOptions.expires) {
    segments.push(`Expires=${cookieOptions.expires.toUTCString()}`);
  }

  if (cookieOptions.domain) {
    segments.push(`Domain=${cookieOptions.domain}`);
  }

  if (cookieOptions.secure) {
    segments.push("Secure");
  }

  document.cookie = segments.join("; ");
}

export function deleteCookie(name: string) {
  setCookie(name, "", {
    path: "/",
    maxAge: 0,
  });
}

export function getAccessTokenCookie() {
  return getCookie(ACCESS_TOKEN_COOKIE_NAME);
}

export function setAccessTokenCookie(accessToken: string) {
  setCookie(ACCESS_TOKEN_COOKIE_NAME, accessToken);
}

export function deleteAccessTokenCookie() {
  deleteCookie(ACCESS_TOKEN_COOKIE_NAME);
}
