
package tomcom.licenceplatechecker.rest;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private final boolean success;
    private final T result;
    private final String error;

    private ApiResponse(boolean success, T result, String error) {
        this.success = success;
        this.result = result;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, result, null);
    }

    public static <T> ApiResponse<T> error(String errorMessage) {
        return new ApiResponse<>(false, null, errorMessage);
    }
}
