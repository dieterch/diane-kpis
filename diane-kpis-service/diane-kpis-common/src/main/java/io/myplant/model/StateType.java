package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StateType {
    IEEE(0),
    VU(5),
    VZ(10);

    private final Integer intValue;

    StateType(int value) {
        intValue = value;
    }

    public static StateType getNameByValue(final int value) {
        for (final StateType s : StateType.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static StateType getNameByValue(final String strValue) {
        return getNameByValue(Integer.parseInt(strValue));
    }

    @JsonValue
    public int getValue() {
        return intValue;
    }
}
