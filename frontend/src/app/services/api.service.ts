import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE } from '../api-base';
import { AppUser, Balance, CalendarResponse, Page, Project, Status, TimePeriod, TimeRequest } from '../models';

function asList<T>(source: Observable<Page<T>>): Observable<T[]> {
  return source.pipe(
    map((page) => page?.content ?? [])
  );
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  users(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${API_BASE}/api/users`);
  }

  balances(employeeId: number): Observable<Balance[]> {
    return asList(this.http.get<Page<Balance>>(`${API_BASE}/api/balance/employee/${employeeId}`, {
      params: { page: '0', size: '50' }
    }));
  }

  requests(employeeId: number): Observable<TimeRequest[]> {
    return asList(this.http.get<Page<TimeRequest>>(`${API_BASE}/api/request/employee/${employeeId}`, {
      params: { page: '0', size: '100' }
    }));
  }

  pending(managerId: number, includeUnassigned?: boolean): Observable<TimeRequest[]> {
    const params: Record<string, string> = { page: '0', size: '100' };
    if (includeUnassigned !== undefined) {
      params['includeUnassigned'] = String(includeUnassigned);
    }
    return asList(this.http.get<Page<TimeRequest>>(`${API_BASE}/api/request/pending/${managerId}`, {
      params
    }));
  }

  createRequest(body: unknown): Observable<TimeRequest> {
    return this.http.post<TimeRequest>(`${API_BASE}/api/request`, body);
  }

  updateRequest(id: number, body: unknown): Observable<TimeRequest> {
    return this.http.put<TimeRequest>(`${API_BASE}/api/request/${id}`, body);
  }

  setStatus(id: number, status: Status): Observable<TimeRequest> {
    return this.http.patch<TimeRequest>(`${API_BASE}/api/request/${id}/status`, null, {
      params: { status }
    });
  }

  cancelRequest(id: number): Observable<TimeRequest> {
    return this.http.post<TimeRequest>(`${API_BASE}/api/request/${id}/cancel`, null);
  }

  approveRequest(id: number, comment?: string): Observable<TimeRequest> {
    return this.http.post<TimeRequest>(`${API_BASE}/api/request/${id}/approve`, comment ? { comment } : null);
  }

  rejectRequest(id: number, comment?: string): Observable<TimeRequest> {
    return this.http.post<TimeRequest>(`${API_BASE}/api/request/${id}/reject`, comment ? { comment } : null);
  }

  deleteRequest(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/request/${id}`);
  }

  projects(): Observable<Project[]> {
    return asList(this.http.get<Page<Project>>(`${API_BASE}/api/projects`, {
      params: { page: '0', size: '100' }
    }));
  }

  createProject(body: unknown): Observable<Project> {
    return this.http.post<Project>(`${API_BASE}/api/projects`, body);
  }

  updateProject(id: number, body: { name?: string; minEmp?: number; managerId?: number }): Observable<Project> {
    return this.http.put<Project>(`${API_BASE}/api/projects/${id}`, body);
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/projects/${id}`);
  }

  projectMembers(projectId: number): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${API_BASE}/api/projects/${projectId}/members`);
  }

  addProjectMember(projectId: number, employeeId: number): Observable<void> {
    return this.http.post<void>(`${API_BASE}/api/projects/${projectId}/members/${employeeId}`, null);
  }

  removeProjectMember(projectId: number, employeeId: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/projects/${projectId}/members/${employeeId}`);
  }

  report(projectId: number, timePeriod: TimePeriod): Observable<Blob> {
    return this.http.get(`${API_BASE}/api/report/project/${projectId}/pdf`, {
      params: { timePeriod },
      responseType: 'blob'
    });
  }

  createBalance(body: unknown): Observable<Balance> {
    return this.http.post<Balance>(`${API_BASE}/api/balance`, body);
  }

  updateBalance(id: number, body: { name?: string; daysLeft?: number; maxDays?: number }): Observable<Balance> {
    return this.http.put<Balance>(`${API_BASE}/api/balance/${id}`, body);
  }

  deleteBalance(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/balance/${id}`);
  }

  createUser(body: unknown): Observable<AppUser> {
    return this.http.post<AppUser>(`${API_BASE}/api/users`, body);
  }

  updateUser(id: number, body: unknown): Observable<AppUser> {
    return this.http.put<AppUser>(`${API_BASE}/api/users/${id}`, body);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/users/${id}`);
  }

  calendar(from?: string, to?: string, projectId?: number): Observable<CalendarResponse> {
    const params: Record<string, string> = {};
    if (from) params['from'] = from;
    if (to) params['to'] = to;
    if (projectId) params['projectId'] = String(projectId);
    return this.http.get<CalendarResponse>(`${API_BASE}/api/calendar`, { params });
  }
}
