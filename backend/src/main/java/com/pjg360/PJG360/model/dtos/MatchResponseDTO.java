package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchResponseDTO {
    private Long id;
    private LocalDateTime dateTime;
    private String roundName;
    private TournamentPhase phase;
    private MatchStatus status;
    private String homeTeam;
    private String homeTeamCode;
    private String homeTeamFlag;
    private String awayTeam;
    private String awayTeamCode;
    private String awayTeamFlag;
    private Integer homeScore;
    private Integer awayScore;
    private String result;
    private String stadium;
    private Boolean refreshed;
    private String matchGroup;
}