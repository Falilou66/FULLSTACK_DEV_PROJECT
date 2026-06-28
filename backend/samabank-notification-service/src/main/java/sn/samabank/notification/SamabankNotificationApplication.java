package sn.samabank.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import sn.samabank.notification.config.SamaBankProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableAsync
@EnableConfigurationProperties(SamaBankProperties.class)
public class SamabankNotificationApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamabankNotificationApplication.class, args);
    }
}
