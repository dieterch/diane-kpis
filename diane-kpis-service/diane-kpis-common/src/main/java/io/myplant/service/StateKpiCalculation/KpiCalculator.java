package io.myplant.service.StateKpiCalculation;

import io.myplant.dataitem.DataItemValue;
import io.myplant.domain.AssetInformation;
import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.integration.Tuple;
import io.myplant.kpi.CassandraKPIStore;
import io.myplant.model.StateType;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.service.CassandraTimeSeriesService;
import io.myplant.service.processors.OutagesProcessor;
import io.myplant.service.processors.StartProcessorV2;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.time.ZoneOffset.UTC;

@Service
@RequiredArgsConstructor
public class KpiCalculator {

    private static final Logger logger = LoggerFactory.getLogger(KpiCalculator.class);
    public static final String DAILY_PATTERN = "yyyyMMdd";
    public static final String MONTHLY_PATTERN = "yyyyMM";
    public static final long connectivityStartDate = 1588291200000L; // 2020-05-01 00:00:00

    private final CassandraKPIStore cassandraKpiStore;
    private final KpiOldValues kpiOldValues;
    private final KpiIeee kpiIeee;
    private final StartProcessorV2 startProcessor;
    private final OutagesProcessor outagesProcessor;
    private final AssetInformationRepository assetInformationRepository;
    private final CassandraTimeSeriesService cassandraTimeSeriesService;

    @Value("${kpi.name.availability}")
    private String kpiNameAvailability;
    @Value("${kpi.name.reliability}")
    private String kpiNameReliability;
    @Value("${kpi.name.mtbfo}")
    private String kpiNameMmtbfo;

    @Value("${kpi.name.ieee.av}")
    private String kpiNameIeeeAv;
    @Value("${kpi.name.ieee.rel}")
    private String kpiNameIeeeRel;
    @Value("${kpi.name.ieee.mtbfo}")
    private String kpiNameIeeeMtbfo;
    @Value("${kpi.name.ieee.startrel}")
    private String kpiNameIeeeStartRel;

    @Value("${kpi.name.kiel.vu}")
    private String kpiNameKielVu;
    @Value("${kpi.name.kiel.vz}")
    private String kpiNameKielVz;
    @Value("${kpi.name.kiel.vu.innio}")
    private String kpiNameKielVuInnio;
    @Value("${kpi.name.kiel.vz.innio}")
    private String kpiNameKielVzInnio;
    @Value("${kpi.name.kiel.startrel}")
    private String kpiNameKielStartRel;

    // new KPIs
    @Value("${kpi.name.utilization}")
    private String kpiNameUtiliization;
    @Value("${kpi.name.days.since.last.outages}")
    private String kpiNameDaySince;
    @Value("${kpi.name.outages.per.day}")
    private String kpiNameOutagesPerDay;
    @Value("${kpi.name.outages.per.day.withoutUnplannedMaintenance}")
    private String kpiNameOutagesPerDayWithoutUnplannedMaintenance;
    @Value("${kpi.name.starts.per.day}")
    private String kpiNameStartsPerDay;
    @Value("${kpi.name.unsuccessful.starts.per.day}")
    private String kpiNameUnsuccessfulStartsPerDay;

    @Value("${kpi.name.connectivity}")
    private String kpiNameConnectivity;
    @Value("${kpi.name.connectivityOperating}")
    private String kpiNameConnectivityOperating;

    @Value("${kpi.name.outages.count}")
    private String kpiNameOutagesCount;
    @Value("${kpi.name.outages.count.withoutUnplannedMaintenance}")
    private String kpiNameOutagesCountWithoutUnplannedMaintenance;
    @Value("${kpi.name.starts.count}")
    private String kpiNameStartsCount;
    @Value("${kpi.name.unsuccessful.starts.count}")
    private String kpiNameUnsuccessfulStartsCount;

    @Value("${kpi.name.total.starts.count}")
    private String kpiNameTotalStartsCount;

