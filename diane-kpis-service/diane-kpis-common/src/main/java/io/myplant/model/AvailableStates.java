package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum AvailableStates {
    UNDEFINED(0),
    NOT_AVAILABLE(2),
    AVAILABLE(4),
    FORCEDOUTAGE(6),
    TROUBLESHOOTING(8),
    //TROUBLESHOOTING_FORCED(9),
    MAINTENANCE(10),
    //MAINTENANCE_FORCED(11),
    DEACTIVATED(12);
    //DEACTIVATED_FORCED(13);

    private final Integer intValue;

    AvailableStates(int value) {
        intValue =value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static AvailableStates getNameByValue(final int value) {
        for (final AvailableStates s: AvailableStates.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    //@JsonCreator
    public static AvailableStates getNameByValue(final String value) {
        for (final AvailableStates s: AvailableStates.values()) {
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

//    public static boolean isTroubleshooting(AvailableStates state){
//        return state == AvailableStates.TROUBLESHOOTING || state == AvailableStates.TROUBLESHOOTING_FORCED;
//    }

    public boolean isTroubleshooting(){
        return intValue == AvailableStates.TROUBLESHOOTING.intValue;// || intValue == AvailableStates.TROUBLESHOOTING_FORCED.intValue;
    }

//    public static boolean isMaintenance(AvailableStates state){
//        return state == AvailableStates.MAINTENANCE || state == AvailableStates.MAINTENANCE_FORCED;
//    }

    public boolean isMaintenance(){
        return intValue == AvailableStates.MAINTENANCE.intValue; // || intValue == AvailableStates.MAINTENANCE_FORCED.intValue;
    }

//    public static boolean isDeactivated(AvailableStates state){
//        return state == AvailableStates.DEACTIVATED || state == AvailableStates.DEACTIVATED_FORCED;
//    }

    public boolean isDeactivated(){
        return intValue == AvailableStates.DEACTIVATED.intValue;// || intValue == AvailableStates.DEACTIVATED_FORCED.intValue;
    }
}
