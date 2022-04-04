package io.myplant.service.StateKpiCalculation;

import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.service.processors.OutagesProcessor;
import io.myplant.service.processors.StartProcessor;
import io.myplant.service.processors.StartProcessorV2;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class KpiCalculatorTest {

    private KpiIeee kpiIeee = new KpiIeee();

    @MockBean
    private AssetInformationRepository mockAssetInformationRepository;

    private KpiCalculator calculator;

    @Before
    public void init() {
        AssetInformation assetInformation = new AssetInformation(1L, "2018-12-13", null, "Eruope/Berlin", 1);
        when(mockAssetInformationRepository.findById(1L)).thenReturn(Optional.of(assetInformation));
        calculator = new KpiCalculator(null, new KpiOldValues(), kpiIeee, new StartProcessorV2(), new OutagesProcessor(), mockAssetInformationRepository, null);
    }

    @Test
    public void calculateDaysSinceLastOutage() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        timesInMonth.put("201911", new ValuesInPeriod());
        timesInDays.put("20191101", new ValuesInPeriod());
        timesInDays.put("20191102", new ValuesInPeriod());
        timesInDays.put("20191103", new ValuesInPeriod());
        timesInDays.put("20191105", new ValuesInPeriod());
        timesInDays.put("20191106", new ValuesInPeriod());
        timesInDays.put("20191107", new ValuesInPeriod());
        timesInDays.put("20191109", new ValuesInPeriod());

        calculator.calculateCumulatedValues(timesInDays, timesInMonth);

        ValuesInPeriod[] results = timesInDays.values().toArray(new ValuesInPeriod[0]);
        assertEquals(1, results[0].getDaysSinceLastOutage());
        assertEquals(2, results[1].getDaysSinceLastOutage());
        assertEquals(3, results[2].getDaysSinceLastOutage());
        assertEquals(5, results[3].getDaysSinceLastOutage());
        assertEquals(6, results[4].getDaysSinceLastOutage());
        assertEquals(7, results[5].getDaysSinceLastOutage());
        assertEquals(9, results[6].getDaysSinceLastOutage());
    }

    @Test
    public void calculateDaysSinceLastOutageWithOutage() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        timesInMonth.put("201911", new ValuesInPeriod());
        timesInDays.put("20191101", new ValuesInPeriod());
        timesInDays.put("20191102", new ValuesInPeriod());
        timesInDays.put("20191103", new ValuesInPeriod());

        ValuesInPeriod val = new ValuesInPeriod();
        val.setCntOutages(1);
        timesInDays.put("20191105", val);
        timesInDays.put("20191106", new ValuesInPeriod());
        timesInDays.put("20191107", new ValuesInPeriod());
        timesInDays.put("20191109", new ValuesInPeriod());

        calculator.calculateCumulatedValues(timesInDays, timesInMonth);

        ValuesInPeriod[] results = timesInDays.values().toArray(new ValuesInPeriod[0]);
        assertEquals(1, results[0].getDaysSinceLastOutage());
        assertEquals(2, results[1].getDaysSinceLastOutage());
        assertEquals(3, results[2].getDaysSinceLastOutage());
        assertEquals(0, results[3].getDaysSinceLastOutage());
        assertEquals(1, results[4].getDaysSinceLastOutage());
        assertEquals(2, results[5].getDaysSinceLastOutage());
        assertEquals(4, results[6].getDaysSinceLastOutage());
    }

    @Test
    public void calculateNumberOfOutagesWithOutage() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        timesInMonth.put("201911", new ValuesInPeriod());
        timesInDays.put("20191101", new ValuesInPeriod());
        timesInDays.put("20191102", new ValuesInPeriod());
        timesInDays.put("20191103", new ValuesInPeriod());

        ValuesInPeriod val = new ValuesInPeriod();
        val.setCntOutages(1);
        timesInDays.put("20191105", val);
        timesInDays.put("20191106", new ValuesInPeriod());
        timesInDays.put("20191107", new ValuesInPeriod());
        timesInDays.put("20191109", new ValuesInPeriod());

        calculator.calculateCumulatedValues(timesInDays, timesInMonth);

        ValuesInPeriod[] results = timesInDays.values().toArray(new ValuesInPeriod[0]);
        assertEquals(0, results[0].getCntOutages());
        assertEquals(0, results[1].getCntOutages());
        assertEquals(0, results[2].getCntOutages());
        assertEquals(1, results[3].getCntOutages());
        assertEquals(0, results[4].getCntOutages());
        assertEquals(0, results[5].getCntOutages());
        assertEquals(0, results[6].getCntOutages());
    }

    @Test
    @Ignore
    public void accumulatedDeviceStatusWithOutage() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        timesInMonth.put("201911", new ValuesInPeriod());
        timesInDays.put("20191101", new ValuesInPeriod());
        timesInDays.put("20191102", new ValuesInPeriod());
        timesInDays.put("20191103", new ValuesInPeriod());

        ValuesInPeriod val = new ValuesInPeriod();
        timesInDays.put("20191105", val);
        timesInDays.put("20191106", new ValuesInPeriod());
        timesInDays.put("20191107", new ValuesInPeriod());
        timesInDays.put("20191109", new ValuesInPeriod());

        List<DeviceState> states = new ArrayList<>();
        states.add(DeviceState.builder().ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).scope(ScopeType.INNIO_Genset).actionFrom(20191106L).build());
        states.add(DeviceState.builder().ieeeState(IeeeStates.PLANNED_OUTAGE).scope(ScopeType.INNIO_Genset).actionFrom(20191107L).build());
        states.add(DeviceState.builder().ieeeState(IeeeStates.FORCEDOUTAGE_REL).scope(ScopeType.INNIO_Genset).actionFrom(20191109L).build());

        calculator.accumulatedDeviceStatus(1L, states, true);

        ValuesInPeriod[] results = timesInDays.values().toArray(new ValuesInPeriod[0]);
        assertEquals(0, results[0].getCntOutages());
        assertEquals(0, results[1].getCntOutages());
        assertEquals(0, results[2].getCntOutages());
        assertEquals(1, results[3].getCntOutages());
        assertEquals(1, results[4].getCntOutages());
        assertEquals(1, results[5].getCntOutages());
        assertEquals(0, results[6].getCntOutages());
    }


    @Test
    public void processStarts() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        timesInMonth.put("201911", new ValuesInPeriod());
        timesInDays.put("20191101", new ValuesInPeriod());
        timesInDays.put("20191102", new ValuesInPeriod());
        timesInDays.put("20191103", new ValuesInPeriod());

        List<Start> starts = new ArrayList<>();
        starts.add(Start.builder().validStart(1).build());

        calculator.processStarts(timesInDays, timesInMonth, starts);

        ValuesInPeriod[] results = timesInDays.values().toArray(new ValuesInPeriod[0]);
        assertEquals(1, results[0].getSuccessfulStarts());
        assertEquals(1, results[0].getSuccessfulStartsPerDayMonthly(), 0.0);
    }

    @Test
    public void processOutages() {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        timesInMonth.put("201911", new ValuesInPeriod());
        timesInDays.put("20191101", new ValuesInPeriod());
        timesInDays.put("20191102", new ValuesInPeriod());
        timesInDays.put("20191103", new ValuesInPeriod());

        List<DeviceState> states = new ArrayList<>();
        states.add(DeviceState.builder().ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).scope(ScopeType.INNIO_Genset).actionFrom(new Date().getTime()).build());
        states.add(DeviceState.builder().ieeeState(IeeeStates.PLANNED_OUTAGE).scope(ScopeType.INNIO_Genset).actionFrom(new Date().getTime()).build());
        states.add(DeviceState.builder().ieeeState(IeeeStates.FORCEDOUTAGE_REL).scope(ScopeType.INNIO_Genset).actionFrom(new Date().getTime()).build());

        calculator.processOutages(timesInDays, timesInMonth, states);

        ValuesInPeriod[] results = timesInDays.values().toArray(new ValuesInPeriod[0]);
        assertEquals(3, results[3].getCntOutages());
    }

    @Test
    public void testGetValuesInPeriodForDay() {
        Map<String, ValuesInPeriod> valuesInPeriodMap = new TreeMap<>();

        ValuesInPeriod value = calculator.getValuesInPeriod(1549526825383L, KpiCalculator.DAILY_PATTERN, valuesInPeriodMap);

        assertNotNull(value);
        assertEquals(1, value.getDaysInPeriod());
    }

    @Test
    public void testGetValuesInPeriodForMonth() {
        Map<String, ValuesInPeriod> valuesInPeriodMap = new TreeMap<>();

        ValuesInPeriod value = calculator.getValuesInPeriod(1549526825383L, KpiCalculator.MONTHLY_PATTERN, valuesInPeriodMap);

        assertNotNull(value);
        assertEquals(28, value.getDaysInPeriod());
    }

    @Test
    public void testGetValuesInPeriodFromCache() {
        Map<String, ValuesInPeriod> valuesInPeriodMap = new TreeMap<>();

        ValuesInPeriod value = calculator.getValuesInPeriod(1549526825383L, KpiCalculator.MONTHLY_PATTERN, valuesInPeriodMap);
        assertNotNull(value);
        value.setDaysInPeriod(15);

        ValuesInPeriod valueFromCache = calculator.getValuesInPeriod(1549526825383L, KpiCalculator.MONTHLY_PATTERN, valuesInPeriodMap);

        assertNotNull(valueFromCache);
        assertEquals(15, valueFromCache.getDaysInPeriod());
    }
}