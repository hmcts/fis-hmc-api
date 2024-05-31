package uk.gov.hmcts.reform.hmc.api.utils;

import java.util.UUID;

public class IdGeneratorUtil {
    private IdGeneratorUtil() {
        throw new IllegalStateException("Utility class");
    }
    public static String getCorrelationId() {
        return UUID.randomUUID().toString();
    }
}
