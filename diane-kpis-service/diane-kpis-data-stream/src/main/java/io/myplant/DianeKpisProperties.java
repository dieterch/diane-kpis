package io.myplant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "statemachine")
public class DianeKpisProperties {
    private final ObjectMapper objectMapper = new ObjectMapper();

    private String tempFolder;
    private int parallelCalculations = 1;

    public String getTempFolder() {
        return tempFolder;
    }

    public void setTempFolder(String tempFolder) {
        this.tempFolder = tempFolder;
    }

    public int getParallelCalculations() {
        return parallelCalculations;
    }

    public void setParallelCalculations(int parallelCalculations) {
        this.parallelCalculations = parallelCalculations;
    }

    @Override
    public String toString() {
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return e.toString();
        }
    }
}
