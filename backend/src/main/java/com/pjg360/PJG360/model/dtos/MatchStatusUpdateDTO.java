package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.MatchStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchStatusUpdateDTO {

    private MatchStatus status;

    private Integer homeScore;
    private Integer awayScore;
}
