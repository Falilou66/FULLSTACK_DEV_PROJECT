package sn.samabank.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SamabankDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(SamabankDiscoveryApplication.class, args);
    }
}
