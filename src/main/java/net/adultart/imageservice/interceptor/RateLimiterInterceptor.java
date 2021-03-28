package net.adultart.imageservice.interceptor;

import io.vertx.core.http.HttpServerRequest;
import net.adultart.imageservice.service.ext.RateLimiter;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.core.interception.jaxrs.PostMatchContainerRequestContext;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

@Provider
@RateLimited(group = 0, maxRequests = 0)
public class RateLimiterInterceptor implements ContainerRequestFilter {
    @Inject
    JsonWebToken jwt;

    @Inject
    RateLimiter rateLimiter;

    @Context
    HttpServerRequest request;

    @Override
    public void filter(ContainerRequestContext context) throws IOException {
        if (jwt == null) {
            return;
        }
        if (context instanceof PostMatchContainerRequestContext) {
            Optional<RateLimited> rateLimited = Arrays.stream(((PostMatchContainerRequestContext) context).getResourceMethod().getMethodAnnotations())
                    .filter(annotation -> annotation instanceof RateLimited)
                    .map(annotation -> (RateLimited) annotation)
                    .findFirst();
            rateLimited.ifPresent(rL -> {
                String subject;
                if (jwt != null && jwt.getSubject() != null) {
                    subject = jwt.getSubject();
                } else {
                    subject = request.getHeader("X-FORWARDED-FOR");
                    if (subject == null) {
                        subject = request.remoteAddress().host();
                    }
                }
                if (rateLimiter.isRateLimited(subject, rL.group(), rL.maxRequests())) {
                    context.abortWith(Response.status(Response.Status.TOO_MANY_REQUESTS).build());
                }
            });
        }
    }
}
