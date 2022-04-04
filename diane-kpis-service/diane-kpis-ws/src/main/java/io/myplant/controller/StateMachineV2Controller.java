package io.myplant.controller;

import io.myplant.Constants;
import io.myplant.Utils;
import io.myplant.model.DeviceStateDto;
import io.myplant.service.StateMachineV2Service;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
@RequestMapping("/state/")
public class StateMachineV2Controller {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineV2Controller.class);

    private StateMachineV2Service stateMachineService;

    @Autowired
    public StateMachineV2Controller(StateMachineV2Service stateMachineService) {
        this.stateMachineService = stateMachineService;
    }

    @ApiOperation(value = "Returns states of asset with id.")
    @GetMapping(value = "{assetId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<DeviceStateDto>> getStateMachineId(@PathVariable long assetId,
                                                                  @RequestParam(value = Constants.CALC_NEW_TOKEN, required = false) String calculateNewString,
                                                                  @RequestParam(value = Constants.DAILY_TOKEN, required = false) String dailyString,
                                                                  @RequestParam(value = Constants.USE_MERGED_TOKEN, required = false) String mergedParam,
                                                                  @RequestParam(value = Constants.FROM_TOKEN, required = false) String fromParam,
                                                                  @RequestParam(value = Constants.TO_TOKEN, required = false) String toParam,
                                                                  @RequestParam(value = Constants.DATE_STRING_TOKEN, required = false, defaultValue = "true") String useDateStringParam,
                                                                  @RequestParam(value = Constants.LANGUAGE_TOKEN, required = false, defaultValue = "en") String language
    ) {
        try {
            boolean daily = Utils.getBoolenFromString(dailyString);
            boolean useMerged = Utils.getBoolenFromString(mergedParam);
            boolean calculateNew = Utils.getBoolenFromString(calculateNewString);
            boolean useDateString = Utils.getBoolenFromString(useDateStringParam);

            Long from = null;
            Long to = null;

            if (StringUtils.isNotEmpty(fromParam)) {
                from = Utils.getTimeFromString(fromParam, false);
            }

            if (StringUtils.isNotEmpty(toParam)) {
                to = Utils.getTimeFromString(toParam, false);
            }
            List<DeviceStateDto> meta = stateMachineService.getStatesOfAsset(assetId, calculateNew, daily, useMerged, null, from, to, useDateString, language, false);

            return new ResponseEntity<>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Returns states of list of asset.")
    @GetMapping(value = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<List<DeviceStateDto>> getStateMachine(@RequestParam(value = Constants.ASSET_SERIALS_TOKEN, required = true) String serialParam,
                                                                @RequestParam(value = Constants.ASSET_MODEL_TOKEN, required = false) String modelParam,
                                                                @RequestParam(value = Constants.USE_MERGED_TOKEN, required = false) String mergedParam,
                                                                @RequestParam(value = Constants.DAILY_TOKEN, required = false) String dailyString,
                                                                @RequestParam(value = Constants.FROM_TOKEN, required = false) String fromParam,
                                                                @RequestParam(value = Constants.TO_TOKEN, required = false) String toParam,
                                                                @RequestParam(value = Constants.DATE_STRING_TOKEN, required = false, defaultValue = "true") String useDateStringParam,
                                                                @RequestParam(value = Constants.LANGUAGE_TOKEN, required = false, defaultValue = "en") String language
    ) {
        try {
            boolean daily = Utils.getBoolenFromString(dailyString);
            boolean useMerged = Utils.getBoolenFromString(mergedParam);
            boolean useDateString = Utils.getBoolenFromString(useDateStringParam);

            Long from = null;
            Long to = null;

            if (StringUtils.isNotEmpty(fromParam)) {
                from = Utils.getTimeFromString(fromParam, false);
            }

            if (StringUtils.isNotEmpty(toParam)) {
                to = Utils.getTimeFromString(toParam, false);
            }

            if (StringUtils.isEmpty(modelParam)) {
                modelParam = "J-Engine";
            }

            String[] serialStrings = serialParam.split(",");

            List<DeviceStateDto> meta;
            meta = stateMachineService.getStatesOfAsset(modelParam, serialStrings, daily, useMerged, null, from, to, useDateString, language);

            return new ResponseEntity<>(meta, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "Download states of list of asset.")
    @GetMapping(value = "download",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public void downloadStateMachine(@RequestParam(value = Constants.ASSET_SERIALS_TOKEN, required = true) String serialParam,
                                     @RequestParam(value = Constants.ASSET_MODEL_TOKEN, required = false) String modelParam,
                                     @RequestParam(value = Constants.USE_MERGED_TOKEN, required = false) String mergedParam,
                                     @RequestParam(value = Constants.FROM_TOKEN, required = false) String fromParam,
                                     @RequestParam(value = Constants.TO_TOKEN, required = false) String toParam,
                                     @RequestParam(value = Constants.DATE_STRING_TOKEN, required = false, defaultValue = "true") String useDateStringParam,
                                     @RequestParam(value = Constants.LANGUAGE_TOKEN, required = false, defaultValue = "en") String language,
                                     HttpServletResponse response
    ) {
        try {
            boolean useMerged = Utils.getBoolenFromString(mergedParam);
            boolean useDateString = Utils.getBoolenFromString(useDateStringParam);

            Long from = null;
            Long to = null;

            if (StringUtils.isNotEmpty(fromParam)) {
                from = Utils.getTimeFromString(fromParam, false);
            }

            if (StringUtils.isNotEmpty(toParam)) {
                to = Utils.getTimeFromString(toParam, false);
            }

            if (StringUtils.isEmpty(modelParam)) {
                modelParam = "J-Engine";
            }

            String[] serialStrings = serialParam.split(",");

            List<DeviceStateDto> meta;
            meta = stateMachineService.getStatesOfAsset(modelParam, serialStrings, false, useMerged, null, from, to, useDateString, language);

            response.setContentType("application/csv");
            response.setHeader("Content-Disposition", "attachment; filename=\"state-machine-v2.csv\"");

            try (CSVPrinter printer =
                         new CSVPrinter(
                                 response.getWriter(),
                                 CSVFormat.DEFAULT.withHeader(meta.get(0).getCsvHeaderStrings()))) {
                for (DeviceStateDto d : meta) {
                    printer.printRecord(d.getCsvRow());
                }
            }

        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
        }
    }
}