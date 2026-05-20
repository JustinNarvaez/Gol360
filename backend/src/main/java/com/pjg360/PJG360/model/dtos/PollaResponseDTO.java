package com.pjg360.PJG360.model.dtos;

import com.pjg360.PJG360.enums.PollaStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PollaResponseDTO {

    private Long id;
    private PollaStatus status;

    // Info del dueno
    private Long ownerId;
    private String ownerName;

    // Info del grupo
    private Long groupId;
    private String groupName;
    private String pollaCode;    // Codigo para unirse
    private Integer totalFans;   // Cuantos participantes hay

    // Partidos de la polla
    private List<Long> matchIds;
    private Integer totalMatches;
}