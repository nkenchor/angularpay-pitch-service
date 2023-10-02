package io.angularpay.pitch.ports.inbound;

import io.angularpay.pitch.models.platform.PlatformConfigurationIdentifier;

public interface InboundMessagingPort {
    void onMessage(String message, PlatformConfigurationIdentifier identifier);
}
