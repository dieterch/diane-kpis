package io.myplant;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.myplant.model.IeeeStates;
import io.myplant.model.OverwriteStateDto;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class IeeeDeserializerTest {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void deserializeIeeeString() {


        try {
            OverwriteStateDto overwriteStateDto = objectMapper.readValue("{\"ieeeState\" :\"FORCEDOUTAGE_REL\"}", OverwriteStateDto.class);

            assertEquals(IeeeStates.FORCEDOUTAGE_REL, overwriteStateDto.getIeeeState());
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}