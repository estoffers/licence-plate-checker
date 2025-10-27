import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {ApiResponse} from './api-response';

@Injectable({
    providedIn: 'root',
})
export class LicencePlateService {
    private apiUrl = '/licence-plate/validate';

    constructor(private http: HttpClient) {
    }

    validateLicencePlate(licensePlate: string): Observable<ApiResponse<string>> {
        return this.http.post<ApiResponse<string>>(this.apiUrl, {
            licencePlate: licensePlate,
        });
    }
}
