package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.myplant.Utils;
import io.myplant.domain.DeviceState;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonPropertyOrder({"actionActual", "actionFrom", "actionTo", "triggerDate", "triggerMSGNo", "triggerText", "triggerCount"
        , "aws", "bws", "avss"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceStateDto {
    private String model;
    private String serial;
    private long assetId;

    private EngineAction actionActual;

    private AvailableStates availableState;

    private long actionFrom;
    private long actionTo;

    private String actionFromGmt;
    private String actionToGmt;
    private String actionFromLocal;
    private String actionToLocal;

    private long triggerDate;
    private long triggerMSGNo;
    private String triggerText;
    private long triggerCount;

    private DemandSelectorSwitchStates AWS;

    private ServiceSelectorSwitchStates BWS;

    private AvailableStates AVSS;

    private String day;
    private String timezone;
    private String timezoneOffset;
    private String scope;
    private IeeeStates ieeeState;
    private IeeeStates kielVzState;
    private IeeeStates kielVuState;

    private Long overwriteId;
    private String description;

    private String outageNumber;
    private int firstRow;
    private double duration;
    private double OH;
    private double CumOH;
    private double AOH;
    private double CumAOH;
    private double PH;
    private double CumPH;
    private double OHatLastFOO;
    private double HSLF;

    public DeviceStateDto(DeviceState state, String model, String serial, String timezone, boolean useDateString, String language) {
        this.model = model;
        this.serial = serial;
        this.assetId = state.getAssetId();
        this.actionActual = state.getActionActual();
        this.actionFrom = state.getActionFrom();
        this.actionTo = state.getActionTo();
        if (useDateString) {
            this.actionFromGmt = Utils.getTimeGmt(state.getActionFrom());
            this.actionToGmt = Utils.getTimeGmt(state.getActionTo());
            this.actionFromLocal = Utils.getTimeAtZone(state.getActionFrom(), timezone);
            this.actionToLocal = Utils.getTimeAtZone(state.getActionTo(), timezone);
        }

        this.timezoneOffset = Utils.getTimezoneOffset(state.getActionFrom(), timezone);
        this.timezone = timezone;
        this.triggerDate = state.getTriggerDate();
        this.triggerMSGNo = state.getTriggerMsgNo();
        this.triggerCount = state.getTriggerCount();
        this.AWS = state.getAws();
        this.BWS = state.getBws();
        this.AVSS = state.getAvss();
        this.scope = state.getScope().getStringValue();

        this.availableState = state.getAvailableState();
        this.ieeeState = state.getIeeeState();
        this.kielVzState = state.getKielVzState();
        this.kielVuState = state.getKielVuState();
        this.overwriteId = state.getOverwriteId();
        this.description = state.getDescription();

        this.outageNumber = state.getOutageNumber() == 0 ? "" : Long.toString(state.getOutageNumber());

        this.duration = Utils.millisToHour(state.getDuration());
        this.OH = state.getDuration() != 0 ? Utils.millisToHour(state.getOH()) : 0;
        this.CumOH = Utils.millisToHour(state.getCumOH());
        this.AOH = state.getDuration() != 0 ? Utils.millisToHour(state.getAOH()) : 0;
        this.CumAOH = Utils.millisToHour(state.getCumAOH());
        this.PH = state.getDuration() != 0 ? Utils.millisToHour(state.getPH()) : 0;
        this.CumPH = Utils.millisToHour(state.getCumPH());
        this.OHatLastFOO = Utils.millisToHour(state.getOHatLastFOO());
        this.HSLF = Utils.millisToHour(state.getHSLF());
    }

    public String toString() {
        return "DeviceStatusV2(actionActual=" + this.getActionActual() + ", actionFrom=" + new Date(this.getActionFrom()) + ", actionTo=" + new Date(this.getActionTo())
                + ", triggerDate=" + new Date(this.getTriggerDate()) + ", triggerMSGNo=" + this.getTriggerMSGNo()
                + ", DemandSelectorSwitch=" + this.getAWS() + ", ServiceSelectorSwitch=" + this.getBWS()
                + ", AV_MAN_Activated_Status=" + this.getAVSS() + ")";
    }

    @JsonIgnore
    public static String[] getCsvHeaders() {
        return new String[]{"serial", "timezone", "actionActual", "availableState"
                , "IEEEState", "VuState", "VzState"
                , "actionFrom", "actionTo", "actionFromGmt", "actionToGmt", "actionFromLocal", "actionToLocal", "TimeZoneOffset"
                , "Causal Alarm Code", "Causal Alarm Text", "Causal Alarm Count", "Responsibility"
                , "Demand Switch", "Service Switch", "RAM Switch"
                , "outageNumber", "firstRow", "duration", "OH", "CumOH", "AOH", "CumAOH", "PH", "CumPH", "OHatLastFOO", "HSLF"};
    }

    @JsonIgnore
    public String[] getCsvHeaderStrings() {
        return getCsvHeaders();
    }

    @JsonIgnore
    public String[] getCsvRow() {
        return new String[]{
                getSerial(), getTimezone(), getActionActual().name(), getAvailableState().name(),
                getIeeeState().name(), getKielVuState().name(), getKielVzState().name(),
                Long.toString(getActionFrom()), Long.toString(getActionTo()), getActionFromGmt(), getActionToGmt(), getActionFromLocal(), getActionToLocal(), getTimezoneOffset(),
                Long.toString(getTriggerMSGNo()), getTriggerText(), Long.toString(getTriggerCount()), getScope(),
                getAWS().name(), getBWS().name(), getAVSS().name(),
                getOutageNumber(), Integer.toString(getFirstRow()), Utils.doubleToString(duration), Utils.doubleToString(OH), Utils.doubleToString(CumOH), Utils.doubleToString(AOH), Utils.doubleToString(CumAOH),
                Utils.doubleToString(PH), Utils.doubleToString(CumPH), Utils.doubleToString(OHatLastFOO), Utils.doubleToString(HSLF)
        };
    }
}