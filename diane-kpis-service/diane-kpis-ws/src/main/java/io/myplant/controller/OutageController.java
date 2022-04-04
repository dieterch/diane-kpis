package io.myplant.controller;

import io.myplant.Constants;
import io.myplant.Utils;
import io.myplant.model.IeeeStates;
import io.myplant.model.OutageDto;
import io.myplant.model.StateType;
import io.myplant.service.StateMachineV2Service;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/outage/")
public class OutageController {
    private static final Logger logger = LoggerFactory.getLogger(OutageController.class);
    private final StateMachineV2Service stateMachineService;

    @Autowired
    public OutageController(StateMachineV2Service stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @ApiOperation(value = "Returns starts of list of asset.")
    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<OutageDto>> getStateMachine(@RequestParam(value = Constants.ASSET_SERIALS_TOKEN, required = true) String serialParam,
                                                           @RequestParam(value = Constants.ASSET_MODEL_TOKEN, required = false) String modelParam,
                                                           @RequestParam(value = Constants.USE_MERGED_TOKEN, required = false, defaultValue = "true") String mergedParam,
                                                           //@RequestParam(value = Constants.DAILY_TOKEN, required = false) String dailyString,
                                                           @RequestParam(value = Constants.OUTAGES_TYPE_TOKEN, required = true) StateType outageType,
                                                           @RequestParam(value = Constants.FROM_TOKEN, required = false) String fromParam,
                                                           @RequestParam(value = Constants.TO_TOKEN, required = false) String toParam,
                                                           @RequestParam(value = Constants.DATE_STRING_TOKEN, required = false, defaultValue = "true") String useDateStringParam,
                                                           @RequestParam(value = Constants.LANGUAGE_TOKEN, required = false, defaultValue = "en") String language,
                                                           @RequestParam(value = Constants.FILTER_UNPLANNED_MAINTENANCE, required = false, defaultValue = "false") boolean filterUnplannedMaintance
    ) {
        try {
            boolean useMerged = Utils.getBoolenFromString(mergedParam);
            boolean useDateString = Utils.getBoolenFromString(useDateStringParam);
            Long from = null;
            if (StringUtils.isNotEmpty(fromParam)) {
                from = Utils.getTimeFromString(fromParam, false);
            }

            Long to = null;
            if (StringUtils.isNotEmpty(toParam)) {
                to = Utils.getTimeFromString(toParam, false);
            }

            if (StringUtils.isEmpty(modelParam)) {
                modelParam = "J-Engine";
            }

            String[] serialStrings = serialParam.split(",");

            List<OutageDto> meta = new ArrayList<>();
            meta = stateMachineService.getOutagesOfAsset(modelParam, serialStrings, outageType, useMerged, from, to, useDateString, language);

            if(filterUnplannedMaintance) {
                // display only outages without unplanned maintenance
                meta = meta.stream().filter(x -> x.getState() != IeeeStates.UNPLANNED_MAINTENANCE).collect(Collectors.toList());
            }
            return new ResponseEntity<>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get outages", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}