package io.myplant.service.processors;

import io.myplant.model.AvailableStates;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IeeeCalculationTest {


    @Test
    public void calcIeeeState_testAvailable() {

        AvailableStates currentAV = AvailableStates.AVAILABLE;
        EngineAction currentEA = EngineAction.MAINS_PARALLEL_OPERATION;
        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.MAINS_PARALLEL_OPERATION;
        IeeeStates beforeIeeeState = IeeeStates.DEACTIVATED_SHUTDOWN;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);

        assertEquals(IeeeStates.AVAILABLE, ieeeState);
    }

    @Test
    public void calcIeeeState_testUnplannedMaintenace() {

        AvailableStates currentAV = AvailableStates.MAINTENANCE;
        EngineAction currentEA = EngineAction.MAINS_PARALLEL_OPERATION;
        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.MAINS_PARALLEL_OPERATION;
        IeeeStates beforeIeeeState = IeeeStates.DEACTIVATED_SHUTDOWN;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);

        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, ieeeState);
    }

    @Test
    public void calcIeeeState_testDeactivatedShutdown() {

        AvailableStates currentAV = AvailableStates.DEACTIVATED;
        EngineAction currentEA = EngineAction.DATA_GAP;
        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.MAINS_PARALLEL_OPERATION;
        IeeeStates beforeIeeeState = IeeeStates.AVAILABLE;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);
        assertEquals(IeeeStates.DEACTIVATED_SHUTDOWN, ieeeState);
    }

    @Test
    public void calcIeeeState_testUnplaned_1() {

        AvailableStates currentAV = AvailableStates.NOT_AVAILABLE;
        EngineAction currentEA = EngineAction.IDLE;
        AvailableStates beforeAV = AvailableStates.NOT_AVAILABLE;
        EngineAction beforeEA = EngineAction.MAINS_PARALLEL_OPERATION;
        IeeeStates beforeIeeeState = null;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, ieeeState);
    }

    @Test
    public void calcIeeeState_testUnplaned_2() {

        AvailableStates currentAV = AvailableStates.NOT_AVAILABLE;
        EngineAction currentEA = EngineAction.FORCED_OUTAGE;
        AvailableStates beforeAV = AvailableStates.MAINTENANCE;
        EngineAction beforeEA = EngineAction.MAINS_PARALLEL_OPERATION;
        IeeeStates beforeIeeeState = IeeeStates.UNPLANNED_MAINTENANCE;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, ieeeState);
    }


    @Test
    public void calcIeeeState_test_force_out_MTBFO_REL() {

        AvailableStates currentAV = AvailableStates.TROUBLESHOOTING;
        EngineAction currentEA = EngineAction.LOAD_RAMPDOWN;
        AvailableStates beforeAV = AvailableStates.TROUBLESHOOTING;
        EngineAction beforeEA = EngineAction.MAINS_PARALLEL_OPERATION;
        IeeeStates beforeIeeeState = IeeeStates.AVAILABLE;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);

        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, ieeeState);
    }

    @Test
    public void calcIeeeState_test_force_out_MTBFO_REL2() {

        AvailableStates currentAV = AvailableStates.NOT_AVAILABLE;
        EngineAction currentEA = EngineAction.FORCED_OUTAGE;

        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.START_PREPARATION;
        IeeeStates beforeIeeeState = IeeeStates.FORCEDOUTAGE_MTBFO_REL;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);

        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, ieeeState);
    }


    @Test
    public void calcIeeeState_test_force_out_REL() {

        AvailableStates currentAV = AvailableStates.TROUBLESHOOTING;
        EngineAction currentEA = EngineAction.LOAD_RAMPDOWN;
        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.READY;
        IeeeStates beforeIeeeState = IeeeStates.AVAILABLE;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, ieeeState);
    }

    @Test
    public void calcIeeeState_test_force_out_REL_1() {

        AvailableStates currentAV = AvailableStates.TROUBLESHOOTING;
        EngineAction currentEA = EngineAction.NOT_READY;
        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.READY;
        IeeeStates beforeIeeeState = IeeeStates.AVAILABLE;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, ieeeState);
    }

    @Test
    public void calcIeeeState_test_force_out_REL2() {

        AvailableStates currentAV = AvailableStates.NOT_AVAILABLE;
        EngineAction currentEA = EngineAction.FORCED_OUTAGE;
        AvailableStates beforeAV = AvailableStates.AVAILABLE;
        EngineAction beforeEA = EngineAction.READY;
        IeeeStates beforeIeeeState = null;

        IeeeStates ieeeState = IeeeStateCalculator.calcIeeeState(currentAV, currentEA, beforeAV, beforeIeeeState, beforeEA);
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, ieeeState);
    }

}