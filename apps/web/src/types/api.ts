export type ApiResponse<T> = T;

export type ApiErrorResponse = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  validationErrors?: Record<string, string>;
};
