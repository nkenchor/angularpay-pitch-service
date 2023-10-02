package io.angularpay.pitch.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EquityPercentModel {

    @JsonProperty("equity_percent")
    private int equityPercent;
}
