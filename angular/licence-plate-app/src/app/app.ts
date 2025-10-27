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
            cityCode: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(3)]],
            letters: ['', [Validators.maxLength(2)]],
            numbers: ['', [Validators.required, Validators.minLength(1), Validators.maxLength(4)]],
        });
    }

    ngOnInit(): void {
    }

    onCityCodeInput(event: Event): void {
        const input = event.target as HTMLInputElement;
        input.value = input.value.toUpperCase().replace(/[^A-ZÄÖÜ]/g, '');
        this.form.get('cityCode')?.setValue(input.value, { emitEvent: false });

        // Auto-focus next field when max length reached
        if (input.value.length === 3) {
            document.getElementById('letters')?.focus();
        }
    }

    onLettersInput(event: Event): void {
        const input = event.target as HTMLInputElement;
        input.value = input.value.toUpperCase().replace(/[^A-Z]/g, '');
        this.form.get('letters')?.setValue(input.value, { emitEvent: false });

        // Auto-focus next field when max length reached
        if (input.value.length === 2) {
            document.getElementById('numbers')?.focus();
        }
    }

    validatePlate(): void {
        if (this.form.invalid) {
            return;
        }

        this.isLoading = true;
        this.error = null;
        this.validationResult = null;

        const cityCode = this.form.get('cityCode')?.value.trim();
        const letters = this.form.get('letters')?.value.trim();
        const numbers = this.form.get('numbers')?.value.trim();

        const licensePlate = `${cityCode}-${letters} ${numbers}`;

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
