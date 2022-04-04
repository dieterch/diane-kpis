package io.myplant.service;

import io.myplant.model.IeeeStates;
import io.myplant.model.MessageTextCache;
import io.myplant.model.ScopeType;
import io.myplant.repository.AssetInformationRepository;
import io.myplant.seshat.api.ModelApi;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageTextCacheServiceTest {

    @MockBean
    private ModelApi modelApi;
    private MessageTextCacheService messageTextCacheService = new MessageTextCacheService(modelApi);

    @Test
    public void testDeleteOldEntry() {

        HashMap<String, MessageTextCache> messageCache = new HashMap<>();
        MessageTextCache c1 = new MessageTextCache(); c1.setLastAccess(100L);
        MessageTextCache c2 = new MessageTextCache(); c2.setLastAccess(200L);
        messageCache.put("1", c1);
        messageCache.put("2", c2);

        messageTextCacheService.removeOldestCacheEntry(messageCache);

        assertEquals(1, messageCache.entrySet().size());
        assertTrue( messageCache.containsKey("2"));
    }
}
