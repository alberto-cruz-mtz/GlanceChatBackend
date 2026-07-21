package alberto.cruz.mtz.glance.chat.backend.configuration.security;

import alberto.cruz.mtz.glance.chat.backend.configuration.security.filter.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, JwtAuthenticationFilter authorizationFilter) {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                //TODO: Volver activar la configuración de CORS
                .cors(cors -> cors.configurationSource(this.corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {
                    http.requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup").permitAll();

                    http.requestMatchers(HttpMethod.POST, "/api/auth/2fa/login").permitAll();
                    http.requestMatchers(HttpMethod.POST, "/api/auth/2fa/generate", "/api/auth/2fa/enable").authenticated();

                    http.requestMatchers(HttpMethod.POST, "/api/auth/devices/request-code", "/api/auth/devices/checked").permitAll();
                    http.requestMatchers(HttpMethod.POST, "/api/auth/devices/authorize").authenticated();

                    http.requestMatchers("/api/profiles/**").authenticated();

                    http.requestMatchers("/ws").permitAll();
                    http.requestMatchers("/swagger-ui/**").permitAll();
                    http.requestMatchers("/swagger-ui.html").permitAll();
                    http.requestMatchers("/v3/api-docs/**").permitAll();

                    http.requestMatchers("/api/chats/**").authenticated();
                    http.requestMatchers(HttpMethod.POST, "/api/upload/presigned-url").authenticated();
                    http.requestMatchers(HttpMethod.GET, "/health").permitAll();

                    http.anyRequest().denyAll();
                })
                .addFilterBefore(authorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(this.passwordEncoder());
        return provider;
    }

    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "capacitor://localhost", "http://localhost"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONAL"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
