package uk.gov.hmcts.reform.hmc.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.HttpStatus;

@Getter
public class RefDataException extends RuntimeException {

    private String server;

    private HttpStatus status;

    public static final long serialVersionUID = 333297431;

    public RefDataException(String server, HttpStatusCode status, Throwable cause) {
        super(cause);
        this.server = server;
        this.status = HttpStatus.valueOf(status.value());
    }
}
