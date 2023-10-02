package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pitch.adapters.outbound.RedisAdapter;
import io.angularpay.pitch.domain.PitchRequest;

import java.util.Objects;

public interface UpdatesPublisherCommand<T extends PitchRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToUpdatesMessage(PitchRequest pitchRequest) throws JsonProcessingException;

    default void publishUpdates(T t) {
        PitchRequest pitchRequest = t.getPitchRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(pitchRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToUpdatesMessage(pitchRequest);
                redisAdapter.publishUpdates(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
