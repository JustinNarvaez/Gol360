package com.pjg360.PJG360.repositories;

import com.pjg360.PJG360.model.entities.Ranking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RankingRepository extends JpaRepository<Ranking, Long> {

    // Ranking de un grupo ordenado por puntos DESC
    List<Ranking> findByGroupIdOrderByPointsDesc(Long groupId);

    // Ranking de un usuario en un grupo especifico
    Optional<Ranking> findByGroupIdAndLocalFanId(Long groupId, Long localFanId);
}