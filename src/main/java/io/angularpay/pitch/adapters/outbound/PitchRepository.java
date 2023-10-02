package io.angularpay.pitch.adapters.outbound;

import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PitchRepository extends MongoRepository<PitchRequest, String> {

    Optional<PitchRequest> findByReference(String reference);
    Page<PitchRequest> findAll(Pageable pageable);
    Page<PitchRequest> findByStatusIn(Pageable pageable, List<RequestStatus> statuses);
    Page<PitchRequest> findByVerified(Pageable pageable, boolean verified);
    Page<PitchRequest> findAByInvesteeUserReference(Pageable pageable, String userReference);
    long countByVerified(boolean verified);
    long countByStatus(RequestStatus status);
}
