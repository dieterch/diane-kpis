package io.myplant.service.StateKpiCalculation.comperator;

import io.myplant.alarm.MessageEvent;

import java.util.Comparator;

/**
 * @author Holger Stanke (502408730), Holger.Stanke@ge.com
 *         Created on 04.03.2016.
 */
public class MessageEventMessageNameComparator implements Comparator<MessageEvent> {

    @Override
    public int compare(MessageEvent me1, MessageEvent me2) {

        String me1Name = me1.getName();
        String me2Name = me2.getName();

        return me1Name.compareTo(me2Name);
    }
}
