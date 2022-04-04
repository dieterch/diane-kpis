package io.myplant.service.processors;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import io.myplant.service.ScopeMapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StateOverwriteProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StateOverwriteProcessor.class);
    private static long MIN60_TIMESPAN = 60*60*1000L;

    private final ScopeMapperService scopeService = new ScopeMapperService();

    private static List<EngineAction> avCalculationStates = Arrays.asList(EngineAction.FORCED_OUTAGE, EngineAction.TROUBLESHOOTING, EngineAction.READY);

    public List<DeviceState> processStates(
            long assetId,
            List<DeviceState> deviceStates,
            List<OverwriteState> overwriteStates,
            AssetInformation assetInformation) {

        if(overwriteStates == null || overwriteStates.size() == 0)
            return deviceStates;

        Collections.sort(overwriteStates);
        //List<DeviceState> overwritesToAdd = new ArrayList<>();
        long outageNumber = 1000000;

        for (OverwriteState overwriteState : overwriteStates) {
            boolean stateInserted = false;
            List<DeviceState> overwrittenStates =
                    deviceStates.stream()
                            .filter(
                                    s -> (overwriteState.getActionFrom() >= s.getActionFrom() && overwriteState.getActionFrom() < s.getActionTo())
                                            || (overwriteState.getActionFrom() <= s.getActionFrom() && overwriteState.getActionTo() >= s.getActionTo())
                                            || (overwriteState.getActionTo() > s.getActionFrom() && overwriteState.getActionTo() <= s.getActionTo()
                                    )
                            )
                            .collect(Collectors.toList());

            for (int i = 0; i < overwrittenStates.size(); i++) {
                DeviceState state2Overwrite = overwrittenStates.get(i);
                if(state2Overwrite.getActionFrom() >= overwriteState.getActionFrom() && state2Overwrite.getActionTo() <= overwriteState.getActionTo())
                    overwriteValues(state2Overwrite, overwriteState, outageNumber);
                else if (i == 0 && state2Overwrite.getActionFrom() != overwriteState.getActionFrom()) {
                    // split action
                    DeviceState insertedState = state2Overwrite.toBuilder().build();
                    state2Overwrite.setActionTo(overwriteState.getActionFrom());
                    insertedState.setActionFrom(overwriteState.getActionFrom());
                    overwriteValues(insertedState, overwriteState,outageNumber);
                    deviceStates.add(insertedState);
                    stateInserted = true;
                } else if (i == (overwrittenStates.size() - 1) && state2Overwrite.getActionTo() != overwriteState.getActionTo()) {
                    // split action
                    DeviceState insertedState = state2Overwrite.toBuilder().build();
                    state2Overwrite.setActionFrom(overwriteState.getActionTo());
                    insertedState.setActionTo(overwriteState.getActionTo());
                    overwriteValues(insertedState, overwriteState, outageNumber);
                    deviceStates.add(insertedState);
                    stateInserted = true;
                } else {
                    overwriteValues(state2Overwrite, overwriteState,outageNumber);
                }
            }

            if (stateInserted) {
                Collections.sort(deviceStates);
            }
            outageNumber++;
        }

        // do Vz joining
        long startOfVzState = 0;
        IeeeStates lastVzState = IeeeStates.UNDEFINED;
        List<DeviceState> statesToInsert = new ArrayList<>();

        for (DeviceState state : deviceStates) {
            // Vz check
            if (IeeeStates.isOutage(state.getKielVzState())) {
                if (startOfVzState == 0) {
                    startOfVzState = state.getActionFrom();
                    lastVzState = state.getKielVzState();
                }
            }

            if(startOfVzState != 0 && state.getActionFrom() < (MIN60_TIMESPAN+startOfVzState)){
                if(state.getActionTo() <= (MIN60_TIMESPAN+startOfVzState))
                    state.setKielVzState(lastVzState);
                else{
                    // create an state to insert beacause we must split the Vz state
                    long splitTimeStamp = MIN60_TIMESPAN+startOfVzState;
                    DeviceState state2Insert = state.toBuilder().actionTo(splitTimeStamp).kielVzState(lastVzState).build();
                    statesToInsert.add(state2Insert);
                    state.setActionFrom(splitTimeStamp);
                    state.setKielVzState(state.getIeeeState());
                }
            }else{
                startOfVzState = 0;
            }

        }

        if(statesToInsert.size() != 0) {
            deviceStates.addAll(statesToInsert);
            Collections.sort(deviceStates);
        }

        return deviceStates;
    }

    private void overwriteValues(DeviceState state2Overwrite, OverwriteState overwriteState, long outageNr){
        state2Overwrite.setOverwriteId(overwriteState.getId());
        state2Overwrite.setScope(overwriteState.getScope());
        state2Overwrite.setIeeeState(overwriteState.getIeeeState());
        state2Overwrite.setKielVuState(overwriteState.getIeeeState());
        state2Overwrite.setKielVzState(overwriteState.getIeeeState());
        state2Overwrite.setDescription(overwriteState.getDescription());
        state2Overwrite.setOutageNumber(outageNr);
    }


}
