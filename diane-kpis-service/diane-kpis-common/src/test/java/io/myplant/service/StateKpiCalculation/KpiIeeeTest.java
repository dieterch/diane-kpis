package io.myplant.service.StateKpiCalculation;

import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KpiIeeeTest {


    KpiIeee kpiIeee = new KpiIeee();

    @Test
    public void calculateIeeeRelAndAv() {
        ValuesInPeriod values = new ValuesInPeriod();

        DeviceState s1 = DeviceState.builder().scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).build();
        kpiIeee.calculateTimes(values, s1, 15L);
        assertEquals(15L, values.getFOH());
        assertEquals(15L, values.getPH());

        DeviceState s2 = DeviceState.builder().scope(ScopeType.INNIO_BOP).ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).build();
        kpiIeee.calculateTimes(values, s2, 15L);
        assertEquals(15L, values.getFOH());
        assertEquals(30L, values.getPH());

        DeviceState s3 = DeviceState.builder().scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.FORCEDOUTAGE_MTBFO_REL).build();
        kpiIeee.calculateTimes(values, s3, 15L);
        assertEquals(30L, values.getFOH());
        assertEquals(45L, values.getPH());

        DeviceState s4 = DeviceState.builder().scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.DEACTIVATED_SHUTDOWN).build();
        kpiIeee.calculateTimes(values, s4, 15L);
        assertEquals(30L, values.getFOH());
        assertEquals(45L, values.getPH());

        DeviceState s5 = DeviceState.builder().scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).build();
        kpiIeee.calculateTimes(values, s5, 15L);
        assertEquals(30L, values.getFOH());
        assertEquals(60L, values.getPH());
        assertEquals(15L, values.getUMH());

        DeviceState s6 = DeviceState.builder().scope(ScopeType.INNIO_Genset).ieeeState(IeeeStates.PLANNED_OUTAGE).build();
        kpiIeee.calculateTimes(values, s6, 15L);
        assertEquals(30L, values.getFOH());
        assertEquals(75L, values.getPH());
        assertEquals(15L, values.getUMH());
        assertEquals(15L, values.getPMH());

        DeviceState s7 = DeviceState.builder().scope(ScopeType.Partner).ieeeState(IeeeStates.PLANNED_OUTAGE).build();
        kpiIeee.calculateTimes(values, s7, 15L);
        assertEquals(30L, values.getFOH());
        assertEquals(90L, values.getPH());
        assertEquals(15L, values.getUMH());
        assertEquals(15L, values.getPMH());

        assertEquals(1.0 - (30.0 / 90.0), values.getIeeeRel(), 0);
        assertEquals(1.0 - ((30.0 + 15.0 + 15.0) / 90.0), values.getIeeeAv(), 0);
    }

    @Test
    public void calculateIeeeMtbfo() {
        ValuesInPeriod values = new ValuesInPeriod();

        DeviceState s1 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).build();
        kpiIeee.calculateTimes(values, s1, 15L);
        assertEquals(15L, values.getAOH());
        assertEquals(15L, values.getOH());

        DeviceState s2 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.PLANNED_OUTAGE).build();
        kpiIeee.calculateTimes(values, s2, 15L);
        assertEquals(15L, values.getAOH());
        assertEquals(30L, values.getOH());

        values.setCntFOO(2);
        assertEquals(7.5, values.getIeeeMtbfo(), 0);
    }


    @Test
    public void calculateIeeeStartRel() {
        ValuesInPeriod values = new ValuesInPeriod();

        Start s1 = Start.builder().validStart(1).validStartGCB(1).build();
        kpiIeee.countStarts(values, s1);
        assertEquals(1, values.getCntTNS());

        Start s2 = Start.builder().validStart(0).validStartGCB(0).excluded(1).build();
        kpiIeee.countStarts(values, s2);
        assertEquals(2, values.getCntTNS());
        assertEquals(1, values.getCntNES());

        Start s3 = Start.builder().validStart(0).validStartGCB(0).failedStart(1).build();
        kpiIeee.countStarts(values, s3);
        assertEquals(3, values.getCntTNS());
        assertEquals(1, values.getCntNES());
        assertEquals(0, values.getCntNFS());

        //values.setCntFOO(2);


        assertEquals(1, values.getIeeeStartRel(), 0);
    }

    @Test
    public void countOutagesWithoutUnplannedMaintenanceFromDeviceState() {
        ValuesInPeriod values = new ValuesInPeriod();

        DeviceState s1 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).build();
        kpiIeee.countOutages(values, s1);
        assertEquals(0, values.getCntOutages());

        DeviceState s2 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.PLANNED_OUTAGE).build();
        kpiIeee.countOutages(values, s2);

        DeviceState s3 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).build();
        kpiIeee.countOutages(values, s3);

        assertEquals(2, values.getCntOutages());
    }

    @Test
    public void countOutagesFromDeviceState() {
        ValuesInPeriod values = new ValuesInPeriod();

        DeviceState s1 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.AVAILABLE).build();
        kpiIeee.countOutagesWithoutUnplannedMaintenance(values, s1);
        assertEquals(0, values.getCntOutages());

        DeviceState s2 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.PLANNED_OUTAGE).build();
        kpiIeee.countOutagesWithoutUnplannedMaintenance(values, s2);

        DeviceState s3 = DeviceState.builder().actionActual(EngineAction.MAINS_PARALLEL_OPERATION).ieeeState(IeeeStates.UNPLANNED_MAINTENANCE).build();
        kpiIeee.countOutagesWithoutUnplannedMaintenance(values, s3);

        assertEquals(1, values.getCntOutagesWithoutUnplannedMaintenance());
    }
}