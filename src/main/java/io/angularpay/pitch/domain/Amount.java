
package io.angularpay.pitch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amount {

    @NotEmpty
    private String currency;

    @NotEmpty
    private String value;
}
