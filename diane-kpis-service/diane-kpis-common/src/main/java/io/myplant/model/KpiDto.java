package io.myplant.model;

import io.myplant.service.StateKpiCalculation.ValuesInPeriod;
import io.myplant.Utils;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class KpiDto {

    @ApiModelProperty(value = "Ieee Reliability")
    private double ieeeRel;

    @ApiModelProperty(value = "Ieee Availability")
    private double ieeeAv;

    @ApiModelProperty(value = "Ieee MTBFO")
    private double ieeeMtbfo;

    @ApiModelProperty(value = "Ieee Starting Reliability")
    private double ieeeStartRel;

    @ApiModelProperty(value = "Ieee Forced Outage during Operation")
    private long cntIeeeFOO;

    @ApiModelProperty(value = "Ieee Number of Valid Starts (i.e. starts not excluded due to maintenance work etc.)")
    private long cntIeeeNVS;

    @ApiModelProperty(value = "Available Operating Hours (oper. hours for customer; this excludes oper. hours, which are part of maintenance, test runs, etc.)")
    private double AOH;
    private double HSLF;

    private double kielVu;
    private double kielVz;
    private double kielVuInnio;
    private double kielVzInnio;

    @ApiModelProperty(value = "Kiel Starting Reliability")
    private double kielStartRel;

    @ApiModelProperty(value = "Valid Starts (same as NVS, except status from “Kiel Vu Status” column should be used (instead of IEEE Status))")
    private long cntKielNVS;

    @ApiModelProperty(value = "time running / total time")
    private double utilization;

    @ApiModelProperty(value = "Outages per day")
    private long cntOutagesPerDay;

    @ApiModelProperty(value = "Successful Starts per day")
    private long cntStartsPerDaySuccessful;

    @ApiModelProperty(value = "Unsuccessful Starts per day")
    private long cntStartsPerDayUnsuccessful;

    public KpiDto(ValuesInPeriod values) {
        ieeeRel = values.getIeeeRel();
        ieeeAv = values.getIeeeAv();
        ieeeMtbfo = Utils.millisToHour(values.getIeeeMtbfo());
        ieeeStartRel  = values.getIeeeStartRel();
        kielVu = values.getKielVu();
        kielVz = values.getKielVz();
        kielVuInnio = values.getKielVuInnio();
        kielVzInnio = values.getKielVzInnio();
        kielStartRel = values.getKielStartRel();

        AOH = Utils.millisToHour(values.getAOH());
        HSLF = Utils.millisToHour(values.getHSLF());
        cntIeeeFOO = values.getCntFOO();
        cntIeeeNVS = values.getCntNVS();
        cntKielNVS = values.getCntNVS_Kiel();

        utilization = values.getUtilization();
        cntOutagesPerDay = values.getCntOutages();
        cntStartsPerDaySuccessful = values.getSuccessfulStarts();
        cntStartsPerDayUnsuccessful = values.getCntNFS();
    }
}