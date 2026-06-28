package sn.samabank.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import sn.samabank.stats.config.SamaBankProperties;

@SpringBootApplication
@EnableDiscoveryClient
@EnableConfigurationProperties(SamaBankProperties.class)
public class SamabankStatsApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamabankStatsApplication.class, args);
    }
}
