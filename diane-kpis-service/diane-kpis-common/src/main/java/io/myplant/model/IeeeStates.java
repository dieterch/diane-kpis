package io.myplant.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IeeeStates {
    UNDEFINED(0),
    DEACTIVATED_SHUTDOWN(5),
    AVAILABLE(10),
    PLANNED_OUTAGE(15),
    UNPLANNED_MAINTENANCE(20),
    FORCEDOUTAGE_REL(25),
    FORCEDOUTAGE_MTBFO_REL(30),
    GAP(35),
    PILOT_ACTIVITIES(40);

    private final Integer intValue;

    IeeeStates(int value) {
        intValue = value;
    }

    //@JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static IeeeStates getNameByValue(final int value) {
        for (final IeeeStates s : IeeeStates.values()) {
            if (s.intValue == value) {
                return s;
            }
        }
        return null;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static IeeeStates getNameByValue(final String value) {
        for (final IeeeStates s : IeeeStates.values()) {
            if (s.getStringValue().equals(value)) {
                return s;
            }
        }
        return null;
    }

    @JsonValue
    public String getStringValue() {
        return getNameByValue(intValue).name();
    }

    //@JsonValue
    public int getValue() {
        return intValue;
    }

    public static boolean isOutageForScope(IeeeStates state) {
        return state == IeeeStates.PLANNED_OUTAGE
                || state == IeeeStates.UNPLANNED_MAINTENANCE
                || state == IeeeStates.FORCEDOUTAGE_MTBFO_REL
                || state == IeeeStates.FORCEDOUTAGE_REL;
    }

    public static boolean isOutage(IeeeStates state) {
        return state == IeeeStates.PLANNED_OUTAGE
                || state == IeeeStates.UNPLANNED_MAINTENANCE
                || state == IeeeStates.FORCEDOUTAGE_MTBFO_REL
                || state == IeeeStates.FORCEDOUTAGE_REL;
    }

    public static boolean isOutageForExport(IeeeStates state) {
        return state == IeeeStates.PLANNED_OUTAGE
                || state == IeeeStates.UNPLANNED_MAINTENANCE
                || state == IeeeStates.FORCEDOUTAGE_MTBFO_REL
                || state == IeeeStates.FORCEDOUTAGE_REL;
    }

    public static boolean isOutageForExportWithoutUnplannedMaintenance(IeeeStates state) {
        return state == IeeeStates.PLANNED_OUTAGE
                || state == IeeeStates.FORCEDOUTAGE_MTBFO_REL
                || state == IeeeStates.FORCEDOUTAGE_REL;
    }

    public static boolean isForcedOutage(IeeeStates state) {
        return state == IeeeStates.FORCEDOUTAGE_MTBFO_REL
                || state == IeeeStates.FORCEDOUTAGE_REL;
    }
}