package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum SplitType {
    Daily(1),
    Monthly(2),
    StartEndDaily(3);

    private final Integer intValue;

    SplitType(int value) {
        intValue =value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static SplitType getNameByValue(final int value) {
        for (final SplitType s: SplitType.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    @JsonValue
    public int getValue()
    {
        return intValue;
    }
}
