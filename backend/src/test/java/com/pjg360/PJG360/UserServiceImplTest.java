package com.pjg360.PJG360;

import com.pjg360.PJG360.model.dtos.ChangePasswordRequestDTO;
import com.pjg360.PJG360.model.dtos.UserResponseDTO;
import com.pjg360.PJG360.model.entities.LocalFan;
import com.pjg360.PJG360.repositories.UserRepository;
import com.pjg360.PJG360.services.impl.UserServiceImpl;
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
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private LocalFan usuario;

    @BeforeEach
    void setUp() {
        usuario = new LocalFan();
        usuario.setFirstName("Paula");
        usuario.setLastName("Martinez");
        usuario.setUserName("paulam");
        usuario.setEmail("paula@email.com");
        usuario.setPassword("encodedPassword");
    }

    // Obtener todo

    @Test
    void getAll_retornaListaDeUsuarios() {
        when(userRepository.findAll()).thenReturn(List.of(usuario));

        List<UserResponseDTO> resultado = userService.getAll();

        assertNotNull(resultado);
        assertFalse(resultado.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getAll_listaVacia_retornaListaVacia() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponseDTO> resultado = userService.getAll();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    // Obtener por ID

    @Test
    void getById_usuarioExiste_retornaDTO() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));

        UserResponseDTO resultado = userService.getById(1);

        assertNotNull(resultado);
        verify(userRepository).findById(1);
    }

    @Test
    void getById_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.getById(99));

        assertEquals("Usuario no encontrado", ex.getMessage());
    }

    // Actualizar datos personales

    @Test
    void updatePersonalData_exitoso() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(userRepository.existsByUserName("paulam")).thenReturn(false);
        when(userRepository.existsByEmail("paula@email.com")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(usuario);

        UserResponseDTO resultado = userService.updatePersonalData(
                1, "Paula", "Martinez", "paulam", "paula@email.com");

        assertNotNull(resultado);
        verify(userRepository).save(usuario);
    }

    @Test
    void updatePersonalData_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updatePersonalData(99, "Paula", "M", "paulam", "paula@email.com"));

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePersonalData_userNameEnUso_lanzaExcepcion() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        // userName diferente al actual y ya existe en BD
        when(userRepository.existsByUserName("otroUser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updatePersonalData(1, "Paula", "M", "otroUser", "paula@email.com"));

        assertEquals("El userName ya está en uso", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePersonalData_emailEnUso_lanzaExcepcion() {
        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(userRepository.existsByUserName("paulam")).thenReturn(false);
        // email diferente al actual y ya existe en BD
        when(userRepository.existsByEmail("otro@email.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.updatePersonalData(1, "Paula", "M", "paulam", "otro@email.com"));

        assertEquals("El email ya está en uso", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    // Cambiar contrasenia

    @Test
    void changePassword_exitoso() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");

        String resultado = userService.changePassword(1, dto);

        assertEquals("Contrasena actualizada exitosamente", resultado);
        verify(userRepository).save(usuario);
    }

    @Test
    void changePassword_usuarioNoExiste_lanzaExcepcion() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        when(userRepository.findById(99)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.changePassword(99, dto));

        assertEquals("Usuario no encontrado", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_contrasenaActualIncorrecta_lanzaExcepcion() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .currentPassword("wrongPassword")
                .newPassword("newPassword123")
                .confirmPassword("newPassword123")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.changePassword(1, dto));

        assertEquals("La contrasena actual es incorrecta", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_contrasenasNuevasNoCoinciden_lanzaExcepcion() {
        ChangePasswordRequestDTO dto = ChangePasswordRequestDTO.builder()
                .currentPassword("password123")
                .newPassword("newPassword123")
                .confirmPassword("differentPassword")
                .build();

        when(userRepository.findById(1)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> userService.changePassword(1, dto));

        assertEquals("Las contrasenas nuevas no coinciden", ex.getMessage());
        verify(userRepository, never()).save(any());
    }
}