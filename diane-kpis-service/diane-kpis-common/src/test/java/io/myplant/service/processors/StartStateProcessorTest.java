package io.myplant.service.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.model.*;
import io.myplant.service.StateMachineV2Service;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StartStateProcessorTest {
    private static final Logger logger = LoggerFactory.getLogger(StateMachineV2Service.class);
    private ObjectMapper mapper = new ObjectMapper();
    private StartStateProcessor processor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        processor = new StartStateProcessor();
    }

    private List<DeviceState> readTestVector(String name) {
        try {
            String totalVector = IOUtils.toString(this.getClass().getResource(name), StandardCharsets.UTF_8);
            DeviceState[] stateArray = mapper.readValue(totalVector, DeviceState[].class);
            return Arrays.asList(stateArray);
        } catch (Exception ex) {
            logger.error("problem to read test vector " + name, ex);
            return null;
        }
    }

    @Test
    public void run() {

        List<DeviceState> deviceStates = readTestVector("/Vectors/vector_117057.json");
        assert deviceStates != null;
        List<Start> starts = processor.run(117057, deviceStates, null);

        logger.info("deviceStates: " + deviceStates.size());
        logger.info("starts: " + starts.size());

        logger.info("" + starts.get(0));
    }

    @Test
    public void checkStates_start() {
        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549497600000L).actionTo(1549526450012L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.START_PREPARATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526450012L).actionTo(1549526510354L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.START).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526510354L).actionTo(1549526548331L).triggerMsgNo(0).build();
        DeviceState d4 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.IDLE).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526548331L).actionTo(1549526553601L).triggerMsgNo(1001).build();
        DeviceState d5 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.SYNCHRONISATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526553601L).actionTo(1549526572941L).triggerMsgNo(1001).build();
        DeviceState d6 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526572941L).actionTo(1549526825383L).triggerMsgNo(1001).build();
        DeviceState d7 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE)
                .actionFrom(1549526825383L).actionTo(1549544464961L).triggerMsgNo(1001).build();
        DeviceState d8 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).availableState(AvailableStates.NOT_AVAILABLE)
                .actionFrom(1549544464961L).actionTo(1549548064961L).triggerMsgNo(1001).build();

        List<DeviceState> deviceStates = new ArrayList<>(Arrays.asList(d1, d2, d3, d4, d5, d6, d7, d8));

        AssetInformation info = new AssetInformation();
        info.setRamStartDate("2019-02-06 20:22:38.789");

        List<Start> starts = processor.run(115965, deviceStates, info);

        assertEquals(1, starts.size());
        assertEquals(1, starts.get(0).getValidStart());
        assertEquals(1, starts.get(0).getValidStartGCB());
    }

    @Test
    public void checkStates_startUnplannedMaint() {
        DeviceState d1 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.READY).actionFrom(1549497600000L).actionTo(1549526450012L).triggerMsgNo(1001).build();
        DeviceState d2 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.START_PREPARATION).actionFrom(1549526450012L).actionTo(1549526510354L).triggerMsgNo(1001).build();
        DeviceState d3 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.START).actionFrom(1549526510354L).actionTo(1549526548331L).triggerMsgNo(0).build();
        DeviceState d4 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.IDLE).actionFrom(1549526548331L).actionTo(1549526553601L).triggerMsgNo(1001).build();
        DeviceState d5 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.SYNCHRONISATION).actionFrom(1549526553601L).actionTo(1549526572941L).triggerMsgNo(1001).build();
        DeviceState d6 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION).actionFrom(1549526572941L).actionTo(1549526825383L).triggerMsgNo(1001).build();
        DeviceState d7 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).availableState(AvailableStates.AVAILABLE).actionFrom(1549526825383L).actionTo(1549530425383L).triggerMsgNo(1001).build();
        DeviceState d8 = DeviceState.builder().aws(DemandSelectorSwitchStates.REMOTE).bws(ServiceSelectorSwitchStates.AUTO).avss(AvailableStates.UNDEFINED)
                .actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).availableState(AvailableStates.NOT_AVAILABLE).actionFrom(1549530425383L).actionTo(1549548064961L).triggerMsgNo(1001).build();

        List<DeviceState> deviceStates = new ArrayList<>(Arrays.asList(d1, d2, d3, d4, d5, d6, d7, d8));

        AssetInformation info = new AssetInformation();
        info.setRamStartDate("2019-02-06 20:22:38.789");

        List<Start> starts = processor.run(115965, deviceStates, info);

        assertEquals(1, starts.size());
        assertEquals(0, starts.get(0).getValidStart());
        assertEquals(0, starts.get(0).getValidStartGCB());
    }

    @Test
    public void checkStates_validate_bugifx_128() {
        List<DeviceState> deviceStates = readTestVector("/Vectors/verify_issue_128.json");

        AssetInformation info = new AssetInformation();
        info.setRamStartDate("2019-02-06 20:22:38.789");

        assert deviceStates != null;
        List<Start> starts = processor.run(115965, deviceStates, info);

        assertEquals(1, starts.size());
        assertEquals(1, starts.get(0).getValidStart());
        assertEquals(1, starts.get(0).getValidStartGCB());
    }

    @Test
    public void checkStates_validate_bugifx_116() {
        List<DeviceState> deviceStates = readTestVector("/Vectors/verify_issue_116.json");

        AssetInformation info = new AssetInformation();
        info.setRamStartDate("2019-02-06 20:22:38.789");

        assert deviceStates != null;
        List<Start> starts = processor.run(115965, deviceStates, info);

        assertEquals(1, starts.size());
        assertEquals(0, starts.get(0).getValidStart());
        assertEquals(0, starts.get(0).getValidStartGCB());
    }
}