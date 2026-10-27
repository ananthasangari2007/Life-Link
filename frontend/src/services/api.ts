const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export class ApiError extends Error {
  status: number;
  errorCode?: string;
  fieldErrors?: Record<string, string>;

  constructor(message: string, status: number, errorCode?: string, fieldErrors?: Record<string, string>) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.errorCode = errorCode;
    this.fieldErrors = fieldErrors;
  }
}

export async function request<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem('lifelink_token');
  const headers = new Headers(options.headers || {});

  if (!headers.has('Content-Type') && !(options.body instanceof FormData)) {
    headers.set('Content-Type', 'application/json');
  }

  if (token && !headers.has('Authorization')) {
    headers.set('Authorization', `Bearer ${token}`);
  }

  const url = endpoint.startsWith('http') ? endpoint : `${API_BASE}${endpoint.startsWith('/') ? '' : '/'}${endpoint}`;

  const res = await fetch(url, {
    ...options,
    headers,
  });

  if (res.status === 401) {
    // If unauthorized, clear token and notify
    localStorage.removeItem('lifelink_token');
    localStorage.removeItem('lifelink_user');
    window.dispatchEvent(new Event('lifelink_auth_expired'));
  }

  const data = await res.json().catch(() => null);

  if (!res.ok) {
    const errorMsg = data?.detail || data?.message || `Request failed with status ${res.status}`;
    throw new ApiError(errorMsg, res.status, data?.errorCode, data?.fieldErrors);
  }

  // Handle ApiResponse<T> envelope
  if (data && typeof data === 'object' && 'data' in data && 'success' in data) {
    return data.data as T;
  }

  return data as T;
}

export const api = {
  get: <T>(endpoint: string) => request<T>(endpoint, { method: 'GET' }),
  post: <T>(endpoint: string, body?: any) => request<T>(endpoint, { method: 'POST', body: body ? JSON.stringify(body) : undefined }),
  patch: <T>(endpoint: string, body?: any) => request<T>(endpoint, { method: 'PATCH', body: body ? JSON.stringify(body) : undefined }),
  delete: <T>(endpoint: string) => request<T>(endpoint, { method: 'DELETE' }),
};
