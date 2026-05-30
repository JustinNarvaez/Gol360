package com.pjg360.PJG360;

import com.pjg360.PJG360.enums.UserType;
import com.pjg360.PJG360.model.dtos.AuthResponseDTO;
import com.pjg360.PJG360.model.dtos.LoginRequestDTO;
import com.pjg360.PJG360.model.dtos.PreferencesRequestDTO;
import com.pjg360.PJG360.model.dtos.RegisterRequestDTO;
import com.pjg360.PJG360.model.entities.LocalFan;
import com.pjg360.PJG360.model.entities.VisitFan;
import com.pjg360.PJG360.repositories.*;
import com.pjg360.PJG360.services.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CityRepository cityRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private StadiumRepository stadiumRepository;

    @Mock
    private PreferenceRepository preferenceRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDTO registerDtoLocalFan;
    private RegisterRequestDTO registerDtoVisitFan;
    private PreferencesRequestDTO preferencesDTO;

    @BeforeEach
    void setUp() {
        preferencesDTO = new PreferencesRequestDTO();
        preferencesDTO.setFavNationIds(List.of(1L, 2L));
        preferencesDTO.setFavStadiumIds(List.of(1L));
        preferencesDTO.setFavNotifications(List.of());

        registerDtoLocalFan = RegisterRequestDTO.builder()
                .firstName("Paula")
                .lastName("Martinez")
                .userName("paulam")
                .email("paula@email.com")
                .password("password123")
                .userType(UserType.LOCAL_FAN)
                .preferences(preferencesDTO)
                .build();

        registerDtoVisitFan = RegisterRequestDTO.builder()
                .firstName("Juan")
                .lastName("Lopez")
                .userName("juanl")
                .email("juan@email.com")
                .password("password123")
                .userType(UserType.VISIT_FAN)
                .preferences(preferencesDTO)
                .favCityIds(List.of(1, 2))
                .build();
    }

    // Registro

    @Test
    void register_localFan_exitoso() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(teamRepository.findAllById(any())).thenReturn(List.of());
        when(stadiumRepository.findAllById(any())).thenReturn(List.of());

        AuthResponseDTO result = authService.register(registerDtoLocalFan);

        assertNotNull(result);
        verify(userRepository).save(any(LocalFan.class));
        verify(preferenceRepository).save(any());
    }

    @Test
    void register_visitFan_exitoso() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(teamRepository.findAllById(any())).thenReturn(List.of());
        when(stadiumRepository.findAllById(any())).thenReturn(List.of());
        when(cityRepository.findAllById(any())).thenReturn(List.of());

        AuthResponseDTO result = authService.register(registerDtoVisitFan);

        assertNotNull(result);
        verify(userRepository).save(any(VisitFan.class));
        verify(preferenceRepository).save(any());
    }

    @Test
    void register_emailYaRegistrado_lanzaExcepcion() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerDtoLocalFan));

        assertEquals("El email ya esta registrado", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_userNameYaEnUso_lanzaExcepcion() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerDtoLocalFan));

        assertEquals("El userName ya esta en uso", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_sinPreferencias_lanzaExcepcion() {
        registerDtoLocalFan.setPreferences(null);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerDtoLocalFan));

        assertEquals("Las preferencias son obligatorias", ex.getMessage());
    }

    @Test
    void register_tipoUsuarioInvalido_lanzaExcepcion() {
        registerDtoLocalFan.setUserType(UserType.OPERATOR);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByUserName(anyString())).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(registerDtoLocalFan));

        assertEquals("Tipo de usuario no valido", ex.getMessage());
    }

    // Login

    @Test
    void login_conEmail_exitoso() {
        LocalFan fan = new LocalFan();
        fan.setEmail("paula@email.com");
        fan.setPassword("encodedPassword");

        LoginRequestDTO dto = LoginRequestDTO.builder()
                .identifier("paula@email.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail("paula@email.com")).thenReturn(Optional.of(fan));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        AuthResponseDTO result = authService.login(dto);

        assertNotNull(result);
    }

    @Test
    void login_conUserName_exitoso() {
        LocalFan fan = new LocalFan();
        fan.setUserName("paulam");
        fan.setPassword("encodedPassword");

        LoginRequestDTO dto = LoginRequestDTO.builder()
                .identifier("paulam")
                .password("password123")
                .build();

        when(userRepository.findByEmail("paulam")).thenReturn(Optional.empty());
        when(userRepository.findByUserName("paulam")).thenReturn(Optional.of(fan));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        AuthResponseDTO result = authService.login(dto);

        assertNotNull(result);
    }

    @Test
    void login_usuarioNoEncontrado_lanzaExcepcion() {
        LoginRequestDTO dto = LoginRequestDTO.builder()
                .identifier("noexiste@email.com")
                .password("password123")
                .build();

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.findByUserName(anyString())).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(dto));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    @Test
    void login_contrasenaIncorrecta_lanzaExcepcion() {
        LocalFan fan = new LocalFan();
        fan.setEmail("paula@email.com");
        fan.setPassword("encodedPassword");

        LoginRequestDTO dto = LoginRequestDTO.builder()
                .identifier("paula@email.com")
                .password("wrongPassword")
                .build();

        when(userRepository.findByEmail("paula@email.com")).thenReturn(Optional.of(fan));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.login(dto));

        assertEquals("Contrasena incorrecta", ex.getMessage());
    }

    // Logout

    @Test
    void logout_retornaMensajeExitoso() {
        String resultado = authService.logout();
        assertEquals("Sesion cerrada exitosamente", resultado);
    }
}