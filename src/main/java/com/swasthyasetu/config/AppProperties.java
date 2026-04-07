package com.swasthyasetu.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private final Jwt jwt = new Jwt();
    private final Storage storage = new Storage();

    public Jwt getJwt() { return jwt; }
    public Storage getStorage() { return storage; }

    public static class Jwt {
        private String secret;
        private long accessExpiryMs;
        private long refreshExpiryMs;
        
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public long getAccessExpiryMs() { return accessExpiryMs; }
        public void setAccessExpiryMs(long accessExpiryMs) { this.accessExpiryMs = accessExpiryMs; }
        public long getRefreshExpiryMs() { return refreshExpiryMs; }
        public void setRefreshExpiryMs(long refreshExpiryMs) { this.refreshExpiryMs = refreshExpiryMs; }
    }

    public static class Storage {
        private String uploadDir;
        public String getUploadDir() { return uploadDir; }
        public void setUploadDir(String uploadDir) { this.uploadDir = uploadDir; }
    }
}
