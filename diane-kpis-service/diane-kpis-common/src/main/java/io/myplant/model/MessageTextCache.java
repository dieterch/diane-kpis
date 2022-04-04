package io.myplant.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@EqualsAndHashCode(exclude="ttl,messageCache")
@NoArgsConstructor
public class MessageTextCache {

    private String model;
    private String serial;
    private String language;
    private long ttl;
    private long lastAccess;
    Map<String,String> messageCache;
}
