package com.backend.BackendJWT.Services;

import com.backend.BackendJWT.Models.Auth.*;
import com.backend.BackendJWT.Config.Jwt.JwtService;
import com.backend.BackendJWT.Repositories.Auth.RoleRepository;
import com.backend.BackendJWT.Repositories.Auth.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private RoleRepository roleRepository;

    public AuthResponse login(LoginRequest request) {
        try {
            // Intenta autenticar al usuario usando el RUT y la contraseña
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getRut(), request.getPassword()));

            // Busca el usuario en el repositorio usando el RUT
            UserDetails user = userRepository.findByRut(request.getRut())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            // Genera el token JWT para el usuario
            String token = jwtService.getToken(user);

            // Retorna la respuesta con el token
            return AuthResponse.builder()
                    .token(token)
                    .build();
        } catch (AuthenticationException e) {
            // Manejo de errores de autenticación
            throw new RuntimeException("Error de autenticación: " + e.getMessage());
        } catch (Exception e) {
            // Manejo de otros errores inesperados
            throw new RuntimeException("Error interno del servidor: " + e.getMessage());
        }
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByRut(request.getRut())) {
            throw new UsernameAlreadyExistsException("Rut '" + request.getRut() + "' is already registered");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email '" + request.getEmail() + "' is already associated with an account");
        }

        // Fetch the default role
        Role defaultRole = roleRepository.findByRoleName(ERole.USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        User user = User.builder()
                .rut(request.getRut())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstname(request.getFirstname())
                .lastname(request.getLastname())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .role(defaultRole)  // Set the fetched role
                .build();

        userRepository.save(user);  // Persist the new user with the role in the database.

        // Generate token and return response
        return AuthResponse.builder()
                .token(jwtService.getToken(user))
                .build();
    }

    public AuthResponse getUser(SearchUserRequest request) {
        try {
            boolean emailExists = userRepository.existsByEmail(request.getEmail());

            if (emailExists) {
                return new AuthResponse("El correo electronico existe");
            } else {
                throw new UsernameNotFoundException("Usuario no encontrado");
            }
        } catch (UsernameNotFoundException e) {
            // Manejo de la excepción específica UsernameNotFoundException
            return new AuthResponse("Error de autenticación"+e.getMessage());
        } catch (Exception e) {
            // Manejo de cualquier otra excepción inesperada
            throw new RuntimeException("Error interno del servidor: " + e.getMessage());
        }
    }



    public AuthResponse updatePassword(UpdatePasswordRequest request) {
        try {
            // Buscar el usuario por su ID y lanzar excepción si no se encuentra
            Optional<User> optionalUser = userRepository.findById(request.getId());
            User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            // Actualizar la contraseña del usuario
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);

            return new AuthResponse("Su contraseña ha sido actualizada");
        } catch (UsernameNotFoundException e) {
            // Manejo de excepción específica si el usuario no es encontrado
            throw new RuntimeException("Error de autenticación "+e.getMessage());
        } catch (Exception e) {
            // Manejo de otras excepciones inesperadas
            throw new RuntimeException("Error interno del servidor: " + e.getMessage());
        }
    }


    // Custom exception classes (create separate files for these)
    public class UsernameAlreadyExistsException extends RuntimeException {
        public UsernameAlreadyExistsException(String message) {
            super(message);
        }
    }

    public class EmailAlreadyExistsException extends RuntimeException {
        public EmailAlreadyExistsException(String message) {
            super(message);
        }
    }
}
