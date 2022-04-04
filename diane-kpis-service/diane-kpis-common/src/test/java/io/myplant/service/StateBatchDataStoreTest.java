package io.myplant.service;

import io.myplant.domain.DeviceState;
import io.myplant.model.AvailableStates;
import io.myplant.model.DemandSelectorSwitchStates;
import io.myplant.model.EngineAction;
import io.myplant.model.ServiceSelectorSwitchStates;
import io.myplant.service.datastore.StateBatchDataStore;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class StateBatchDataStoreTest {

    private StateBatchDataStore stateDataStoreService;

    @Before
    public void setUp() throws Exception {

        stateDataStoreService = new StateBatchDataStore(null, null);
    }

    @Test
    public void findDiff_deleteTwo() {

        List<DeviceState> storedStates = new ArrayList<>();
        List<DeviceState> newStates = new ArrayList<>();

        DeviceState s1 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1001L, 1002L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s1.setId(1L); storedStates.add(s1);
        DeviceState s2 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1003L, 1004L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s2.setId(2L); storedStates.add(s2);
        DeviceState s3 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1005L, 1006L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s3.setId(3L); storedStates.add(s3);
        DeviceState s4 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1007L, 1008L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s4.setId(4L); storedStates.add(s4);

        DeviceState n1 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1001L, 1002L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n1);
        DeviceState n2 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1003L, 1004L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n2);

        List<Long> statesToDelete = new ArrayList<>();
        List<DeviceState> statesToInsert = new ArrayList<>();
        stateDataStoreService.findDiff(newStates, storedStates, statesToInsert, statesToDelete);

        assertEquals(0, statesToInsert.size());
        assertEquals(2, statesToDelete.size());
        Long aLong = statesToDelete.get(0);
        assertEquals(3L, statesToDelete.get(0).longValue());
        assertEquals(4L, statesToDelete.get(1).longValue());
    }


    @Test
    public void findDiff_doNothing() {

        List<DeviceState> storedStates = new ArrayList<>();
        List<DeviceState> newStates = new ArrayList<>();

        DeviceState s1 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1001L, 1002L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s1.setId(1L); storedStates.add(s1);
        DeviceState s2 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1003L, 1004L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s2.setId(2L); storedStates.add(s2);

        DeviceState n1 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1001L, 1002L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n1);
        DeviceState n2 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1003L, 1004L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n2);


        List<Long> statesToDelete = new ArrayList<>();
        List<DeviceState> statesToInsert = new ArrayList<>();
        stateDataStoreService.findDiff(newStates, storedStates, statesToInsert, statesToDelete);

        assertEquals(0, statesToInsert.size());
        assertEquals(0, statesToDelete.size());
    }

    @Test
    public void findDiff_changedAfterFirts() {

        List<DeviceState> storedStates = new ArrayList<>();
        List<DeviceState> newStates = new ArrayList<>();

        DeviceState s1 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1001L, 1002L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s1.setId(1L); storedStates.add(s1);
        DeviceState s2 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1004L, 1005L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s2.setId(2L); storedStates.add(s2);
        DeviceState s3 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1005L, 1006L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s3.setId(3L); storedStates.add(s3);
        DeviceState s4 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1007L, 1008L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        s4.setId(4L); storedStates.add(s4);

        DeviceState n1 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1001L, 1002L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n1);
        DeviceState n2 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1003L, 1004L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n2);
        DeviceState n3 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1005L, 1006L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n3);
        DeviceState n4 = new DeviceState(0, EngineAction.ENGINE_COOLDOWN, 1007L, 1008L, 1003L, 1004, "trigger", "resp", 1, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.OFF, AvailableStates.AVAILABLE);
        newStates.add(n4);

        List<Long> statesToDelete = new ArrayList<>();
        List<DeviceState> statesToInsert = new ArrayList<>();
        stateDataStoreService.findDiff(newStates, storedStates, statesToInsert, statesToDelete);

        assertEquals(3, statesToInsert.size());
        assertEquals(3, statesToDelete.size());
        assertEquals(2L, statesToDelete.get(0).longValue());
        assertEquals(3L, statesToDelete.get(1).longValue());
    }


}
