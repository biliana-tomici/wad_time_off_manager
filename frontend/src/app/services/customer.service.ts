import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface UserDto {
  id?: number;
  firstName: string;
  lastName: string;
  // poți adăuga restul câmpurilor dacă ai nevoie (projects, balances etc.)
}

@Injectable({
  providedIn: 'root'
})
export class CustomerService {
  // URL-ul către backend-ul tău Spring Boot (asigură-te că portul e 8080)
  private apiUrl = 'http://localhost:8080/api/customer';

  constructor(private http: HttpClient) {}

  // 1. GET - Obține toți utilizatorii (sau poți adapta după rutele tale)
  getCustomers(): Observable<UserDto[]> {
    return this.http.get<UserDto[]>(this.apiUrl);
  }

  // 2. GET by ID - Obține un utilizator după ID
  getCustomerById(id: number): Observable<UserDto> {
    return this.http.get<UserDto>(`${this.apiUrl}/${id}`);
  }

  // 3. POST - Creează un utilizator nou
  createCustomer(user: UserDto): Observable<UserDto> {
    return this.http.post<UserDto>(this.apiUrl, user);
  }

  // 4. PUT - Actualizează un utilizator
  updateCustomer(id: number, user: UserDto): Observable<UserDto> {
    return this.http.put<UserDto>(`${this.apiUrl}/${id}`, user);
  }

  // 5. DELETE - Șterge un utilizator
  deleteCustomer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
