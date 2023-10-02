
package io.angularpay.pitch.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document("pitch_requests")
public class PitchRequest {

    @Id
    private String id;
    @Version
    private int version;
    @JsonProperty("service_code")
    private String serviceCode;
    private boolean verified;
    @JsonProperty("verified_on")
    private String verifiedOn;
    private Amount amount;
    private String summary;
    private Bargain bargain;
    @JsonProperty("created_on")
    private String createdOn;
    @JsonProperty("equity_percent")
    private int equityPercent;
    private Investee investee;
    private List<Investor> investors;
    @JsonProperty("last_modified")
    private String lastModified;
    private String reference;
    @JsonProperty("request_tag")
    private String requestTag;
    private RequestStatus status;
}
