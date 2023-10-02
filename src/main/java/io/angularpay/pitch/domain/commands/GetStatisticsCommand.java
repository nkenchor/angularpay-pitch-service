package io.angularpay.pitch.domain.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.angularpay.pitch.adapters.outbound.MongoAdapter;
import io.angularpay.pitch.domain.RequestStatus;
import io.angularpay.pitch.domain.Role;
import io.angularpay.pitch.exceptions.ErrorObject;
import io.angularpay.pitch.models.GetStatisticsCommandRequest;
import io.angularpay.pitch.models.Statistics;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class GetStatisticsCommand extends AbstractCommand<GetStatisticsCommandRequest, List<Statistics>> {

    private final MongoAdapter mongoAdapter;

    public GetStatisticsCommand(ObjectMapper mapper, MongoAdapter mongoAdapter) {
        super("GetStatisticsCommand", mapper);
        this.mongoAdapter = mongoAdapter;
    }

    @Override
    protected String getResourceOwner(GetStatisticsCommandRequest request) {
        return ""; // TODO do this for non-user resources
    }

    @Override
    protected List<Statistics> handle(GetStatisticsCommandRequest request) {
        List<Statistics> statistics = new ArrayList<>();

        long total = this.mongoAdapter.getTotalCount();
        statistics.add(Statistics.builder()
                .name("Total")
                .value(String.valueOf(total))
                .build());

        long verified = this.mongoAdapter.getCountByVerificationStatus(true);
        statistics.add(Statistics.builder()
                .name("Verified")
                .value(String.valueOf(verified))
                .build());

        long unverified = this.mongoAdapter.getCountByVerificationStatus(false);
        statistics.add(Statistics.builder()
                .name("Unverified")
                .value(String.valueOf(unverified))
                .build());

        long active = this.mongoAdapter.getCountByRequestStatus(RequestStatus.ACTIVE);
        statistics.add(Statistics.builder()
                .name("Active")
                .value(String.valueOf(active))
                .build());

        long inactive = this.mongoAdapter.getCountByRequestStatus(RequestStatus.INACTIVE);
        statistics.add(Statistics.builder()
                .name("Inactive")
                .value(String.valueOf(inactive))
                .build());

        long completed = this.mongoAdapter.getCountByRequestStatus(RequestStatus.COMPLETED);
        statistics.add(Statistics.builder()
                .name("Completed")
                .value(String.valueOf(completed))
                .build());

        long cancelled = this.mongoAdapter.getCountByRequestStatus(RequestStatus.CANCELLED);
        statistics.add(Statistics.builder()
                .name("Cancelled")
                .value(String.valueOf(cancelled))
                .build());

        return statistics;
    }

    @Override
    protected List<ErrorObject> validate(GetStatisticsCommandRequest request) {
        return Collections.emptyList();
    }

    @Override
    protected List<Role> permittedRoles() {
        return Arrays.asList(Role.ROLE_PLATFORM_ADMIN, Role.ROLE_PLATFORM_USER);
    }
}
