package com.crypto.portfolio.config;

import com.crypto.portfolio.security.CustomUserDetailsService;
import com.crypto.portfolio.security.CustomAuthenticationEntryPoint;
import com.crypto.portfolio.security.handler.CustomAccessDeniedHandler;
import com.crypto.portfolio.security.handler.OAuth2LoginSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    //Container chứa tất cả các filter(middleware) của Spring security dùng để xác thực request trước khi đưa cho controller ()(
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                //Disable CSRF vì ta dùng Stateless REST API
                .csrf(AbstractHttpConfigurer::disable)
                // Cấu hình CORS Domain, method, Header cho phép từ phía FE
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                //Cấu hình quyền truy cập
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/login/**",
                                "/oauth2/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/auth/oauth2/authorization/**",
                                "/actuator/**"
                        ).permitAll()
                        // Admin Role
                        .requestMatchers("/api/admin/**", "/api/assets/manage/**").hasRole("ADMIN")
                        // Feature Role
                        .requestMatchers("/api/analysis/advanced/**").hasAnyRole("PREMIUM", "ADMIN")
                        .anyRequest().authenticated()
                )
                //Cấu hình Session Management: Không lưu session trên server
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //Đăng ký AuthenticationProvider
                .authenticationProvider(authenticationProvider())
                //Bật OAuth2 Login (Google, Facebook, X)
                .oauth2Login(oauth2 -> oauth2
                        //Chuyển hướng đến backend để lấy code từ GG => đưa cho GG => Lấy thông tin từ profile GG
                        .redirectionEndpoint(redirection -> redirection
                                .baseUri("/auth/callback/*")
                        )
                        //Định nghĩa endpoint để bắt đầu OAuth2 login flow
                        .authorizationEndpoint(auth -> auth
                                .baseUri("/auth/oauth2/authorization")
                        )
                        .successHandler(oAuth2LoginSuccessHandler)
                )
                .oauth2ResourceServer(oauth2 ->
                        oauth2.jwt(Customizer.withDefaults()))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler));

        return http.build();
    }

    //Security dành cho admin
//    @Bean
//    @Order(1)
//    public SecurityFilterChain adminSecurityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
//        http
//                // 1. Chỉ áp dụng cho các URL bắt đầu bằng /admin/
//                .securityMatcher("/admin/**")
//
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//
//                // 2. Cấu hình quyền truy cập cụ thể cho Admin
//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().hasRole("ADMIN") // Chỉ cho phép người dùng có ROLE_ADMIN
//                )
//
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authenticationProvider(authenticationProvider())
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    // Được AuthenticationManager sử dụng để xác thực người dùng
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    // Dùng để điều phối các AuthenticationProvider nào để xác thực
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Cung cấp cấu hình cho CorsFilter (không phải middleware).Cho phép React/Postman gọi API.
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 1. Cho phép Frontend gọi vào
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:5173"));

        // 2. Cho phép các phương thức
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 3. Cho phép Frontend GỬI LÊN các header này
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "X-XSRF-TOKEN" // FE gửi mã CSRF lên được
        ));

        // 4. Cho phép sử dụng Cookie (HttpOnly) - QUAN TRỌNG
        configuration.setAllowCredentials(true);

        // 5. --- BỔ SUNG: Cho phép Frontend ĐỌC ĐƯỢC các header trả về ---
        // Giúp Frontend debug được Cookie hoặc đọc Token nếu trả về trong Header
        configuration.setExposedHeaders(List.of("Set-Cookie", "Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }


}