    @Value("${kpi.name.outages.count}")
    private String kpiOutagesCount;
    @Value("${kpi.name.starts.count}")
    private String kpiStartsCount;
    @Value("${kpi.name.unsuccessful.starts.count}")
    private String kpiUnsuccessfulStartsCount;

    @Value("${kpi.name.total.starts.count}")
    private String kpiTotalStartsCount;

    // only states with overwrites, no daily or monthly states
    public ValuesInPeriod calculateTimesForTimeRange(final long assetId, final List<DeviceState> states) {
        AssetInformation assetInformation = null;
        Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);
        if (storedOpt.isPresent())
            assetInformation = storedOpt.get();

        ValuesInPeriod valuesForRange = new ValuesInPeriod();
        DeviceState lastFooState = null;
        for (DeviceState state : states) {
            long stateDuration = getDuration(assetId, state);
            if (stateDuration < 0) {
                continue;
            }

            valuesForRange.setHSLF(state.getHSLF());

            kpiIeee.calculateTimes(valuesForRange, state, stateDuration);

            if (kpiIeee.isCountableIeeeOutages(state)) {
                if (lastFooState == null || (lastFooState != null && !isSameState(state, lastFooState, StateType.IEEE))) {
                    valuesForRange.incCntFOO();
                    lastFooState = state;
                }
            } else {
                lastFooState = null;
            }
        }

        List<Start> starts = startProcessor.run(assetId, states, assetInformation);
        List<DeviceState> outages = getOutagesForAsset(states);

        for (Start start : starts) {
            kpiIeee.countStarts(valuesForRange, start);
        }

        for (DeviceState deviceState : outages) {
            kpiIeee.countOutages(valuesForRange, deviceState);
            kpiIeee.countOutagesWithoutUnplannedMaintenance(valuesForRange, deviceState);
        }

