
package io.angularpay.pitch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offer {

    @JsonProperty("equity_percent")
    private int equityPercent;
    private String reference;
    private OfferStatus status;
    @JsonProperty("user_reference")
    private String userReference;
    @JsonProperty("deleted_on")
    private String deletedOn;
    @JsonProperty("is_deleted")
    private boolean deleted;
    private String comment;
    @JsonProperty("created_on")
    private String createdOn;
}
