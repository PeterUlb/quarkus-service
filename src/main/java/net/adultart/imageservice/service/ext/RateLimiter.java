package net.adultart.imageservice.service.ext;

public interface RateLimiter {
    boolean isRateLimited(String userId, long group, long limit);
}
