package com.novatech.paginaweb;

import com.novatech.paginaweb.dao.UsuarioRepository;
import com.novatech.paginaweb.model.Usuario;
import com.novatech.paginaweb.service.impl.UsuarioServiceImpl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    // Usamos el Object de Java para que Mockito cree un objeto simulado genérico
    // Esto evita que el compilador busque la clase JavaMailSender que está rota
    @Mock(strictness = Mock.Strictness.LENIENT)
    private Object mailSender;

    @InjectMocks
    private UsuarioServiceImpl usuarioService;

    // --- PRUEBAS DE SEGURIDAD / SPRING SECURITY (loadUserByUsername) ---

    @Test
    void testLoadUserByUsernameExitosamente() {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setCorreo("admin@novatech.com");
        usuarioMock.setContrasena("$2a$10$EnmascaradoBCryptEjemplo");
        usuarioMock.setRol("ADMIN");
        usuarioMock.setBloqueadoHasta(null);

        when(usuarioRepository.findByCorreo("admin@novatech.com")).thenReturn(Optional.of(usuarioMock));

        String correoAbuscar = "admin@novatech.com";
        UserDetails userDetails = usuarioService.loadUserByUsername(correoAbuscar);

        assertNotNull(userDetails);
        assertEquals("admin@novatech.com", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        verify(usuarioRepository, times(1)).findByCorreo("admin@novatech.com");
    }

    @Test
    void testLoadUserByUsernameFallaCuentaBloqueada() {
        Usuario usuarioBloqueado = new Usuario();
        usuarioBloqueado.setCorreo("bloqueado@novatech.com");
        usuarioBloqueado.setBloqueadoHasta(LocalDateTime.now().plusMinutes(15)); 

        when(usuarioRepository.findByCorreo("bloqueado@novatech.com")).thenReturn(Optional.of(usuarioBloqueado));

        assertThrows(LockedException.class, () -> {
            usuarioService.loadUserByUsername("bloqueado@novatech.com");
        });
    }

    @Test
    void testLoadUserByUsernameFallaUsuarioNoExiste() {
        when(usuarioRepository.findByCorreo("noexiste@novatech.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            usuarioService.loadUserByUsername("noexiste@novatech.com");
        });
    }

    // --- PRUEBAS DE REGISTRO DE USUARIO (Validaciones Guava) ---

    @Test
    void testRegistrarNuevoUsuarioExitosamenteYAsignaRolPorDefecto() {
        Usuario inputUsuario = new Usuario();
        inputUsuario.setNombre("Yorman");
        inputUsuario.setCorreo("yorman@novatech.com");
        inputUsuario.setContrasena("NovaTech2026@"); 
        inputUsuario.setRol(""); 

        when(usuarioRepository.findByCorreo("yorman@novatech.com")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(i -> i.getArgument(0));

        Usuario resultado = usuarioService.registrarNuevoUsuario(inputUsuario);

        assertNotNull(resultado);
        assertEquals("Usuario", resultado.getRol());
        assertEquals(0, resultado.getIntentosFallidos());
        assertNotEquals("NovaTech2026@", resultado.getContrasena());
        assertTrue(resultado.getContrasena().length() > 10);

        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void testRegistrarUsuarioFallaContrasenaInsegura() {
        Usuario usuarioInseguro = new Usuario();
        usuarioInseguro.setNombre("Carlos");
        usuarioInseguro.setCorreo("carlos@novatech.com");
        usuarioInseguro.setContrasena("12345"); 

        IllegalArgumentException excepcion = assertThrows(IllegalArgumentException.class, () -> {
            usuarioService.registrarNuevoUsuario(usuarioInseguro);
        });

        assertTrue(excepcion.getMessage().contains("La contraseña debe tener al menos 8 caracteres"));
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // --- PRUEBAS DE RECUPERACIÓN (Evitando llamadas directas de Mail) ---

    @Test
        void testGenerarTokenRecuperacionSinDependenciaMail() throws Exception {
            // 1. ARRANGE
            String correoDestino = "soporte@novatech.com";
            Usuario usuarioExistente = new Usuario();
            usuarioExistente.setCorreo(correoDestino);

            when(usuarioRepository.findByCorreo(correoDestino)).thenReturn(Optional.of(usuarioExistente));

            // Forzamos a la JVM a cargar la interfaz usando su ruta de paquete de producción por String
            Class<?> mailSenderClass = Class.forName("org.springframework.mail.javamail.JavaMailSender");
            
            // Creamos el mock tipado dinámicamente con Mockito
            Object mockMailSenderActual = mock(mailSenderClass);
            
            // Inyectamos a la fuerza el mock que ahora SÍ es del tipo correcto (JavaMailSender)
            org.springframework.test.util.ReflectionTestUtils.setField(usuarioService, "mailSender", mockMailSenderActual);

            // 2. ACT
            String tokenGenerado = usuarioService.generarTokenRecuperacion(correoDestino);

            // 3. ASSERT
            assertNotNull(tokenGenerado);
            assertEquals(8, tokenGenerado.length(), "El token debe tener 8 caracteres.");
    }
}