
package io.angularpay.pitch.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.angularpay.pitch.domain.Amount;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class CreateRequest {

    @NotEmpty
    private String summary;

    @NotNull
    @Valid
    private Amount amount;

    @NotNull
    @JsonProperty("equity_percent")
    private int equityPercent;
}
