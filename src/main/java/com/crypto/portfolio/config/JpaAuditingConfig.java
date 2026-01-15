package com.crypto.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class JpaAuditingConfig {

    private static final String SYSTEM = "SYSTEM";
    private static final String ANONYMOUS = "ANONYMOUS";

    @Bean
    public AuditorAware<String> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            // Cron job
            if (auth == null) {
                return Optional.of(SYSTEM);
            }

            // Oauth2 resource server + jwt
            if (auth instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();
                String email = jwt.getClaimAsString("email");
                return Optional.ofNullable(email)
                        .or(() -> Optional.of(SYSTEM));
            }

            // Anonymous
            if (auth instanceof AnonymousAuthenticationToken || !auth.isAuthenticated()) {
                return Optional.of(ANONYMOUS);
            }

            // Fallback
            return Optional.ofNullable(auth.getName())
                    .or(() -> Optional.of(SYSTEM));
        };
    }
}
