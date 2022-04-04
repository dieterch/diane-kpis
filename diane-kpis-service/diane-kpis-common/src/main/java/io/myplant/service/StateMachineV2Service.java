package io.myplant.service;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.model.DeviceStateDto;
import io.myplant.model.OutageDto;
import io.myplant.model.StateType;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.repository.OverwriteStateRepository;
import io.myplant.service.StateKpiCalculation.StateMachineCalculator;
import io.myplant.service.datastore.StateBatchDataStore;
import io.myplant.service.processors.DailyProcessor;
import io.myplant.service.processors.OutagesProcessor;
import io.myplant.service.processors.StateInterprationProcessor;
import io.myplant.service.processors.StateOverwriteProcessor;
import io.myplant.utils.StopWatch;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateMachineV2Service {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineV2Service.class);

    private final StateBatchDataStore stateDataStoreService;
    private final StateMachineCalculator stateMachineCalculator;
    private final AssetService assetService;
    private final OverwriteStateRepository overwriteStateRepository;
    private final AssetInformationRepository assetInformationRepository;
    private final MessageTextCacheService messageTextCacheService;
    private final AssetInformationService assetInformationService;
    private final OutagesProcessor outagesProcessor;
    private final StateOverwriteProcessor stateOverwriteProcessor;
    private final StateInterprationProcessor stateInterprationProcessor;
    private final DailyProcessor dailyProcessor;

    public List<DeviceStateDto> getStatesOfAsset(long assetId, boolean calculateNew, boolean daily, boolean useMerged, StateType outageType, Long from, Long to, boolean useDateString, String language, boolean dryRun) {

        List<DeviceStateDto> dtoStates = null;

        try {
            logger.info("get states - assetId {}: called with flag calcnew '{}' and use date string {}", assetId, calculateNew, useDateString);

            if (calculateNew) {
                stateMachineCalculator.calculateStateMachineAndStoreResultFromAssetId(assetId, dryRun);
            }

            List<DeviceState> states = stateDataStoreService.queryDeviceStates(Arrays.asList(assetId), from, to);

            List<OverwriteState> overwriteStates = new ArrayList<>();
            if (useMerged || outageType != null) {
                overwriteStates = overwriteStateRepository.findByAssetId(assetId);
            }

            dtoStates = processStatesDto(null, null, assetId, states, overwriteStates, daily, useMerged, outageType, useDateString, language);

            logger.info("get states - assetId {}: {} states found", assetId, dtoStates == null ? 0 : dtoStates.size());
        } catch (Exception ex) {
            logger.error("get states - problem to fetch data", ex);
        }
        return dtoStates;
    }

    public List<DeviceStateDto> getStatesOfAsset(String model, String[] serialStrings, boolean daily, boolean useMerged, StateType outageType, Long from, Long to, boolean useDateString, String language) {
        List<DeviceStateDto> result = new ArrayList<>();

        HashMap<Long, String> mapIdSerial = assetService.getIdSerialMap(model, serialStrings);
        List<Long> assetIds = new ArrayList<>(mapIdSerial.keySet());

        Map<Long, List<DeviceState>> statesOfAssetMap = getStatesOfAssetMap(assetIds, daily, useMerged, outageType, from, to, useDateString, language);

        for (Map.Entry<Long, List<DeviceState>> set : statesOfAssetMap.entrySet()) {
            List<DeviceStateDto> dtoStates = convertToDtoList(model, mapIdSerial.get(set.getKey()), set.getValue(), useDateString, language);

            result.addAll(dtoStates);
        }

        return result;
    }

    public Map<Long, List<DeviceState>> getStatesOfAssetMap(List<Long> assetIds, boolean daily, boolean useMerged, StateType outageType, Long from, Long to, boolean useDateString, String language) {

        HashMap<Long, List<DeviceState>> dtoStates = new HashMap<>();

        StopWatch watch = new StopWatch();
        watch.start();

        try {
            logger.info("get states - assetIds {}: get merged states called with flags useMerged '{}' outage type '{}'", org.apache.commons.lang3.StringUtils.join(assetIds, ","), useMerged, outageType);

            Map<Long, List<DeviceState>> statesMap = stateDataStoreService.queryDeviceStatesMap(assetIds, from, to);

            if (from != null)
                logger.info("get states - assetIds {}: get states from {} to {} from db tooks {}", org.apache.commons.lang3.StringUtils.join(assetIds, ","), from, to, watch.pretty(watch.stop()));
            else
                logger.info("get states - assetIds {}: get states from db tooks {}", org.apache.commons.lang3.StringUtils.join(assetIds, ","), watch.pretty(watch.stop()));

            watch.start();

            HashMap<Long, List<OverwriteState>> overwriteStatesMap = new HashMap<>();
            if (useMerged || outageType != null) {
                logger.debug("load overwriteStatesMap {}", assetIds);
                overwriteStatesMap = convertToMap(overwriteStateRepository.findByAssetIdIn(new HashSet<>(assetIds)));
            }

            logger.info("get states - assetIds {}: get overwrite states from db tooks {}", org.apache.commons.lang3.StringUtils.join(assetIds, ","), watch.pretty(watch.stop()));
            watch.start();

            for (Long assetId : assetIds) {
                if (!statesMap.containsKey(assetId)) {
                    dtoStates.put(assetId, new ArrayList<>());
                    continue;
                }

                if (!overwriteStatesMap.containsKey(assetId))
                    overwriteStatesMap.put(assetId, new ArrayList<>());

                List<DeviceState> states = statesMap.get(assetId);
                cutStates(assetId, states, from, to);

                List<DeviceState> statesForOneAsset = processStates(assetId, states, overwriteStatesMap.get(assetId), daily, useMerged, outageType, useDateString, language);
                dtoStates.put(assetId, statesForOneAsset);
            }

            logger.info("get states - assetIds {}: {} states found, calculating tooks {}", getAssetList(assetIds, dtoStates), dtoStates.size(), watch.pretty(watch.stop()));

        } catch (Exception ex) {
            logger.error("get states - problem to fetch data", ex);
        }

        return dtoStates;
    }

    public List<OutageDto> getOutagesOfAsset(String model, String[] serialStrings, StateType outageType, boolean useMerged, Long from, Long to, boolean useDateString, String language) {

        List<OutageDto> dtoOutages = new ArrayList<>();

        HashMap<Long, String> mapIdSerial = assetService.getIdSerialMap(model, serialStrings);
        List<Long> assetIds = new ArrayList<>(mapIdSerial.keySet());

        StopWatch watch = new StopWatch();
        watch.start();

        try {
            logger.info("get outages - assetIds {}: get merged states called, outage type '{}'", org.apache.commons.lang3.StringUtils.join(assetIds, ","), outageType);

            Map<Long, List<DeviceState>> statesMap = stateDataStoreService.queryDeviceStatesMap(assetIds, from, to);
            if (from != null) {
                logger.info("get outages - assetIds {}: get outages from {} to {} from db tooks {}", org.apache.commons.lang3.StringUtils.join(assetIds, ","), from, to, watch.pretty(watch.stop()));
            } else {
                logger.info("get outages - assetIds {}: get outages from db tooks {}", org.apache.commons.lang3.StringUtils.join(assetIds, ","), watch.pretty(watch.stop()));
            }

            watch.start();

            HashMap<Long, List<OverwriteState>> overwriteStatesMap;
            overwriteStatesMap = convertToMap(overwriteStateRepository.findByAssetIdIn(mapIdSerial.keySet()));

            logger.info("get outages - assetIds {}: get overwrite states from db tooks {}", org.apache.commons.lang3.StringUtils.join(assetIds, ","), watch.pretty(watch.stop()));
            watch.start();

            for (Long assetId : assetIds) {
                if (!statesMap.containsKey(assetId)) {
                    continue;
                }

                AssetInformation assetInformation = getAssetInformation(assetId);

                if (!overwriteStatesMap.containsKey(assetId)) {
                    overwriteStatesMap.put(assetId, new ArrayList<>());
                }

                List<DeviceState> states = statesMap.get(assetId);
                cutStates(assetId, states, from, to);

                List<DeviceState> outages = processStates(assetId, states, overwriteStatesMap.get(assetId), false, useMerged, outageType, useDateString, language);
                dtoOutages.addAll(convertToOutageDtoList(model, mapIdSerial.get(assetId), outages, assetInformation, outageType, useDateString, language));
            }

            logger.info("get outages - assetIds {}: {} outages found, calculating tooks {}", getAssetListOut(assetIds, mapIdSerial, dtoOutages), dtoOutages.size(), watch.pretty(watch.stop()));

        } catch (Exception ex) {
            logger.error("get outages - problem to fetch data", ex);
        }

        return dtoOutages;
    }

    private List<DeviceStateDto> processStatesDto(String model, String serial, long assetId, List<DeviceState> states, List<OverwriteState> overwriteStates, boolean daily, boolean useMerged, StateType outageType, boolean useDateString, String language) {
        states = processStates(assetId, states, overwriteStates, daily, useMerged, outageType, useDateString, language);
        return convertToDtoList(model, serial, states, useDateString, language);
    }

    private List<DeviceState> processStates(long assetId, List<DeviceState> states, List<OverwriteState> overwriteStates, boolean daily, boolean useMerged, StateType outageType, boolean useDateString, String language) {
        AssetInformation assetInformation = getAssetInformation(assetId);
        return processStates(assetInformation, states, overwriteStates, daily, useMerged, outageType, useDateString, language);
    }

    private AssetInformation getAssetInformation (long assetId){
        AssetInformation assetInformation = null;
        Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);

        if (storedOpt.isPresent()) {
            assetInformation = storedOpt.get();
        }
        else{
            assetInformation = new AssetInformation();
            assetInformation.setId(assetId);
        }
        return assetInformation;
    }

    protected List<DeviceState> processStates(AssetInformation assetInformation, List<DeviceState> states, List<OverwriteState> overwriteStates, boolean daily, boolean useMerged, StateType outageType, boolean useDateString, String language) {
        StopWatch watch = new StopWatch();
        watch.start();

        states = stateInterprationProcessor.processStates(assetInformation.getId(), states, assetInformation);
        logger.info("assetId {}: stateInterprationProcessor tooks {}", assetInformation.getId(), watch.pretty(watch.stop()));
        watch.start();

        if (useMerged) {
            states = stateOverwriteProcessor.processStates(assetInformation.getId(), states, overwriteStates, assetInformation);
            logger.info("stateOverwriteProcessor tooks {}", watch.pretty(watch.stop()));
            watch.start();
        }

        if (outageType != null) {
            states = outagesProcessor.processStates(states, outageType);
            logger.info("outagesProcessor tooks {}", watch.pretty(watch.stop()));
            watch.start();
        }

        if (daily) {
            states = dailyProcessor.processStates(states);
            logger.info("dailyProcessor tooks {}", watch.pretty(watch.stop()));
        }

        return states;
    }


    private String getAssetList(List<Long> assetIds, HashMap<Long, List<DeviceState>> dtoStates) {
        return assetIds.stream()
                .map(n -> n.toString() + "(" + dtoStates.get(n).size() + ")")
                .collect(Collectors.joining(","));
    }

    private String getAssetListOut(List<Long> assetIds, HashMap<Long, String> mapIdSerial, List<OutageDto> dtoStates) {
        return assetIds.stream()
                .map(n -> n.toString() + "(" + dtoStates.stream().filter(s -> s.getSerial().equals(mapIdSerial.get(n))).count() + ")")
                .collect(Collectors.joining(","));
    }


    private void cutStates(Long assetId, List<DeviceState> states, Long from, Long to) {
        if (from != null && !states.isEmpty() && states.get(0).getActionFrom() < from) {
            logger.info("assetId {}: cut date of first state to {}", assetId, from);
            states.get(0).setActionFrom(from);
        }

        if (to != null && !states.isEmpty() && states.get(states.size() - 1).getActionTo() > to) {
            logger.info("assetId {}: cut date of last state to {}", assetId, from);
            states.get(states.size() - 1).setActionTo(to);
        }
    }

    private List<DeviceStateDto> convertToDtoList(String model, String serial, List<DeviceState> states, boolean useDateString, String language) {
        List<DeviceStateDto> result = new ArrayList<>();
        long lastOutagenumber = 0;

        for (DeviceState state : states) {
            DeviceStateDto deviceStateDto = convertToMergedDto(model, serial, state, useDateString, language);
            result.add(deviceStateDto);

            if (state.getOutageNumber() != 0 && state.getOutageNumber() != lastOutagenumber) {
                deviceStateDto.setFirstRow(1);
                lastOutagenumber = state.getOutageNumber();
            }
        }

        return result;
    }

    private DeviceStateDto convertToMergedDto(String model, String serial, DeviceState state, boolean useDateString, String language) {
        String timezone = assetInformationService.getCachedTimezone(state.getAssetId());
        DeviceStateDto dto = new DeviceStateDto(state, model, serial, timezone, useDateString, language);
        dto.setTriggerText(messageTextCacheService.getMessage(model, serial, language, state.getTriggerMsgNo() + ""));

        return dto;

    }

    private List<OutageDto> convertToOutageDtoList(String model, String serial, List<DeviceState> states, AssetInformation assetInformation, StateType outageType, boolean useDateString, String language) {
        return states.stream().map(s -> convertToOutageDto(model, serial, s, assetInformation, outageType, useDateString, language)).collect(Collectors.toList());
    }

    private OutageDto convertToOutageDto(String model, String serial, DeviceState state, AssetInformation assetInformation, StateType outageType, boolean useDateString, String language) {
        OutageDto dto = new OutageDto(state, model, serial, null, assetInformation.getTimezone(), useDateString);
        dto.setCausalAlarmText(messageTextCacheService.getMessage(model, serial, language, state.getTriggerMsgNo() + ""));

        switch (outageType) {
            case VU:
                dto.setState(state.getKielVuState());
                break;
            case VZ:
                dto.setState(state.getKielVzState());
                break;
            case IEEE:
                dto.setState(state.getIeeeState());
                break;
        }

        return dto;
    }

    private HashMap<Long, List<OverwriteState>> convertToMap(List<OverwriteState> overwriteStates) {
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