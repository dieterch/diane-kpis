package io.myplant.service;

import io.myplant.service.StateKpiCalculation.StateUtils;
import io.myplant.domain.AssetInformation;
import io.myplant.repository.AssetInformationRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;


@Service
public class AssetInformationService {
    private static final Logger logger = LoggerFactory.getLogger(AssetInformationService.class);

    private final AssetInformationRepository assetInformationRepository;
    private HashMap<Long, String> cachedTimeZone = new HashMap<>();
    private HashMap<Long, Long> cachedTimeZoneOffset = new HashMap<>();
    private HashMap<Long, Long> cachedMonitoringStart = new HashMap<>();
    private Object cacheLock = new Object();

    @Autowired
    public AssetInformationService(AssetInformationRepository assetInformationRepository) {
        this.assetInformationRepository = assetInformationRepository;
    }

    public String getCachedTimezone(Long deviceId){
        synchronized (cacheLock) {
            String timezone = cachedTimeZone.get(deviceId);
            if(timezone != null){
                return timezone;
                }
            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(deviceId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored != null && !StringUtils.isEmpty(stored.getTimezone())){
                cachedTimeZone.put(deviceId, stored.getTimezone());
                return stored.getTimezone();
            }
            else{
                cachedTimeZone.put(deviceId, "");
                return "";
            }
        }
    }

    public Long getCachedTimezoneOffice(Long deviceId) {
        synchronized (cacheLock) {
            Long timezoneOffset = cachedTimeZoneOffset.get(deviceId);
            if (timezoneOffset != null) {
                return timezoneOffset;
            }

            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(deviceId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if (stored != null && StringUtils.isEmpty(stored.getTimezone())) {
                long offset = 0;
                try {
                    TimeZone tz = TimeZone.getTimeZone(stored.getTimezone());
                    offset = (long) tz.getRawOffset();
                } catch (Exception ex) {
                    logger.error("can't check timezone offset for " + stored.getTimezone());
                }
                cachedTimeZoneOffset.put(deviceId, offset);
                return offset;
            } else {
                cachedTimeZoneOffset.put(deviceId, 0L);
                return 0L;
            }
        }
    }

    public long getCachedMonitoringStart(long deviceId) {
        Long monitoringStart = cachedMonitoringStart.get(deviceId);
        if (monitoringStart != null) {
            return monitoringStart;
        }

        AssetInformation assetInformation = null;
        Optional<AssetInformation> storedOpt = assetInformationRepository.findById(deviceId);
        if(storedOpt.isPresent())
            assetInformation = storedOpt.get();

        if(assetInformation == null){
            cachedMonitoringStart.put(deviceId, 0L);
            return 0L;
        }

        try {
            long result = 0L;
            if (!StringUtils.isBlank(assetInformation.getRamStartDate())) {
                result = StateUtils.getComissionDateTimestamp(assetInformation.getRamStartDate());
                cachedMonitoringStart.put(deviceId, result);
                return result;
            }
            if (!StringUtils.isBlank(assetInformation.getCommissionDate())) {
                result = StateUtils.getComissionDateTimestamp(assetInformation.getCommissionDate());
                cachedMonitoringStart.put(deviceId, result);
                return result;
            }
        } catch (Exception ex) {
            logger.error("assetId " + deviceId + ": commissioning date has wrong format", ex);
        }
        return 0L;
    }


    // every 3 hours
    @Scheduled(cron = "0 0 */3 * * *")
    public void clearCache() {
        synchronized (cacheLock) {
            cachedTimeZone = new HashMap<>();
            cachedTimeZoneOffset = new HashMap<>();
            cachedMonitoringStart = new HashMap<>();
        }
    }


    public void saveCommissionDate(Long deviceId, String comissionDate) {
        if(comissionDate != null){
            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(deviceId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored == null){
                stored= new AssetInformation();
                stored.setId(deviceId);
            }
            if (!comissionDate.equals(stored.getCommissionDate())) {
                stored.setCommissionDate(comissionDate);
                assetInformationRepository.save(stored);
            }
        }
    }

    public void saveTimezone(Long deviceId, String timezone) {
        if(timezone != null){
            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(deviceId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored == null){
                stored= new AssetInformation();
                stored.setId(deviceId);
            }

            if (!timezone.equals(stored.getTimezone())) {
                stored.setTimezone(timezone);
                assetInformationRepository.save(stored);
            }
        }
    }

    public void saveInformation(Long deviceId, String comissionDate, String timezone) {
        if(timezone != null || comissionDate != null){

            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(deviceId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored == null){
                stored= new AssetInformation();
                stored.setId(deviceId);
            }

            boolean changed = false;
            if (comissionDate != null && !comissionDate.equals(stored.getCommissionDate())) {
                stored.setCommissionDate(comissionDate);
                changed = true;
            }

            if (timezone != null && !timezone.equals(stored.getTimezone())) {
                stored.setTimezone(timezone);
                changed = true;
            }

            if(changed)
                assetInformationRepository.save(stored);
        }
    }

    public HashMap<Long, AssetInformation> getInfoMap(List<Long> assetIds) {
        HashMap<Long, AssetInformation> result = new HashMap<>();
        for(Long assetId : assetIds){

            AssetInformation stored = null;
            Optional<AssetInformation> storedOpt = assetInformationRepository.findById(assetId);
            if(storedOpt.isPresent())
                stored = storedOpt.get();

            if(stored == null){
                stored = new AssetInformation();
            }
            result.put(assetId, stored);
        }
        return result;
    }


}
