package io.myplant.service.processors;

import io.myplant.model.AvailableStates;
import io.myplant.model.DemandSelectorSwitchStates;
import io.myplant.model.EngineAction;
import io.myplant.model.ServiceSelectorSwitchStates;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class AvailableStateCalculation {
    private static final Logger logger = LoggerFactory.getLogger(AvailableStateCalculation.class);

    private static List<EngineAction> tripStates = Arrays.asList(EngineAction.FORCED_OUTAGE, EngineAction.TROUBLESHOOTING, EngineAction.NOT_READY);
    private static List<EngineAction> energyProdStates = Arrays.asList(EngineAction.RAMPUP_ISLAND_OPERATION, EngineAction.MAINS_PARALLEL_OPERATION, EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION);

    public static AvailableStates calculateKielAvailableState(long assetId, DemandSelectorSwitchStates aws, ServiceSelectorSwitchStates bws, AvailableStates AV_MAN_Activated_Status, EngineAction actionActual) {

        // overwrite only for Kiel valid
        if(AV_MAN_Activated_Status != AvailableStates.UNDEFINED)
            return AV_MAN_Activated_Status;

        if(actionActual == EngineAction.DATA_GAP || actionActual == EngineAction.UNDEFINED)
            return AvailableStates.NOT_AVAILABLE;

        if(aws == DemandSelectorSwitchStates.ON || aws == DemandSelectorSwitchStates.OFF)
            return AvailableStates.NOT_AVAILABLE;

        if(aws == DemandSelectorSwitchStates.REMOTE || aws == DemandSelectorSwitchStates.UNDEFINED){
            if(bws == ServiceSelectorSwitchStates.AUTO){
                // input from willi
                if(tripStates.contains(actionActual))
                    return AvailableStates.NOT_AVAILABLE;
                return AvailableStates.AVAILABLE;
            } else if (bws == ServiceSelectorSwitchStates.MAN || bws == ServiceSelectorSwitchStates.OFF || bws == ServiceSelectorSwitchStates.UNDEFINED){
                return AvailableStates.NOT_AVAILABLE;
            }
        }

        logger.error("assetId " + assetId + ": AWS: " + aws + " BWS: " + bws + "are an Error in KIEL RAM definition");
        return AvailableStates.UNDEFINED;
    }


     public static AvailableStates calculateAvailableState(long assetId, ServiceSelectorSwitchStates bws, EngineAction actionActual ) {

        if(actionActual == EngineAction.DATA_GAP || actionActual == EngineAction.UNDEFINED)
            return AvailableStates.NOT_AVAILABLE;

        if(bws == ServiceSelectorSwitchStates.AUTO){
            // input from willi
            if(tripStates.contains(actionActual))
                return AvailableStates.NOT_AVAILABLE;
            return AvailableStates.AVAILABLE;
        }
        else if (bws == ServiceSelectorSwitchStates.MAN ){
            if(energyProdStates.contains(actionActual))
                return AvailableStates.AVAILABLE;
            return AvailableStates.NOT_AVAILABLE;
        }
        else if (bws == ServiceSelectorSwitchStates.OFF || bws == ServiceSelectorSwitchStates.UNDEFINED){
            return AvailableStates.NOT_AVAILABLE;
        }

        //logger.error("assetId " + assetId + ": AWS: " + aws + " BWS: " + bws + "are an Error in KIEL RAM definition");
        return AvailableStates.UNDEFINED;
    }

    public static AvailableStates calculateAvailableState(
            long assetId,
            DemandSelectorSwitchStates aws,
            ServiceSelectorSwitchStates bws,
            AvailableStates avss,
            EngineAction actionActual,
            boolean remotelyControlled) {

        if (avss == AvailableStates.UNDEFINED) {
            if (remotelyControlled) {
                if (aws == DemandSelectorSwitchStates.ON || aws == DemandSelectorSwitchStates.OFF)
                    return AvailableStates.NOT_AVAILABLE;
                // continue when (aws == DemandSelectorSwitchStates.REMOTE || aws ==
                // DemandSelectorSwitchStates.UNDEFINED)
            }

            if (bws == ServiceSelectorSwitchStates.MAN
                    || bws == ServiceSelectorSwitchStates.OFF
                    || bws == ServiceSelectorSwitchStates.UNDEFINED)
                return AvailableStates.NOT_AVAILABLE;
            // continue when bws == ServiceSelectorSwitchStates.AUTO

            if (actionActual == EngineAction.FORCED_OUTAGE
                    || actionActual == EngineAction.TROUBLESHOOTING
                    || actionActual == EngineAction.NOT_READY) return AvailableStates.NOT_AVAILABLE;

            if (actionActual == EngineAction.DATA_GAP || actionActual == EngineAction.UNDEFINED)
                return AvailableStates.UNDEFINED;

            return AvailableStates.AVAILABLE;
        } else{
            switch (avss){
                case TROUBLESHOOTING: return AvailableStates.TROUBLESHOOTING; //_FORCED;
                case MAINTENANCE: return AvailableStates.MAINTENANCE; // _FORCED;
                case DEACTIVATED: return AvailableStates.DEACTIVATED; //_FORCED;
            }
            return avss;
        }

    }

}
