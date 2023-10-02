package io.angularpay.pitch.adapters.outbound;

import io.angularpay.pitch.configurations.AngularPayConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;

import static io.angularpay.pitch.common.Constants.*;

@Configuration
public class RedisOutboundConfiguration {

    @Bean
    ChannelTopic updatesTopic() {
        return new ChannelTopic(UPDATES_TOPIC);
    }

    @Bean
    ChannelTopic ttlTopic() {
        return new ChannelTopic(TTL_TOPIC);
    }

    @Bean
    ChannelTopic userNotificationsTopic() {
        return new ChannelTopic(USER_NOTIFICATIONS_TOPIC);
    }
}
