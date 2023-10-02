package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.domain.Investor;
import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.Role;
import io.angularpay.pitch.exceptions.ErrorObject;
import io.angularpay.pitch.models.GetUserInvestmentsCommandRequest;
import io.angularpay.pitch.models.UserInvestmentModel;
import io.angularpay.pitch.validation.DefaultConstraintValidator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class GetUserInvestmentsCommand extends AbstractCommand<GetUserInvestmentsCommandRequest, List<UserInvestmentModel>> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;

    public GetUserInvestmentsCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator) {
        super("GetUserInvestmentsCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
    }

    @Override
    protected String getResourceOwner(GetUserInvestmentsCommandRequest request) {
        return request.getAuthenticatedUser().getUserReference();
    }

    @Override
    protected List<UserInvestmentModel> handle(GetUserInvestmentsCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPaging().getIndex(), request.getPaging().getSize());
        List<UserInvestmentModel> investmentRequests = new ArrayList<>();
        List<PitchRequest> response = this.mongoAdapter.listRequests(pageable).getContent();
        for (PitchRequest pitchRequest : response) {
            List<Investor> investors = pitchRequest.getInvestors();
            for (Investor investor : investors) {
                if (request.getAuthenticatedUser().getUserReference().equalsIgnoreCase(investor.getUserReference())) {
                    investmentRequests.add(UserInvestmentModel.builder()
                            .requestReference(pitchRequest.getReference())
                            .investmentReference(investor.getReference())
                            .userReference(investor.getUserReference())
                            .requestCreatedOn(investor.getCreatedOn())
                            .build());
                }
            }
        }
        return investmentRequests;
    }

    @Override
    protected List<ErrorObject> validate(GetUserInvestmentsCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }
}
