package io.angularpay.pitch.adapters.outbound;

import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.RequestStatus;
import io.angularpay.pitch.ports.outbound.PersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MongoAdapter implements PersistencePort {

    private final PitchRepository pitchRepository;

    @Override
    public PitchRequest createRequest(PitchRequest request) {
        return pitchRepository.save(request);
    }

    @Override
    public PitchRequest updateRequest(PitchRequest request) {
        return pitchRepository.save(request);
    }

    @Override
    public Optional<PitchRequest> findRequestByReference(String reference) {
        return pitchRepository.findByReference(reference);
    }

    @Override
    public Page<PitchRequest> listRequests(Pageable pageable) {
        return pitchRepository.findAll(pageable);
    }

    @Override
    public Page<PitchRequest> findRequestsByStatus(Pageable pageable, List<RequestStatus> statuses) {
        return pitchRepository.findByStatusIn(pageable, statuses);
    }

    @Override
    public Page<PitchRequest> findRequestsByVerification(Pageable pageable, boolean verified) {
        return pitchRepository.findByVerified(pageable, verified);
    }

    @Override
    public Page<PitchRequest> findByInvesteeUserReference(Pageable pageable, String userReference) {
        return pitchRepository.findAByInvesteeUserReference(pageable, userReference);
    }

    @Override
    public long getCountByVerificationStatus(boolean verified) {
        return pitchRepository.countByVerified(verified);
    }

    @Override
    public long getCountByRequestStatus(RequestStatus status) {
        return pitchRepository.countByStatus(status);
    }

    @Override
    public long getTotalCount() {
        return pitchRepository.count();
    }
}
