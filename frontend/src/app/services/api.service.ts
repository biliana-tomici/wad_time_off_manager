import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE } from '../api-base';
import { AppUser, Balance, Page, Project, Status, TimePeriod, TimeRequest } from '../models';

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

  pending(managerId: number): Observable<TimeRequest[]> {
    return asList(this.http.get<Page<TimeRequest>>(`${API_BASE}/api/request/pending/${managerId}`, {
      params: { page: '0', size: '100' }
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

  updateProject(id: number, body: unknown): Observable<Project> {
    return this.http.put<Project>(`${API_BASE}/api/projects/${id}`, body);
  }

  deleteProject(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/projects/${id}`);
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

  deleteBalance(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/balance/${id}`);
  }

  createUser(body: unknown): Observable<AppUser> {
    return this.http.post<AppUser>(`${API_BASE}/api/users`, body);
  }

  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${API_BASE}/api/users/${id}`);
  }
}
