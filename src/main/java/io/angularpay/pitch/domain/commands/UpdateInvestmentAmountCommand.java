package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.adapters.outbound.RedisAdapter;
import io.angularpay.pitch.domain.Amount;
import io.angularpay.pitch.domain.InvestmentTransactionStatus;
import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.Role;
import io.angularpay.pitch.exceptions.CommandException;
import io.angularpay.pitch.exceptions.ErrorObject;
import io.angularpay.pitch.helpers.CommandHelper;
import io.angularpay.pitch.models.GenericCommandResponse;
import io.angularpay.pitch.models.UpdateInvestmentAmountCommandRequest;
import io.angularpay.pitch.validation.DefaultConstraintValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static io.angularpay.pitch.exceptions.ErrorCode.REQUEST_REMOVED_ERROR;
import static io.angularpay.pitch.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.pitch.helpers.CommandHelper.validRequestStatusAndInvestmentExists;

@Service
public class UpdateInvestmentAmountCommand extends AbstractCommand<UpdateInvestmentAmountCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;
    
    public UpdateInvestmentAmountCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("UpdateInvestmentAmountCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateInvestmentAmountCommandRequest request) {
        return this.commandHelper.getInvestmentOwner(request.getRequestReference(), request.getInvestmentReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateInvestmentAmountCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getInvestmentReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> updateInvestmentAmount(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateInvestmentAmount(UpdateInvestmentAmountCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        found.getInvestors().forEach(x-> {
            if (request.getInvestmentReference().equalsIgnoreCase(x.getReference())) {
                if (x.isDeleted() || (Objects.nonNull(x.getInvestmentStatus()) && x.getInvestmentStatus().getStatus() == InvestmentTransactionStatus.SUCCESSFUL)) {
                    throw CommandException.builder()
                            .status(HttpStatus.UNPROCESSABLE_ENTITY)
                            .errorCode(REQUEST_REMOVED_ERROR)
                            .message(REQUEST_REMOVED_ERROR.getDefaultMessage())
                            .build();
                }
                Amount amount = Amount.builder()
                        .currency(request.getUpdateInvestmentApiModel().getCurrency())
                        .value(request.getUpdateInvestmentApiModel().getValue())
                        .build();
                x.setAmount(amount);
                x.setComment(request.getUpdateInvestmentApiModel().getComment());
            }
        });
        PitchRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .pitchRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateInvestmentAmountCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Collections.emptyList();
    }

    @Override
    public String convertToUpdatesMessage(PitchRequest pitchRequest) throws JsonProcessingException {
        return this.commandHelper.toJsonString(pitchRequest);
    }

    @Override
    public RedisAdapter getRedisAdapter() {
        return this.redisAdapter;
    }
}
