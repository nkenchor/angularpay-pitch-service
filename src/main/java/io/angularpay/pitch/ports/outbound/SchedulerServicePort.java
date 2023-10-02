package io.angularpay.pitch.ports.outbound;

import io.angularpay.pitch.models.SchedulerServiceRequest;
import io.angularpay.pitch.models.SchedulerServiceResponse;

import java.util.Map;
import java.util.Optional;

public interface SchedulerServicePort {
    Optional<SchedulerServiceResponse> createScheduledRequest(SchedulerServiceRequest request, Map<String, String> headers);
}
