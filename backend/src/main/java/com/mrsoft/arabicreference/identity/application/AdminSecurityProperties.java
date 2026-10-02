package com.mrsoft.arabicreference.identity.application;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.admin")
public class AdminSecurityProperties {

    private String jwtSecret;
    private Duration accessTokenTtl = Duration.ofMinutes(10);
    private Duration refreshTokenTtl = Duration.ofDays(14);
    private int maxFailedAttempts = 5;
    private Duration lockoutDuration = Duration.ofMinutes(15);
    private final RateLimit rateLimit = new RateLimit();
    private final Bootstrap bootstrap = new Bootstrap();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public void setAccessTokenTtl(Duration accessTokenTtl) {
        this.accessTokenTtl = accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    public void setRefreshTokenTtl(Duration refreshTokenTtl) {
        this.refreshTokenTtl = refreshTokenTtl;
    }

    public int getMaxFailedAttempts() {
        return maxFailedAttempts;
    }

    public void setMaxFailedAttempts(int maxFailedAttempts) {
        this.maxFailedAttempts = maxFailedAttempts;
    }

    public Duration getLockoutDuration() {
        return lockoutDuration;
    }

    public void setLockoutDuration(Duration lockoutDuration) {
        this.lockoutDuration = lockoutDuration;
    }

    public RateLimit getRateLimit() {
        return rateLimit;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static class RateLimit {
        private int login = 10;
        private int refresh = 30;
        private Duration window = Duration.ofMinutes(1);

        public int getLogin() {
            return login;
        }

        public void setLogin(int login) {
            this.login = login;
        }

        public int getRefresh() {
            return refresh;
        }

        public void setRefresh(int refresh) {
            this.refresh = refresh;
        }

        public Duration getWindow() {
            return window;
        }

        public void setWindow(Duration window) {
            this.window = window;
        }
    }

    public static class Bootstrap {
        private String username = "";
        private String email = "";
        private String displayName = "";
        private String password = "";

        public boolean complete() {
            return notBlank(username) && notBlank(email) && notBlank(displayName) && notBlank(password);
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        private static boolean notBlank(String value) {
            return value != null && !value.isBlank();
        }
    }
}
