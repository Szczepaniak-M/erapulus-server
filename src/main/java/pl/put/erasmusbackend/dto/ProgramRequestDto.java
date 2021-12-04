package pl.put.erasmusbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Builder
public class ProgramRequestDto {
    @NotNull
    @JsonProperty("name")
    private String name;

    @NotNull
    @JsonProperty("abbrev")
    private String abbrev;

    @JsonProperty("description")
    private String description;
}
