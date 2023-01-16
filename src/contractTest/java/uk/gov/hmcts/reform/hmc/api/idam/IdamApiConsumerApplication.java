package uk.gov.hmcts.reform.hmc.api.idam;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.idam.client"})
public class IdamApiConsumerApplication {}
