package com.mrsoft.arabicreference.identity.infrastructure.security;

import com.mrsoft.arabicreference.identity.application.AdminSecurityProperties;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.stereotype.Component;

@Component
public class JwtAccessTokens {

    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final AdminSecurityProperties properties;

    public JwtAccessTokens(AdminSecurityProperties properties) {
        byte[] secret = properties.getJwtSecret() == null
                ? new byte[0]
                : properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secret.length < 32) {
            throw new IllegalStateException("ADMIN_JWT_SECRET must be at least 32 bytes.");
        }
        SecretKey key = new SecretKeySpec(secret, "HmacSHA256");
        this.encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        jwtDecoder.setJwtValidator(new JwtTimestampValidator(java.time.Duration.ZERO));
        this.decoder = jwtDecoder;
        this.properties = properties;
    }

    public Issued issue(UUID userId, Instant issuedAt) {
        return issue(userId, issuedAt, issuedAt.plus(properties.getAccessTokenTtl()));
    }

    public Issued issue(UUID userId, Instant issuedAt, Instant expiresAt) {
        UUID tokenId = UUID.randomUUID();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).type("JWT").build();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userId.toString())
                .id(tokenId.toString())
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .build();
        String value = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new Issued(value, tokenId, expiresAt);
    }

    public Jwt decode(String token) {
        return decoder.decode(token);
    }

    public record Issued(String value, UUID tokenId, Instant expiresAt) {
    }
}
