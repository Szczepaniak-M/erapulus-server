package com.erapulus.server.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRequestDto {
    
    @NotNull
    @JsonProperty("name")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("universityId")
    private Integer universityId;

    @JsonProperty("programId")
    private Integer programId;

    @JsonProperty("moduleId")
    private Integer moduleId;

    @JsonIgnore
    private String path;

}
