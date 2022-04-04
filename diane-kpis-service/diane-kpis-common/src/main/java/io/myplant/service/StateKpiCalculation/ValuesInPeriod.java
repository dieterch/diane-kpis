package io.myplant.service.StateKpiCalculation;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ValuesInPeriod {
    public static final long millisPerDay = 24*60*60*1000L;

    private int daysInPeriod = 1;

    private long HSLF;

    // FOH - Forced Outage Hours (cannot be postponed beyond the end of the next weekend - IEEE definition!)
    // sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)” or
    // “Forced outage (operation -MTBFO&REL)” IEEE status and Scope = “INNIO-Genset”. Redundant trips joining should be
    // applied per “Joining redundant outages into one event” section.
    private long FOH;

    public synchronized long addFOH(long timespan) {
        FOH += timespan;
        return FOH;
    }

    // PH - Period Hours (period when engine is monitored)
    // sum up all calendar hours except of the IEEE status “Deactivated Shutdown”
    private long PH;

    public synchronized long addPH(long timespan) {
        PH += timespan;
        return PH;
    }

    // UMH - Unplanned Maintenance Hours (can be postponed beyond the end of the next weekend - IEEE definition!)
    // sum up calendar time (hours) when the engine was in “Unplanned Maintenance” IEEE status and
    // Scope = “INNIO-Genset”. Redundant events joining should be applied per “Joining redundant outages into one event” section.
    private long UMH;

    public synchronized long addUMH(long timespan) {
        UMH += timespan;
        return UMH;
    }

    // PMH - Planned Maintenance Hours (is part of Maintenance Plan)
    // sum up calendar time (hours) when the engine was in “Planned Outage” IEEE status and Scope = “INNIO-Genset”.
    private long PMH;

    public synchronized long addPMH(long timespan) {
        PMH += timespan;
        return PMH;
    }

    // OH – Operating hours, sum up if (Action Actual is “RampUp_Netzparallel” or “Betrieb” or “Netzparallel”
    // or “RampUp_Insel” or “Inselbetrieb” or “Rampdown”)
    private long OH;

    public synchronized long addOH(long timespan) {
        OH += timespan;
        return OH;
    }

    // AOH - Available Operating Hours (oper. hours for customer; this excludes oper. hours, which are part of maintenance, test runs, etc.)
    // sum up if (Action Actual is “RampUp_Netzparallel” or “Betrieb” or “Netzparallel” or
    // “RampUp_Insel” or “Inselbetrieb” or “Rampdown”) and IEEE Status is “Available”
    private long AOH;

    public synchronized long addAOH(long timespan) {
        AOH += timespan;
        return AOH;
    }

    // AH - Available Hours (engine is available for the customer)
    // sum up calendar time (hours) where IEEE Status is “Available”.
    private long AH; // not needed ??


    // FOO - Forced Outage during Operation (during AOH)
    // count all FOO i.e. “Forced outage (operation -MTBFO&REL)” outages, where Scope = “INNIO-Genset”.
    // Note: 1 FOO may be multiple records in State Machine. Redundant trips joining should be applied per
    // “Joining redundant outages into one event” section.
    private long cntFOO;

    public synchronized long incCntFOO() {
        return ++cntFOO;
    }

    // FO – Forced Outage (either during Operation or any other state)
    // count all FO i.e. “Forced outage (operation -MTBFO&REL)” and “Forced outage (Reliability)” outages,
    // where Scope = “INNIO-Genset”. Note: 1 FO may be multiple records in State Machine.
    // Redundant trips joining should be applied per “Joining redundant outages into one event” section.
    private long cntFO; // not needed ??

    // NES – Number of Excluded Starts
    private long cntNES;

    public synchronized long incCntNES() {
        return ++cntNES;
    }

    // NES – Number of Excluded Starts (status from “Kiel Vu Status”)
    private long cntNES_Kiel;

    public synchronized long incCntNES_Kiel() {
        return ++cntNES_Kiel;
    }

    // TNS – Total Number of Starts
    private long cntTNS;

    public synchronized long incCntTNS() {
        return ++cntTNS;
    }

    // TNO – Total Number of Outages
    private long cntOutages;

    public synchronized long incCntOutages() {
        return ++cntOutages;
    }

    // TNO – Total Number of Outages without unplanned maintenance
    private long cntOutagesWithoutUnplannedMaintenance;

    public synchronized long incCntOutagesWithoutUnplannedMaintenance() {
        return ++cntOutagesWithoutUnplannedMaintenance;
    }

    public double getOutagesPerDayMonthly() {
        return cntOutages / (double) daysInPeriod;
    }

    public double getOutagesPerDayWithoutUnplannedMaintenanceMonthly() {
        return cntOutagesWithoutUnplannedMaintenance / (double) daysInPeriod;
    }

    // NFS – Number of Failed Starts
    // NFS – if after a valid start attempt (start not excluded) the IEEE Status changes from Available to
    // “Forced outage (Reliability)” and the Scope for the outage is “INNIO-Genset”, then the start is considered as
    // a failed one for this metric. Note for information only: this approach treats 3 consecutive automatic start
    // attempts as 1 start and only a trip alarm is treated as a failed start or fail of the 3 consecutive start
    // (which always results in a trip alarm too).
    private long cntNFS;

    public synchronized long incCntNFS() {
        return ++cntNFS;
    }

    // NFSSL – Number of Failed Starts until Set Load is achieved. NFSSL = NFS + NFSGCB-SL
    private long cntNFS_SL; // not needed ??

    // NFSGCB-SL – Number of Failed Starts (IEEE Status = “Forced outage (Reliability)” and Scope = “INNIO-Genset”)
    // from GCB close message until Set Load is achieved. Use new “3691: Target load reached” message from AMM.
    private long cntNFS_GCB_SL; // not needed ??

    // AOHSL – AOH calculated from 3691: Target load reached time to GCB Open. Use new “3691: Target load reached” message from AMM.
    private long cntAOH_SL; // not needed ??


    // FOOSL – #FOO during AOHSL. Redundant trips joining should be applied per “Joining redundant outages into one event” section.
    private long cntFOO_SL; // not needed ??

    // FOOGE-BOP - Count “Forced outage (operation -MTBFO&REL)” outages where Scope = “INNIO-BOP”. Note: 1 FOOGE-BOP may
    // be multiple records in State Machine. Redundant trips joining should be applied per “Joining redundant outages into one event”
    // section.
    private long cntFOO_Innio_BOP; // not needed ??

    // FOHKielVu – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)” or
    // “Forced outage (operation -MTBFO&REL)” in “Kiel Vu Status” column and Scope = “INNIO-Genset” or “INNIO-BOP” or
    // “Partner”. No trips joining should be applied.
    private long FOH_KielVu;

    public synchronized long addFOH_KielVu(long timespan) {
        FOH_KielVu += timespan;
        return FOH_KielVu;
    }

    // FOHKielVuGE – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)”
    // or “Forced outage (operation -MTBFO&REL)” in “Kiel Vu Status” column and Scope = “INNIO-Genset” or “INNIO-BOP”.
    // No trips joining should be applied.
    private long FOH_KielVuInnio;

    public synchronized long addFOH_KielVuInnio(long timespan) {
        FOH_KielVuInnio += timespan;
        return FOH_KielVuInnio;
    }

    // UMHKielVu – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” in “Kiel Vu Status”
    // column and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”. No joining of events should be applied.
    private long UMH_KielVu;

    public synchronized long addUMH_KielVu(long timespan) {
        UMH_KielVu += timespan;
        return UMH_KielVu;
    }

    // UMHKielVuGE – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” in “Kiel Vu Status”
    // column and Scope = “INNIO-Genset” or “INNIO-BOP”. No joining of events should be applied.
    private long UMH_KielVuInnio;

    public synchronized long addUMH_KielVuInnio(long timespan) {
        UMH_KielVuInnio += timespan;
        return UMH_KielVuInnio;
    }

    // FOHKielVz – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)” or
    // “Forced outage (operation -MTBFO&REL)” in “Kiel Vz Status” column and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”.
    private long FOH_KielVz;

    public synchronized long addFOH_KielVz(long timespan) {
        FOH_KielVz += timespan;
        return FOH_KielVz;
    }

    // UMHKielVz – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” per “Kiel Vz Status”
    // column and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”.
    private long UMH_KielVz;

    public synchronized long addUMH_KielVz(long timespan) {
        UMH_KielVz += timespan;
        return UMH_KielVz;
    }

    // FOHKielVzGE – sum up calendar time (hours) when the engine was either in “Forced outage (Reliability)”
    // or “Forced outage (operation -MTBFO&REL)” in “Kiel Vz Status” column and Scope = “INNIO-Genset” or “INNIO-BOP”.
    // No trips joining should be applied.
    private long FOH_KielVzInnio;

    public synchronized long addFOH_KielVzInnio(long timespan) {
        FOH_KielVzInnio += timespan;
        return FOH_KielVzInnio;
    }

    // UMHKielVzGE – sum up calendar time (hours) when the engine was in “Unplanned Maintenance” in “Kiel Vz Status”
    // column and Scope = “INNIO-Genset” or “INNIO-BOP”. No joining of events should be applied.
    private long UMH_KielVzInnio;

    public synchronized long addUMH_KielVzInnio(long timespan) {
        UMH_KielVzInnio += timespan;
        return UMH_KielVzInnio;
    }

    private long daysSinceLastOutage;

    private long totalStartsCount;

    // NVSKiel – same as NVS, except status from “Kiel Vu Status” column should be used (instead of IEEE Status)
    // so that no outage joining is applied.
    // NVS - Number of Valid Starts (i.e. starts not excluded due to maintenance work etc.)
    // NVS = TNS - NES. See “Excluded Starts” section for exclusion reasons.
    public long getCntNVS_Kiel() {
        return getCntTNS() - getCntNES_Kiel();
    }

    // NFSKiel – Number of failed starts per Kiel contract - engine trip (Kiel Vu Status = “Forced outage (Reliability)”
    // and Scope = “INNIO-Genset” or “INNIO-BOP” or “Partner”) until “3691: Target load reached” message or, when fast start requested, but not meeting start time of 280s from “Demand on” till “3691: Target load reached” messages. Compare “3691: Target load reached” message with 2900 “Fast start requested - conditions met” and 2899 “Fast start requested - conditions not met” messages.
    private long cntNFS_Kiel;

    public synchronized long incCntNFS_Kiel() {
        return ++cntNFS_Kiel;
    }


    // NVS - Number of Valid Starts (i.e. starts not excluded due to maintenance work etc.)
    // NVS = TNS - NES. See “Excluded Starts” section for exclusion reasons.
    public long getCntNVS() {
        return getCntTNS() - getCntNES();
    }

    // NSS - Number of Successful Starts (Successful Start - change from Blockstart to Operation - GCB Closed)
    // NSS = NVS – NFS
    public long getCntNSS() {
        return getCntNVS() - getCntNFS();
    }

    // NSSKiel = NVSKiel – NFSKiel
    public long getCntNSS_Kiel() {
        return getCntNVS_Kiel() - getCntNFS_Kiel();
    }

    public long getSuccessfulStarts() {
        return cntTNS - cntNFS;
    }

    public double getSuccessfulStartsPerDayMonthly() {
        return getSuccessfulStarts() / (double) daysInPeriod;
    }

    public long getUnsuccessfulStarts() {
        return cntNFS;
    }

    public double getUnsuccessfulStartsPerDayMonthly() {
        return getUnsuccessfulStarts() / (double) daysInPeriod;
    }

    // Reliability IEEE/ISO = 1 - FOH / PH
    public double getIeeeRel() {
        return 1 - (double) getFOH() / (double) getPH();
    }

    // Availability IEEE/ISO = 1- (FOH + UMH + PMH) / PH
    public double getIeeeAv() {
        return 1 - (double) (getFOH() + getUMH() + getPMH()) / (double) getPH();
    }

    // MTBFO = AOH / #FOO (Mean Time Between Forced Outage)
    public double getIeeeMtbfo() {
        if (getCntFOO() == 0)
            return 0;
        return (double) getAOH() / (double) getCntFOO();
    }

    // Starting Reliability = NSS / NVS
    public double getIeeeStartRel() {
        if (getCntNVS() == 0)
            return 0;
        return (double) getCntNSS() / (double) getCntNVS();
    }

    // Vu = (FOHKielVu + UMHKielVu) / PH; Note: Without min 1hr downtime; bases on Period Hours (PH)
    public double getKielVu() {
        if (getPH() == 0)
            return 0;
        return (double) (getFOH_KielVu() + getUMH_KielVu()) / (double) getPH();
    }

    // Vu (INNIO only) = (FOHKielVuGE + UMHKielVuGE) / PH; Note: Excludes Partner (KAM), Without min 1hr downtime;
    // bases on Period Hours (PH)
    public double getKielVuInnio() {
        if (getPH() == 0)
            return 0;
        return (double) (getFOH_KielVuInnio() + getUMH_KielVuInnio()) / (double) getPH();
    }

    // Vz = 1 - (FOHKielVz + UMHKielVz) / OH; Note: With assumption of min 1hr downtime; bases on Operating Hours (OH).
    // Vz metric is much more demanding than Vu.
    public double getKielVz() {
        if (getOH() == 0)
            return 0;
        return 1 - (double) (getFOH_KielVz() + getUMH_KielVz()) / (double) getOH();
    }

    // Vz (INNIO only) = (FOHKielVzGE + UMHKielVzGE) / PH; Note: Excludes Partner (KAM), Without min 1hr downtime;
    // bases on Period Hours (PH)
    public double getKielVzInnio() {
        if (getPH() == 0)
            return 0;
        return 1 - (double) (getFOH_KielVzInnio() + getUMH_KielVzInnio()) / (double) getPH();
    }

    //Kiel Staring Reliability = NSSKiel / NVS
    public double getKielStartRel() {
        if (getCntNVS() == 0)
            return 0;
        return (double) getCntNSS_Kiel() / (double) getCntNVS();
    }

    public double getUtilization() {
        if (getOH() > 0) {
            return round(getOH() / (double) (daysInPeriod * 1000 * 60 * 60 * 24), 8);
        }
        return 0;
    }

    public double getUtilizationPerDayMonthly() {
        return getUtilization() / daysInPeriod;
    }


    public long connectivityTime;

    public long addConnectivity(long timespanInMilli) {
        return connectivityTime +=  timespanInMilli;
    }

    public double getConnectivity() {
        if (connectivityTime > 0) {
            return round(connectivityTime / (double) (daysInPeriod * millisPerDay), 8);
        }
        return 0;
    }

    public long connectivityOperating;

    public long addConnectivityOperating(long timespanInMilli) {
        return connectivityOperating +=  timespanInMilli;
    }

    public double getConnectivityOperating() {
        if (getOH() > 0) {
            return round(connectivityOperating / (double) getOH(), 8);
        }
        return 0;
    }



    // old calculation
    private long totalTime;
    private long forceOutage;
    private long unplannedMaintenance;
    private long serviceTime;
    private long tripCount;

    public double getAvailability() {
        if (totalTime > 0)
            return round(100.0 * (totalTime - (forceOutage + unplannedMaintenance)) / totalTime, 8);
        return 0;
    }

    public double getReliability() {
        if (totalTime > 0)
            return round(100.0 * (totalTime - forceOutage) / totalTime, 8);
        return 0;

    }


    public long getMtbfo() {
        if (tripCount > 0)
            return serviceTime / tripCount;
        return 0;
    }

    private double round(double value, int digits) {
        double factor = Math.pow(10, digits);
        return Math.round(value * factor) / factor;
    }
}