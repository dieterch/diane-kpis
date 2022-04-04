package io.myplant.service;

import io.myplant.model.MessageTextCache;
import io.myplant.model.Alarm;
import io.myplant.seshat.api.ModelApi;
import io.myplant.utils.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class MessageTextCacheService {
    private static final Logger logger = LoggerFactory.getLogger(MessageTextCacheService.class);

    //@Value("create.statemachine.message-text-cache-ttl")
    private long ttlTimeSpan = 3600000;

    //@Value("create.statemachine.message-text-cache-size")
    private long cacheSize = 150;

    private HashMap<String, MessageTextCache> messageCache = new HashMap<>();

    private final ModelApi modelApi;

    @Autowired
    public MessageTextCacheService(ModelApi modelApi) {

        this.modelApi = modelApi;
    }

    public String getMessage(String model, String serial, String language, String messageNo) {
        if (model == null || serial == null || language == null) return null;
        Map<String, String> messages = getMessages(model, serial, language);
        if (!messages.containsKey(messageNo)) return null;
        return messages.get(messageNo);
    }

    public synchronized Map<String, String> getMessages(
            String model, String serial, String language) {

        String cacheKey = getCacheKey(model, serial, language);
        MessageTextCache cachedVersion = null;
        if(messageCache.containsKey(cacheKey)){
            cachedVersion = messageCache.get(cacheKey);
        }

        if (cachedVersion == null || cachedVersion.getTtl() < new Date().getTime()) {
            Map<String, String> messagesFromSeshat = getMessagesFromSeshat(model, serial, language);
            if (messagesFromSeshat.size() == 0) {
                return new HashMap<>();
            }
            MessageTextCache messageMap = new MessageTextCache();
            messageMap.setModel(model);
            messageMap.setSerial(serial);
            messageMap.setLanguage(language);
            messageMap.setTtl(ttlTimeSpan + new Date().getTime());
            messageMap.setMessageCache(messagesFromSeshat);
            messageMap.setLastAccess(new Date().getTime());
            messageCache.put(cacheKey, messageMap);

            if (messageCache.size() > cacheSize) {
                removeOldestCacheEntry(messageCache);
            }

            return messagesFromSeshat;
        }

        cachedVersion.setLastAccess(new Date().getTime());
        return cachedVersion.getMessageCache();
    }

    protected void removeOldestCacheEntry(HashMap<String, MessageTextCache> cache){
        Optional<Map.Entry<String, MessageTextCache>> oldestOptional = cache.entrySet()
                .stream()
                .sorted(
                        (f1, f2) ->
                                Long.compare(
                                        f1.getValue().getLastAccess(), f2.getValue().getLastAccess()))
                .findFirst();
        if(oldestOptional.isPresent())
            cache.remove(oldestOptional.get().getKey());
    }


    public Map<String, String> getMessagesFromSeshat(String model, String serial, String language) {
        StopWatch watch = new StopWatch();
        watch.start();
        List<Alarm> modelAlarms =
                modelApi.getModelAlarms(model, language, null, null, serial, true);

        Map<String, String> map = modelAlarms
                .stream()
                .collect(Collectors.toMap(Alarm::getMessageNumber, Alarm::getMessage));
        logger.info("get message text for {} returns {} message texts and tooks {}", getCacheKey(model, serial,language), map.size(), watch.pretty(watch.stop()));
        return map;
    }

    public String getCacheKey(String model, String serial, String language) {
        return model + "/" + serial + "/" + language;
    }
}
