import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private apiUrl = environment.apiUrl;

  constructor(private http: HttpClient) {}

  // Siempre obtener token desde localStorage (el más reciente)
  private getToken(): string | null {
    return localStorage.getItem('token');
  }

  // Método para crear los headers con el token
  private getHeaders(): HttpHeaders {
    let headers = new HttpHeaders().set('Content-Type', 'application/json');

    const token = this.getToken();
    if (token) {
      try {
        // verificamos expiración
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp;
        const now = Math.floor(Date.now() / 1000);

        if (exp > now) {
          headers = headers.set('Authorization', `Bearer ${token}`);
        } else {
          localStorage.removeItem('token'); // limpia token vencido
        }
      } catch (e) {
        localStorage.removeItem('token'); // token corrupto
      }
    }

    return headers;
  }

  // Métodos genéricos
  get<T>(endpoint: string): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}/${endpoint}`, { headers: this.getHeaders() });
  }

  getWithParams<T>(endpoint: string, params: Record<string, string>): Observable<T> {
    return this.http.get<T>(`${this.apiUrl}/${endpoint}`, {
      headers: this.getHeaders(),
      params
    });
  }

  getBlob(endpoint: string, params?: Record<string, string>): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${endpoint}`, {
      headers: this.getHeaders(),
      params,
      responseType: 'blob'
    });
  }

  post<T>(endpoint: string, data: any, expectText = false): Observable<T> {
    if (expectText) {
      return this.http.post(`${this.apiUrl}/${endpoint}`, data, {
        headers: this.getHeaders(),
        responseType: 'text'
      }) as unknown as Observable<T>;
    }
    return this.http.post<T>(`${this.apiUrl}/${endpoint}`, data, { headers: this.getHeaders() });
  }

  put<T>(endpoint: string, data: any): Observable<T> {
    return this.http.put<T>(`${this.apiUrl}/${endpoint}`, data, { headers: this.getHeaders() });
  }

  delete<T>(endpoint: string): Observable<T> {
    return this.http.delete<T>(`${this.apiUrl}/${endpoint}`, { headers: this.getHeaders() });
  }
}
