package capstone.project.gaze_tracking_interest.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // 🔒 CSRF 토큰 비활성화
                .formLogin(form -> form.disable()) // 🔒 기본 로그인 폼 비활성화
                .httpBasic(basic -> basic.disable()) // 🔒 HTTP Basic 인증도 비활성화
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll() // ✅ /api 경로는 인증 없이 접근 가능
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
