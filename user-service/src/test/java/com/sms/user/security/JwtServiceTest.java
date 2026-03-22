package com.sms.user.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    // A valid 256-bit Base64-encoded secret for HS256
    private static final String TEST_SECRET =
            "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", TEST_SECRET);
    }

    private String generateTestToken(String username, long expirationMillis) {
        Key key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(TEST_SECRET));
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractUsername_withValidToken_shouldReturnCorrectUsername() {
        String token = generateTestToken("john.doe", 3600_000L);

        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("john.doe");
    }

    @Test
    void isTokenValid_withValidTokenAndMatchingUserDetails_shouldReturnTrue() {
        String token = generateTestToken("john.doe", 3600_000L);
        UserDetails userDetails = User.withUsername("john.doe")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isTrue();
    }

    @Test
    void isTokenValid_withValidTokenButWrongUsername_shouldReturnFalse() {
        String token = generateTestToken("john.doe", 3600_000L);
        UserDetails userDetails = User.withUsername("other.user")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    void isTokenValid_withExpiredToken_shouldReturnFalse() {
        // Token expired 1 second ago
        String token = generateTestToken("john.doe", -1000L);
        UserDetails userDetails = User.withUsername("john.doe")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        boolean valid = jwtService.isTokenValid(token, userDetails);

        assertThat(valid).isFalse();
    }

    @Test
    void extractUsername_withExpiredToken_shouldStillExtractSubject() {
        String token = generateTestToken("expired.user", -1000L);

        // extractUsername parses without expiry validation
        String username = jwtService.extractUsername(token);

        assertThat(username).isEqualTo("expired.user");
    }

    @Test
    void extractClaim_withValidToken_shouldExtractExpiration() {
        long expirationMillis = 3600_000L;
        String token = generateTestToken("john.doe", expirationMillis);

        Date expiration = jwtService.extractClaim(token,
                claims -> claims.getExpiration());

        assertThat(expiration).isAfter(new Date());
    }
}
