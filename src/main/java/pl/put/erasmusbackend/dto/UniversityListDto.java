package pl.put.erasmusbackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniversityListDto {
    @JsonProperty("id")
    private Integer id;

    @JsonProperty("name")
    private String name;
}
