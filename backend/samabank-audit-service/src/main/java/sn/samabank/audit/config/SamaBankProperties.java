package sn.samabank.audit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "samabank")
public class SamaBankProperties {
    private Jwt jwt = new Jwt();
    private Cors cors = new Cors();

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    public static class Jwt {
        private String secret;
        private long accessTokenTtl = 900;

        public String getSecret() { return secret; }
        public void setSecret(String s) { this.secret = s; }
        public long getAccessTokenTtl() { return accessTokenTtl; }
        public void setAccessTokenTtl(long v) { this.accessTokenTtl = v; }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of("http://localhost:4200");

        public List<String> getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(List<String> v) { this.allowedOrigins = v; }
    }
}
