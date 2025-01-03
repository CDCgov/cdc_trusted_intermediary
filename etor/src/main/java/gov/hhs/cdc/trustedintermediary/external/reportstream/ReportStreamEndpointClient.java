package gov.hhs.cdc.trustedintermediary.external.reportstream;

import gov.hhs.cdc.trustedintermediary.context.ApplicationContext;
import gov.hhs.cdc.trustedintermediary.etor.RSEndpointClient;
import gov.hhs.cdc.trustedintermediary.wrappers.AuthEngine;
import gov.hhs.cdc.trustedintermediary.wrappers.Cache;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClient;
import gov.hhs.cdc.trustedintermediary.wrappers.HttpClientException;
import gov.hhs.cdc.trustedintermediary.wrappers.Logger;
import gov.hhs.cdc.trustedintermediary.wrappers.SecretRetrievalException;
import gov.hhs.cdc.trustedintermediary.wrappers.Secrets;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.Formatter;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.FormatterProcessingException;
import gov.hhs.cdc.trustedintermediary.wrappers.formatter.TypeReference;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;

/** Implementation of the RSEndpointClient interface for interacting with ReportStream endpoints */
public class ReportStreamEndpointClient implements RSEndpointClient {
    private static final String RS_URL_PREFIX =
            ApplicationContext.getProperty("REPORT_STREAM_URL_PREFIX");
    private static final String RS_DOMAIN_NAME =
            Optional.ofNullable(RS_URL_PREFIX)
                    .map(urlPrefix -> urlPrefix.replace("https://", "").replace("http://", ""))
                    .orElse("");
    private static final String RS_WATERS_API_URL = RS_URL_PREFIX + "/api/waters";
    private static final String RS_AUTH_API_URL = RS_URL_PREFIX + "/api/token";
    private static final String RS_HISTORY_API_URL =
            RS_URL_PREFIX + "/api/waters/report/{id}/history";
    private static final String RS_DELIVERY_API_URL = RS_WATERS_API_URL + "/report/{id}/delivery";

    private static final String OUR_PRIVATE_KEY_ID =
            "trusted-intermediary-private-key-" + ApplicationContext.getEnvironment();
    private static final String RS_TOKEN_CACHE_ID = "report-stream-token";

    private static final String CLIENT_NAME = "flexion.etor-service-sender";
    private static final Map<String, String> RS_AUTH_API_HEADERS =
            Map.of("Content-Type", "application/x-www-form-urlencoded");

    @Inject private HttpClient client;
    @Inject private AuthEngine jwt;
    @Inject private Formatter formatter;
    @Inject private Logger logger;
    @Inject private Secrets secrets;
    @Inject private Cache cache;

    private static final ReportStreamEndpointClient INSTANCE = new ReportStreamEndpointClient();

    public static ReportStreamEndpointClient getInstance() {
        return INSTANCE;
    }

    private ReportStreamEndpointClient() {}

    protected String requestAuthEndpoint() throws ReportStreamEndpointClientException {
        logger.logInfo("Requesting token from ReportStream");
        String ourPrivateKey;
        String response;
        try {
            ourPrivateKey = retrievePrivateKey();
            String senderToken =
                    jwt.generateToken(
                            CLIENT_NAME,
                            CLIENT_NAME,
                            CLIENT_NAME,
                            RS_DOMAIN_NAME,
                            300,
                            ourPrivateKey);
            String body = composeAuthRequestBody(senderToken);
            response = client.post(RS_AUTH_API_URL, RS_AUTH_API_HEADERS, body);
        } catch (Exception e) {
            throw new ReportStreamEndpointClientException(
                    "Error getting the API token from ReportStream", e);
        }
        // only cache our private key if we successfully authenticate to RS
        cacheOurPrivateKeyIfNotCachedAlready(ourPrivateKey);

        return response;
    }

    @Override
    public String requestWatersEndpoint(String body, String bearerToken)
            throws ReportStreamEndpointClientException {
        logger.logInfo("Sending payload to ReportStream");

        Map<String, String> headers =
                Map.of(
                        "Authorization",
                        "Bearer " + bearerToken,
                        "client",
                        CLIENT_NAME,
                        "Content-Type",
                        "application/fhir+ndjson");

        try {
            return client.post(RS_WATERS_API_URL, headers, body);
        } catch (HttpClientException e) {
            throw new ReportStreamEndpointClientException(
                    "Error POSTing the payload to ReportStream", e);
        }
    }

    @Override
    public String requestHistoryEndpoint(String outboundReportId, String bearerToken)
            throws ReportStreamEndpointClientException {
        logger.logInfo("Requesting history API from ReportStream");

        Map<String, String> headers = Map.of("Authorization", "Bearer " + bearerToken);

        try {
            String url = RS_HISTORY_API_URL.replace("{id}", outboundReportId);
            return client.get(url, headers);
        } catch (HttpClientException e) {
            throw new ReportStreamEndpointClientException(
                    "Error GETting the history from ReportStream", e);
        }
    }

    @Override
    public String requestDeliveryEndpoint(String inboundReportId, String bearerToken)
            throws ReportStreamEndpointClientException {
        logger.logInfo("Requesting delivery API from ReportStream");

        Map<String, String> headers = Map.of("Authorization", "Bearer " + bearerToken);

        try {
            String url = RS_DELIVERY_API_URL.replace("{id}", inboundReportId);
            return client.get(url, headers);
        } catch (HttpClientException e) {
            throw new ReportStreamEndpointClientException(
                    "Error GETting deliveries from ReportStream", e);
        }
    }

    protected String requestToken() throws ReportStreamEndpointClientException {
        logger.logInfo("Requesting token from ReportStream");

        String response = requestAuthEndpoint();
        try {
            Map<String, String> responseObject =
                    formatter.convertJsonToObject(response, new TypeReference<>() {});
            return responseObject.get("access_token");
        } catch (FormatterProcessingException e) {
            throw new ReportStreamEndpointClientException(
                    "Unable to extract access_token from response", e);
        }
    }

    @Override
    public String getRsToken() throws ReportStreamEndpointClientException {
        logger.logInfo("Looking up ReportStream token");

        var token = cache.get(RS_TOKEN_CACHE_ID);

        if (token != null && isValidToken(token)) {
            logger.logDebug("valid cache token");
            return token;
        }

        token = requestToken();

        cache.put(RS_TOKEN_CACHE_ID, token);

        return token;
    }

    protected boolean isValidToken(String token) {
        LocalDateTime expirationDate = jwt.getExpirationDate(token);

        return LocalDateTime.now().isBefore(expirationDate.minus(15, ChronoUnit.SECONDS));
    }

    protected String retrievePrivateKey() throws SecretRetrievalException {
        String key = cache.get(OUR_PRIVATE_KEY_ID);
        if (key != null) {
            return key;
        }

        key = secrets.getKey(OUR_PRIVATE_KEY_ID);

        return key;
    }

    void cacheOurPrivateKeyIfNotCachedAlready(String privateKey) {
        String key = cache.get(OUR_PRIVATE_KEY_ID);
        if (key != null) {
            return;
        }

        cache.put(OUR_PRIVATE_KEY_ID, privateKey);
    }

    protected String composeAuthRequestBody(String senderToken) {
        String scope = "flexion.*.report";
        String grantType = "client_credentials";
        String clientAssertionType = "urn:ietf:params:oauth:client-assertion-type:jwt-bearer";
        return "scope="
                + scope
                + "&grant_type="
                + grantType
                + "&client_assertion_type="
                + clientAssertionType
                + "&client_assertion="
                + senderToken;
    }
}
