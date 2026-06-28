package sn.samabank.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import sn.samabank.customer.config.SamaBankProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableConfigurationProperties(SamaBankProperties.class)
public class SamabankCustomerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamabankCustomerApplication.class, args);
    }
}
