package org.example.booksharing.contoller;

import jakarta.validation.Valid;
import org.example.booksharing.GlobalExceptionHandler;
import org.example.booksharing.dto.AuthRequest;
import org.example.booksharing.dto.AuthResponse;
import org.example.booksharing.dto.UserDto;
import org.example.booksharing.dto.DtoMapper;
import org.example.booksharing.entities.User;
import org.example.booksharing.repository.UserRepository;
import org.example.booksharing.security.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody AuthRequest request) {
        try {
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("status", 409, "error", "Conflict", "message", "Username already exists"));
            }

            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            // Initialize default values
            user.setPrivateProfile(false);
            user.setPrivateBooks(false);
            
            User savedUser = userRepository.save(user);

            UserDto userDto = DtoMapper.toUserDto(savedUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDto);
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("status", 409, "error", "Conflict", "message", 
                            "Username or email already exists or constraint violation: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "error", "Internal Server Error", 
                            "message", "Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new GlobalExceptionHandler.ResourceNotFoundException("User not found"));

            String token = jwtUtil.generateToken(user.getUsername());

            AuthResponse response = new AuthResponse(
                    token,
                    user.getUsername(),
                    user.getDisplayName(),
                    user.getAvatarUrl()
            );

            return ResponseEntity.ok(response);
        } catch (org.springframework.security.core.AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "error", "Unauthorized", "message", "Invalid credentials"));
        }
    }
}

