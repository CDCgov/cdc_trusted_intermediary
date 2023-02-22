package gov.hhs.cdc.trustedintermediary.wrappers;

import java.io.IOException;

public interface HttpClient {
    String post(String path, String body) throws IOException;

    HttpClient setToken(String token);
}