        return valuesForRange;
    }

    public boolean isSameState(DeviceState currentState, DeviceState stateBefore, StateType stateType) {
        return currentState.outageOfType(stateType) == stateBefore.outageOfType(stateType)
                && currentState.getScope() == stateBefore.getScope();
    }

    // only states with overwrites, no daily or monthly states
    public void accumulatedDeviceStatus(final long assetId, final List<DeviceState> states, boolean dryRun) {
        Map<String, ValuesInPeriod> timesInDays = new TreeMap<>();
        Map<String, ValuesInPeriod> timesInMonth = new TreeMap<>();

        for (DeviceState status : states) {
            calculateTimes(assetId, status, DAILY_PATTERN, timesInDays);
            calculateTimes(assetId, status, MONTHLY_PATTERN, timesInMonth);
        }

        if(!states.isEmpty())
            checkMissingEntries(states.get(0).getActionFrom(), timesInDays, timesInMonth);

        List<Start> starts = getStartsForAsset(assetId, states);
        List<DeviceState> outages = getOutagesForAsset(states);

        processStarts(timesInDays, timesInMonth, starts);
        processOutages(timesInDays, timesInMonth, outages);
        calculateCumulatedValues(timesInDays, timesInMonth);
        processSendingDataKpis(assetId, connectivityStartDate, System.currentTimeMillis(), timesInDays, timesInMonth, states);


        logger.info("assetId {}: calculated {} daily groups", assetId, timesInDays.size());
        logger.info("assetId {}: calculated {} monthly groups", assetId, timesInMonth.size());

        List<Tuple<String, Double>> dailyRel = getValuesPerTime(timesInDays, ValuesInPeriod::getReliability);
        List<Tuple<String, Double>> monthlyRel = getValuesPerTime(timesInMonth, ValuesInPeriod::getReliability);
        logLatestValue("assetId " + assetId + ": last  reliability daily: ", dailyRel, monthlyRel);

        List<Tuple<String, Double>> dailyAvail = getValuesPerTime(timesInDays, ValuesInPeriod::getAvailability);
        List<Tuple<String, Double>> monthlyAvail = getValuesPerTime(timesInMonth, ValuesInPeriod::getAvailability);
        logLatestValue("assetId " + assetId + ": last availability daily: ", dailyAvail, monthlyAvail);

        List<Tuple<String, Double>> dailyMtbfo = getValuesPerTime(timesInDays, (valuesInPeriod -> (double) valuesInPeriod.getMtbfo()));
        List<Tuple<String, Double>> monthlyMtbfo = getValuesPerTime(timesInMonth, (valuesInPeriod -> (double) valuesInPeriod.getMtbfo()));
        logLatestValue("assetId " + assetId + ": last        MTBFO daily: ", dailyMtbfo, monthlyMtbfo);

        List<Tuple<String, Double>> ieeeDailyRel = getValuesPerTime(timesInDays, ValuesInPeriod::getIeeeRel);
        List<Tuple<String, Double>> ieeeMonthlyRel = getValuesPerTime(timesInMonth, ValuesInPeriod::getIeeeRel);
        logLatestValue("assetId " + assetId + ": last IEEE  reliab daily: ", ieeeDailyRel, ieeeMonthlyRel);

        List<Tuple<String, Double>> ieeeDailyAvail = getValuesPerTime(timesInDays, ValuesInPeriod::getIeeeAv);
        List<Tuple<String, Double>> ieeeMonthlyAvail = getValuesPerTime(timesInMonth, ValuesInPeriod::getIeeeAv);
        logLatestValue("assetId " + assetId + ": last IEEE availab daily: ", ieeeDailyAvail, ieeeMonthlyAvail);

        List<Tuple<String, Double>> ieeeDailyMtbfo = getValuesPerTime(timesInDays, ValuesInPeriod::getIeeeMtbfo);
        List<Tuple<String, Double>> ieeeMonthlyMtbfo = getValuesPerTime(timesInMonth, ValuesInPeriod::getIeeeMtbfo);
        logLatestValue("assetId " + assetId + ": last IEEE   MTBFO daily: ", ieeeDailyMtbfo, ieeeMonthlyMtbfo);

        // new KPIs
        List<Tuple<String, Double>> utilizationDaily = getValuesPerTime(timesInDays, ValuesInPeriod::getUtilization);
        List<Tuple<String, Double>> utilizationMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getUtilizationPerDayMonthly);
        logLatestValue("assetId " + assetId + ": last IEEE   MTBFO daily: ", ieeeDailyMtbfo, ieeeMonthlyMtbfo);

        List<Tuple<String, Double>> daysSinceLastOutageDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getDaysSinceLastOutage());
        List<Tuple<String, Double>> daysSinceLastOutageMonthly = getValuesPerTime(timesInMonth, (v) -> (double) v.getDaysSinceLastOutage());
        logLatestValue("assetId " + assetId + ": days since outage daily: ", daysSinceLastOutageDaily, daysSinceLastOutageDaily);

        List<Tuple<String, Double>> outagesPerDayDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getCntOutages());
        List<Tuple<String, Double>> outagesPerDayMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getOutagesPerDayMonthly);
        logLatestValue("assetId " + assetId + ": last outages per day   : ", outagesPerDayDaily, outagesPerDayDaily);

        List<Tuple<String, Double>> outagesPerDayWithoutUnplannedMaintenanceDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getCntOutagesWithoutUnplannedMaintenance());
        List<Tuple<String, Double>> outagesPerDayWithoutUnplannedMaintenanceMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getOutagesPerDayWithoutUnplannedMaintenanceMonthly);
        logLatestValue("assetId " + assetId + ": last outages per day without unplanned maintenance: ", outagesPerDayWithoutUnplannedMaintenanceDaily, outagesPerDayWithoutUnplannedMaintenanceMonthly);


        List<Tuple<String, Double>> successfulStartPerDayDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getSuccessfulStarts());
        List<Tuple<String, Double>> successfulStartPerDayMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getSuccessfulStartsPerDayMonthly);
        logLatestValue("assetId " + assetId + ": successful starts daily: ", successfulStartPerDayDaily, successfulStartPerDayDaily);

        List<Tuple<String, Double>> unsuccessfulStartPerDayDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getUnsuccessfulStarts());
        List<Tuple<String, Double>> unsuccessfulStartPerDayMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getUnsuccessfulStartsPerDayMonthly);
        logLatestValue("assetId " + assetId + ": un..cessful strts daily: ", unsuccessfulStartPerDayDaily, unsuccessfulStartPerDayDaily);

        List<Tuple<String, Double>> outagesCountDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getCntOutages());
        List<Tuple<String, Double>> outagesCountMonthly = getValuesPerTime(timesInMonth, (v) -> (double) v.getCntOutages());
        logLatestValue("assetId " + assetId + ": last outages count daily: ", outagesCountDaily, outagesCountDaily);

        List<Tuple<String, Double>> outagesCountWithoutUnplannedMaintenanceDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getCntOutagesWithoutUnplannedMaintenance());
        List<Tuple<String, Double>> outagesCountWithoutUnplannedMaintenanceMonthly = getValuesPerTime(timesInMonth, (v) -> (double) v.getCntOutagesWithoutUnplannedMaintenance());
        logLatestValue("assetId " + assetId + ": last outages count without unplanned maintenance daily: ", outagesCountDaily, outagesCountDaily);

        List<Tuple<String, Double>> startsCountDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getSuccessfulStarts());
        List<Tuple<String, Double>> startsCountMonthly = getValuesPerTime(timesInMonth, (v) -> (double) v.getSuccessfulStarts());
        logLatestValue("assetId " + assetId + ": last starts count daily: ", startsCountDaily, startsCountDaily);

        List<Tuple<String, Double>> unsuccessfulStartCountDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getUnsuccessfulStarts());
        List<Tuple<String, Double>> unsuccessfulStartCountMonthly = getValuesPerTime(timesInMonth, (v) -> (double) v.getUnsuccessfulStarts());
        logLatestValue("assetId " + assetId + ": last unsuccessful starts count daily: ", unsuccessfulStartCountDaily, unsuccessfulStartCountDaily);

        List<Tuple<String, Double>> totalStartsCountDaily = getValuesPerTime(timesInDays, (v) -> (double) v.getTotalStartsCount());
        List<Tuple<String, Double>> totalStartsCountMonthly = getValuesPerTime(timesInMonth, (v) -> (double) v.getTotalStartsCount());
        logLatestValue("assetId " + assetId + ": last total starts count daily: ", totalStartsCountDaily, totalStartsCountDaily);

        // connectivity KPIs
        List<Tuple<String, Double>> connectivityDaily = getValuesPerTime(timesInDays, ValuesInPeriod::getConnectivity);
        List<Tuple<String, Double>> connectivityMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getConnectivity);
        logLatestValue("assetId " + assetId + ": connectivity            :", connectivityDaily, totalStartsCountMonthly);

        // connectivity during operating
        List<Tuple<String, Double>> connectivityOperatingDaily = getValuesPerTime(timesInDays, ValuesInPeriod::getConnectivityOperating);
        List<Tuple<String, Double>> connectivityOperatingMonthly = getValuesPerTime(timesInMonth, ValuesInPeriod::getConnectivityOperating);
        logLatestValue("assetId " + assetId + ": connectivity            :", connectivityOperatingDaily, connectivityOperatingMonthly);




        if (!dryRun) {
            cassandraKpiStore.updateKPI(assetId, kpiNameAvailability, dailyAvail, monthlyAvail);
            cassandraKpiStore.updateKPI(assetId, kpiNameReliability, dailyRel, monthlyRel);
            cassandraKpiStore.updateKPI(assetId, kpiNameMmtbfo, dailyMtbfo, monthlyMtbfo);

            cassandraKpiStore.updateKPI(assetId, kpiNameIeeeAv, ieeeDailyAvail, ieeeMonthlyAvail);
            cassandraKpiStore.updateKPI(assetId, kpiNameIeeeRel, ieeeDailyRel, ieeeMonthlyRel);
            cassandraKpiStore.updateKPI(assetId, kpiNameIeeeMtbfo, ieeeDailyMtbfo, ieeeMonthlyMtbfo);
            cassandraKpiStore.updateKPI(assetId, kpiNameIeeeMtbfo, ieeeDailyMtbfo, ieeeMonthlyMtbfo);

            // new KPIs
            cassandraKpiStore.updateKPI(assetId, kpiNameUtiliization, utilizationDaily, utilizationMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameDaySince, daysSinceLastOutageDaily, daysSinceLastOutageMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameOutagesPerDay, outagesPerDayDaily, outagesPerDayMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameOutagesPerDayWithoutUnplannedMaintenance, outagesPerDayWithoutUnplannedMaintenanceDaily, outagesPerDayWithoutUnplannedMaintenanceMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameStartsPerDay, successfulStartPerDayDaily, successfulStartPerDayMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameUnsuccessfulStartsPerDay, unsuccessfulStartPerDayDaily, unsuccessfulStartPerDayMonthly);

            cassandraKpiStore.updateKPI(assetId, kpiNameOutagesCount, outagesCountDaily, outagesCountMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameOutagesCountWithoutUnplannedMaintenance, outagesCountWithoutUnplannedMaintenanceDaily, outagesCountWithoutUnplannedMaintenanceMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameStartsCount, startsCountDaily, startsCountMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameUnsuccessfulStartsCount, unsuccessfulStartCountDaily, unsuccessfulStartCountMonthly);

            cassandraKpiStore.updateKPI(assetId, kpiTotalStartsCount, totalStartsCountDaily, totalStartsCountMonthly);

            cassandraKpiStore.updateKPI(assetId, kpiNameConnectivity, connectivityDaily, connectivityMonthly);
            cassandraKpiStore.updateKPI(assetId, kpiNameConnectivityOperating, connectivityOperatingDaily, connectivityOperatingMonthly);



            logger.info("KPIs stored successfully");
        } else {
            logger.info("dryRun flag set to true - not KPIs stored");
        }
    }

    private void checkMissingEntries(long actionFrom, Map<String, ValuesInPeriod> timesInDays, Map<String, ValuesInPeriod> timesInMonth) {
        long now = System.currentTimeMillis();
        for(long l = actionFrom; l < now; l += 24*60*60*1000L) {
            getValuesInPeriod(l, DAILY_PATTERN, timesInDays);
            getValuesInPeriod(l, MONTHLY_PATTERN, timesInMonth);
        }
    }

    private List<Start> getStartsForAsset(final long assetId, final List<DeviceState> states) {
        AssetInformation assetInformation = null;
        Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);
        if (storedOpt.isPresent()) {
            assetInformation = storedOpt.get();
        }

        return startProcessor.run(assetId, states, assetInformation);
    }

    private List<DeviceState> getOutagesForAsset(final List<DeviceState> states) {
        return outagesProcessor.processStates(states, StateType.IEEE);
    }

    protected void processStarts(final Map<String, ValuesInPeriod> timesInDays, final Map<String, ValuesInPeriod> timesInMonth, List<Start> starts) {
        for (Start start : starts) {
            ValuesInPeriod valuesInPeriodDays = getValuesInPeriod(start.getStartDate(), DAILY_PATTERN, timesInDays);
            ValuesInPeriod valuesInPeriodMonth = getValuesInPeriod(start.getStartDate(), MONTHLY_PATTERN, timesInMonth);

            kpiIeee.countStarts(valuesInPeriodDays, start);
            kpiIeee.countStarts(valuesInPeriodMonth, start);
        }
    }

    protected void processOutages(final Map<String, ValuesInPeriod> timesInDays, final Map<String, ValuesInPeriod> timesInMonth, List<DeviceState> deviceStates) {
        for (DeviceState deviceState : deviceStates) {
            ValuesInPeriod valuesInPeriodDays = getValuesInPeriod(deviceState.getActionFrom(), DAILY_PATTERN, timesInDays);
            ValuesInPeriod valuesInPeriodMonth = getValuesInPeriod(deviceState.getActionFrom(), MONTHLY_PATTERN, timesInMonth);

            kpiIeee.countOutages(valuesInPeriodDays, deviceState);
            kpiIeee.countOutages(valuesInPeriodMonth, deviceState);

            kpiIeee.countOutagesWithoutUnplannedMaintenance(valuesInPeriodDays, deviceState);
            kpiIeee.countOutagesWithoutUnplannedMaintenance(valuesInPeriodMonth, deviceState);
        }
    }

    protected void processSendingDataKpis(long assetId, long from, long to , final Map<String, ValuesInPeriod> timesInDays, final Map<String, ValuesInPeriod> timesInMonth, List<DeviceState> deviceStates) {
        long now = System.currentTimeMillis();

        List<DataItemValue<Double>> sendingConditionItems = cassandraTimeSeriesService.fetchDataItems(assetId, 0, now);
        if(sendingConditionItems.isEmpty())
            return; // no sending kpis possible

        List<Tuple<Long, Long>> sendingEntries = getSendingEntries(sendingConditionItems, now);
        calculateConnectivity(sendingEntries, timesInDays, timesInMonth, deviceStates);
    }

    // returns a list of tuples with a: start time and b: time span
    protected List<Tuple<Long,Long>> getSendingEntries(List<DataItemValue<Double>> sendingConditionItems, long endTimestamp){

    // sending condition
    //  Unknown(0),
    //  NoData(10),
    //  Never(20),
    //  Sending(30)

        sendingConditionItems = sendingConditionItems.stream().sorted(Comparator.comparingLong(DataItemValue::getTimestamp)).collect(Collectors.toList());

        long lastSendingTimeStamp = 0L;

        // start time, time span
        List<Tuple<Long,Long>> sendingEntries = new ArrayList<>();

        for (DataItemValue <Double> di : sendingConditionItems){
            if(lastSendingTimeStamp == 0){
                if(di.getValue() == 30) {
                    lastSendingTimeStamp = di.getTimestamp();
                }
                continue;
            }

            if(di.getValue() == 30.0) {
                // found another sending entry... ingnore
                continue;
            }else {
                // found negative edge
                sendingEntries.add(new Tuple<>(lastSendingTimeStamp, di.getTimestamp() - lastSendingTimeStamp));
                lastSendingTimeStamp = 0;
            }
        }
        // add the last entry
        if(lastSendingTimeStamp != 0)
            sendingEntries.add(new Tuple<>(lastSendingTimeStamp, endTimestamp - lastSendingTimeStamp));
        return sendingEntries;
    }

    protected void calculateConnectivity(List<Tuple<Long,Long>> sendingEntries, final Map<String, ValuesInPeriod> timesInDays, final Map<String, ValuesInPeriod> timesInMonth, List<DeviceState> deviceStates){
        for (Tuple<Long,Long> entry:sendingEntries){

            long endTimestamp = entry.a + entry.b;
            long currentTimestamp = entry.a;
            long nextTimestamp;
            do{
                nextTimestamp = currentTimestamp + (24*60*60*1000L);
                if(nextTimestamp > endTimestamp)
                    nextTimestamp = endTimestamp;
                long endOfDay = getEndOfDay(currentTimestamp);
                if(nextTimestamp > endOfDay)
                    nextTimestamp = endOfDay+1L;

                long usedTimespan = nextTimestamp - currentTimestamp;

                ValuesInPeriod valuesInPeriodDays = getValuesInPeriod(currentTimestamp, DAILY_PATTERN, timesInDays);
                ValuesInPeriod valuesInPeriodMonth = getValuesInPeriod(currentTimestamp, MONTHLY_PATTERN, timesInMonth);

                valuesInPeriodDays.addConnectivity(usedTimespan);
                valuesInPeriodMonth.addConnectivity(usedTimespan);

                currentTimestamp = nextTimestamp;
            }while(nextTimestamp < endTimestamp);
        }


        // check if connectivity was ok during running (operating) states
        for(DeviceState state : deviceStates){
            if(KpiIeee.isOperatingAction(state.getActionActual())){
                // check if during whole the connectivity
                long connectivityInState = getConnectivityTimespanDuringState(state.getActionFrom(), state.getActionTo(), sendingEntries);

                ValuesInPeriod valuesInPeriodDays = getValuesInPeriod(state.getActionFrom(), DAILY_PATTERN, timesInDays);
                ValuesInPeriod valuesInPeriodMonth = getValuesInPeriod(state.getActionFrom(), MONTHLY_PATTERN, timesInMonth);

                valuesInPeriodDays.addConnectivityOperating(connectivityInState);
                valuesInPeriodMonth.addConnectivityOperating(connectivityInState);
            }

        }
    }

    protected long getConnectivityTimespanDuringState(long stateFrom, long stateTo, List<Tuple<Long,Long>> sendingEntries){
        long timespanConnectivity = 0;

        for(Tuple<Long,Long> entry : sendingEntries){
            long connectivityStart = entry.a;
            long connectivityEnd = entry.a + entry.b;
            if(stateFrom > connectivityEnd || stateTo < connectivityStart)
                continue;

            // S connectivity start
            // E connectivity end
            // F state from
            // T state to

            // S-------------------E
            //     F======T
            if(stateFrom >= connectivityStart && stateTo <= connectivityEnd)
                // during whole state data was sent
                return stateTo - stateFrom;

            //    S-----E
            // F---=====---T
            if(connectivityStart >= stateFrom  && connectivityEnd <= stateTo)
                timespanConnectivity += connectivityEnd - connectivityStart;

            //           S-----E
            // F---------==T
            if(connectivityStart >= stateFrom  && connectivityEnd >= stateTo)
                timespanConnectivity += stateTo - connectivityStart;

            // S-------E
            //     F===--------T
            if(stateFrom >= connectivityStart && stateTo >= connectivityEnd)
                timespanConnectivity += connectivityEnd - stateFrom;
        }

        return timespanConnectivity;
    }

    protected void calculateCumulatedValues(Map<String, ValuesInPeriod> timesInDays, Map<String, ValuesInPeriod> timesInMonth) {
        String lastDailyKey = null;

        for(Map.Entry<String, ValuesInPeriod> entry : timesInDays.entrySet()){
            String key = entry.getKey();
            ValuesInPeriod dailyValue = entry.getValue();
            ValuesInPeriod monthlyValue = timesInMonth.get(key.substring(0, 6));

            if (dailyValue.getCntOutages() > 0) {
                dailyValue.setDaysSinceLastOutage(0);
            } else if (lastDailyKey == null) {
                dailyValue.setDaysSinceLastOutage(1);
            } else {
                ValuesInPeriod lastDailyValues = timesInDays.get(lastDailyKey);

                // Add days from last value to the date-difference between the last to the current
                long daysSinceLastOutage = getDateDiff(key, lastDailyKey) + lastDailyValues.getDaysSinceLastOutage();

                dailyValue.setDaysSinceLastOutage(daysSinceLastOutage);
            }

            dailyValue.setTotalStartsCount(dailyValue.getSuccessfulStarts() + dailyValue.getUnsuccessfulStarts());

            monthlyValue.setDaysSinceLastOutage(dailyValue.getDaysSinceLastOutage());

            lastDailyKey = key;
        }
    }

    private long getDateDiff(String currentDate, String lastDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DAILY_PATTERN, Locale.ENGLISH);
        LocalDate date1 = LocalDate.parse(currentDate, formatter);
        LocalDate date2 = LocalDate.parse(lastDate, formatter);

        Period period = Period.between(date2, date1);

        return period.getDays();
    }

    private List<Tuple<String, Double>> getValuesPerTime(Map<String, ValuesInPeriod> valuesMap, Function<ValuesInPeriod, Double> getFunction) {
        return valuesMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(es -> new Tuple<>(es.getKey(), getFunction.apply(es.getValue()))).collect(Collectors.toList());
    }

    private void logLatestValue(String logString, List<Tuple<String, Double>> dailyRel, List<Tuple<String, Double>> monthlyRel) {
        Tuple<String, Double> latestDailyRel = getLatestValue(dailyRel);
        Tuple<String, Double> latestMonthleyRel = getLatestValue(monthlyRel);

        logger.info(logString + ((latestDailyRel == null) ? "na" : latestDailyRel.a + ": " + latestDailyRel.b) + "  monthly: " + ((latestMonthleyRel == null) ? "na" : latestMonthleyRel.a + ": " + latestMonthleyRel.b));
    }

    private Tuple<String, Double> getLatestValue(List<Tuple<String, Double>> values) {
        if (values.isEmpty()) {
            return null;
        }

        return new Tuple<>(values.get(values.size() - 1).a, values.get(values.size() - 1).b);
    }

    private void calculateTimes(final long assetId, DeviceState state, String pattern, Map<String, ValuesInPeriod> valuesInPeriodMap) {
        ValuesInPeriod valuesInPeriod = getValuesInPeriod(state.getActionFrom(), pattern, valuesInPeriodMap);
        long stateDuration = getDuration(assetId, state);

        if (stateDuration < 0) {
            return;
        }

        kpiOldValues.calculateOldValues(valuesInPeriod, state, stateDuration);
        kpiIeee.calculateTimes(valuesInPeriod, state, stateDuration);
    }

    private long getDuration(long assetId, DeviceState state) {
        long stateDuration = state.getActionTo() - state.getActionFrom();

        if (stateDuration < 0) {
            logger.debug("assetId {}: State duration cannot be negative: {}", assetId, state);
            return -1;
        }

        return stateDuration;
    }

    protected ValuesInPeriod getValuesInPeriod(long timestamp, String pattern, Map<String, ValuesInPeriod> valuesInPeriodMap) {
        LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), UTC);

        String currentStatusKey = DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH).format(date);
        ValuesInPeriod valuesInPeriod;

        if (valuesInPeriodMap.containsKey(currentStatusKey)) {
            valuesInPeriod = valuesInPeriodMap.get(currentStatusKey);
        } else {
            valuesInPeriod = new ValuesInPeriod();
            valuesInPeriodMap.put(currentStatusKey, valuesInPeriod);

            if (pattern.length() == MONTHLY_PATTERN.length()) {
                valuesInPeriod.setDaysInPeriod(getDaysOfMonth(date));
            }
        }

        return valuesInPeriod;
    }

    protected List<Tuple<Long,ValuesInPeriod>> getValuesInPeriod(long startTimestamp, long timeSpan, String pattern, Map<String, ValuesInPeriod> valuesInPeriodMap) {

        List<Tuple<Long,ValuesInPeriod>> result = new ArrayList<>();

        long endTimeSpan = startTimestamp + timeSpan;
        long nextTimestamp = startTimestamp;
        do{
            ValuesInPeriod valuesInPeriod = getValuesInPeriod(nextTimestamp, pattern, valuesInPeriodMap);
            result.add(new Tuple<>(nextTimestamp, valuesInPeriod));

            LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(nextTimestamp), UTC);
            if(pattern.equals(DAILY_PATTERN))
                date = date.plusDays(1);
            else
                date = date.plusMonths(1);

            nextTimestamp = date.toEpochSecond(UTC) * 1000;
        }while(nextTimestamp < endTimeSpan);

        return result;
    }


    /**
     * Returns the length of the month in days. If the month is the same as the current date, then use the day of the month
     */
    private int getDaysOfMonth(LocalDateTime date) {
        LocalDate localDate = LocalDate.now();

        if (date.getMonthValue() == localDate.getMonthValue() && date.getYear() == localDate.getYear()) {
            return localDate.getDayOfMonth();
        } else {
            return YearMonth.of(date.getYear(), date.getMonthValue()).lengthOfMonth();
        }
    }

    protected long getEndOfDay(long timestamp){
        LocalDateTime currentDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), UTC);
        return LocalDateTime.of(currentDate.toLocalDate(), LocalTime.MAX).toInstant(UTC).toEpochMilli();
    }
}
