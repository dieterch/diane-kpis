package io.myplant.service.StateKpiCalculation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class StateUtilsTest {

    @Test
    public void getTimestampEndOfDay() {
        // 2018-11-15 14:33:29 should be: 2018-11-15 23:59:59.999
        long actual = StateUtils.getTimestampEndOfDay(1542292409000L);
        assertEquals(1542326399999L, actual);
    }

    @Test
    public void getTimestampStartOfDay1() {
        // 2018-11-15 14:33:29 should be: 2018-11-15 00:00:00.000
        long actual = StateUtils.getTimestampStartOfDay(1542292409000L);
        assertEquals(1542240000000L, actual);
    }

    @Test
    public void getDailyString() {
        // 2018-11-15 14:33:29 should be: 20181115
        String actual = StateUtils.getDailyString(1542292409000L);
        assertEquals("20181115", actual);
    }


    @Test
    public void getMonthlyString() {
        // 2018-11-15 14:33:29 should be: 201811
        String actual = StateUtils.getMonthlyString(1542292409000L);
        assertEquals("201811", actual);
    }


}