package org.digit.health.registration.web.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Identifier {

    @JsonProperty("type")
    private String type;

    @JsonProperty("identifierId")
    private String identifierId;
}
