export type Status = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'EDITED';

export type TimePeriod = 'WEEKLY' | 'MONTHLY' | 'YEARLY';

export type Role = 'EMPLOYEE' | 'MANAGER' | 'ADMIN';

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
  employeeId?: number | null;
  daysLeft: number;
  name: string;
  maxDays: number;
}

export interface AppUser {
  id: number;
  firstName: string;
  lastName: string;
  role?: Role | null;
  projects?: Project[] | null;
  manager?: Manager | null;
}

export interface TimeRequest {
  id: number;
  employeeId?: number | null;
  employeeName?: string | null;
  balanceId?: number | null;
  balanceName?: string | null;
  status: Status;
  startDate: string;
  endDate: string;
  workingDays?: number | null;
  explanation: string;
  creationDate?: string | null;
  modifiedDate?: string | null;
  decidedById?: number | null;
  decidedByName?: string | null;
  decidedAt?: string | null;
  decisionComment?: string | null;
  coverageWarnings?: string[] | null;
}

export interface Page<T> {
  content?: T[];
  totalElements?: number;
  totalPages?: number;
  number?: number;
  size?: number;
}

export interface CalendarAbsence {
  requestId: number;
  employeeId: number;
  employeeName: string;
  balanceId?: number | null;
  balanceName?: string | null;
  status: Status;
  startDate: string;
  endDate: string;
  workingDays?: number | null;
  explanation?: string | null;
  projectIds?: number[] | null;
}

export interface CalendarProjectCoverage {
  date: string;
  projectId: number;
  projectName: string;
  minEmp: number;
  totalMembers: number;
  availableCount: number;
  understaffed: boolean;
}

export interface CalendarResponse {
  from: string;
  to: string;
  absences: CalendarAbsence[];
  coverages: CalendarProjectCoverage[];
}
