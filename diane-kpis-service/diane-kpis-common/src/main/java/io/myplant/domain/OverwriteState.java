package io.myplant.domain;

import io.myplant.model.IeeeStates;
import io.myplant.model.ScopeType;
import lombok.*;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude="id")
@Entity
@Table(name = "overwrite_state")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverwriteState  implements Comparable{
    @Id
    @GeneratedValue
    private Long id;

//    private String model;
//    private String serial;

    private long assetId;


    private int actionActual;

    private long actionFrom;
    private long actionTo;

    private Long triggerDate;
    private Integer triggerMsgNo;
    //private String triggerText;

    private Long duration;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private ScopeType scope;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private IeeeStates ieeeState;

    private String description;

    @Override
    public int compareTo(Object o) {
        return Long.compare(actionFrom, ((OverwriteState)o).getActionFrom());
    }
}
