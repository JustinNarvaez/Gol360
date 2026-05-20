package com.pjg360.PJG360.model.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rankings")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ranking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer position;
    private Integer points;

    // Usuario en el ranking
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_fan_id", nullable = false)
    private LocalFan localFan;

    // Grupo al que pertenece
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private PollaGroup group;
}