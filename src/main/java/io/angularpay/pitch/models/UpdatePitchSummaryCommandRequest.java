package io.angularpay.pitch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotEmpty;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdatePitchSummaryCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotEmpty
    private String summary;

    UpdatePitchSummaryCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
