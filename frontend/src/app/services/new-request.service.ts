import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class NewRequestService {
private baseUrl=  'http://localhost:8080/api/request';
  constructor( private http: HttpClient) { }

  createNewRequest(requestBody: any): Observable<any> {
    return this.http.post(this.baseUrl, requestBody)
  }
}
