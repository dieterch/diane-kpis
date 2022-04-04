package io.myplant.domain;

import io.myplant.model.ScopeType;
import lombok.*;

import javax.persistence.*;

@Data
@EqualsAndHashCode(exclude="id")
@Entity
@Table(name = "starting_rel")
@RequiredArgsConstructor
@AllArgsConstructor
@Builder()
public class Start implements Comparable{
    @Id
    @GeneratedValue
    private Long id;

    private final long assetId;
    private final long startDate;

    @Enumerated
    @Column(columnDefinition = "smallint")
    private ScopeType scope;

    private int validStart;
    private int validStartGCB;
    private int failedStart;
    private int excluded;
    private int excludedVu;
    private String reason;

    private long timeToMainsParallel;

    private int tripsBeforeRampUpMainsParallel;
    private int tripsBeforeMainsParallel;
    private long triggerMSGNo;
    private String triggerText;
    private long outageNumber;

    @Override
    public int compareTo(Object o) {
        return Long.compare(startDate, ((Start)o).getStartDate());
    }
}
