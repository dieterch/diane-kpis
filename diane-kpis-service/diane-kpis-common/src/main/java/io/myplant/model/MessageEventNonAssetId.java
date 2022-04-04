package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventNonAssetId {

        @JsonProperty("t")
        private long timestamp;

        @JsonProperty("na")
        private String name;

        @JsonProperty("s")
        private int severity;
}
