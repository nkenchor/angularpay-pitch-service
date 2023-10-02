package io.angularpay.pitch.adapters.inbound;

import io.angularpay.pitch.domain.commands.PlatformConfigurationsConverterCommand;
import io.angularpay.pitch.models.platform.PlatformConfigurationIdentifier;
import io.angularpay.pitch.ports.inbound.InboundMessagingPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static io.angularpay.pitch.models.platform.PlatformConfigurationSource.TOPIC;

@Service
@RequiredArgsConstructor
public class RedisMessageAdapter implements InboundMessagingPort {

    private final PlatformConfigurationsConverterCommand converterCommand;

    @Override
    public void onMessage(String message, PlatformConfigurationIdentifier identifier) {
        this.converterCommand.execute(message, identifier, TOPIC);
    }
}
