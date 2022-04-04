package io.myplant.service.StateKpiCalculation.comperator;

import io.myplant.alarm.MessageEvent;

import java.util.Comparator;

/**
 * @author Holger Stanke (502408730), Holger.Stanke@ge.com
 *         Created on 04.03.2016.
 *
 * This comparator compares two {@link MessageEvent} by their severity in descending order.
 */
public class MessageEventSeverityComparator implements Comparator<MessageEvent> {

    @Override
    public int compare(MessageEvent me1, MessageEvent me2) {

        Integer me1Severity = me1.getSeverity();
        Integer me2Severity = me2.getSeverity();

        return me2Severity.compareTo(me1Severity);
    }

}
