package io.myplant.service.processors;

import io.myplant.model.AvailableStates;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;


public class IeeeStateCalculator {
    private static final Logger logger = LoggerFactory.getLogger(IeeeStateCalculator.class);

    private static List<EngineAction> operationEngineActions = Arrays.asList(EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION, EngineAction.OPERATION,
            EngineAction.MAINS_PARALLEL_OPERATION, EngineAction.RAMPUP_ISLAND_OPERATION, EngineAction.ISLAND_OPERATION,
            EngineAction.LOAD_RAMPDOWN);

    public static IeeeStates calcIeeeState(AvailableStates state, EngineAction actionActual, AvailableStates lastAvailableState, IeeeStates lastOutageIeeeState, EngineAction lastActionActual){

        if(state == AvailableStates.AVAILABLE){
            return IeeeStates.AVAILABLE;
        }

        if (state.isDeactivated()) {
            return IeeeStates.DEACTIVATED_SHUTDOWN;
        }

        if(actionActual == EngineAction.DATA_GAP || actionActual == EngineAction.UNDEFINED){
            return IeeeStates.DEACTIVATED_SHUTDOWN;
        }

        if (state.isMaintenance() || state.isTroubleshooting() || state == AvailableStates.NOT_AVAILABLE) {
            // outage

            if(state.isMaintenance())
                return IeeeStates.UNPLANNED_MAINTENANCE;

            if(!state.isTroubleshooting()){
                // check if it is not the first row
                if(lastOutageIeeeState != null)
                    return lastOutageIeeeState;

                if(!(actionActual == EngineAction.FORCED_OUTAGE || actionActual == EngineAction.NOT_READY))
                    return IeeeStates.UNPLANNED_MAINTENANCE;
            }

            if(operationEngineActions.contains(lastActionActual))
                return IeeeStates.FORCEDOUTAGE_MTBFO_REL;
            else
                return IeeeStates.FORCEDOUTAGE_REL;

 /*

            // check if it is not the first row
            if(lastIeeeState != null && (lastIeeeState == IeeeStates.UNPLANNED_MAINTENANCE || lastIeeeState == IeeeStates.FORCEDOUTAGE_MTBFO_REL || lastIeeeState == IeeeStates.FORCEDOUTAGE_REL))
                return lastIeeeState;

            if(lastAvailableState == AvailableStates.NOT_AVAILABLE || lastAvailableState.isMaintenance()
                    || lastAvailableState.isDeactivated() || lastAvailableState == AvailableStates.UNDEFINED){
                return IeeeStates.UNPLANNED_MAINTENANCE;
            }

            if(lastAvailableState == AvailableStates.AVAILABLE || lastAvailableState.isTroubleshooting()){
                if(operationEngineActions.contains(lastActionActual))
                    return IeeeStates.FORCEDOUTAGE_MTBFO_REL;
                else
                    return IeeeStates.FORCEDOUTAGE_REL;
            }
*/
        }

        logger.error("AVState " + state + ": actionActual: " + actionActual + " lastAvailableState: " + lastAvailableState + " lastOutageIeeeState: " + lastOutageIeeeState + "are an Error in KIEL RAM definition");

        return IeeeStates.UNDEFINED;
    }
}
