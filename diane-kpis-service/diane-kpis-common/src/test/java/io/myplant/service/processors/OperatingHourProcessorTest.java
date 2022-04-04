package io.myplant.service.processors;

import io.myplant.domain.DeviceState;
import io.myplant.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OperatingHourProcessorTest {
    private OperatingHourProcessor operatingHourProcessor = new OperatingHourProcessor();

    @Test
    public void test_operatingHourProcessor_phCalculation() {
        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549497600000L).actionTo(1549526450012L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526400000L).actionTo(1549699200000L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).ieeeState(IeeeStates.DEACTIVATED_SHUTDOWN).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526400000L).actionTo(1549699200000L).triggerMsgNo(1001).build();
        DeviceState d4 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).ieeeState(IeeeStates.DEACTIVATED_SHUTDOWN).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549699200000L).actionTo(1549699200000L).triggerMsgNo(1001).build();

        List<DeviceState> deviceStates = new ArrayList<>(Arrays.asList(d1, d2, d3, d4));
        operatingHourProcessor.processStates(deviceStates);
        assertEquals(28850012L, d1.getPH());
        assertTrue(d1.getDuration() <= d1.getPH());
        assertEquals(172800000L, d2.getPH());
        assertEquals(201650012L, d2.getCumPH());
        assertTrue(d2.getDuration() <= d2.getPH());
        assertEquals(0L, d3.getPH());
        assertEquals(201650012L, d3.getCumPH());
        assertEquals(0, d4.getDuration());
        assertEquals(0, d4.getPH());
    }

    @Test
    public void test_operatingHourProcessor_ohCalculation() {
        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.ISLAND_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549497600000L).actionTo(1549526450012L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526400000L).actionTo(1549699200000L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).ieeeState(IeeeStates.DEACTIVATED_SHUTDOWN).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526400000L).actionTo(1549699200000L).triggerMsgNo(1001).build();

        List<DeviceState> deviceStates = new ArrayList<>(Arrays.asList(d1, d2, d3));
        operatingHourProcessor.processStates(deviceStates);
        assertEquals(28850012L, d1.getOH());
        assertEquals(28850012L, d1.getCumOH());
        assertTrue(d1.getDuration() <= d1.getOH());
        assertEquals(172800000L, d2.getOH());
        assertEquals(201650012L, d2.getCumOH());
        assertTrue(d2.getDuration() <= d2.getOH());
        assertEquals(0L, d3.getOH());
        assertEquals(201650012L, d3.getCumOH());
    }

    @Test
    public void test_operatingHourProcessor_aohCalculation() {
        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.ISLAND_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549497600000L).actionTo(1549526450012L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526400000L).actionTo(1549699200000L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.RAMPUP_ISLAND_OPERATION).ieeeState(IeeeStates.DEACTIVATED_SHUTDOWN).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526400000L).actionTo(1549699200000L).triggerMsgNo(1001).build();

        List<DeviceState> deviceStates = new ArrayList<>(Arrays.asList(d1, d2, d3));
        operatingHourProcessor.processStates(deviceStates);
        assertEquals(28850012L, d1.getAOH());
        assertEquals(28850012L, d1.getCumAOH());
        assertTrue(d1.getDuration() <= d1.getAOH());
        assertEquals(172800000L, d2.getAOH());
        assertEquals(201650012L, d2.getCumAOH());
        assertTrue(d2.getDuration() <= d2.getAOH());
        assertEquals(0L, d3.getAOH());
        assertEquals(201650012L, d3.getCumAOH());
    }
}