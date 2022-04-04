package io.myplant.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.myplant.model.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;

@Controller
@RequestMapping("/state-types")
public class StateTypeControler {

    @Autowired ObjectMapper mapper;

    @ApiOperation(value = "Return list of state types.")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<String> getTypes() {

        HashMap<String, HashMap<String, Integer>> types = new HashMap<>();
        HashMap<String, Integer> typeMap = new HashMap<>();
        for (final AvailableStates s: AvailableStates.values()) {
            typeMap.put(s.getStringValue(), s.getValue());
        }
        types.put("AvailableStates", typeMap);

        typeMap = new HashMap<>();
        for (final DemandSelectorSwitchStates s: DemandSelectorSwitchStates.values()) {
            typeMap.put(s.getStringValue(), s.getValue());
        }
        types.put("DemandSelectorSwitchStates", typeMap);


        typeMap = new HashMap<>();
        for (final ServiceSelectorSwitchStates s: ServiceSelectorSwitchStates.values()) {
            typeMap.put(s.getStringValue(), s.getValue());
        }
        types.put("ServiceSelectorSwitchStates", typeMap);

        typeMap = new HashMap<>();
        for (final IeeeStates s: IeeeStates.values()) {
            typeMap.put(s.getStringValue(), s.getValue());
        }
        types.put("IeeeStates", typeMap);


        typeMap = new HashMap<>();
        for (final EngineAction s: EngineAction.values()) {
            typeMap.put(s.getStringValue(), s.getValue());
        }
        types.put("EngineAction", typeMap);

        try {
            return new ResponseEntity<String>(mapper.writeValueAsString(types), HttpStatus.OK);
        } catch (JsonProcessingException e) {

            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
