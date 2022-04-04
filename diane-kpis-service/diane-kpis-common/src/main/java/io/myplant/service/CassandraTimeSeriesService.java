package io.myplant.service;

import io.myplant.DianeKpisConstants;
import io.myplant.cassandra.CassandraTimeseries;
import io.myplant.cassandra.TypeNotSupportedException;
import io.myplant.dataitem.DataItemValue;
import io.myplant.rediscache.RedisAssetModelLookup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class CassandraTimeSeriesService {
    private final RedisAssetModelLookup redisAssetModelLookup;
    private final CassandraTimeseries cassandraTimeseries;

    long dataItemId_SendingCondition;
    long dataItemId_SendingConditionNumeric;

    @PostConstruct
    private void init() {
        dataItemId_SendingCondition = lookupDataItemId(DianeKpisConstants.MODEL, "SendingDataCondition");
        dataItemId_SendingConditionNumeric = lookupDataItemId(DianeKpisConstants.MODEL, "SendingDataConditionNumeric");
    }

    public List<DataItemValue<Double>> fetchDataItems(long deviceId, long from, long to) throws TypeNotSupportedException {
        List<DataItemValue<Double>> result = cassandraTimeseries.fetchRange(deviceId, dataItemId_SendingConditionNumeric, from, to, 1.0);
        log.info("found {} data items in range", result.size());
        return result;
    }

    private long lookupDataItemId(final String model, final String dataItemName) {
        long result = redisAssetModelLookup.lookUpDataItemNameToIdMapping(model, dataItemName);
        if (result == RedisAssetModelLookup.UNMATCHABLE_ID)
            throw new RuntimeException(
                    "unable to get id for data item: " + model + "/" + dataItemName);
        return result;
    }
}
