package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.myplant.Utils;
import io.myplant.domain.Start;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StartDto {

    private String model;
    private String serial;
    private long assetId;
    private long startDate;
    private String startDateGmt;
    private String startDateLocal;

    private long timeToMainsParallel;
    private int tripsBeforeRampUpMainsParallel;
    private int tripsBeforeMainsParallel;

    private int validStartTargetLoad;
    private int validStartGCB;
    private int failedStart;
    private int excluded;
    private int excludedVu;

    private String reason;
    private String scope;

    private long triggerMSGNo;
    private String triggerText;

    private String outageNumber;

    public StartDto(String model, String serial, Start start, boolean useDateString, String timezone) {

        this.model = model;
        this.serial = serial;
        this.assetId = start.getAssetId();
        this.startDate = start.getStartDate();
        this.scope = start.getScope() != null ? start.getScope().getStringValue() : "";
        this.validStartTargetLoad = start.getValidStart();
        this.validStartGCB = start.getValidStartGCB();
        this.failedStart = start.getFailedStart();
        this.excluded = start.getExcluded();
        this.excludedVu = start.getExcludedVu();
        this.reason = start.getReason();
        this.timeToMainsParallel = start.getTimeToMainsParallel();
        this.tripsBeforeRampUpMainsParallel = start.getTripsBeforeRampUpMainsParallel();
        this.tripsBeforeMainsParallel = start.getTripsBeforeMainsParallel();
        this.triggerMSGNo = start.getTriggerMSGNo();
        this.triggerText = start.getTriggerText();

        this.outageNumber = start.getOutageNumber() == 0 ? "": Long.toString(start.getOutageNumber());

        if(useDateString) {
            this.startDateGmt = Utils.getTimeGmt(startDate);
            this.startDateLocal = Utils.getTimeAtZone(startDate, timezone);
        }
    }


    @JsonIgnore
    public String[] getCsvHeader() {
        return new String[] {"Serial","startDate","Date/Time Gmt","Date/Time Local","Responsibility"
                ,"Valid Start GCB Close","Valid Start Target Load"
                ,"Exluded", "Exluded Vu"
                ,"Time To \"Mains Parallel\"","Trip before \"RampUp MainsParallel\"","Trip before \"Mains Parallel\""
                ,"Causal Alarm Code","Causal Alarm Text", "Reason", "outageNumber"};
        }


    @JsonIgnore
    public String[] getCsvRow() {
        return new String[] {
                getSerial(),Long.toString(getStartDate()), getStartDateGmt(), getStartDateLocal(), getScope(),
                Long.toString(getValidStartGCB()), Long.toString(getValidStartTargetLoad()),
                Long.toString(getExcluded()), Long.toString(getExcludedVu()),
                Long.toString((getTimeToMainsParallel()/1000)), Long.toString(getTripsBeforeRampUpMainsParallel()), Long.toString(getTripsBeforeMainsParallel()),
                Long.toString(getTriggerMSGNo()),getTriggerText(),getReason(),getOutageNumber()};
    }
}
