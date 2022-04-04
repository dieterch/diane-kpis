package io.myplant.service;

import io.myplant.model.ModelSerial;
import io.myplant.model.Asset;
import io.myplant.rediscache.RedisAssetModelLookup;
import io.myplant.seshat.api.AssetApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AssetService {
    private static final Logger logger = LoggerFactory.getLogger(AssetService.class);

    private final AssetApi assetApi;
    private final RedisAssetModelLookup modelLookup;

    @Autowired
    public AssetService(AssetApi assetApi, RedisAssetModelLookup modelLookup) {
        this.assetApi = assetApi;
        this.modelLookup = modelLookup;
    }

    public ModelSerial getSerialModel(long id){
        Asset asset = assetApi.get(id, false, false);

        return new ModelSerial(asset.getModel(), asset.getSerialNumber());
    }

    public HashMap<Long, String> getIdSerialMap(String model, String[] serials) {
        HashMap<Long, String> result = new HashMap<Long, String>();

        for (String serial : serials) {
            long assetId = modelLookup.lookUpAssetSerialNumberToIdMapping(model, serial);
            if (assetId == RedisAssetModelLookup.UNMATCHABLE_ID) {
                logger.error(String.format("Asset ID not valid for model/serial '%s/%s': ", model, serial));
                continue;
            }
            result.put(assetId, serial);
        }
        return result;
    }

    public List<Long> getIds(String model, String[] serials) {
        List<Long> result = new ArrayList<>();

        for (String serial : serials) {
            long assetId = modelLookup.lookUpAssetSerialNumberToIdMapping(model, serial);
            if (assetId == RedisAssetModelLookup.UNMATCHABLE_ID) {
                logger.error(String.format("Asset ID not valid for model/serial '%s/%s': ", model, serial));
                continue;
            }
            result.add(assetId);
        }
        return result;
    }
}
