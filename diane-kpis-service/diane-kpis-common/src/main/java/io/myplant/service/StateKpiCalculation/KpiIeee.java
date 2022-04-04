package io.myplant.service.StateKpiCalculation;

import io.myplant.domain.DeviceState;
import io.myplant.domain.Start;
import io.myplant.model.EngineAction;
import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import io.myplant.model.StateType;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class KpiIeee {

    // OH – Operating hours, sum up if (Action Actual is “RampUp_Netzparallel” or “Betrieb” or “Netzparallel”
    // or “RampUp_Insel” or “Inselbetrieb” or “Rampdown”)
    private static List<EngineAction> operatingStates = Arrays.asList(EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION, EngineAction.OPERATION
            , EngineAction.MAINS_PARALLEL_OPERATION, EngineAction.RAMPUP_ISLAND_OPERATION
            , EngineAction.ISLAND_OPERATION, EngineAction.LOAD_RAMPDOWN);

    public static boolean isOperatingAction(EngineAction action){
        return operatingStates.contains(action);
    }

    public ValuesInPeriod calculateTimes(ValuesInPeriod valuesInPeriod, DeviceState state, long stateDuration) {

        // FOH - Forced Outage Hours (cannot be postponed beyond the end of the next weekend - IEEE definition!)
        // sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)” or
        // “Forced outage (operation -MTBFO&REL)” IEEE status and Scope = “INNIO-Genset”. Redundant trips joining should be
        // applied per “Joining redundant outages into one event” section.
        if (IeeeStates.isForcedOutage(state.getIeeeState()) && isINNIO_Genset(state.getScope()))
            valuesInPeriod.addFOH(stateDuration);

        // PH - Period Hours (period when engine is monitored)
        // sum up all calendar hours except of the IEEE status “Deactivated Shutdown”
        if (state.getIeeeState() != IeeeStates.DEACTIVATED_SHUTDOWN)
            valuesInPeriod.addPH(stateDuration);

        // UMH - Unplanned Maintenance Hours (can be postponed beyond the end of the next weekend - IEEE definition!)
        // sum up calendar time (hours) when the engine was in “Unplanned Maintenance” IEEE status and
        // Scope = “INNIO-Genset”. Redundant events joining should be applied per “Joining redundant outages into one event” section.
        if (state.getIeeeState() == IeeeStates.UNPLANNED_MAINTENANCE && isINNIO_Genset(state.getScope()))
            valuesInPeriod.addUMH(stateDuration);

        // PMH - Planned Maintenance Hours (is part of Maintenance Plan)
        // sum up calendar time (hours) when the engine was in “Planned Outage” IEEE status and Scope = “INNIO-Genset”.
        if (state.getIeeeState() == IeeeStates.PLANNED_OUTAGE && isINNIO_Genset(state.getScope()))
            valuesInPeriod.addPMH(stateDuration);

        // OH – Operating hours, sum up if (Action Actual is “RampUp_Netzparallel” or “Betrieb” or “Netzparallel”
        // or “RampUp_Insel” or “Inselbetrieb” or “Rampdown”)
        if (operatingStates.contains(state.getActionActual()))
            valuesInPeriod.addOH(stateDuration);

        // AOH - Available Operating Hours (oper. hours for customer; this excludes oper. hours, which are part of maintenance, test runs, etc.)
        // sum up if (Action Actual is “RampUp_Netzparallel” or “Betrieb” or “Netzparallel” or
        // “RampUp_Insel” or “Inselbetrieb” or “Rampdown”) and IEEE Status is “Available”
        if (operatingStates.contains(state.getActionActual()) && state.getIeeeState() == IeeeStates.AVAILABLE)
            valuesInPeriod.addAOH(stateDuration);

        // FOHKielVu – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)” or
        // “Forced outage (operation -MTBFO&REL)” in “Kiel Vu Status” column and Scope = “INNIO-Genset” or “INNIO-BOP” or
        // “Partner”. No trips joining should be applied.
        if (IeeeStates.isForcedOutage(state.getKielVuState()) &&
                (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP
                        || state.getScope() == ScopeType.Partner))
            valuesInPeriod.addFOH_KielVu(stateDuration);

        // FOHKielVuGE – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)”
        // or “Forced outage (operation -MTBFO&REL)” in “Kiel Vu Status” column and Scope = “INNIO-Genset” or “INNIO-BOP”.
        // No trips joining should be applied.
        if (IeeeStates.isForcedOutage(state.getKielVuState()) &&
                (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP))
            valuesInPeriod.addFOH_KielVuInnio(stateDuration);

        // UMHKielVu – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” in “Kiel Vu Status”
        // column and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”. No joining of events should be applied.
        if (state.getKielVuState() == IeeeStates.UNPLANNED_MAINTENANCE
                && (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP
                || state.getScope() == ScopeType.Partner))
            valuesInPeriod.addUMH_KielVu(stateDuration);

        // UMHKielVuGE – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” in “Kiel Vu Status”
        // column and Scope = “INNIO-Genset” or “INNIO-BOP”. No joining of events should be applied.
        if (state.getKielVuState() == IeeeStates.UNPLANNED_MAINTENANCE
                && (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP))
            valuesInPeriod.addUMH_KielVuInnio(stateDuration);

        // FOHKielVz – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)” or
        // “Forced outage (operation -MTBFO&REL)” in “Kiel Vz Status” column and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”.
        if (IeeeStates.isForcedOutage(state.getKielVzState()) &&
                (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP
                        || state.getScope() == ScopeType.Partner))
            valuesInPeriod.addFOH_KielVz(stateDuration);

        // UMHKielVz – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” per “Kiel Vz Status”
        // column and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”.
        if (state.getKielVzState() == IeeeStates.UNPLANNED_MAINTENANCE
                && (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP
                || state.getScope() == ScopeType.Partner))
            valuesInPeriod.addUMH_KielVz(stateDuration);

        // FOHKielVzGE – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)”
        // or “Forced outage (operation -MTBFO&REL)” in “Kiel Vu Status” column and Scope = “INNIO-Genset” or “INNIO-BOP”.
        // No trips joining should be applied.
        if (IeeeStates.isForcedOutage(state.getKielVzState()) &&
                (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP))
            valuesInPeriod.addFOH_KielVzInnio(stateDuration);

        // UMHKielVzGE – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” in “Kiel Vz Status”
        // column and Scope = “INNIO-Genset” or “INNIO-BOP”. No joining of events should be applied.
        if (state.getKielVzState() == IeeeStates.UNPLANNED_MAINTENANCE
                && (isINNIO_Genset(state.getScope()) || state.getScope() == ScopeType.INNIO_BOP))
            valuesInPeriod.addUMH_KielVzInnio(stateDuration);


        return valuesInPeriod;
    }

    public boolean isCountableIeeeOutages(DeviceState state) {
        // FOO - Forced Outage during Operation (during AOH)
        // count all FOO i.e. “Forced outage (operation -MTBFO&REL)” outages, where Scope = “INNIO-Genset”.
        // Note: 1 FOO may be multiple records in State Machine. Redundant trips joining should be applied per
        // “Joining redundant outages into one event” section.
        return state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL && isINNIO_Genset(state.getScope());
    }

    public ValuesInPeriod countIeeeOutages(ValuesInPeriod valuesInPeriod, DeviceState state) {
        // FOO - Forced Outage during Operation (during AOH)
        // count all FOO i.e. “Forced outage (operation -MTBFO&REL)” outages, where Scope = “INNIO-Genset”.
        // Note: 1 FOO may be multiple records in State Machine. Redundant trips joining should be applied per
        // “Joining redundant outages into one event” section.
        if (state.getIeeeState() == IeeeStates.FORCEDOUTAGE_MTBFO_REL && isINNIO_Genset(state.getScope()))
            valuesInPeriod.incCntFOO();

        return valuesInPeriod;
    }

    public boolean isINNIO_Genset(ScopeType scope) {
        return scope == ScopeType.INNIO_Genset || scope == ScopeType.Unclear;
    }

    public ValuesInPeriod countStarts(ValuesInPeriod valuesInPeriod, Start start) {

        // TNS – Total Number of Starts
        valuesInPeriod.incCntTNS();

        // NES – Number of Excluded Starts
        if (start.getExcluded() == 1)
            valuesInPeriod.incCntNES();

        if (start.getExcludedVu() == 1)
            valuesInPeriod.incCntNES_Kiel();

        // NFS – Number of Failed Starts
        // NFS – if after a valid start attempt (start not excluded) the IEEE Status changes from
        // Available to
        // “Forced outage (Reliability)” and the Scope for the outage is “INNIO-Genset”, then the start
        // is considered as
        // a failed one for this metric. Note for information only: this approach treats 3 consecutive
        // automatic start
        // attempts as 1 start and only a trip alarm is treated as a failed start or fail of the 3
        // consecutive start
        // (which always results in a trip alarm too).
        // TODO: ask mariusz
        if (start.getValidStart() == 1 && start.getFailedStart() == 1) {
            valuesInPeriod.incCntNFS();
            valuesInPeriod.incCntNFS_Kiel();
        }

        return valuesInPeriod;
    }

    public ValuesInPeriod countOutages(ValuesInPeriod valuesInPeriod, DeviceState deviceState) {
        if (IeeeStates.isOutageForExport(deviceState.outageOfType(StateType.IEEE))) {
            valuesInPeriod.incCntOutages();
        }

        return valuesInPeriod;
    }

    public ValuesInPeriod countOutagesWithoutUnplannedMaintenance(ValuesInPeriod valuesInPeriod, DeviceState deviceState) {
        if (IeeeStates.isOutageForExportWithoutUnplannedMaintenance(deviceState.outageOfType(StateType.IEEE))) {
            valuesInPeriod.incCntOutagesWithoutUnplannedMaintenance();
        }

        return valuesInPeriod;
    }
}