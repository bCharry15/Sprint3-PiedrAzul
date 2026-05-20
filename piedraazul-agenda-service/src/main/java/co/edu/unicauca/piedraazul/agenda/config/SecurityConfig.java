package co.edu.unicauca.piedraazul.agenda.config;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
    .requestMatchers(
        "/api/agenda/health",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    ).permitAll()

    .requestMatchers("/api/auth/**").permitAll()

    .requestMatchers("/api/medicos/**").hasRole("ADMIN")
    .requestMatchers("/api/configuraciones-disponibilidad/**").hasRole("ADMIN")

    .requestMatchers("/api/citas/exportar").hasAnyRole("ADMIN", "AGENDADOR")
    .requestMatchers("/api/citas/**").hasAnyRole("ADMIN", "AGENDADOR", "PACIENTE", "MEDICO")

    .requestMatchers("/api/disponibilidad/**").hasAnyRole("ADMIN", "AGENDADOR", "PACIENTE", "MEDICO")

    .anyRequest().authenticated()
)
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extraerRolesDesdeKeycloak);
        return converter;
    }

    private Collection<GrantedAuthority> extraerRolesDesdeKeycloak(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return List.of();
        }

        @SuppressWarnings("unchecked")
        Collection<String> roles = (Collection<String>) realmAccess.get("roles");

        return roles.stream()
            .map(rol -> new SimpleGrantedAuthority("ROLE_" + rol))
            .collect(Collectors.toList());
    }
}