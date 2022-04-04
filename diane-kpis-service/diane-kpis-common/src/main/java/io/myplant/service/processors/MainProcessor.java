package io.myplant.service.processors;

import io.myplant.utils.StopWatch;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.model.StateType;
import io.myplant.repository.AssetInformationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MainProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MainProcessor.class);

    private final AssetInformationRepository assetInformationRepository;
    private final StateInterprationProcessor stateInterprationProcessor;
    private final OutagesProcessor outagesProcessor;
    private final StateOverwriteProcessor stateOverwriteProcessor;
    private final DailyProcessor dailyProcessor;


    private List<DeviceState> processStates(String model, String serial, long assetId, List<DeviceState> states, List<OverwriteState> overwriteStates, boolean daily, boolean useMerged, StateType outageType){
        AssetInformation assetInformation = null;
        Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);
        if(storedOpt.isPresent())
            assetInformation = storedOpt.get();

        StopWatch watch = new StopWatch();
        watch.start();

        states = stateInterprationProcessor.processStates(assetId, states, assetInformation);
        logger.info("stateInterprationProcessor tooks {}", watch.pretty(watch.stop())); watch.start();

        if(useMerged || outageType != null)
            states = stateOverwriteProcessor.processStates(assetId, states, overwriteStates, assetInformation);
        logger.info("stateOverwriteProcessor tooks {}", watch.pretty(watch.stop())); watch.start();

        if(outageType != null){
            states = outagesProcessor.processStates(states, outageType);
        }
        logger.info("outagesProcessor tooks {}", watch.pretty(watch.stop())); watch.start();

        if(daily)
            states = dailyProcessor.processStates(states);
        logger.info("dailyProcessor tooks {}", watch.pretty(watch.stop())); watch.start();

        return states;
    }
}
