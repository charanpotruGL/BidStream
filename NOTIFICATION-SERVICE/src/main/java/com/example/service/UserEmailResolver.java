package com.example.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UserEmailResolver {

    private final String pattern;

    public UserEmailResolver(
            @Value("${notification.email.user-address-pattern:user{userId}@example.com}") String pattern) {
        this.pattern = pattern;
    }

    public String resolve(Long userId) {
        if (userId == null) {
            return null;
        }
        return pattern.replace("{userId}", String.valueOf(userId));
    }
}
