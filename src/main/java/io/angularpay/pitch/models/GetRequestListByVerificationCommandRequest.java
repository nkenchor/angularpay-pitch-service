package io.angularpay.pitch.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class GetRequestListByVerificationCommandRequest extends AccessControl {

    private boolean verified;

    @NotNull
    @Valid
    private Paging paging;

    GetRequestListByVerificationCommandRequest(AuthenticatedUser authenticatedUser) {
        super(authenticatedUser);
    }
}
