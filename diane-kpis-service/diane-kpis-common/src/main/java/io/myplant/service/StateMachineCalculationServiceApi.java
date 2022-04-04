package io.myplant.service;

import io.myplant.model.DeviceStatusV2;
import io.myplant.model.MessageEventNonAssetId;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "StateKpiCalculation-api", url = "${calculation.svc.url}")
@RequestMapping(value = "/api/state", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
public interface StateMachineCalculationServiceApi {

    @PostMapping("/{assetId}/")
    List<DeviceStatusV2> get(@PathVariable("assetId") long assetId,
                             @RequestBody List<MessageEventNonAssetId> events);

    @PostMapping("/{assetId}")
    String getString(@PathVariable("assetId") long assetId,
                            @RequestBody List<MessageEventNonAssetId> events);
}
