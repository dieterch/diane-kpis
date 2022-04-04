package io.myplant.service.processors;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.myplant.domain.DeviceState;
import io.myplant.domain.OverwriteState;
import io.myplant.model.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StateOverwriteProcessorTest {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void processStates() {


        // from 2018-11-15 00:00:00 to 2018-11-15 23:59:59
        DeviceState first = new DeviceState(0, EngineAction.IDLE, /* ActionFrom */ 1542240001000L, /*actionTo*/ 1542326399000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);
        // from 2018-11-16 00:00:00 to 2018-11-16 23:59:59
        DeviceState second = new DeviceState(0, EngineAction.IDLE, /* ActionFrom */ 1542326400000L, /*actionTo*/ 1542412799000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);

        OverwriteState o1 = OverwriteState.builder().actionFrom(1542240001000L).actionTo(1542326399000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).build();
        List<DeviceState> input = new ArrayList<>();
        input.addAll(Arrays.asList(first, second));

        StateOverwriteProcessor processor = new StateOverwriteProcessor();
        List<DeviceState> dailyList = processor.processStates(0, input, Arrays.asList(o1), null);
        assertTrue(dailyList.size() == 3);

    }


    @Test
    public void processStates_2() {

        //       "169954757"	"117002"	"32"	"1553418574342"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"1"	"6"	"6"	"0"
        //     "169954758"	"117002"	"34"	"1553544882186"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"6"	"0"
        //   "169954759"	"117002"	"34"	"1553544882186"	"1553544893496"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"2"	"0"


        // from 2018-11-15 00:00:00 to 2018-11-15 23:59:59
        DeviceState d1 = DeviceState.builder().actionFrom(1000L).actionTo(2000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d2 = DeviceState.builder().actionFrom(2000L).actionTo(3000L).scope(ScopeType.INNIO_BOP).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d3 = DeviceState.builder().actionFrom(3000L).actionTo(4000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();


        OverwriteState o1 = OverwriteState.builder().actionFrom(2000L).actionTo(4000L).scope(ScopeType.Partner).ieeeState(IeeeStates.PLANNED_OUTAGE).description("Test: Changed from Undefined to Available").build();


        StateOverwriteProcessor processor = new StateOverwriteProcessor();
        List<DeviceState> input = new ArrayList<>();
        input.addAll(Arrays.asList(d1, d2, d3));
        List<DeviceState> dailyList = processor.processStates(0, input, Arrays.asList(o1), null);
        assertTrue(dailyList.size() == 3);
        assertEquals(1000L, dailyList.get(0).getActionFrom());
        assertEquals(2000L, dailyList.get(0).getActionTo());
        assertEquals(ScopeType.INNIO_Genset, dailyList.get(0).getScope());
        assertEquals(2000L, dailyList.get(1).getActionFrom());
        assertEquals(ScopeType.Partner, dailyList.get(1).getScope());
    }

    @Test
    public void processStates_3() {

        //       "169954757"	"117002"	"32"	"1553418574342"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"1"	"6"	"6"	"0"
        //     "169954758"	"117002"	"34"	"1553544882186"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"6"	"0"
        //   "169954759"	"117002"	"34"	"1553544882186"	"1553544893496"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"2"	"0"


        // from 2018-11-15 00:00:00 to 2018-11-15 23:59:59
        DeviceState d1 = DeviceState.builder().actionFrom(1000L).actionTo(2000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d2 = DeviceState.builder().actionFrom(2000L).actionTo(3000L).scope(ScopeType.INNIO_BOP).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d3 = DeviceState.builder().actionFrom(3000L).actionTo(3050L).scope(ScopeType.INNIO_BOP).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d4 = DeviceState.builder().actionFrom(3050L).actionTo(4000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();


        OverwriteState o1 = OverwriteState.builder().actionFrom(2000L).actionTo(4000L).scope(ScopeType.Partner).ieeeState(IeeeStates.PLANNED_OUTAGE).description("Test: Changed from Undefined to Available").build();


        StateOverwriteProcessor processor = new StateOverwriteProcessor();
        List<DeviceState> input = new ArrayList<>();
        input.addAll(Arrays.asList(d1, d2, d3, d4));
        List<DeviceState> dailyList = processor.processStates(0, input, Arrays.asList(o1), null);
        assertTrue(dailyList.size() == 4);
        assertEquals(1000L, dailyList.get(0).getActionFrom());
        assertEquals(2000L, dailyList.get(0).getActionTo());
        assertEquals(ScopeType.INNIO_Genset, dailyList.get(0).getScope());
        assertEquals(2000L, dailyList.get(1).getActionFrom());
        assertEquals(ScopeType.Partner, dailyList.get(1).getScope());

        assertEquals(IeeeStates.PLANNED_OUTAGE, dailyList.get(1).getIeeeState());
        assertEquals(IeeeStates.PLANNED_OUTAGE, dailyList.get(2).getIeeeState());
        assertEquals(IeeeStates.PLANNED_OUTAGE, dailyList.get(3).getIeeeState());
    }

    @Test
    public void processStates_Split_1() {

        String descr = "Test: Changed from Undefined to Available";
        //       "169954757"	"117002"	"32"	"1553418574342"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"1"	"6"	"6"	"0"
        //     "169954758"	"117002"	"34"	"1553544882186"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"6"	"0"
        //   "169954759"	"117002"	"34"	"1553544882186"	"1553544893496"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"2"	"0"


        // from 2018-11-15 00:00:00 to 2018-11-15 23:59:59
        DeviceState d1 = DeviceState.builder().actionFrom(1000L).actionTo(2000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d2 = DeviceState.builder().actionFrom(2000L).actionTo(3000L).scope(ScopeType.INNIO_BOP).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d3 = DeviceState.builder().actionFrom(3000L).actionTo(4000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();

        OverwriteState o1 = OverwriteState.builder().actionFrom(1500).actionTo(3000).scope(ScopeType.Partner).ieeeState(IeeeStates.PLANNED_OUTAGE).description(descr).build();

        List<DeviceState> input = new ArrayList<>();
        input.addAll(Arrays.asList(d1, d2, d3));
        StateOverwriteProcessor processor = new StateOverwriteProcessor();
        List<DeviceState> dailyList = processor.processStates(0, input, Arrays.asList(o1), null);

        assertTrue(dailyList.size() == 4);
        assertEquals(1000L, dailyList.get(0).getActionFrom());
        assertEquals(1500L, dailyList.get(0).getActionTo());
        assertEquals(ScopeType.INNIO_Genset, dailyList.get(0).getScope());
        assertEquals(1500L, dailyList.get(1).getActionFrom());
        assertEquals(2000L, dailyList.get(1).getActionTo());
        assertEquals(ScopeType.Partner, dailyList.get(1).getScope());
    }

    @Test
    public void processStates_Split_2() {

        String descr = "Test: Changed from Undefined to Available";
        //       "169954757"	"117002"	"32"	"1553418574342"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"1"	"6"	"6"	"0"
        //     "169954758"	"117002"	"34"	"1553544882186"	"1553544882186"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"6"	"0"
        //   "169954759"	"117002"	"34"	"1553544882186"	"1553544893496"	"1553418574342"	"1001"	""	"M"	"0"	"6"	"2"	"0"


        // from 2018-11-15 00:00:00 to 2018-11-15 23:59:59
        DeviceState d1 = DeviceState.builder().actionFrom(1000L).actionTo(2000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d2 = DeviceState.builder().actionFrom(2000L).actionTo(3000L).scope(ScopeType.INNIO_BOP).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d3 = DeviceState.builder().actionFrom(3000L).actionTo(4000L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();

        OverwriteState o1 = OverwriteState.builder().actionFrom(2000).actionTo(3500).scope(ScopeType.Partner).ieeeState(IeeeStates.PLANNED_OUTAGE).description(descr).build();

        List<DeviceState> input = new ArrayList<>();
        input.addAll(Arrays.asList(d1, d2, d3));
        StateOverwriteProcessor processor = new StateOverwriteProcessor();
        List<DeviceState> dailyList = processor.processStates(0, input, Arrays.asList(o1), null);

        assertTrue(dailyList.size() == 4);
        assertEquals(2000L, dailyList.get(1).getActionFrom());
        assertEquals(3000L, dailyList.get(1).getActionTo());
        assertEquals(ScopeType.Partner, dailyList.get(1).getScope());
        assertEquals(3000L, dailyList.get(2).getActionFrom());
        assertEquals(3500L, dailyList.get(2).getActionTo());
        assertEquals(ScopeType.Partner, dailyList.get(2).getScope());
        assertEquals(3500L, dailyList.get(3).getActionFrom());
        assertEquals(4000L, dailyList.get(3).getActionTo());
        assertEquals(ScopeType.INNIO_Genset, dailyList.get(3).getScope());
    }

}