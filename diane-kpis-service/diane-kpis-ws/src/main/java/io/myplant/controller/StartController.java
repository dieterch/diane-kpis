package io.myplant.controller;

import io.myplant.Constants;
import io.myplant.Utils;
import io.myplant.model.StartDto;
import io.myplant.service.StartsService;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/start/")
public class StartController {
    private static final Logger logger = LoggerFactory.getLogger(StartController.class);
    private final StartsService startsService;

    @Autowired
    public StartController(StartsService startsService) {
        this.startsService = startsService;
    }

    @ApiOperation(value = "Returns starts of list of asset.")
    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<StartDto>> getStateMachine(@RequestParam(value = Constants.ASSET_SERIALS_TOKEN, required = true) String serialParam,
                                                          @RequestParam(value = Constants.ASSET_MODEL_TOKEN, required = false) String modelParam,
                                                          @RequestParam(value = Constants.USE_MERGED_TOKEN, required = false) String mergedParam,
                                                          @RequestParam(value = Constants.DATE_STRING_TOKEN, required = false, defaultValue = "true") String useDateStringParam,
                                                          @RequestParam(value = Constants.LANGUAGE_TOKEN, required = false, defaultValue = "en") String language
    ) {
        try {
            boolean useMerged = Utils.getBoolenFromString(mergedParam);
            boolean useDateString = Utils.getBoolenFromString(useDateStringParam);
            Long from = null;
            Long to = null;

            if (StringUtils.isEmpty(modelParam)) {
                modelParam = "J-Engine";
            }

            String[] serialStrings = serialParam.split(",");

            List<StartDto> meta;
            meta = startsService.getStartsOfAssets(modelParam, serialStrings, useMerged, from, to, useDateString, language);

            return new ResponseEntity<>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get starts", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}