package uk.gov.hmcts.reform.hmc.api.utils;

import java.util.UUID;

public interface IdGeneratorUtil {

    public static String getCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
