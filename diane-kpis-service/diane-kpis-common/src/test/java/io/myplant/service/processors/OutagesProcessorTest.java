package io.myplant.service.processors;

import io.myplant.domain.DeviceState;
import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import io.myplant.model.StateType;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class OutagesProcessorTest {

    @Test
    public void processStates_outageType() {
        DeviceState d1 = DeviceState.builder().actionFrom(1553418574342L).actionTo(1553544882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_REL).build();
        DeviceState d2 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).build();
        DeviceState d3 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.PLANNED_OUTAGE).build();
        DeviceState d4 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).build();
        DeviceState d5 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.DEACTIVATED_SHUTDOWN).build();

        OutagesProcessor processor = new OutagesProcessor();
        List<DeviceState> result = processor.processStates(Arrays.asList(d1, d2, d3, d4, d5), StateType.IEEE);
        assertEquals(4, result.size());
    }


    @Test
    public void processStates_sameState_sameScope() {
        DeviceState d1 = DeviceState.builder().actionFrom(1553418574342L).actionTo(1553544882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_REL).build();
        DeviceState d2 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_REL).build();

        OutagesProcessor processor = new OutagesProcessor();
        List<DeviceState> result = processor.processStates(Arrays.asList(d1, d2), StateType.IEEE);
        assertEquals(1, result.size());
        assertEquals(1553418574342L, result.get(0).getActionFrom());
        assertEquals(1553566882186L, result.get(0).getActionTo());
    }


// not longer valid
//    @Test
//    public void processStates_sameState_diffScope() {
//        DeviceState d1 = DeviceState.builder().actionFrom(1553418574342L).actionTo(1553544882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_REL).build();
//        DeviceState d2 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(null).ieeeState(IeeeStates.FORCEDOUTAGE_REL).build();
//
//        OutagesProcessor processor = new OutagesProcessor();
//        List<DeviceState> result = processor.processStates(0, Arrays.asList(d1, d2), StateType.IEEE);
//        assertEquals(2, result.size());
//        assertEquals(1553418574342L, result.get(0).getActionFrom());
//        assertEquals(1553544882186L, result.get(0).getActionTo());
//        assertEquals(1553544882186L, result.get(1).getActionFrom());
//        assertEquals(1553566882186L, result.get(1).getActionTo());
//    }

    @Test
    public void processStates_sameState_sameScope_diffType() {
        DeviceState d1 = DeviceState.builder().actionFrom(1553418574342L).actionTo(1553544882186L).scope(ScopeType.INNIO_Genset)
                .ieeeState(IeeeStates.FORCEDOUTAGE_REL).kielVuState(IeeeStates.FORCEDOUTAGE_REL).build();
        DeviceState d2 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(ScopeType.INNIO_Genset)
                .ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).kielVuState(IeeeStates.FORCEDOUTAGE_REL).build();

        OutagesProcessor processor = new OutagesProcessor();
        List<DeviceState> result = processor.processStates(Arrays.asList(d1, d2), StateType.VU);
        assertEquals(1, result.size());
        assertEquals(1553418574342L, result.get(0).getActionFrom());
        assertEquals(1553566882186L, result.get(0).getActionTo());
    }

    @Test
    public void processStates_none_outage() {
        DeviceState d1 = DeviceState.builder().actionFrom(1553418574342L).actionTo(1553544882186L).scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.AVAILABLE).build();
        DeviceState d2 = DeviceState.builder().actionFrom(1553544882186L).actionTo(1553566882186L).scope(null).ieeeState(IeeeStates.FORCEDOUTAGE_REL).build();

        OutagesProcessor processor = new OutagesProcessor();
        List<DeviceState> result = processor.processStates(Arrays.asList(d1, d2), StateType.IEEE);
        assertEquals(1, result.size());
        assertEquals(1553544882186L, result.get(0).getActionFrom());
        assertEquals(1553566882186L, result.get(0).getActionTo());
    }
}