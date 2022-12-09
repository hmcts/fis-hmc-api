package uk.gov.hmcts.reform.hmc.api.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ServerErrorException extends RuntimeException {

    private String server;

    private HttpStatus status;

    public static final long serialVersionUID = 333297431;

    public ServerErrorException(String server, HttpStatus status, Throwable cause) {
        super(cause);
        this.server = server;
        this.status = status;
    }
}
