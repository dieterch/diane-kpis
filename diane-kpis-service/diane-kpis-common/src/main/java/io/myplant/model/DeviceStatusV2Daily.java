package io.myplant.model;

import io.myplant.service.StateKpiCalculation.StateUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class DeviceStatusV2Daily extends DeviceStatusV2 {
    private String day;

    public DeviceStatusV2Daily(DeviceStatusV2 state) {
        super(state.getActionActual(), state.getActionFrom(), state.getActionTo(),state.getTriggerDate()
                ,state.getTriggerMSGNo(),state.getTriggerText(), state.getTriggerResponsibility(), state.getTriggerCount()
                , state.getDemandSelectorSwitch(), state.getServiceSelectorSwitch(), state.getAV_MAN_Activated_Status());
        this.day = StateUtils.getDailyString(state.getActionFrom());
    }

    public DeviceStatusV2Daily(DeviceStatusV2 state, String dayKey) {
        super(state.getActionActual(), state.getActionFrom(), state.getActionTo(),state.getTriggerDate()
                ,state.getTriggerMSGNo(),state.getTriggerText(), state.getTriggerResponsibility(), state.getTriggerCount()
                , state.getDemandSelectorSwitch(), state.getServiceSelectorSwitch(), state.getAV_MAN_Activated_Status());
        this.day = dayKey;
    }

    @Override
    public void setActionFrom(long ActionFrom) {
        super.setActionFrom(ActionFrom);
        this.day = StateUtils.getDailyString(ActionFrom);
    }

    public String toString() {
        return super.toString() + " (" + this.day + ")";
    }
}
