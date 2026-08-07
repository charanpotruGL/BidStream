package com.example.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Long extractUserId(String token) {
        JsonNode payload = parsePayload(token);
        JsonNode userIdNode = payload.get("userId");
        return userIdNode != null && userIdNode.isNumber() ? userIdNode.asLong() : null;
    }

    public String extractUsername(String token) {
        JsonNode payload = parsePayload(token);
        JsonNode sub = payload.get("sub");
        return sub != null && !sub.isNull() ? sub.asText() : null;
    }

    public String extractRole(String token) {
        JsonNode payload = parsePayload(token);
        JsonNode role = payload.get("role");
        return role != null && !role.isNull() ? role.asText() : null;
    }

    public boolean isTokenValid(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        try {
            return verifySignature(token) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isExpired(String token) throws Exception {
        JsonNode payload = parsePayload(token);
        JsonNode exp = payload.get("exp");
        if (exp == null || !exp.isNumber()) {
            return false;
        }
        long expirySeconds = exp.asLong();
        return System.currentTimeMillis() / 1000 >= expirySeconds;
    }

    private boolean verifySignature(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        String algorithm = resolveMacAlgorithm(parts[0]);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), algorithm));
        byte[] expected = mac.doFinal((parts[0] + "." + parts[1]).getBytes(StandardCharsets.UTF_8));
        byte[] actual = decodeBase64Url(parts[2]);
        return MessageDigest.isEqual(expected, actual);
    }

    private String resolveMacAlgorithm(String headerSegment) {
        try {
            JsonNode header = objectMapper.readTree(decodeBase64Url(headerSegment));
            String alg = header.get("alg") != null ? header.get("alg").asText() : "";
            return switch (alg) {
                case "HS384" -> "HmacSHA384";
                case "HS512" -> "HmacSHA512";
                default -> "HmacSHA256";
            };
        } catch (Exception e) {
            return "HmacSHA256";
        }
    }

    private JsonNode parsePayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token");
        }
        try {
            return objectMapper.readTree(decodeBase64Url(parts[1]));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JWT payload", e);
        }
    }

    private byte[] decodeBase64Url(String value) {
        String base64 = value.replace('-', '+').replace('_', '/');
        int mod = base64.length() % 4;
        if (mod > 0) {
            base64 += "====".substring(mod);
        }
        return Base64.getDecoder().decode(base64);
    }
}
