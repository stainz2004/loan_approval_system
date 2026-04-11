const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export async function apiFetch<T = void>(path: string, options: RequestInit = {}): Promise<T> {
  const res = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers: options.body !== undefined ? { 'Content-Type': 'application/json' } : {},
  });
  if (!res.ok) {
    const errorBody = await res.json().catch(() => null) as { message?: string } | null;
    throw new Error(errorBody?.message ?? `Request failed (${res.status})`);
  }
  if (res.status === 204 || res.headers.get('content-length') === '0') return undefined as T;
  return res.json() as Promise<T>;
}
