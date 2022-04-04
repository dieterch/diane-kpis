package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

// BWS
public enum ServiceSelectorSwitchStates {
    UNDEFINED(0),
    OFF(2),
    MAN(4),
    AUTO(6);

    private final Integer intValue;

    ServiceSelectorSwitchStates(int value) {
        intValue =value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static ServiceSelectorSwitchStates getNameByValue(final int value) {
        for (final ServiceSelectorSwitchStates s: ServiceSelectorSwitchStates.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    //@JsonCreator
    public static ServiceSelectorSwitchStates getNameByValue(final String value) {
        for (final ServiceSelectorSwitchStates s: ServiceSelectorSwitchStates.values()) {
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
