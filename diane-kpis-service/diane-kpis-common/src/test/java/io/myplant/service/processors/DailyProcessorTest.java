package io.myplant.service.processors;

import io.myplant.service.StateKpiCalculation.StateUtils;
import io.myplant.domain.DeviceState;
import io.myplant.model.AvailableStates;
import io.myplant.model.DemandSelectorSwitchStates;
import io.myplant.model.EngineAction;
import io.myplant.model.ServiceSelectorSwitchStates;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class DailyProcessorTest {


    @Test
    public void createDailyListNoSplit() {


        // from 2018-11-15 00:00:00 to 2018-11-15 23:59:59
        DeviceState first = new DeviceState(0, EngineAction.IDLE, /* ActionFrom */ 1542240001000L, /*actionTo*/ 1542326399000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);
        // from 2018-11-16 00:00:00 to 2018-11-16 23:59:59
        DeviceState second = new DeviceState(0, EngineAction.IDLE, /* ActionFrom */ 1542326400000L, /*actionTo*/ 1542412799000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);


        DailyProcessor dailyProcessor = new DailyProcessor();
        List<DeviceState> dailyList = dailyProcessor.processStates(Arrays.asList(first, second));
        assertTrue(dailyList.size() == 2);
        assertEquals("20181115", StateUtils.getDailyString(dailyList.get(0).getActionFrom()));
        assertEquals("20181116", StateUtils.getDailyString(dailyList.get(1).getActionFrom()));
    }

    @Test
    public void createDailyListSplitOneEntryToTwo() {

        // from 2018-11-15 00:00:00 to 2018-11-16 11:00:00
        DeviceState first = new DeviceState(0, EngineAction.IDLE, 1542240001000L, 1542366000000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);
        // from 2018-11-16 11:00:01 to 2018-11-16 23:59:59
        DeviceState second = new DeviceState(0, EngineAction.IDLE, 1542366001000L, 1542412799000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);

        DailyProcessor dailyProcessor = new DailyProcessor();
        List<DeviceState> dailyList = dailyProcessor.processStates(Arrays.asList(first, second));
        assertEquals(3, dailyList.size());
        assertEquals(1542240001000L, dailyList.get(0).getActionFrom());
        assertEquals(1542326399999L, dailyList.get(0).getActionTo());
        assertEquals(1542326400000L, dailyList.get(1).getActionFrom());
        assertEquals(1542366000000L, dailyList.get(1).getActionTo());
        assertEquals("20181116", StateUtils.getDailyString(dailyList.get(1).getActionFrom()));
        assertEquals("20181116", StateUtils.getDailyString(dailyList.get(2).getActionFrom()));
    }

    @Test
    public void createDailyListSplitOneEntryToThree() {

        // from 2018-11-15 00:00:00 to 2018-11-17 11:00:00
        DeviceState first = new DeviceState(0, EngineAction.IDLE, 1542240001000L, 1542452400000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);
        // from 2018-11-17 11:00:01 to 2018-11-17 23:59:59
        DeviceState second = new DeviceState(0, EngineAction.IDLE, 1542452401000L, 1542499199000L, null, 0, null, null, 0, DemandSelectorSwitchStates.ON, ServiceSelectorSwitchStates.AUTO, AvailableStates.UNDEFINED);

        DailyProcessor dailyProcessor = new DailyProcessor();
        List<DeviceState> dailyList = dailyProcessor.processStates(Arrays.asList(first, second));

        assertTrue(dailyList.size() == 4);
        assertEquals("20181115", StateUtils.getDailyString(dailyList.get(0).getActionFrom()));
        assertEquals(1542326399999L, dailyList.get(0).getActionTo());
        assertEquals(1542326400000L, dailyList.get(1).getActionFrom());
        assertEquals(1542412799999L, dailyList.get(1).getActionTo());
        assertEquals("20181116", StateUtils.getDailyString(dailyList.get(1).getActionFrom()));
        assertEquals(1542412800000L, dailyList.get(2).getActionFrom());
        assertEquals(1542452400000L, dailyList.get(2).getActionTo());
        assertEquals("20181117", StateUtils.getDailyString(dailyList.get(2).getActionFrom()));
        assertEquals("20181117", StateUtils.getDailyString(dailyList.get(2).getActionFrom()));
    }

}