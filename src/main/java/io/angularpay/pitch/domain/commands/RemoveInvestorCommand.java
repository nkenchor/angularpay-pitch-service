package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.adapters.outbound.RedisAdapter;
import io.angularpay.pitch.domain.DeletedBy;
import io.angularpay.pitch.domain.Investor;
import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.Role;
import io.angularpay.pitch.exceptions.ErrorObject;
import io.angularpay.pitch.helpers.CommandHelper;
import io.angularpay.pitch.models.*;
import io.angularpay.pitch.validation.DefaultConstraintValidator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.pitch.domain.DeletedBy.INVESTOR;
import static io.angularpay.pitch.domain.DeletedBy.TTL_SERVICE;
import static io.angularpay.pitch.helpers.CommandHelper.*;
import static io.angularpay.pitch.helpers.Helper.getAllParties;
import static io.angularpay.pitch.helpers.Helper.getAllPartiesExceptActor;
import static io.angularpay.pitch.models.UserNotificationType.INVESTOR_DELETED_BY_SELF;
import static io.angularpay.pitch.models.UserNotificationType.INVESTOR_DELETED_BY_TTL;

@Service
public class RemoveInvestorCommand extends AbstractCommand<RemoveInvestorCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public RemoveInvestorCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter) {
        super("RemoveInvestorCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(RemoveInvestorCommandRequest request) {
        switch (request.getDeletedBy()) {
            case PLATFORM:
            case TTL_SERVICE:
                return request.getAuthenticatedUser().getUserReference();
            default:
                return this.commandHelper.getInvestmentOwner(request.getRequestReference(), request.getInvestmentReference());
        }
    }

    @Override
    protected GenericCommandResponse handle(RemoveInvestorCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        String investmentReference = request.getInvestmentReference();
        validRequestStatusAndInvestmentExists(found, investmentReference);
        Supplier<GenericCommandResponse> supplier = () -> removeInvestor(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse removeInvestor(RemoveInvestorCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusOrThrow(found);
        found.getInvestors().forEach(x-> {
            if (request.getInvestmentReference().equalsIgnoreCase(x.getReference())) {
                validateInvestmentStatusOrThrow(x);
                x.setDeleted(true);
                x.setDeletedOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
                x.setDeletedBy(request.getDeletedBy());
            }
        });
        PitchRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .pitchRequest(response)
                .itemReference(request.getInvestmentReference())
                .build();
    }

    @Override
    protected List<ErrorObject> validate(RemoveInvestorCommandRequest request) {
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

    @Override
    public UserNotificationType getUserNotificationType(GenericCommandResponse commandResponse) {
        DeletedBy deletedBy = commandResponse.getPitchRequest().getInvestors().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(commandResponse.getItemReference()))
                .findFirst()
                .map(Investor::getDeletedBy)
                .orElse(TTL_SERVICE);
        return deletedBy == INVESTOR ? INVESTOR_DELETED_BY_SELF : INVESTOR_DELETED_BY_TTL;
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return this.getUserNotificationType(commandResponse) == INVESTOR_DELETED_BY_SELF ?
                getAllPartiesExceptActor(commandResponse.getPitchRequest(), commandResponse.getItemReference()) :
                getAllParties(commandResponse.getPitchRequest());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, PitchRequest> parameters) throws JsonProcessingException {
        String summary;
        Optional<String> optional = parameters.getCommandResponse().getPitchRequest().getInvestors().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .map(Investor::getUserReference)
                .findFirst();
        if (optional.isPresent() && optional.get().equalsIgnoreCase(parameters.getUserReference())) {
            summary = "the comment you made on a Pitch post, was deleted because payment wasn't received";
        } else {
            summary = "someone's investment on a Pitch post that you commented on, was deleted";
        }

        UserNotificationInvestmentPayload userNotificationInvestmentPayload = UserNotificationInvestmentPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
                .investmentReference(parameters.getCommandResponse().getItemReference())
                .build();
        String payload = mapper.writeValueAsString(userNotificationInvestmentPayload);

        String attributes = mapper.writeValueAsString(parameters.getRequest());

        UserNotification userNotification = UserNotification.builder()
                .reference(UUID.randomUUID().toString())
                .createdOn(Instant.now().truncatedTo(ChronoUnit.SECONDS).toString())
                .serviceCode(parameters.getRequest().getServiceCode())
                .userReference(parameters.getUserReference())
                .type(parameters.getType())
                .summary(summary)
                .payload(payload)
                .attributes(attributes)
                .build();

        return mapper.writeValueAsString(userNotification);
    }
}
