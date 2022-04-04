package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

public enum ScopeType {
    None(0),
    INNIO_Genset(1),
    INNIO_BOP(2),
    Unclear(3),
    Partner(4),
    Customer(5);

    private final Integer intValue;

    ScopeType(int value) {
        intValue =value;
    }

    //@JsonCreator
    public static ScopeType getNameByValue(final int value) {
        for (final ScopeType s: ScopeType.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ScopeType getNameByValue(final String value) {
        if(StringUtils.isEmpty(value))
            return ScopeType.None;
        value.replace("-","_");
        for (final ScopeType s: ScopeType.values()) {
            if (s.getStringValue().equals(value)) {
                return s;
            }
        }
        return null;
    }

    @JsonValue
    public String getStringValue(){
        if(intValue == 0)
            return "";
        return (getNameByValue(intValue).name()).replace("_", "-").replace("None","");
    }

    //@JsonValue
    public int getValue()
    {
        return intValue;
    }

}
