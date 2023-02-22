package uk.gov.hmcts.reform.hmc.api.refdatacategory;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.hmc.api.restclient"})
public class RefDataCategoryApiConsumerApplication {}
