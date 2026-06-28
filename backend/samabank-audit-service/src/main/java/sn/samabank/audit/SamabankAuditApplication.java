package sn.samabank.audit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import sn.samabank.audit.config.SamaBankProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableConfigurationProperties(SamaBankProperties.class)
public class SamabankAuditApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamabankAuditApplication.class, args);
    }
}
