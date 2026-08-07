package com.gl.app.ApiGateway.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "mySecureJWTSecretKey1234567890123456789012345678901234";

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
    }

    private String buildToken(Date expiration) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("alice")
                .claim("userId", 42)
                .claim("role", "ADMIN")
                .issuedAt(new Date())
                .expiration(expiration)
                .signWith(key)
                .compact();
    }

    @Test
    void extractClaims_fromValidToken() {
        String token = buildToken(new Date(System.currentTimeMillis() + 3600000));

        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtTokenProvider.extractRole(token)).isEqualTo("ADMIN");
        assertThat(jwtTokenProvider.isTokenValid(token)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_false() {
        String token = buildToken(new Date(System.currentTimeMillis() - 10000));

        assertThat(jwtTokenProvider.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_tamperedToken_false() {
        String token = buildToken(new Date(System.currentTimeMillis() + 3600000)) + "tampered";

        assertThat(jwtTokenProvider.isTokenValid(token)).isFalse();
    }

    @Test
    void isTokenValid_garbageToken_false() {
        assertThat(jwtTokenProvider.isTokenValid("not-a-token")).isFalse();
    }

    @Test
    void extractClaims_fromSignedTokenWithoutUserId_returnsNull() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        String token = Jwts.builder()
                .subject("bob")
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        assertThat(jwtTokenProvider.extractUserId(token)).isNull();
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("bob");
        assertThat(jwtTokenProvider.extractRole(token)).isNull();
    }
}
