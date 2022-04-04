package io.myplant.controller;

import io.myplant.model.OverwriteStateDto;
import io.myplant.service.OverwriteStateService;
import io.myplant.Constants;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;


@Controller
@RequestMapping("/overwrite-state/")
public class OverwriteStateController {
    private static final Logger logger = LoggerFactory.getLogger(OverwriteStateController.class);

    private final OverwriteStateService overwriteStateService;

    @Autowired
    public OverwriteStateController(OverwriteStateService overwriteStateService) {
        this.overwriteStateService = overwriteStateService;
    }

    @ApiOperation(value = "Return a state with id.")
    @GetMapping(value = "{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<OverwriteStateDto> getStateId(@PathVariable long id) {
        try {
            OverwriteStateDto meta = overwriteStateService.getById(id);
            if(meta == null)
                return new ResponseEntity(HttpStatus.NOT_FOUND);

            return new ResponseEntity<OverwriteStateDto>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "change a state with id.")
    @PutMapping(value = "{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<OverwriteStateDto> putStateId(@PathVariable long id, @RequestBody OverwriteStateDto request) {
        try {
            OverwriteStateDto meta = overwriteStateService.setById(id, request);
            return new ResponseEntity<OverwriteStateDto>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "create a state.")
    @PostMapping(value = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<OverwriteStateDto> postState(@RequestBody OverwriteStateDto request) {
        try {
            OverwriteStateDto meta = overwriteStateService.createOne(request);
            return new ResponseEntity<OverwriteStateDto>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "delete a state.")
    @DeleteMapping(value = "{id}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<OverwriteStateDto> deleteStateId(@PathVariable long id) {
        try {
            overwriteStateService.deleteOne(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @ApiOperation(value = "Return states of asset with serial.")
    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<OverwriteStateDto>> getStates(@RequestParam(value = Constants.ASSET_SERIALS_TOKEN, required = true) String serialParam,
                                                                 @RequestParam(value = Constants.ASSET_MODEL_TOKEN, required = false) String modelParam
    ) {
        try {
            if(StringUtils.isEmpty(modelParam)){
                modelParam = "J-Engine";
            }

            String[] serialStrings = serialParam.split(",");


            List<OverwriteStateDto> states = overwriteStateService.getAll(modelParam, serialStrings);


            return new ResponseEntity<>(states, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }

    private long getTimeFromString(String dateString, boolean useDateString){
        if(useDateString){
            return Date.from(ZonedDateTime.parse(dateString).toInstant()).getTime();
        }
        else
            return Long.parseLong(dateString);
    }


    private boolean getBoolenFromString(String boolString){
        return StringUtils.isNotEmpty(boolString) && (boolString.equals("1") || boolString.toLowerCase().equals("true"));
    }


}
