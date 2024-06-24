package io.shawlynot.producer.kraken;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "channel", defaultImpl = Void.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = KrakenBookResponse.class, name = "book"),
})
public interface KrakenResponse {
}
