package io.angularpay.pitch.ports.outbound;

import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PersistencePort {
    PitchRequest createRequest(PitchRequest request);
    PitchRequest updateRequest(PitchRequest request);
    Optional<PitchRequest> findRequestByReference(String reference);
    Page<PitchRequest> listRequests(Pageable pageable);
    Page<PitchRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses);
    Page<PitchRequest> findRequestsByVerification(Pageable pageable, boolean verified);
    Page<PitchRequest> findByInvesteeUserReference(Pageable pageable, String userReference);
    long getCountByVerificationStatus(boolean verified);
    long getCountByRequestStatus(RequestStatus status);
    long getTotalCount();
}
