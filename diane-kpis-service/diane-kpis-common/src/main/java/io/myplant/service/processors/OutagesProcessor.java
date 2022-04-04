package io.myplant.service.processors;

import io.myplant.domain.DeviceState;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import io.myplant.model.StateType;
import io.myplant.service.ScopeMapperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OutagesProcessor {
    private static final Logger logger = LoggerFactory.getLogger(OutagesProcessor.class);

    private final ScopeMapperService scopeService = new ScopeMapperService();

    private static List<EngineAction> avCalculationStates = Arrays.asList(EngineAction.FORCED_OUTAGE, EngineAction.TROUBLESHOOTING, EngineAction.READY);

    private static long MIN30_TIMESPAN = 30*60*1000L;
    private static long MIN60_TIMESPAN = 60*60*1000L;


    public List<DeviceState> processStates(List<DeviceState> states, StateType stateType) {
        List<DeviceState> result = new ArrayList<>();

        DeviceState newOutage = null;
        for (DeviceState state : states) {
            if(!IeeeStates.isOutageForExport(state.outageOfType(stateType))){
                if(newOutage != null) {
                    result.add(newOutage);
                    newOutage = null;
                }
                continue;
            }

            if(newOutage == null){
                newOutage = state;
                continue;
            }

            if(isSameOutage(state, newOutage, stateType)){
                newOutage.setActionTo(state.getActionTo());
                continue;
            }

            result.add(newOutage);
            newOutage = state;
        }

        if(newOutage != null)
            result.add(newOutage);

        return result;
    }

    public static boolean isSameOutage(DeviceState state, DeviceState lastState, StateType stateType) {
        return lastState.outageOfType(stateType) == state.outageOfType(stateType);
    }
}