package com.pjg360.PJG360.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RankingResponseDTO {

    private Integer position;
    private Long localFanId;
    private String localFanName;
    private Integer points;
    private Long groupId;
    private String groupName;
}