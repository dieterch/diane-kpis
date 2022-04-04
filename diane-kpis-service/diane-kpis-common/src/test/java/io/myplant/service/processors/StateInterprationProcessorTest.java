package io.myplant.service.processors;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.model.*;
import io.myplant.service.ScopeMapperService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StateInterprationProcessorTest {

    StateInterprationProcessor processor;
    @Mock
    private ScopeMapperService scopeService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        processor = new StateInterprationProcessor(scopeService);
    }

    @Test
    public void checkStates_1() {

        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1553168648789L).actionTo(1553168653043L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1553168653043L).actionTo(1553168653043L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).actionFrom(1553168653043L).actionTo(1553168774812L).triggerMsgNo(0).build();
        DeviceState d4 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.FORCED_OUTAGE).actionFrom(1553168774812L).actionTo(1553169434543L).triggerMsgNo(1001).build();
        DeviceState d5 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).actionFrom(1553169434543L).actionTo(1555169434543L).triggerMsgNo(1001).build();

        List<DeviceState> actual = new ArrayList<>();
        actual.addAll(Arrays.asList(d1, d2, d3, d4, d5));

        List<DeviceState> result = processor.processStates(0, actual, AssetInformation.builder().ramStartDate("2015-01-01").avCalcType(1).build());

        assertEquals(6, result.size());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(0).getIeeeState());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(1).getIeeeState());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(2).getIeeeState());
        assertEquals(IeeeStates.AVAILABLE, result.get(2).getKielVuState());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(2).getKielVzState());
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, result.get(3).getIeeeState());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(4).getKielVzState());
        assertEquals(IeeeStates.AVAILABLE, result.get(5).getKielVzState());

    }

    @Test
    public void checkStates_2() {

        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.DEACTIVATED)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1552383281077L).actionTo(1552383288748L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.TROUBLESHOOTING)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1552383288748L).actionTo(1552383292494L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.DEACTIVATED)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1552383292494L).actionTo(1552383294921L).triggerMsgNo(1001).build();
        DeviceState d4 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.MAINTENANCE)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1552383294921L).actionTo(1552383298968L).triggerMsgNo(1001).build();
        DeviceState d5 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.MAINTENANCE)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1553169434543L).actionTo(1552383300585L).triggerMsgNo(1001).build();

        List<DeviceState> actual = new ArrayList<>();
        actual.addAll(Arrays.asList(d1, d2, d3, d4, d5));

        List<DeviceState> result = processor.processStates(0, actual, AssetInformation.builder().ramStartDate("2015-01-01").avCalcType(1).build());

        assertEquals(5, result.size());
        assertEquals(IeeeStates.DEACTIVATED_SHUTDOWN, result.get(0).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, result.get(1).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, result.get(2).getIeeeState());
        assertEquals(IeeeStates.DEACTIVATED_SHUTDOWN, result.get(2).getKielVuState());
        assertEquals(IeeeStates.FORCEDOUTAGE_REL, result.get(2).getKielVzState());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(3).getIeeeState());
        assertEquals(IeeeStates.UNPLANNED_MAINTENANCE, result.get(4).getKielVzState());
    }

    @Test
    public void checkIssues114() {
        DeviceState d0 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.AVAILABLE)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).actionFrom(1553168648789L).actionTo(1553168653043L).triggerMsgNo(1001).build();
        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.NOT_AVAILABLE)
                .actionActual(EngineAction.FORCED_OUTAGE).actionFrom(1553168648789L).actionTo(1553168653043L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.NOT_AVAILABLE)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1553168653043L).actionTo(1553168653043L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.NOT_AVAILABLE)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1553168653043L).actionTo(1553168653044L).triggerMsgNo(1001).build();
        DeviceState d4 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.NOT_AVAILABLE)
                .actionActual(EngineAction.TROUBLESHOOTING).actionFrom(1553168653044L).actionTo(1553168653045L).triggerMsgNo(1001).build();
        DeviceState d5 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.AVAILABLE)
                .actionActual(EngineAction.READY).actionFrom(1553168653045L).actionTo(1553168774812L).triggerMsgNo(0).build();
        DeviceState d6 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.AVAILABLE)
                .actionActual(EngineAction.START_PREPARATION).actionFrom(1553168774812L).actionTo(1553169434543L).triggerMsgNo(1001).build();
        DeviceState d7 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.NOT_AVAILABLE)
                .actionActual(EngineAction.FORCED_OUTAGE).actionFrom(1553169434543L).actionTo(1555169434544L).triggerMsgNo(1001).build();
        DeviceState d8 = DeviceState.builder().aws(DemandSelectorSwitchStates.UNDEFINED).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED).availableState(AvailableStates.NOT_AVAILABLE)
                .actionActual(EngineAction.FORCED_OUTAGE).actionFrom(1553169434544L).actionTo(1555169434545L).triggerMsgNo(1001).build();

        List<DeviceState> actual = new ArrayList<>(Arrays.asList(d0, d1, d2, d3, d4, d5, d6, d7, d8));

        List<DeviceState> result = processor.processStates(0, actual, AssetInformation.builder().ramStartDate("2015-01-01").avCalcType(1).build());

        assertEquals(11, result.size());
        assertEquals(IeeeStates.AVAILABLE, result.get(0).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(1).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(2).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(3).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(4).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(5).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(6).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(7).getIeeeState());
        assertEquals(IeeeStates.FORCEDOUTAGE_MTBFO_REL, result.get(8).getIeeeState());
    }
}