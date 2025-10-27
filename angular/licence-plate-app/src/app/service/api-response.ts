export interface ApiResponse<T> {
    success: boolean;
    result?: T | null;
    error?: string;
}
