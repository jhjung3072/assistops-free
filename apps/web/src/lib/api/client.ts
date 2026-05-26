import {
  deleteAccessTokenCookie,
  getAccessTokenCookie,
} from "@/lib/cookie";
import type { ApiErrorResponse } from "@/types/api";

const API_BASE_URL =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

type ApiRequestOptions = RequestInit;

export type ApiBlobResponse = {
  blob: Blob;
  filename?: string;
};

export class ApiClientError extends Error {
  status: number;
  response?: ApiErrorResponse;

  constructor(status: number, message: string, response?: ApiErrorResponse) {
    super(message);
    this.name = "ApiClientError";
    this.status = status;
    this.response = response;
  }
}

export async function apiRequest<T>(
  path: string,
  options: ApiRequestOptions = {},
): Promise<T> {
  const { headers, body, ...requestOptions } = options;
  const accessToken = getAccessTokenCookie();
  const isJsonBody = Boolean(body) && !(body instanceof FormData);

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...requestOptions,
      body,
      headers: {
        Accept: "application/json",
        ...(isJsonBody ? { "Content-Type": "application/json" } : {}),
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        ...headers,
      },
    });
  } catch {
    throw new ApiClientError(
      0,
      "API 서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.",
    );
  }

  const data = await parseJson(response);

  if (!response.ok) {
    const errorResponse = data as ApiErrorResponse | undefined;
    const message =
      errorResponse?.message ??
      (response.status === 401
        ? "인증이 필요합니다. 다시 로그인해 주세요."
        : "요청을 처리하지 못했습니다.");

    if (response.status === 401 && accessToken) {
      deleteAccessTokenCookie();
    }

    throw new ApiClientError(response.status, message, errorResponse);
  }

  return data as T;
}

export async function apiRequestBlob(
  path: string,
  options: ApiRequestOptions = {},
): Promise<ApiBlobResponse> {
  const { headers, body, ...requestOptions } = options;
  const accessToken = getAccessTokenCookie();

  let response: Response;

  try {
    response = await fetch(`${API_BASE_URL}${path}`, {
      ...requestOptions,
      body,
      headers: {
        Accept: "*/*",
        ...(accessToken ? { Authorization: `Bearer ${accessToken}` } : {}),
        ...headers,
      },
    });
  } catch {
    throw new ApiClientError(
      0,
      "API 서버에 연결할 수 없습니다. 백엔드가 실행 중인지 확인해 주세요.",
    );
  }

  if (!response.ok) {
    const errorResponse = await parseJson(response) as
      | ApiErrorResponse
      | undefined;
    const message =
      errorResponse?.message ??
      (response.status === 401
        ? "인증이 필요합니다. 다시 로그인해 주세요."
        : "파일을 다운로드하지 못했습니다.");

    if (response.status === 401 && accessToken) {
      deleteAccessTokenCookie();
    }

    throw new ApiClientError(response.status, message, errorResponse);
  }

  return {
    blob: await response.blob(),
    filename: getFilenameFromContentDisposition(
      response.headers.get("Content-Disposition"),
    ),
  };
}

export function getApiErrorMessage(error: unknown, fallback: string) {
  if (error instanceof ApiClientError) {
    return error.message;
  }

  return fallback;
}

async function parseJson(response: Response) {
  const text = await response.text();

  if (!text) {
    return undefined;
  }

  try {
    return JSON.parse(text) as unknown;
  } catch {
    return undefined;
  }
}

function getFilenameFromContentDisposition(contentDisposition: string | null) {
  if (!contentDisposition) {
    return undefined;
  }

  const encodedMatch = /filename\*=UTF-8''([^;]+)/i.exec(contentDisposition);

  if (encodedMatch?.[1]) {
    return decodeURIComponent(encodedMatch[1]);
  }

  const quotedMatch = /filename="([^"]+)"/i.exec(contentDisposition);

  if (quotedMatch?.[1]) {
    return quotedMatch[1];
  }

  return undefined;
}
