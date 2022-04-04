package io.myplant.service.StateKpiCalculation;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.model.DeviceStatusV2;
import io.myplant.model.MessageEventNonAssetId;
import io.myplant.rediscache.RedisAssetModelLookup;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.repository.OverwriteStateRepository;
import io.myplant.service.StateMachineCalculationServiceApi;
import io.myplant.service.datastore.StateBatchDataStore;
import io.myplant.service.processors.DailyProcessor;
import io.myplant.service.processors.OperatingHourProcessor;
import io.myplant.service.processors.StateInterprationProcessor;
import io.myplant.service.processors.StateOverwriteProcessor;
import io.myplant.utils.StopWatch;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StateMachineCalculator {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineCalculator.class);

    private final MessageReader messageReader;
    private final StateMachineCalculationServiceApi calculationServiceApi;
    private final StateBatchDataStore stateDataStoreService;
    private final KpiCalculator kpiCalculator;
    private final RedisAssetModelLookup modelLookup;
    private final AssetInformationRepository assetInformationRepository;
    private final StateInterprationProcessor stateInterprationProcessor;
    private final StateOverwriteProcessor stateOverwriteProcessor;
    private final OverwriteStateRepository overwriteStateRepository;
    private final OperatingHourProcessor operatingHourProcessor;
    private final DailyProcessor dailyProcessor;

    public void calculateStateMachineAndStoreResultFromAssetSerial(final List<String> serials, boolean dryRun) {
        for (String serial : serials) {
            long assetId = modelLookup.lookUpAssetSerialNumberToIdMapping("J-Engine", serial);
            if (assetId == RedisAssetModelLookup.UNMATCHABLE_ID) {
                logger.error(String.format("Asset ID not valid for model/serial '%s/%s': ", "J-Engine", serial));
                continue;
            }
            calculateStateMachineAndStoreResultFromAssetId(assetId, dryRun);
        }
    }

    public void calculateStateMachineAndStoreResultFromAssetId(final Long assetId, boolean dryRun) {
        StopWatch watch = new StopWatch();
        watch.start();

        try {
            // from cassandra we get it in descending order
            List<MessageEventNonAssetId> messagesForDevice = messageReader.getMessagesForDevice(assetId);
            logger.info("assetId {}: get {} messages tooks {}", assetId, messagesForDevice.size(), watch.pretty(watch.stop()));
            if (messagesForDevice.isEmpty()) {
                logger.info("assetId {}: no messages found, skip delete states", assetId);
                return;
            }

            watch.start();
            AssetInformation assetInformation = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);
            if (storedOpt.isPresent())
                assetInformation = storedOpt.get();

            messageReader.removeLastDayMessages(messagesForDevice);

            // call willis vb code
            List<DeviceStatusV2> deviceStatusV2s = calculationServiceApi.get(assetId, messagesForDevice);
            logger.info("assetId {}: calculate {} states tooks {}", assetId, deviceStatusV2s.size(), watch.pretty(watch.stop()));
            watch.start();

            // convert for database
            List<DeviceState> states = deviceStatusV2s.stream().map(s -> new DeviceState(assetId, s.getActionActual(), s.getActionFrom(), s.getActionTo(), s.getTriggerDate(), s.getTriggerMSGNo(), s.getTriggerText(), s.getTriggerResponsibility(), s.getTriggerCount()
                    , s.getDemandSelectorSwitch(), s.getServiceSelectorSwitch(), s.getAV_MAN_Activated_Status())).collect(Collectors.toList());

            states = dailyProcessor.processStates(states);
            states = stateInterprationProcessor.processStates(assetId, states, assetInformation);
            states = operatingHourProcessor.processStates(states);

            stateDataStoreService.deleteAndInsertDeviceStatusDiff(assetId, states);
            logger.info("assetId {}: save states to db tooks {}", assetId, watch.pretty(watch.stop()));
            watch.start();

            // for kpi calculation use overwrites and daily state
            List<OverwriteState> overwriteStates = overwriteStateRepository.findByAssetId(assetId);

            states = stateOverwriteProcessor.processStates(assetId, states, overwriteStates, assetInformation);
            logger.info("stateOverwriteProcessor tooks {}", watch.pretty(watch.stop()));
            watch.start();

            kpiCalculator.accumulatedDeviceStatus(assetId, states, dryRun);

            logger.info("assetId {}: calculate {} states tooks {}", assetId, deviceStatusV2s.size(), watch.pretty(watch.stop()));
            watch.start();

            //exportStatesToJson(deviceId, states);

        } catch (Exception e) {
            logger.error("Failed to create state machine for asset " + assetId, e);
        }
    }

    private void exportStatesToJson(final Long deviceId, List<DeviceState> states) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Files.write(Paths.get("c:\\temp\\vector_" + deviceId + ".json"), mapper.writeValueAsString(states).getBytes());
        } catch (Exception ex) {

        }
    }
}