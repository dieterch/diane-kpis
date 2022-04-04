package io.myplant.config;

import io.myplant.auth.EnableTokenAuthentication;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableTokenAuthentication(sessionCreationPolicy = STATELESS)
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
}