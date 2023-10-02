package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.adapters.outbound.RedisAdapter;
import io.angularpay.pitch.domain.Offer;
import io.angularpay.pitch.domain.OfferStatus;
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

import static io.angularpay.pitch.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.pitch.helpers.CommandHelper.validRequestStatusAndBargainExists;
import static io.angularpay.pitch.helpers.Helper.getAllPartiesExceptInvestee;
import static io.angularpay.pitch.models.UserNotificationType.BARGAIN_REJECTED;

@Service
public class RejectBargainCommand extends AbstractCommand<RejectBargainCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public RejectBargainCommand(ObjectMapper mapper, MongoAdapter mongoAdapter, DefaultConstraintValidator validator, CommandHelper commandHelper, RedisAdapter redisAdapter) {
        super("RejectBargainCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(RejectBargainCommandRequest request) {
        return commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected GenericCommandResponse handle(RejectBargainCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        validRequestStatusAndBargainExists(found, request.getBargainReference());
        Supplier<GenericCommandResponse> supplier = () -> rejectBargain(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse rejectBargain(RejectBargainCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        found.getBargain().getOffers().forEach(x-> {
            if (request.getBargainReference().equalsIgnoreCase(x.getReference())) {
                x.setStatus(OfferStatus.REJECTED);
            }
        });
        if (request.getBargainReference().equalsIgnoreCase(found.getBargain().getAcceptedBargainReference())) {
            found.getBargain().setAcceptedBargainReference(null);
        }
        PitchRequest response = this.mongoAdapter.updateRequest(found);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .itemReference(request.getBargainReference())
                .pitchRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(RejectBargainCommandRequest request) {
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
        return BARGAIN_REJECTED;
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return getAllPartiesExceptInvestee(commandResponse.getPitchRequest());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, PitchRequest> parameters) throws JsonProcessingException {
        String summary;
        Optional<String> optional = parameters.getCommandResponse().getPitchRequest().getBargain().getOffers().stream()
                .filter(x -> x.getReference().equalsIgnoreCase(parameters.getCommandResponse().getItemReference()))
                .map(Offer::getUserReference)
                .findFirst();
        if (optional.isPresent() && optional.get().equalsIgnoreCase(parameters.getUserReference())) {
            summary = "the bargain you made on a Pitch post, was rejected";
        } else {
            summary = "someone's bargain on a Pitch post that you commented on, was rejected";
        }

        UserNotificationBargainPayload userNotificationInvestmentPayload = UserNotificationBargainPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
                .bargainReference(parameters.getCommandResponse().getItemReference())
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
