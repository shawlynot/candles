package io.shawlynot.client;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Objects;

import static io.shawlynot.client.KrakenUtil.NONCE_FIELD;
import static io.shawlynot.client.KrakenUtil.getNonce;

@Configuration
@EnableConfigurationProperties(KrakenConfigProperties.class)
public class KrakenConfig {



}
