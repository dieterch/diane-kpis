package io.myplant.service.StateKpiCalculation.comperator;

import io.myplant.alarm.MessageEvent;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Holger Stanke (502408730), Holger.Stanke@ge.com
 *         Created on 04.03.2016.

 * This is a chained comparator that is used to sort a list by multiple
 * attributes by chaining a sequence of comparators of individual fields
 * together.
 */
public class MessageEventChainedComparator implements Comparator<MessageEvent> {

    private List<Comparator<MessageEvent>> listComparators;

    @SafeVarargs
    public MessageEventChainedComparator(Comparator<MessageEvent>... comparators) {
        this.listComparators = Arrays.asList(comparators);
    }

    @Override
    public int compare(MessageEvent me1, MessageEvent me2) {
        for (Comparator<MessageEvent> comparator : listComparators) {
            int result = comparator.compare(me1, me2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }

}
