package io.myplant;

import io.myplant.domain.AssetInformation;
import org.junit.Test;

import static org.junit.Assert.*;

public class UtilsTest {

    @Test
    public void getTimeAtZone() {

//        String timeAtZone = Utils.getTimeAtZone(1549640374123L, "America/New_York");
//        assertEquals("Fr 2019/02/08 10:39:34.123", timeAtZone);
//
//        String zoneOffset = Utils.getTimezoneOffset(1549640374123L, "America/New_York");
//        assertEquals("-0500", zoneOffset);
//
//
//
//        String time = Utils.getTimeGmt(1549640374123L);
//        assertEquals("Fr 2019/02/08 15:39:34.123", time);


    }


    @Test
    public void getMonitoringStart() {

        AssetInformation comm = new AssetInformation();
        comm.setRamStartDate("2014-03-05");

        long timestamp = Utils.getMonitoringStart(0, comm);

        assertEquals(1393977600000L, timestamp);
    }

    @Test
    public void getMonitoringStartOrg() {

        AssetInformation comm = new AssetInformation();
        comm.setCommissionDate("2014-05-03");

        long timestamp = Utils.getMonitoringStart(0, comm);

        assertEquals(1399075200000L, timestamp);
    }

    @Test
    public void getMonitoringStartWothout() {

        AssetInformation comm = new AssetInformation();

        long timestamp = Utils.getMonitoringStart(0, comm);

        assertEquals(0L, timestamp);
    }

}