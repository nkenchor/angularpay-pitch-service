package io.angularpay.pitch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpdateEquityPercentCommandRequest extends AccessControl {

    @NotEmpty
    private String requestReference;

    @NotNull
    @Valid
    private int equityPercent;

    UpdateEquityPercentCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
