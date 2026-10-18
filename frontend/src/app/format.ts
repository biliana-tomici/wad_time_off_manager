const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

export function fullName(person?: { firstName?: string; lastName?: string } | null): string {
  if (!person) {
    return 'No one selected';
  }
  const name = `${person.firstName ?? ''} ${person.lastName ?? ''}`.trim();
  return name || 'Unnamed';
}

export function initials(person?: { firstName?: string; lastName?: string } | null): string {
  if (!person) {
    return '?';
  }
  const letters = `${person.firstName?.[0] ?? ''}${person.lastName?.[0] ?? ''}`.toUpperCase();
  return letters || '?';
}

export function prettyDate(iso?: string | null): string {
  if (!iso) {
    return '—';
  }
  const [year, month, day] = iso.split('-').map(Number);
  if (!year || !month || !day) {
    return iso;
  }
  return `${day} ${MONTHS[month - 1]} ${year}`;
}

export function inclusiveDays(start?: string | null, end?: string | null): number {
  if (!start || !end) {
    return 0;
  }
  const startMs = Date.parse(`${start}T00:00:00`);
  const endMs = Date.parse(`${end}T00:00:00`);
  if (Number.isNaN(startMs) || Number.isNaN(endMs) || endMs < startMs) {
    return 0;
  }
  return Math.round((endMs - startMs) / 86_400_000) + 1;
}

export function workingDays(start?: string | null, end?: string | null): number {
  if (!start || !end) {
    return 0;
  }
  const [sy, sm, sd] = start.split('-').map(Number);
  const [ey, em, ed] = end.split('-').map(Number);
  if (!sy || !sm || !sd || !ey || !em || !ed) {
    return 0;
  }
  const startUtc = Date.UTC(sy, sm - 1, sd);
  const endUtc = Date.UTC(ey, em - 1, ed);
  if (Number.isNaN(startUtc) || Number.isNaN(endUtc) || endUtc < startUtc) {
    return 0;
  }
  let count = 0;
  for (let ms = startUtc; ms <= endUtc; ms += 86_400_000) {
    const day = new Date(ms).getUTCDay();
    if (day !== 0 && day !== 6) {
      count++;
    }
  }
  return count;
}

export function statusLabel(status?: string | null): string {
  switch (status) {
    case 'PENDING':
      return 'Pending';
    case 'APPROVED':
      return 'Approved';
    case 'REJECTED':
      return 'Rejected';
    case 'CANCELLED':
      return 'Cancelled';
    case 'EDITED':
      return 'Edited';
    default:
      return status || 'Unknown';
  }
}

export function errorText(err: unknown): string {
  const status = typeof err === 'object' && err && 'status' in err ? Number((err as { status: number }).status) : NaN;
  if (typeof err === 'object' && err !== null) {
    const body = (err as { error?: unknown }).error;
    if (typeof body === 'object' && body !== null) {
      const errors = (body as { errors?: Record<string, string> }).errors;
      if (errors && typeof errors === 'object') {
        const messages = Object.entries(errors).map(([k, v]) => `${k}: ${v}`);
        if (messages.length > 0) {
          return messages.join('; ');
        }
      }
      const detail = (body as { detail?: unknown }).detail;
      if (typeof detail === 'string' && detail.trim()) {
        return detail;
      }
    }
    if (typeof body === 'string') {
      try {
        const parsed = JSON.parse(body) as { detail?: string };
        if (parsed?.detail) {
          return parsed.detail;
        }
      } catch {
        if (status >= 500 && (body.includes('proxy') || body.includes('ECONNREFUSED') || body.trim() === '')) {
          return 'Cannot reach the backend API. Please ensure the Spring Boot server is running.';
        }
      }
    }
    if ((body === null || body === undefined) && (status === 500 || status === 502 || status === 503 || status === 504)) {
      return 'Cannot reach the backend API. Please ensure the Spring Boot server is running.';
    }
  }
  if (status === 0 || status === 502 || status === 503 || status === 504) {
    return 'Cannot reach the backend API. Please ensure the Spring Boot server is running.';
  }
  if (status === 401) {
    return 'Invalid email or password.';
  }
  if (status === 403) {
    return 'You do not have permission to perform this action.';
  }
  if (status === 400) {
    return 'The server rejected this request. Please check your input fields.';
  }
  if (status === 404) {
    return 'That record is no longer there.';
  }
  if (!Number.isNaN(status)) {
    return `The request failed (${status}).`;
  }
  return 'Something went wrong.';
}

export function generateSecurePassword(length = 12): string {
  const upper = 'ABCDEFGHJKLMNPQRSTUVWXYZ';
  const lower = 'abcdefghijkmnopqrstuvwxyz';
  const digits = '23456789';
  const special = '!@#$%^&*-_=+';
  const all = upper + lower + digits + special;
  const finalLength = Math.max(8, length);

  const randomIndex = (max: number): number => {
    if (typeof crypto !== 'undefined' && typeof crypto.getRandomValues === 'function') {
      const buf = new Uint32Array(1);
      crypto.getRandomValues(buf);
      return buf[0] % max;
    }
    return Math.floor(Math.random() * max);
  };

  const chars: string[] = [
    upper[randomIndex(upper.length)],
    lower[randomIndex(lower.length)],
    digits[randomIndex(digits.length)],
    special[randomIndex(special.length)],
  ];

  for (let i = 4; i < finalLength; i++) {
    chars.push(all[randomIndex(all.length)]);
  }

  for (let i = chars.length - 1; i > 0; i--) {
    const j = randomIndex(i + 1);
    const tmp = chars[i];
    chars[i] = chars[j];
    chars[j] = tmp;
  }

  return chars.join('');
}


