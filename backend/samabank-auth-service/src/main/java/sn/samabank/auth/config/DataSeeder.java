package sn.samabank.auth.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import sn.samabank.auth.entity.Role;
import sn.samabank.auth.entity.User;
import sn.samabank.auth.repository.UserRepository;

@Configuration
@Profile("!test")
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner seedUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            long count = userRepository.count();
            log.info("[SEED] Utilisateurs existants : {}", count);

            if (count == 0) {
                log.info("[SEED] Creation des comptes de reference...");

                User admin = User.create(
                        "admin",
                        "admin@samabank.local",
                        passwordEncoder.encode("password"),
                        Role.ADMIN
                );
                userRepository.save(admin);
                log.info("[SEED]  admin / password (ADMIN)");

                User teller = User.create(
                        "teller01",
                        "teller01@samabank.local",
                        passwordEncoder.encode("password"),
                        Role.TELLER
                );
                userRepository.save(teller);
                log.info("[SEED]  teller01 / password (TELLER)");

                User customer = User.create(
                        "client01",
                        "client01@samabank.local",
                        passwordEncoder.encode("password"),
                        Role.CUSTOMER
                );
                userRepository.save(customer);
                log.info("[SEED]  client01 / password (CUSTOMER)");

                log.info("[SEED] Seed termine - 3 utilisateurs crees");
            } else {
                log.info("[SEED] Utilisateurs deja presents - ignore");
            }
        };
    }
}
