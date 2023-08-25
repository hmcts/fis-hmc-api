package uk.gov.hmcts.reform.hmc.api.hearingcft;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.hmc.api.services"})
public class HearingCftApiConsumerApplication {}
