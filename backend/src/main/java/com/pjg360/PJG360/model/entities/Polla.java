package com.pjg360.PJG360.model.entities;

import com.pjg360.PJG360.enums.PollaStatus;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "pollas")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Polla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Dueno de la polla
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private LocalFan owner;

    // Partidos incluidos en la polla
    @ManyToMany
    @JoinTable(
            name = "polla_matches",
            joinColumns = @JoinColumn(name = "polla_id"),
            inverseJoinColumns = @JoinColumn(name = "match_id")
    )
    private List<Match> matches;

    // Grupo de participantes
    @OneToOne
    @JoinColumn(name = "group_id")
    private PollaGroup group;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PollaStatus status;
}