package io.myplant.service;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.model.*;
import io.myplant.service.processors.OutagesProcessor;
import io.myplant.service.processors.StateInterprationProcessor;
import io.myplant.service.processors.StateOverwriteProcessor;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StateMachineV2ServiceTest {
    private String language = "en";
    private StateMachineV2Service machine = new StateMachineV2Service(
            null, null, null, null,
            null, null, null, new OutagesProcessor(),
            new StateOverwriteProcessor(), new StateInterprationProcessor(new ScopeMapperService()), null);

    private List<DeviceState> generateDeviceStateTestList(){
        List<DeviceState> states = new ArrayList<>();
        DeviceState dv = new DeviceState();
        dv.setAws(DemandSelectorSwitchStates.REMOTE);
        dv.setBws(ServiceSelectorSwitchStates.AUTO);
        dv.setAvss(AvailableStates.TROUBLESHOOTING);
        dv.setActionActual(EngineAction.READY);
        dv.setActionFrom(1570233600000L);
        dv.setActionTo(1570233700000L);
        dv.setScope(ScopeType.Partner);
        dv.setIeeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL);
        dv.setKielVuState(IeeeStates.FORCEDOUTAGE_MTBFO_REL);
        dv.setKielVzState(IeeeStates.FORCEDOUTAGE_MTBFO_REL);
        dv.setDescription("test");

        states.add(dv);
        return states;
    }

    private List<OverwriteState> generateOverwriteStateTestList(){
        List<OverwriteState> states = new ArrayList<>();
        OverwriteState ov = new OverwriteState();
        ov.setId(1570233600000L);
        ov.setActionFrom(1570233600000L);
        ov.setActionTo(1570233700000L);
        ov.setIeeeState(IeeeStates.PLANNED_OUTAGE);
        ov.setDescription("overwrite");

        states.add(ov);
        return states;
    }

    @Test
    public void processStatesUnmergedTest() {
        AssetInformation assetInformation = new AssetInformation();
        assetInformation.setId(1);
        assetInformation.setAvCalcType(0);
        assetInformation.setRamStartDate("2019-10-05 10:10:10");

        List<DeviceState> result = machine.processStates(assetInformation, generateDeviceStateTestList(), generateOverwriteStateTestList(), false, false, StateType.IEEE, false, language);

        assertEquals(1, result.size());
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, result.get(0).getIeeeState());
        assertEquals("test", result.get(0).getDescription());
    }

    @Test
    public void processStatesMergedTest() {
        AssetInformation assetInformation = new AssetInformation();
        assetInformation.setId(1);
        assetInformation.setAvCalcType(0);
        assetInformation.setRamStartDate("2019-10-05 10:10:10");

        List<DeviceState> result = machine.processStates(assetInformation, generateDeviceStateTestList(), generateOverwriteStateTestList(), false, true, StateType.IEEE, false, language);

        assertEquals(1, result.size());
        assertEquals(IeeeStates.PLANNED_OUTAGE, result.get(0).getIeeeState());
        assertEquals("overwrite", result.get(0).getDescription());
    }
}