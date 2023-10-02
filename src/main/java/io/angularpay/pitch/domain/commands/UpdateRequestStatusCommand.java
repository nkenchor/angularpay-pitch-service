package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.adapters.outbound.RedisAdapter;
import io.angularpay.pitch.domain.PitchRequest;
import io.angularpay.pitch.domain.RequestStatus;
import io.angularpay.pitch.domain.Role;
import io.angularpay.pitch.exceptions.CommandException;
import io.angularpay.pitch.exceptions.ErrorObject;
import io.angularpay.pitch.helpers.CommandHelper;
import io.angularpay.pitch.models.*;
import io.angularpay.pitch.validation.DefaultConstraintValidator;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import static io.angularpay.pitch.domain.RequestStatus.CANCELLED;
import static io.angularpay.pitch.domain.RequestStatus.COMPLETED;
import static io.angularpay.pitch.exceptions.ErrorCode.REQUEST_CANCELLED_ERROR;
import static io.angularpay.pitch.exceptions.ErrorCode.REQUEST_COMPLETED_ERROR;
import static io.angularpay.pitch.helpers.CommandHelper.getRequestByReferenceOrThrow;
import static io.angularpay.pitch.helpers.Helper.getAllParties;
import static io.angularpay.pitch.models.UserNotificationType.*;

@Service
public class UpdateRequestStatusCommand extends AbstractCommand<UpdateRequestStatusCommandRequest, GenericCommandResponse>
        implements UpdatesPublisherCommand<GenericCommandResponse>,
        UserNotificationsPublisherCommand<GenericCommandResponse> {

    private final MongoAdapter mongoAdapter;
    private final DefaultConstraintValidator validator;
    private final CommandHelper commandHelper;
    private final RedisAdapter redisAdapter;

    public UpdateRequestStatusCommand(
            ObjectMapper mapper,
            MongoAdapter mongoAdapter,
            DefaultConstraintValidator validator,
            CommandHelper commandHelper,
            RedisAdapter redisAdapter) {
        super("UpdateRequestStatusCommand", mapper);
        this.mongoAdapter = mongoAdapter;
        this.validator = validator;
        this.commandHelper = commandHelper;
        this.redisAdapter = redisAdapter;
    }

    @Override
    protected String getResourceOwner(UpdateRequestStatusCommandRequest request) {
        return this.commandHelper.getRequestOwner(request.getRequestReference());
    }

    @Override
    protected GenericCommandResponse handle(UpdateRequestStatusCommandRequest request) {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        if (found.getStatus() == COMPLETED) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(REQUEST_COMPLETED_ERROR)
                    .message(REQUEST_COMPLETED_ERROR.getDefaultMessage())
                    .build();
        }
        if (found.getStatus() == CANCELLED) {
            throw CommandException.builder()
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .errorCode(REQUEST_CANCELLED_ERROR)
                    .message(REQUEST_CANCELLED_ERROR.getDefaultMessage())
                    .build();
        }
        Supplier<GenericCommandResponse> supplier = () -> updateRequestStatus(request);
        return this.commandHelper.executeAcid(supplier);
    }

    private GenericCommandResponse updateRequestStatus(UpdateRequestStatusCommandRequest request) throws OptimisticLockingFailureException {
        PitchRequest found = getRequestByReferenceOrThrow(this.mongoAdapter, request.getRequestReference());
        PitchRequest response = this.commandHelper.updateProperty(found, request::getStatus, found::setStatus);
        return GenericCommandResponse.builder()
                .requestReference(response.getReference())
                .pitchRequest(response)
                .build();
    }

    @Override
    protected List<ErrorObject> validate(UpdateRequestStatusCommandRequest request) {
        return this.validator.validate(request);
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_KYC_ADMIN, Role.ROLE_PLATFORM_ADMIN);
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
        RequestStatus status = commandResponse.getPitchRequest().getStatus();
        switch (status) {
            case ACTIVE:
                return INVESTMENT_ACTIVATED;
            case INACTIVE:
                return INVESTMENT_DEACTIVATED;
            case CANCELLED:
                return INVESTMENT_CANCELLED;
            case COMPLETED:
            default:
                return INVESTMENT_COMPLETED;
        }
    }

    @Override
    public List<String> getAudience(GenericCommandResponse commandResponse) {
        return getAllParties(commandResponse.getPitchRequest());
    }

    @Override
    public String convertToUserNotificationsMessage(UserNotificationBuilderParameters<GenericCommandResponse, PitchRequest> parameters) throws JsonProcessingException {
        UserNotificationType type = this.getUserNotificationType(parameters.getCommandResponse());
        String status;
        switch (type) {
            case INVESTMENT_ACTIVATED:
                status = "active";
                break;
            case INVESTMENT_DEACTIVATED:
                status = "inactive";
                break;
            case INVESTMENT_CANCELLED:
                status = "cancelled";
                break;
            case INVESTMENT_COMPLETED:
            default:
                status = "completed";
                break;
        }
        String summary;
        if (parameters.getUserReference().equalsIgnoreCase(parameters.getRequest().getInvestee().getUserReference())) {
            summary = "your Pitch post was marked as: " + status;
        } else {
            summary = "a Pitch post you commented on was marked as: " + status;
        }

        UserNotificationRequestPayload userNotificationInvestmentPayload = UserNotificationRequestPayload.builder()
                .requestReference(parameters.getCommandResponse().getRequestReference())
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
