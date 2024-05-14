package uk.gov.hmcts.reform.hmc.api.mapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public interface FisHmcObjectMapper {

    public static ObjectMapper getObjectMapper() {
        ObjectMapper om = new ObjectMapper();
        om.registerModule(new JavaTimeModule());
        om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        om.enable(SerializationFeature.WRITE_ENUM_KEYS_USING_INDEX);
        return om;
    }
}
