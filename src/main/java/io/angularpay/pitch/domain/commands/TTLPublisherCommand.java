package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.angularpay.pitch.adapters.outbound.RedisAdapter;
import io.angularpay.pitch.domain.PitchRequest;

import java.util.Objects;

public interface TTLPublisherCommand<T extends PitchRequestSupplier> {

    RedisAdapter getRedisAdapter();

    String convertToTTLMessage(PitchRequest pitchRequest, T t) throws JsonProcessingException;

    default void publishTTL(T t) {
        PitchRequest peerFundRequest = t.getPitchRequest();
        RedisAdapter redisAdapter = this.getRedisAdapter();
        if (Objects.nonNull(peerFundRequest) && Objects.nonNull(redisAdapter)) {
            try {
                String message = this.convertToTTLMessage(peerFundRequest, t);
                redisAdapter.publishTTL(message);
            } catch (JsonProcessingException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
}
