package io.ulbrich.util;

import io.smallrye.jwt.build.Jwt;

import java.util.Set;

public class JwtTokenGenerator {
    public static String generateToken() {
        String userId = "1";
        Set<String> roles = Set.of("user");

        long currentTimeInSecs = currentTimeInSecs();
        return Jwt.claims()
                .subject(userId)
                .issuedAt(currentTimeInSecs)
                .expiresAt(currentTimeInSecs + 1800)
                .groups(roles)
                .sign();
    }

    public static int currentTimeInSecs() {
        long currentTimeMS = System.currentTimeMillis();
        return (int) (currentTimeMS / 1000);
    }
}
