package uk.gov.hmcts.reform.hmc.api.utils;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class IdGeneratorUtilTest {

    @Test
    public void getCorrelationIdTest() {
        Assertions.assertNotNull(IdGeneratorUtil.getCorrelationId());
        Assertions.assertFalse(IdGeneratorUtil.getCorrelationId().isEmpty());
    }
}
