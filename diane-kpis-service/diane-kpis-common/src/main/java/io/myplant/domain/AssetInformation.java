package io.myplant.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.myplant.Constants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@Data
@Entity
@Table(name = "asset_information")
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder=true)
public class AssetInformation {
    @Id
    private long id;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String ramStartDate;
    @JsonInclude(JsonInclude.Include.ALWAYS)
    private String commissionDate;

    private String timezone;

    // 0 locally controlled
    // 1 remotly controlled
    private Integer avCalcType;

    @Transient
    public boolean isKiel(){
        return Constants.KIEL_FLEET_IDS.contains(id);
    };
}
