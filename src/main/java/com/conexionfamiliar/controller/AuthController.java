package com.conexionfamiliar.controller;
import com.conexionfamiliar.dto.AuthRequest;
import com.conexionfamiliar.dto.AuthResponse;
import com.conexionfamiliar.dto.UserRequest;
import com.conexionfamiliar.model.entity.User;
import com.conexionfamiliar.security.JwtUtil;
import com.conexionfamiliar.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public Mono<ResponseEntity<User>> register(@RequestBody UserRequest request) {
        return userService.register(request).map(u -> ResponseEntity.ok(u));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest request) {
        return userService.findByEmail(request.getEmail())
                .filter(u -> new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().matches(request.getPassword(), u.getPassword()))
                .map(u -> ResponseEntity.ok(new AuthResponse(jwtUtil.generateToken(u), Long.parseLong(System.getProperty("jwt.expiration","3600")))))
                .switchIfEmpty(Mono.just(ResponseEntity.status(401).body(new AuthResponse(null,0))));
    }
}
