package io.myplant.domain;

import io.myplant.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude="id")
//@Entity
//@Table(name = "state_machine")
@AllArgsConstructor
@Builder(toBuilder=true)
public class DeviceState implements Comparable{
    @Id
    @GeneratedValue
    private Long id;

    private long assetId;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private EngineAction actionActual;

    private long actionFrom;
    private long actionTo;

    private Long triggerDate;
    private long triggerMsgNo;
    private long triggerCount;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private DemandSelectorSwitchStates aws;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private ServiceSelectorSwitchStates bws;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private AvailableStates avss;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private AvailableStates availableState;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private IeeeStates ieeeState;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private IeeeStates kielVzState;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private IeeeStates kielVuState;

    private long outageNumber;

    public long getDuration(){
        long duration = actionTo - actionFrom;
        if(duration < 0)
            return 0;
        return duration;
    }

    private long OH;
    private long CumOH;
    private long AOH;
    private long CumAOH;
    private long PH;
    private long CumPH;
    private long OHatLastFOO;
    private long HSLF;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private ScopeType scope;

    @Transient
    private Long overwriteId;
    @Transient
    private String description;


    public DeviceState() {
    }

    public DeviceState(long assetId, EngineAction actionActual, long actionFrom, long actionTo, Long triggerDate, long triggerMSGNo, String triggerText, String triggerResponsibility, long triggerCount
            , DemandSelectorSwitchStates aws, ServiceSelectorSwitchStates bws, AvailableStates avss) {
        this.assetId = assetId;
        this.actionActual = actionActual;
        this.actionFrom = actionFrom;
        this.actionTo = actionTo;
        this.triggerDate = triggerDate;
        this.triggerMsgNo = triggerMSGNo;
        this.triggerCount = triggerCount;
        this.aws = aws;
        this.bws = bws;
        this.avss = avss;
        this.scope = ScopeType.None;
    }

    @Override
    public int compareTo(Object o) {
        return Long.compare(actionFrom, ((DeviceState)o).getActionFrom());
    }

    public IeeeStates outageOfType(StateType type){
        switch (type){
            case VU: return kielVuState;
            case VZ: return kielVzState;
            default: return ieeeState;
        }
    }
}
