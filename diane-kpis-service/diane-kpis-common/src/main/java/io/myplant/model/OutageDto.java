package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.myplant.Utils;
import io.myplant.domain.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OutageDto {
    private String model;
    private String serial;
    private String site;

    private long outageFrom;
    private long outageTo;

    private String outageFromGmt;
    private String outageToGmt;
    private String outageFromLocal;
    private String outageToLocal;

    private String timezone;
    private String timezoneOffset;

    private double duration;
    private long causalAlarm;
    private String causalAlarmText;
    private String scope;
    IeeeStates state;

    private long outageNumber;

    public OutageDto(DeviceState state, String model, String serial, String site, String timezone, boolean useDateString) {

        this.model = model;
        this.serial = serial;
        //this.assetId = assetId;
        this.site = site;

        this.outageFrom = state.getActionFrom();
        this.outageTo = state.getActionTo();
        this.duration = Utils.millisToHour(state.getDuration()); // in hours
        this.causalAlarm = state.getTriggerMsgNo();
        this.scope = state.getScope().getStringValue();
        this.state = state.getIeeeState();

        this.outageNumber = state.getOutageNumber();

        if (useDateString) {
            this.outageFromGmt = Utils.getTimeGmt(outageFrom);
            this.outageToGmt = Utils.getTimeGmt(outageTo);
            this.outageFromLocal = Utils.getTimeAtZone(outageFrom, timezone);
            this.outageToLocal = Utils.getTimeAtZone(outageTo, timezone);
        }
    }

    @JsonIgnore
    public String[] getCsvHeader() {
        return new String[]{"Site", "Serial", "timezone", "Outage from", "Outage to"
                , "Outage from GMT", "Outage to GMT", "Outage from Local", "Outage to Local"
                , "duration", "Responsibility", "Causal Alarm Code", "Causal Alarm Text"
                , "State", "OutageNumber"};
    }

    @JsonIgnore
    public String[] getCsvRow() {
        return new String[]{
                getSite(), getSerial(), getTimezone(), Long.toString(getOutageFrom()), Long.toString(getOutageTo())
                , getOutageFromGmt(), getOutageToGmt(), getOutageFromLocal(), getOutageToLocal()
                , Double.toString(getDuration()), getScope(), Long.toString(getCausalAlarm()), getCausalAlarmText()
                , getState().name(), Long.toString(getOutageNumber())};
    }
}