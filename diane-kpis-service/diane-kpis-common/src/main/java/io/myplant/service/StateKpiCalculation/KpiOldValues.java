package io.myplant.service.StateKpiCalculation;

import io.myplant.domain.DeviceState;
import io.myplant.model.EngineAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class KpiOldValues {

    private static final Logger logger = LoggerFactory.getLogger(StateMachineCalculator.class);


    private static List<EngineAction> nonTotalTimeStates = Arrays.asList(EngineAction.DATA_GAP, EngineAction.UNDEFINED);
    private static List<EngineAction> forcedOutageStates = Arrays.asList(EngineAction.FORCED_OUTAGE);
    private static List<EngineAction> unplannedMaintenanceStates = Arrays.asList(EngineAction.TROUBLESHOOTING);
    private static List<EngineAction> serviceTimeStates = Arrays.asList(EngineAction.START_PREPARATION, EngineAction.START, EngineAction.IDLE, EngineAction.SYNCHRONISATION
            , EngineAction.OPERATION, EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION, EngineAction.MAINS_PARALLEL_OPERATION, EngineAction.RAMPUP_ISLAND_OPERATION, EngineAction.ISLAND_OPERATION
            , EngineAction.LOAD_RAMPDOWN, EngineAction.ENGINE_COOLDOWN, EngineAction.READY, EngineAction.MAINS_FAILURE);
    private static List<EngineAction> tripStates = Arrays.asList(EngineAction.FORCED_OUTAGE);

    public ValuesInPeriod calculateOldValues(ValuesInPeriod valuesInPeriod, DeviceState state, long stateDuration) {
        if (!nonTotalTimeStates.contains(state.getActionActual()))
            valuesInPeriod.setTotalTime(valuesInPeriod.getTotalTime() + stateDuration);

        if (unplannedMaintenanceStates.contains(state.getActionActual()))
            valuesInPeriod.setUnplannedMaintenance(valuesInPeriod.getUnplannedMaintenance() + stateDuration);
        else {
            if (forcedOutageStates.contains(state.getActionActual()))
                valuesInPeriod.setForceOutage(valuesInPeriod.getForceOutage() + stateDuration);
            if (serviceTimeStates.contains(state.getActionActual()))
                valuesInPeriod.setServiceTime(valuesInPeriod.getServiceTime() + stateDuration);

            if (tripStates.contains(state.getActionActual()))
                valuesInPeriod.setTripCount(valuesInPeriod.getTripCount() + 1);
        }
//        if(!(status.getActionActual() == EngineAction.DATA_GAP || status.getActionActual() == EngineAction.UNDEFINED))
//            valuesInPeriod.setTotalTime(valuesInPeriod.getTotalTime() + stateDuration);
//        if(status.getAV_MAN_Activated_Status() == AvailableStates.FORCEDOUTAGE)
//            valuesInPeriod.setForceOutage(valuesInPeriod.getForceOutage() + stateDuration);
//        if(status.getAV_MAN_Activated_Status() == AvailableStates.TROUBLESHOOTING)
//            valuesInPeriod.setUnplannedMaintenance(valuesInPeriod.getUnplannedMaintenance() + stateDuration);
//        if(status.getAV_MAN_Activated_Status() == AvailableStates.MAINTENANCE)
//            valuesInPeriod.setServiceTime(valuesInPeriod.getServiceTime() + stateDuration);
//        if(status.getAV_MAN_Activated_Status() == AvailableStates.MAINTENANCE)
//            valuesInPeriod.setServiceTime(valuesInPeriod.getServiceTime() + stateDuration);

        return valuesInPeriod;
    }


}
