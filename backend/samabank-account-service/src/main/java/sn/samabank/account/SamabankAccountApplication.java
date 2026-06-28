package sn.samabank.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import sn.samabank.account.config.SamaBankProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableConfigurationProperties(SamaBankProperties.class)
public class SamabankAccountApplication {

    public static void main(String[] args) {
        SpringApplication.run(SamabankAccountApplication.class, args);
    }
}
