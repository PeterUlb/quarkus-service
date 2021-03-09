package net.adultart.imageservice;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

@ApplicationScoped
public class Producers {
    @Produces
    @ApplicationScoped
    public S3Presigner banana() {
        return S3Presigner.create();
    }
}
