package io.myplant.service;

import io.myplant.domain.DeviceState;
import io.myplant.model.KpiDto;
import io.myplant.model.StateType;
import io.myplant.service.StateKpiCalculation.KpiCalculator;
import io.myplant.service.StateKpiCalculation.ValuesInPeriod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KpiService {

    private final KpiCalculator kpiCalculator;
    private final StateMachineV2Service stateMachineV2Service;
    private final AssetService assetService;

    public HashMap<Long, KpiDto> getKpisRange(String model, String[] serialStrings, boolean useMerged, Long from, Long to, StateType stateType) {
        HashMap<Long, KpiDto> result = new HashMap<>();

        HashMap<Long, String> mapIdSerial = assetService.getIdSerialMap(model, serialStrings);
        List<Long> assetIds = new ArrayList<>(mapIdSerial.keySet());

        Map<Long, List<DeviceState>> statesOfAssetMap = stateMachineV2Service.getStatesOfAssetMap(assetIds, true, useMerged, null, from, to, false, null);
        for (Map.Entry<Long, List<DeviceState>> set : statesOfAssetMap.entrySet()) {

            ValuesInPeriod values = kpiCalculator.calculateTimesForTimeRange(set.getKey(), set.getValue());

            result.put(set.getKey(), new KpiDto(values));
        }

        return result;
    }
}