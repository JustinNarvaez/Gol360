package com.pjg360.PJG360.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PollaRequestDTO {

    private Long ownerId;        // ID del LocalFan que crea la polla
    private String groupName;    // Nombre del grupo
    private List<Long> matchIds; // IDs de partidos incluidos
}