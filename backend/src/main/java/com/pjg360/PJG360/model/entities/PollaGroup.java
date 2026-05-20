package com.pjg360.PJG360.model.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "polla_groups")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PollaGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // Codigo unico para unirse al grupo
    @Column(unique = true, nullable = false)
    private String pollaCode;

    private Integer totalPoints;

    // Dueno del grupo
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private LocalFan owner;

    // Participantes del grupo
    @ManyToMany
    @JoinTable(
            name = "group_fans",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "fan_id")
    )
    private List<LocalFan> fans;
}