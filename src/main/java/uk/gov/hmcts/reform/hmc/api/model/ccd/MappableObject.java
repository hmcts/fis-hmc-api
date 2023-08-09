package uk.gov.hmcts.reform.hmc.api.model.ccd;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public interface MappableObject {

    default Map<String, Object> toMap(ObjectMapper mapper) {
        return mapper.convertValue(this, new TypeReference<>() {});
    }
}
