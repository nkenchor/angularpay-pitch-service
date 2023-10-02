package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.Role;
import io.angularpay.pitch.exceptions.ErrorObject;
import io.angularpay.pitch.models.GetRequestListByVerificationCommandRequest;
import io.angularpay.pitch.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class GetRequestListByVerificationCommand extends AbstractCommand<GetRequestListByVerificationCommandRequest, List<PitchRequest>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetRequestListByVerificationCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator) {
        super("GetRequestListByVerificationCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetRequestListByVerificationCommandRequest request) {
        return "";
    }

    @Override
    protected List<PitchRequest> handle(GetRequestListByVerificationCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        return this.mongoAdapter.findRequestsByVerification(pageable, request.isVerified()).getContent();
    }

    @Override
    protected List<ErrorObject> validate(GetRequestListByVerificationCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_KYC_ADMIN, Role.ROLE_PLATFORM_ADMIN);
    }
}
