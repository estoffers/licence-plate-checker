import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import {LicencePlateService} from './service/licence-plate-validation.service';
import {ApiResponse} from './service/api-response';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule],
    templateUrl: './app.html',
    styleUrls: ['./app.sass'],
})
export class AppComponent implements OnInit {
    form!: FormGroup;
    isLoading = false;
    validationResult: string | null | undefined = null;
    error: string | null = null;

    constructor(
        private fb: FormBuilder,
        private licencePlateService: LicencePlateService
    ) {
        this.form = this.fb.group({
            licensePlate: ['', [Validators.required, Validators.minLength(2)]],
        });
    }

    ngOnInit(): void {
    }

    validatePlate(): void {
        if (this.form.invalid) {
            return;
        }

        this.isLoading = true;
        this.error = null;
        this.validationResult = null;

        const licensePlate: string = this.form.get('licensePlate')?.value.trim();

        this.licencePlateService.validateLicencePlate(licensePlate).subscribe({
            next: (response: ApiResponse<string>) => {
                if (response.success) {
                    this.validationResult = response.result;
                } else {
                    this.error = response.error || 'Validation failed';
                }
                this.isLoading = false;
            },
            error: (err: any) => {
                this.error = err.error?.error || 'An error occurred during validation';
                this.isLoading = false;
            },
        });
    }
}
