package com.pjg360.PJG360.model.entities;


import com.pjg360.PJG360.enums.MatchStatus;
import com.pjg360.PJG360.enums.TournamentPhase;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column  // ← quita el nullable = false
    private LocalDateTime dateTime;

    // Nombre original de openfootball: "Matchday 1", "Round of 16", etc.
    private String roundName;

    // Fase del torneo clasificada
    @Enumerated(EnumType.STRING)
    private TournamentPhase phase;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "home_team_id")
    private Team homeTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "away_team_id")
    private Team awayTeam;

    private Integer homeScore;
    private Integer awayScore;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stadium_id")
    private Stadium stadium;


    private String matchGroup;

    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL)
    private List<MatchEvent> eventMatches;

    // Indica si fue actualizado desde la API
    private Boolean refreshed;
}
