package io.ulbrich.mocks;

import com.google.auth.Credentials;
import com.google.auth.ServiceAccountSigner;
import io.quarkus.test.Mock;

import javax.inject.Singleton;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Mock
@Singleton
public class TestGcpCredentials extends Credentials implements ServiceAccountSigner {
    @Override
    public String getAuthenticationType() {
        return null;
    }

    @Override
    public Map<String, List<String>> getRequestMetadata(URI uri) throws IOException {
        return null;
    }

    @Override
    public boolean hasRequestMetadata() {
        return false;
    }

    @Override
    public boolean hasRequestMetadataOnly() {
        return false;
    }

    @Override
    public void refresh() throws IOException {

    }

    @Override
    public String getAccount() {
        return "MOCK-ACCOUNT";
    }

    @Override
    public byte[] sign(byte[] toSign) {
        byte[] b = new byte[1024];
        new Random().nextBytes(b);
        return b;
    }
}
