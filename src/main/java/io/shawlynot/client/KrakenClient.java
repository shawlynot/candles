package io.shawlynot.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.shawlynot.client.KrakenUtil.NONCE_FIELD;
import static io.shawlynot.client.KrakenUtil.getNonce;

@Component
public class KrakenClient {

    private final static String WEBSOCKET_AUTH_PATH = "/0/private/GetWebSocketsToken";

    private final KrakenConfigProperties krakenConfigProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public KrakenClient(KrakenConfigProperties krakenConfigProperties) {
        this.krakenConfigProperties = krakenConfigProperties;
    }

    public void start() throws IOException {
        try {
            Objects.requireNonNull(krakenConfigProperties.apiKey(), "Must provide a kraken API key");
            Objects.requireNonNull(krakenConfigProperties.apiSecret(), "Must provide a kraken secret key");

            // get auth header
            var body = new LinkedHashMap<String, String>();
            body.put(NONCE_FIELD, String.valueOf(getNonce()));

            var requestSignature = KrakenUtil.getSignature(krakenConfigProperties.apiSecret(), body, WEBSOCKET_AUTH_PATH);

            var wsAuthRequest = HttpRequest.newBuilder()
                    .uri(new URI(krakenConfigProperties.restUrl() + WEBSOCKET_AUTH_PATH))
                    .headers(
                            "Content-Type", "application/json",
                            "Accept", "application/json",
                            "API-Key", krakenConfigProperties.apiKey(),
                            "API-Sign", requestSignature
                    )
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();
            var restClient = HttpClient.newHttpClient();
            var authResponse = restClient.send(wsAuthRequest, HttpResponse.BodyHandlers.ofString());
            var authResult = objectMapper.readValue(authResponse.body(), WsAuthResponse.class);
            if (!authResult.error().isEmpty()) {
                var errorMessage = authResult.error().stream().map(Object::toString).collect(Collectors.joining(","));
                throw new IOException("Error getting ws auth token: " +  errorMessage);
            }
            var wsAuthToken = Objects.requireNonNull(authResult.result().token(), "Error getting ws auth token: token missing from auth response");


        } catch (URISyntaxException | InterruptedException e) {
            throw new RuntimeException(e); // these should never be thrown
        }
    }

    private record WsAuthResponse(
            List<Object> error,
            WsAuthResult result
    ){}

    private record WsAuthResult(
            String token
    ){}
}
