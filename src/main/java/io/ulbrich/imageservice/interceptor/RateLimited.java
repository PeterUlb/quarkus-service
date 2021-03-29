package io.ulbrich.imageservice.interceptor;

import javax.ws.rs.NameBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@NameBinding
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimited {
    // TODO: This will not work well when two endpoints define the same group with different maxRequests
    // Instead, use central configuration which defines maxRequests per endpointGroup!
    long group();

    long maxRequests();
}
