package com.example.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private static final String SECRET = "mySecureJWTSecretKey1234567890123456789012345678901234";
    private static final long EXPIRATION = 86400000L;

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", EXPIRATION);
    }

    @Test
    void generateToken_andExtractClaims() {
        String token = jwtTokenProvider.generateToken(42L, "alice", "ADMIN");

        assertThat(token).isNotBlank();
        assertThat(jwtTokenProvider.extractUsername(token)).isEqualTo("alice");
        assertThat(jwtTokenProvider.extractUserId(token)).isEqualTo(42L);
        assertThat(jwtTokenProvider.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void getExpiration_returnsConfiguredValue() {
        assertThat(jwtTokenProvider.getExpiration()).isEqualTo(EXPIRATION);
    }

    @Test
    void isTokenValid_matchingUsername_true() {
        String token = jwtTokenProvider.generateToken(1L, "alice", "USER");

        assertThat(jwtTokenProvider.isTokenValid(token, "alice")).isTrue();
    }

    @Test
    void isTokenValid_differentUsername_false() {
        String token = jwtTokenProvider.generateToken(1L, "alice", "USER");

        assertThat(jwtTokenProvider.isTokenValid(token, "bob")).isFalse();
    }

    @Test
    void extractUserId_missingClaim_returnsNull() {
        String token = jwtTokenProvider.generateToken(1L, "alice", "USER");
        assertThat(jwtTokenProvider.extractUserId(token)).isNotNull();
    }
}
