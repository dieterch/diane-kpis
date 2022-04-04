package io.myplant.service.processors;

import io.myplant.model.AvailableStates;
import io.myplant.model.DemandSelectorSwitchStates;
import io.myplant.model.EngineAction;
import io.myplant.model.ServiceSelectorSwitchStates;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvailableStateCalculationTest {


    @Test
    public void calculateAvailableStateNotAvailable1() {

        DemandSelectorSwitchStates aws = DemandSelectorSwitchStates.ON;
        ServiceSelectorSwitchStates bws = ServiceSelectorSwitchStates.OFF;
        AvailableStates AV_MAN_Activated_Status = AvailableStates.UNDEFINED;
        EngineAction actionActual = EngineAction.READY;

        AvailableStates result = AvailableStateCalculation.calculateKielAvailableState(1, aws, bws, AV_MAN_Activated_Status, actionActual);
        assertEquals(result, AvailableStates.NOT_AVAILABLE);
    }

    @Test
    public void calculateAvailableStateNotAvailable2() {

        DemandSelectorSwitchStates aws = DemandSelectorSwitchStates.REMOTE;
        ServiceSelectorSwitchStates bws = ServiceSelectorSwitchStates.MAN;
        AvailableStates AV_MAN_Activated_Status = AvailableStates.UNDEFINED;
        EngineAction actionActual = EngineAction.READY;

        AvailableStates result = AvailableStateCalculation.calculateKielAvailableState(1, aws, bws, AV_MAN_Activated_Status, actionActual);
        assertEquals(result, AvailableStates.NOT_AVAILABLE);
    }

    @Test
    public void calculateAvailableStateAvailable() {

        DemandSelectorSwitchStates aws = DemandSelectorSwitchStates.REMOTE;
        ServiceSelectorSwitchStates bws = ServiceSelectorSwitchStates.AUTO;
        AvailableStates AV_MAN_Activated_Status = AvailableStates.UNDEFINED;
        EngineAction actionActual = EngineAction.READY;

        AvailableStates result = AvailableStateCalculation.calculateKielAvailableState(1, aws, bws, AV_MAN_Activated_Status, actionActual);
        assertEquals(result, AvailableStates.AVAILABLE);
    }

    @Test
    public void calculateAvailableStateAVOverwrite() {

        DemandSelectorSwitchStates aws = DemandSelectorSwitchStates.REMOTE;
        ServiceSelectorSwitchStates bws = ServiceSelectorSwitchStates.AUTO;
        AvailableStates AV_MAN_Activated_Status = AvailableStates.DEACTIVATED;
        EngineAction actionActual = EngineAction.READY;

        AvailableStates result = AvailableStateCalculation.calculateKielAvailableState(1, aws, bws, AV_MAN_Activated_Status, actionActual);
        assertEquals(result, AvailableStates.DEACTIVATED);
    }

}