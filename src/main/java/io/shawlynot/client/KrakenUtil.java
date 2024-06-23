package io.shawlynot.client;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.function.ThrowingFunction;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class KrakenUtil {

    public static final String NONCE_FIELD = "nonce";

    private KrakenUtil () {

    }

    public static long getNonce() {
        return System.currentTimeMillis();
    }

    /**
     * Calculate the API-Sign header for a Kraken REST request. The body must be an ordered map and must contain the
     * nonce.
     * @param secret base64 encoded API secret
     * @param body request body in an ordered map
     * @param path uri path
     * @return API key header
     */
    public static String getSignature(String secret, Map<String, String> body, String path) {
        var nonce = Objects.requireNonNull(body.get(NONCE_FIELD), "Request must include nonce");
        var encodedBody = body.entrySet().stream()
                .map(ThrowingFunction.of(
                        entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
                )).collect(Collectors.joining("&"));
        var encoded = nonce + encodedBody;
        var sha256Hash = DigestUtils.sha256(encoded);
        var message = ArrayUtils.addAll(path.getBytes(StandardCharsets.UTF_8), sha256Hash);
        var mac = new HmacUtils(HmacAlgorithms.HMAC_SHA_512, Base64.getDecoder().decode(secret))
                .hmac(message);
        return Base64.getEncoder().encodeToString(mac);
    }
}
