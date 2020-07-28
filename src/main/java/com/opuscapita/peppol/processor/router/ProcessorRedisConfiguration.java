package com.opuscapita.peppol.processor.router;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@Configuration
public class ProcessorRedisConfiguration {

    @Value("${redis.host:redis}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    @Value("${redis.password:}")
    private String password;

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory jedisConnectionFactory = null;

        try {
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(host, port);
            redisStandaloneConfiguration.setPassword(password);
            jedisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
        } catch (RedisConnectionFailureException e) {
            e.printStackTrace();
        }

        return jedisConnectionFactory;
    }

    @Bean
    public RedisTemplate<String, CachedRoute> redisTemplate() {
        RedisTemplate<String, CachedRoute> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        return template;
    }

}
