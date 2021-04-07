package io.ulbrich.imageservice.service.ext;

import io.quarkus.redis.client.RedisClient;
import io.ulbrich.imageservice.config.RateLimiterConfig;
import io.vertx.redis.client.Response;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.LocalDateTime;

@ApplicationScoped
public class RateLimiterImpl implements RateLimiter {

    private static final Logger LOG = Logger.getLogger(RateLimiterImpl.class);

    @Inject
    RedisClient redisClient;

    @Inject
    RateLimiterConfig rateLimiterConfig;

    @Override
    public boolean isRateLimited(String userId, long group) {
        String key = userId + ":" + group + ":" + LocalDateTime.now().getHour();

        long alreadyUsed = 0;
        Response response = redisClient.get(key);
        if (response != null) {
            alreadyUsed = Long.parseLong(response.toString());
        }
        LOG.debug(key + "-> " + alreadyUsed);

        if (alreadyUsed >= rateLimiterConfig.getRateForEndpointGroup(group)) {
            return true;
        }

        redisClient.multi();
        redisClient.incr(key);
        redisClient.expire(key, "3600");
        redisClient.exec();

        return false;
    }
}
