package uk.gov.hmcts.reform.hmc.api.model.response.error;
import lombok.Getter;

@Getter
public class ApiError {
private final String message;

    public ApiError(String error) {
        this.message = error;
    }
}
