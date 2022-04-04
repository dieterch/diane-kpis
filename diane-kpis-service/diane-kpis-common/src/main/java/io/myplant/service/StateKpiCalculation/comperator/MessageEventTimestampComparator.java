package io.myplant.service.StateKpiCalculation.comperator;

import io.myplant.alarm.MessageEvent;

import java.util.Comparator;

/**
 * @author Holger Stanke (502408730), Holger.Stanke@ge.com
 *         Created on 04.03.2016.
 *
 * This comparator compares two {@link MessageEvent} by their timestamps in ascending.
 */
public class MessageEventTimestampComparator implements Comparator<MessageEvent> {

    @Override
    public int compare(MessageEvent me1, MessageEvent me2) {

        Long me1Timestamp = me1.getTimestamp();
        Long me2Timestamp = me2.getTimestamp();

        return me1Timestamp.compareTo(me2Timestamp);
    }
}
