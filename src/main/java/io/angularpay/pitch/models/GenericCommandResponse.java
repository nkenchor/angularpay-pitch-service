
package io.angularpay.pitch.models;

import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.commands.PitchRequestSupplier;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder(toBuilder = true)
@RequiredArgsConstructor
public class GenericCommandResponse extends GenericReferenceResponse implements PitchRequestSupplier {

    private final String requestReference;
    private final String itemReference;
    private final PitchRequest pitchRequest;
}
