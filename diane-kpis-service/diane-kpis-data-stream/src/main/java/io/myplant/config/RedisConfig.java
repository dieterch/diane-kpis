package io.myplant.config;

import io.myplant.rediscache.RedisAssetModelLookup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
public class RedisConfig {
    @Bean
    @Autowired
    public RedisAssetModelLookup getRedisAssetModelLookup(RedisConnectionFactory connectionFactory) {
        return new RedisAssetModelLookup(connectionFactory);
    }
}
