package io.myplant.model;

import io.myplant.domain.OverwriteState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(exclude="id")
@NoArgsConstructor
public class OverwriteStateDto {
    private Long id;
    private long assetId;

    private String model;
    private String serial;

    private int actionActual;

    private long actionFrom;
    private long actionTo;

    private Long triggerDate;
    private Integer triggerMsgNo;
    private String triggerText;

    private Long duration;

    private String scope;
    private IeeeStates ieeeState;

    private String description;

    public OverwriteStateDto(OverwriteState state, String model, String serial) {
        id = state.getId();
        this.assetId = getAssetId();
        this.model = model;
        this.serial = serial;
        this.actionActual = state.getActionActual();
        this.actionFrom = state.getActionFrom();
        this.actionTo = state.getActionTo();
        this.triggerDate = state.getTriggerDate();
        this.triggerMsgNo = state.getTriggerMsgNo();
        this.duration = state.getDuration();
        this.scope = state.getScope().name();
        this.ieeeState = state.getIeeeState();
        this.description = state.getDescription();
    }
}
