package io.angularpay.pitch.helpers;

import io.angularpay.pitch.domain.Bargain;
import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.RequestStatus;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;

import static io.angularpay.pitch.common.Constants.SERVICE_CODE;
import static io.angularpay.pitch.util.SequenceGenerator.generateRequestTag;

public class ObjectFactory {

    public static PitchRequest pmtRequestWithDefaults() {
        return PitchRequest.builder()
                .reference(UUID.randomUUID().toString())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .lastModified(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .serviceCode(SERVICE_CODE)
                .verified(false)
                .status(RequestStatus.ACTIVE)
                .requestTag(generateRequestTag())
                .investors(new ArrayList<>())
                .bargain(Bargain.builder()
                        .offers(new ArrayList<>())
                        .build())
                .build();
    }
}