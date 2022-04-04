package io.myplant.service.StateKpiCalculation;

import io.myplant.cassandra.CassandraTimeseries;
import io.myplant.model.MessageEventNonAssetId;
import io.myplant.service.StateKpiCalculation.comperator.MessageEventChainedComparator;
import io.myplant.service.StateKpiCalculation.comperator.MessageEventMessageNameComparator;
import io.myplant.service.StateKpiCalculation.comperator.MessageEventSeverityComparator;
import io.myplant.service.StateKpiCalculation.comperator.MessageEventTimestampComparator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageReader {
    private static final Logger logger = LoggerFactory.getLogger(MessageReader.class);

    private final CassandraTimeseries cassandraTimeseries;

    private static final HashSet<Integer> SEVERITIES = new HashSet<>(Arrays.asList(600, 610, 650, 700, 800));
    private static final Long MILLIS_PER_DAY = 24 * 3600 * 1000L;

    public List<MessageEventNonAssetId> getMessagesForDevice(final Long deviceId) {
        // from cassandra we get it in descending order
        val messagesForDevice = cassandraTimeseries.fetchMessages(deviceId, 0, Long.MAX_VALUE, SEVERITIES);
        messagesForDevice.sort(new MessageEventChainedComparator(
                new MessageEventTimestampComparator(),
                new MessageEventSeverityComparator(),
                new MessageEventMessageNameComparator()));

        return messagesForDevice.stream().map(m-> new MessageEventNonAssetId(m.getTimestamp(),m.getName(),m.getSeverity())).collect(Collectors.toList());
    }

    public void removeLastDayMessages(List<MessageEventNonAssetId> messagesForAsset){
        var lastMessage = getLastMessage(messagesForAsset);
        var endOfLastDay = Long.MAX_VALUE;
        while (lastMessage != null) {
            if (endOfLastDay == Long.MAX_VALUE)
                endOfLastDay = (lastMessage.getTimestamp() / MILLIS_PER_DAY) * MILLIS_PER_DAY;// round down to beginning of day

            if (lastMessage.getTimestamp() > endOfLastDay)
                messagesForAsset.remove(messagesForAsset.size() - 1);
            else
                break;

            lastMessage = getLastMessage(messagesForAsset);
        }
    }

    private MessageEventNonAssetId getLastMessage(final List<MessageEventNonAssetId> messagesForAsset) {
        if (messagesForAsset.isEmpty())
            return null;
        else
            return messagesForAsset.get(messagesForAsset.size() - 1);
    }
}
