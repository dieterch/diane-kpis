package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum EngineAction {
    UNDEFINED(0),
    DATA_GAP(2),
    START_PREPARATION(4),
    START(6),
    IDLE(8),
    SYNCHRONISATION(10),
    OPERATION(12),
    RAMPUP_MAINS_PARALLEL_OPERATION(14),
    MAINS_PARALLEL_OPERATION(16),
    RAMPUP_ISLAND_OPERATION(18),
    ISLAND_OPERATION(20),
    LOAD_RAMPDOWN(22),
    ENGINE_COOLDOWN(24),
    READY(26),
    NOT_READY(28),
    MAINS_FAILURE(30),
    FORCED_OUTAGE(32),
    TROUBLESHOOTING(34);

//   V1
//    UNDEFINED(0),
//    BLOCKSTART(1),
//    BWS_MAN(2),
//    BWS_MAN_TRIP(3),
//    BWS_MAN_OPERATING(4),
//    BWS_MAN_OPERATING_TRIP(5),
//    OPERATING(6),
//    READY(7),
//    UNPLANNED_STANDSTILL(8),
//    UNPLANNED_STANDSTILL_TROUBLESHOOTING(9),
//    GAP(10);


    private final Integer intValue;

    EngineAction(int value) {
        intValue =value;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static EngineAction getNameByValue(final int value) {
        for (final EngineAction s: EngineAction.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    //@JsonCreator
    public static EngineAction getNameByValue(final String value) {
        for (final EngineAction s: EngineAction.values()) {
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

    public static boolean isOperating(EngineAction engineAction){
        return engineAction == EngineAction.ISLAND_OPERATION
                || engineAction == EngineAction.LOAD_RAMPDOWN
                || engineAction == EngineAction.MAINS_PARALLEL_OPERATION
                || engineAction == EngineAction.OPERATION
                || engineAction == EngineAction.RAMPUP_ISLAND_OPERATION
                || engineAction == EngineAction.RAMPUP_MAINS_PARALLEL_OPERATION;
    }
}