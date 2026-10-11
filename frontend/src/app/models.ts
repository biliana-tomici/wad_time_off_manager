export type Status = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'EDITED';

export type TimePeriod = 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export interface Manager {
  id: number;
  firstName: string;
  lastName: string;
}

export interface Project {
  id: number;
  minEmp: number;
  name: string;
  manager?: Manager | null;
}

export interface Balance {
  id: number;
  employee?: Manager | null;
  daysLeft: number;
  name: string;
  maxDays: number;
}

export interface AppUser {
  id: number;
  firstName: string;
  lastName: string;
  projects?: Project[] | null;
  manager?: Manager | null;
}

export interface TimeRequest {
  id: number;
  employee?: AppUser | null;
  balance?: Balance | null;
  status: Status;
  startDate: string;
  endDate: string;
  explanation: string;
  creationDate?: string | null;
  modifiedDate?: string | null;
}

export interface Page<T> {
  content?: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
}
