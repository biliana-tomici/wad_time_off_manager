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
  if (status === 0) {
    return 'Cannot reach the API on port 8080. Start the Spring Boot app and try again.';
  }
  if (status === 400) {
    return 'The server rejected this. Check the dates, the allowance, and the manager.';
  }
  if (status === 404) {
    return 'That record is no longer there.';
  }
  if (!Number.isNaN(status)) {
    return `The request failed (${status}).`;
  }
  return 'Something went wrong.';
}
