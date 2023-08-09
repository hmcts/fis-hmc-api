package uk.gov.hmcts.reform.hmc.api.refdatavenue;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.hmc.api.clients"})
public class RefDataVenueApiConsumerApplication {

}
