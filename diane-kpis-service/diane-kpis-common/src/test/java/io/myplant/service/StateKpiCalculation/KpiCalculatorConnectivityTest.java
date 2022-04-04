package io.myplant.service.StateKpiCalculation;

import io.myplant.dataitem.DataItemValue;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.integration.Tuple;
import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.service.CassandraTimeSeriesService;
import io.myplant.service.processors.OutagesProcessor;
import io.myplant.service.processors.StartProcessor;
import io.myplant.service.processors.StartProcessorV2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.*;
import java.util.*;

import static java.time.Duration.between;
import static java.time.ZoneOffset.UTC;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class KpiCalculatorConnectivityTest {

    private KpiIeee kpiIeee = new KpiIeee();

    @MockBean
    private CassandraTimeSeriesService mockCassandraTimeSeriesService;

    private KpiCalculator calculator;

    @Before
    public void init() {
        //when(mockCassandraTimeSeriesService.fetchDataItems(1L,0L, 0L)).thenReturn();
        calculator = new KpiCalculator(null, new KpiOldValues(), kpiIeee, new StartProcessorV2(), new OutagesProcessor(), null, mockCassandraTimeSeriesService);
    }

    @Test
    public void getSendingEntries() {
        List<DataItemValue<Double>> sendingConditionItems = new ArrayList<>();
        sendingConditionItems.add(new DataItemValue<>(10, 10.0));
        sendingConditionItems.add(new DataItemValue<>(20, 30.0));
        sendingConditionItems.add(new DataItemValue<>(30, 30.0));
        sendingConditionItems.add(new DataItemValue<>(40, 20.0));
        sendingConditionItems.add(new DataItemValue<>(50, 30.0));

        List<Tuple<Long, Long>> sendingEntries = calculator.getSendingEntries(sendingConditionItems, 60);

        assertEquals(2, sendingEntries.size());
        assertEquals(20L, (long)sendingEntries.get(0).a);
        assertEquals(20L, (long)sendingEntries.get(0).b);
        assertEquals(10L, (long)sendingEntries.get(1).b);
    }

    @Test
    public void calculateConnectivityForTwoDays() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        List<Tuple<Long, Long>> sendingEntries = new ArrayList<>();
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T20:00:00Z").toEpochMilli(), 20L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T23:00:00Z").toEpochMilli(), 30L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-25T00:00:01Z").toEpochMilli(), 40L ));

        calculator.calculateConnectivity(sendingEntries, timesInDays, timesInMonth, new ArrayList<>());

        assertTrue(timesInDays.containsKey("20191224"));
        assertEquals(50L, timesInDays.get("20191224").connectivityTime);
        assertTrue(timesInDays.containsKey("20191225"));
        assertEquals(40L, timesInDays.get("20191225").connectivityTime);

        assertTrue(timesInMonth.containsKey("201912"));
        assertEquals(90L, timesInMonth.get("201912").connectivityTime);
    }

    @Test
    public void calculateConnectivityForLongSpans() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        List<Tuple<Long, Long>> sendingEntries = new ArrayList<>();
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T20:00:00Z").toEpochMilli()
                , Duration.between(Instant.parse("2019-12-24T20:00:00Z"),Instant.parse("2020-01-01T20:00:00Z")).toMillis() ));
        sendingEntries.add(new Tuple<>(Instant.parse("2020-01-01T23:00:00Z").toEpochMilli(), 30L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2020-01-02T00:00:01Z").toEpochMilli(), 40L ));

        calculator.calculateConnectivity(sendingEntries, timesInDays, timesInMonth, new ArrayList<>());

        assertTrue(timesInDays.containsKey("20191224"));
        assertTrue(timesInDays.containsKey("20191225"));
        assertTrue(timesInDays.containsKey("20200101"));
        assertTrue(timesInDays.containsKey("20200102"));
        assertEquals(4*60*60*1000L, timesInDays.get("20191224").connectivityTime);
        assertEquals(24*60*60*1000L, timesInDays.get("20191225").connectivityTime);
        assertEquals(20*60*60*1000L + 30L, timesInDays.get("20200101").connectivityTime);
        assertEquals(40L, timesInDays.get("20200102").connectivityTime);

        assertTrue(timesInMonth.containsKey("201912"));
        assertTrue(timesInMonth.containsKey("202001"));
        assertEquals(4*60*60*1000L /*24ter*/ + 7*24*60*60*1000L, timesInMonth.get("201912").connectivityTime);
    }


    @Test
    public void calculateRunningConnectivityTimespan() {

        List<Tuple<Long, Long>> sendingEntries = new ArrayList<>();
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T20:00:00Z").toEpochMilli(), 20L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T23:00:00Z").toEpochMilli(), 30L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-25T00:00:01Z").toEpochMilli(), 40L ));

        long result = calculator.getConnectivityTimespanDuringState( Instant.parse("2019-12-24T10:00:00Z").toEpochMilli()
                , Instant.parse("2019-12-24T23:55:00Z").toEpochMilli(), sendingEntries);

        assertEquals(50, result);

    }

    @Test
    public void calculateRunningConnectivityTimespan1() {

        List<Tuple<Long, Long>> sendingEntries = new ArrayList<>();
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T20:00:00Z").toEpochMilli(), 20L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T23:00:00Z").toEpochMilli(), 30L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-25T00:00:01Z").toEpochMilli(), 40L ));

        long result = calculator.getConnectivityTimespanDuringState( Instant.parse("2019-12-24T21:00:00Z").toEpochMilli()
                , Instant.parse("2019-12-24T23:55:00Z").toEpochMilli(), sendingEntries);

        assertEquals(30, result);
    }

    @Test
    public void calculateRunningConnectivityTimespan3() {

        List<Tuple<Long, Long>> sendingEntries = new ArrayList<>();
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T20:00:00Z").toEpochMilli(), 2000L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T23:00:00Z").toEpochMilli(), 30L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-25T00:00:01Z").toEpochMilli(), 40L ));

        long result = calculator.getConnectivityTimespanDuringState( Instant.parse("2019-12-24T20:00:00Z").toEpochMilli()
                , Instant.parse("2019-12-24T20:00:01Z").toEpochMilli(), sendingEntries);

        assertEquals(1000, result);
    }

    @Test
    public void calculateRunningConnectivityTimespan4() {

        List<Tuple<Long, Long>> sendingEntries = new ArrayList<>();
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T20:00:00Z").toEpochMilli(), 2000L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-24T23:00:00Z").toEpochMilli(), 30L ));
        sendingEntries.add(new Tuple<>(Instant.parse("2019-12-25T00:00:01Z").toEpochMilli(), 40L ));

        long result = calculator.getConnectivityTimespanDuringState( Instant.parse("2019-12-24T20:00:01Z").toEpochMilli()
                , Instant.parse("2019-12-24T23:00:01Z").toEpochMilli(), sendingEntries);

        assertEquals(1000 + 30, result);
    }


    @Test
    public void valuesInPeriodRangeTwoDays() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        Instant start = Instant.parse("2019-12-24T00:00:00Z");
        Instant end = Instant.parse("2019-12-25T23:00:00Z");

        List<Tuple<Long,ValuesInPeriod>> resultDays = calculator.getValuesInPeriod(start.toEpochMilli(), Duration.between(start,end).toMillis(), KpiCalculator.DAILY_PATTERN, timesInDays);

        assertEquals(2, resultDays.size());
        assertEquals(start.toEpochMilli(), (long)resultDays.get(0).a);
        assertTrue(timesInDays.containsKey("20191224"));
        assertTrue(timesInDays.containsKey("20191225"));

        List<Tuple<Long,ValuesInPeriod>> resultMonth = calculator.getValuesInPeriod(start.toEpochMilli(), Duration.between(start,end).toMillis(), KpiCalculator.MONTHLY_PATTERN, timesInMonth);

        assertEquals(1, resultMonth.size());
        assertEquals(start.toEpochMilli(), (long)resultMonth.get(0).a);
        assertTrue(timesInMonth.containsKey("201912"));
    }

    @Test
    public void TestEnOfDay() {

        long endOfDay = calculator.getEndOfDay(1577228400000L);
        assertEquals(1577231999999L, endOfDay);
    }



}