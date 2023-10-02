
package io.angularpay.pitch.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class AddBargainApiModel {

    @JsonProperty("equity_percent")
    private int equityPercent;

    @NotEmpty
    private String comment;
}
