package io.myplant.controller;

import io.myplant.domain.AssetInformation;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.service.AssetInformationService;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;


@Controller
@RequestMapping("/asset-info/")
public class AssetInformationController {
    private static final Logger logger = LoggerFactory.getLogger(AssetInformationController.class);

    private final AssetInformationRepository repository;
    private final AssetInformationService assetInformationService;

    @Autowired
    public AssetInformationController(AssetInformationRepository repository, AssetInformationService assetInformationService) {
        this.repository = repository;
        this.assetInformationService = assetInformationService;
    }

    @ApiOperation(value = "Return a ram start date for asset id.")
    @GetMapping(value = "{assetId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<AssetInformation> getStateId(@PathVariable long assetId) {
        try {
            Optional<AssetInformation> meta = repository.findById(assetId);
            AssetInformation assetInfo;
            if(meta.isPresent()) {
                assetInfo = meta.get();
            }
            else{
                assetInfo = new AssetInformation();
                assetInfo.setId(assetId);
            }

            return new ResponseEntity<>(assetInfo, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get commision", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "change ram start date for asset id.")
    @PutMapping(value = "{assetId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<AssetInformation> putStateId(@PathVariable long assetId, @RequestBody AssetInformation request) {
        try {
            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = repository.findById(assetId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if (stored == null) stored = request;
            else {
                stored.setRamStartDate(request.getRamStartDate());
                if(request.getAvCalcType() != null)
                    stored.setAvCalcType(request.getAvCalcType());
                assetInformationService.clearCache();
            }
            repository.save(stored);

            return new ResponseEntity<AssetInformation>(stored, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @ApiOperation(value = "create ram start date for asset id.")
    @PostMapping(value = "",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity<AssetInformation> postState(@RequestBody AssetInformation request) {
        try {
            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = repository.findById(request.getId());
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored != null)
                return new ResponseEntity(HttpStatus.FORBIDDEN);

            stored = new AssetInformation();
            stored.setRamStartDate(request.getRamStartDate());
            if(request.getAvCalcType() != null)
                stored.setAvCalcType(request.getAvCalcType());
            repository.save(stored);

            assetInformationService.clearCache();
            return new ResponseEntity<AssetInformation>(request, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @ApiOperation(value = "delete ram start date for asset id..")
    @DeleteMapping(value = "{assetId}",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseBody
    public ResponseEntity deleteStateId(@PathVariable long assetId) {
        try {
            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = repository.findById(assetId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored != null){
                stored.setRamStartDate(null);
                repository.save(stored);
                assetInformationService.clearCache();
            }

            return new ResponseEntity(HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Failed to get state machine", e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
