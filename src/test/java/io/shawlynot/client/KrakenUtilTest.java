package io.shawlynot.client;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

class KrakenUtilTest {

    @Test
    void testSignature() {
        // test data from https://docs.kraken.com/api/docs/guides/spot-rest-auth/
        var privateKey = "kQH5HW/8p1uGOVjbgWA7FunAmGO8lsSUXNsu3eow76sz84Q18fWxnyRzBHCd3pd5nE9qa99HAZtuZuj6F1huXg==";

        LinkedHashMap<String, String> message = new LinkedHashMap<>();
        message.put("nonce", "1616492376594");
        message.put("ordertype", "limit");
        message.put("pair", "XBTUSD");
        message.put("price", "37500");
        message.put("type", "buy");
        message.put("volume", "1.25");

        var path = "/0/private/AddOrder";

        var hash = KrakenUtil.getSignature(privateKey, message, path);

        Assertions.assertEquals("4/dpxb3iT4tp/ZCVEwSnEsLxx0bqyhLpdfOpc6fn7OR8+UClSV5n9E6aSS8MPtnRfp32bAb0nmbRn6H8ndwLUQ==", hash);
    }

}