package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// AWS
public enum DemandSelectorSwitchStates {
    UNDEFINED(0),
    OFF(2),
    ON(4),
    REMOTE(6);

    private final Integer intValue;

    DemandSelectorSwitchStates(int value) {
        intValue =value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static DemandSelectorSwitchStates getNameByValue(final int value) {
        for (final DemandSelectorSwitchStates s: DemandSelectorSwitchStates.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    //@JsonCreator
    public static DemandSelectorSwitchStates getNameByValue(final String value) {
        for (final DemandSelectorSwitchStates s: DemandSelectorSwitchStates.values()) {
            if (s.getStringValue().equals(value)) {
                return s;
            }
        }
        return null;
    }

    @JsonValue
    public String getStringValue(){
        return getNameByValue(intValue).name();
    }

    //@JsonValue
    public int getValue()
    {
        return intValue;
    }
}
