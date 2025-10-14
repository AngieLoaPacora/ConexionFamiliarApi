package com.conexionfamiliar.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component  //  Esto hace que Spring lo detecte como bean
@RequiredArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        if (!jwtUtil.validateToken(authToken)) {
            return Mono.empty();
        }

        String username = jwtUtil.getUsernameFromToken(authToken);
        return Mono.just(new UsernamePasswordAuthenticationToken(username, null, jwtUtil.getRolesFromToken(authToken)));
    }
}
