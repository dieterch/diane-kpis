package io.myplant.service;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.domain.Start;
import io.myplant.model.StartDto;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.repository.OverwriteStateRepository;
import io.myplant.service.datastore.StateBatchDataStore;
import io.myplant.service.processors.StartProcessor;
import io.myplant.service.processors.StartProcessorV2;
import io.myplant.service.processors.StateInterprationProcessor;
import io.myplant.service.processors.StateOverwriteProcessor;
import io.myplant.utils.StopWatch;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StartsService {
    private static final Logger logger = LoggerFactory.getLogger(StartsService.class);

    private final AssetService assetService;
    private final StateBatchDataStore stateDataStoreService;
    private final AssetInformationRepository assetInformationRepository;
    private final MessageTextCacheService messageTextCacheService;
    private final StartProcessorV2 startProcessor;
    private final OverwriteStateRepository overwriteStateRepository;
    private final StateInterprationProcessor stateInterprationProcessor;
    private final StateOverwriteProcessor stateOverwriteProcessor;


    public List<StartDto> getStartsOfAssets(String modelName, String[] serialStrings, boolean useMerged, Long from, Long to, boolean useDateString, String language) {
        List<StartDto> dtoStarts = new ArrayList<>();

        HashMap<Long, String> mapIdSerial = assetService.getIdSerialMap(modelName, serialStrings);
        List<Long> assetIds = new ArrayList<>(mapIdSerial.keySet());

        StopWatch watch = new StopWatch();
        watch.start();

        try {
            logger.info("assetId {}: get starts ", StringUtils.join(assetIds, ","));

            Map<Long, List<DeviceState>> statesMap = stateDataStoreService.queryDeviceStatesMap(assetIds, from, to);
            logger.info("assetId {}: get states from db tooks {}", StringUtils.join(assetIds, ","), watch.pretty(watch.stop()));
            watch.start();

            HashMap<Long, List<OverwriteState>> overwriteStatesMap = new HashMap<>();
            if (useMerged) {
                overwriteStatesMap = convertToMap(overwriteStateRepository.findByAssetIdIn(new HashSet<>(assetIds)));
            }

            for (Long assetId : assetIds) {
                if (!statesMap.containsKey(assetId)) {
                    continue;
                }

                AssetInformation assetInformation = null;
                Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);
                if (storedOpt.isPresent())
                    assetInformation = storedOpt.get();

                List<DeviceState> states = statesMap.get(assetId);
                List<OverwriteState> overwriteStates = overwriteStatesMap.get(assetId);

                states = stateInterprationProcessor.processStates(assetId, states, assetInformation);

                if (useMerged && overwriteStates != null && overwriteStates.size() != 0) {
                    states = stateOverwriteProcessor.processStates(assetId, states, overwriteStates, assetInformation);
                }

                List<Start> starts = startProcessor.run(assetId, states, assetInformation);
                List<Start> starts2 = startProcessor.run(assetId, states, assetInformation);

                for (int i = 0; i < starts.size(); i++) {
                    Start a = starts.get(i);
                    Start b = null;

                    if (starts2.size() > i) {
                        b = starts2.get(i);
                    }
                }

                List<StartDto> startDtos = convertToDtoList(modelName, mapIdSerial.get(assetId), starts, useDateString, assetInformation.getTimezone(), language);
                dtoStarts.addAll(startDtos);
            }

            logger.info("assetId {}: {} states found, calculating tooks {}", getAssetList(assetIds, mapIdSerial, dtoStarts), dtoStarts.size(), watch.pretty(watch.stop()));

        } catch (Exception ex) {
            logger.error("problem to fetch data", ex);
        }

        return dtoStarts;
    }

    private StartDto convertToDto(String model, String serial, Start start, boolean useDateString, String timezone, String language) {
        StartDto dto = new StartDto(model, serial, start, useDateString, timezone);
        dto.setTriggerText(messageTextCacheService.getMessage(model, serial, language, start.getTriggerMSGNo() + ""));
        return dto;
    }

    private List<StartDto> convertToDtoList(String modelName, String serial, List<Start> starts, boolean useDateStringParam, String timezone, String language) {
        return starts.stream()
                .map(s -> convertToDto(modelName,
                        serial,
                        s, useDateStringParam, timezone, language))
                .collect(Collectors.toList());
    }

    private String getAssetList(List<Long> assetIds, HashMap<Long, String> mapIdSerial, List<StartDto> dtoStarts) {
        return assetIds.stream()
                .map(n -> n.toString() + "(" + dtoStarts.stream().filter(s -> s.getSerial().equals(mapIdSerial.get(n))).count() + ")")
                .collect(Collectors.joining(","));
    }

    public HashMap<Long, List<OverwriteState>> convertToMap(List<OverwriteState> overwriteStates) {
        HashMap<Long, List<OverwriteState>> result = new HashMap<>();

        for (OverwriteState state : overwriteStates) {
            if (!result.containsKey(state.getAssetId())) {
                result.put(state.getAssetId(), new ArrayList<>());
            }

            result.get(state.getAssetId()).add(state);
        }
        return result;
    }
}