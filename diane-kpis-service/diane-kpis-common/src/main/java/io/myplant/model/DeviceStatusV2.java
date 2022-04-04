package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceStatusV2 {
    private EngineAction ActionActual = EngineAction.UNDEFINED;

    private long ActionFrom;
    private long ActionTo;

    private long TriggerDate;
    private long TriggerMSGNo;
    private String TriggerText;
    private String TriggerResponsibility;
    private long TriggerCount;

    @JsonProperty("dss")
    private DemandSelectorSwitchStates DemandSelectorSwitch;
    @JsonProperty("sss")
    private ServiceSelectorSwitchStates ServiceSelectorSwitch;
    @JsonProperty("avss")
    private AvailableStates AV_MAN_Activated_Status;

    public String toString() {
        return "DeviceStatusV2(ActionActual=" + this.getActionActual() + ", ActionFrom=" + new Date(this.getActionFrom()) + ", actionTo=" + new Date(this.getActionTo())
                + ", TriggerDate=" + new Date(this.getTriggerDate()) + ", TriggerMSGNo=" + this.getTriggerMSGNo()
                + ", DemandSelectorSwitch=" + this.getDemandSelectorSwitch() + ", ServiceSelectorSwitch=" + this.getServiceSelectorSwitch()
                + ", AV_MAN_Activated_Status=" + this.getAV_MAN_Activated_Status() + ")";
    }
}
