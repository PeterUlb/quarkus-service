package io.ulbrich.imageservice.config;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.util.Map;

@ApplicationScoped
public class RateLimiterConfig {
    private long defaultRate;
    private Map<Long, Long> endpointGroupRates;

    @PostConstruct
    void init() {
        this.endpointGroupRates = Map.of(
                0L, 100L,
                1L, 100L,
                9998L, 5L
        );
        this.defaultRate = 1000L;
    }

    public long getRateForEndpointGroup(long endpointGroupId) {
        return endpointGroupRates.getOrDefault(endpointGroupId, defaultRate);
    }

    public Map<Long, Long> getEndpointGroupRates() {
        return endpointGroupRates;
    }

    public void setEndpointGroupRates(Map<Long, Long> endpointGroupRates) {
        this.endpointGroupRates = endpointGroupRates;
    }
}
