package com.pjg360.PJG360.services.impl;

import com.pjg360.PJG360.model.dtos.PreferencesRequestDTO;
import com.pjg360.PJG360.model.dtos.PreferencesResponseDTO;
import com.pjg360.PJG360.model.entities.Preference;
import com.pjg360.PJG360.model.entities.VisitFan;
import com.pjg360.PJG360.model.mappers.CityMapper;
import com.pjg360.PJG360.model.mappers.PreferencesMapper;
import com.pjg360.PJG360.repositories.*;
import org.springframework.stereotype.Service;
import com.pjg360.PJG360.services.IPreferenceService;

@Service
public class PreferenceServiceImpl implements IPreferenceService {

    private final UserRepository userRepository;
    private final PreferenceRepository preferenceRepository;
    private final TeamRepository teamRepository;
    private final StadiumRepository stadiumRepository;
    private final CityRepository cityRepository;

    public PreferenceServiceImpl(UserRepository userRepository,
                                 PreferenceRepository preferenceRepository,
                                 TeamRepository teamRepository,
                                 StadiumRepository stadiumRepository,
                                 CityRepository cityRepository) {
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
        this.teamRepository = teamRepository;
        this.stadiumRepository = stadiumRepository;
        this.cityRepository = cityRepository;
    }

    @Override
    public PreferencesResponseDTO savePreferences(Long userId, PreferencesRequestDTO dto) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Preference pref = (user.getPreferences() != null)
                ? user.getPreferences() : new Preference();

        pref.setUser(user);
        pref.setFavNotifications(dto.getFavNotifications());

        pref.setFavNations(teamRepository.findAllById(
                dto.getFavNationIds().stream().map(Long::intValue).toList()
        ));

        pref.setFavStadiums(stadiumRepository.findAllById(
                dto.getFavStadiumIds().stream().map(Long::intValue).toList()
        ));

        preferenceRepository.save(pref);

        PreferencesResponseDTO response = PreferencesMapper.toDTO(pref);

        if (user instanceof VisitFan vf && dto.getFavCityIds() != null) {
            vf.setFavCities(cityRepository.findAllById(
                    dto.getFavCityIds().stream().map(Long::intValue).toList()
            ));
            userRepository.save(vf);
            response.setFavCities(CityMapper.toDTOList(vf.getFavCities()));
        }

        return response;
    }

    @Override
    public PreferencesResponseDTO getPreferences(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        PreferencesResponseDTO response = PreferencesMapper.toDTO(user.getPreferences());

        if (user instanceof VisitFan vf) {
            response.setFavCities(CityMapper.toDTOList(vf.getFavCities()));
        }

        return response;
    }
}