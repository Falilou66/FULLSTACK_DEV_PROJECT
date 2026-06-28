package sn.samabank.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import sn.samabank.auth.config.SamaBankProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@EnableConfigurationProperties(SamaBankProperties.class)
public class SamabankAuthApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamabankAuthApplication.class, args);
    }
}
