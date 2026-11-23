package com.skillswap.config;

import com.skillswap.model.AppUser;
import com.skillswap.repo.AppUserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Configuration @EnableWebSecurity
public class SecurityConfig {
    private final AppUserRepository users;
    @Value("${app.jwt-secret}") String secret;
    @Value("${app.cors-origin}") String origin;
    @Value("${app.demo}") boolean demo;
    public SecurityConfig(AppUserRepository users) {this.users=users;}
    @Bean public PasswordEncoder passwordEncoder(){return new BCryptPasswordEncoder(12);}
    private String sign(String payload){
        try {Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));}
        catch(Exception ex){throw new IllegalStateException(ex);}
    }
    public String createToken(AppUser user){String payload=user.id+"."+Instant.now().plusSeconds(86400).getEpochSecond()+"."+user.sessionKey;return payload+"."+sign(payload);}
    public Optional<AppUser> resolve(String token){
        try {String[] p=token.split("\\.");if(p.length!=4)return Optional.empty();
            String data=p[0]+"."+p[1]+"."+p[2];
            if(!MessageDigest.isEqual(sign(data).getBytes(StandardCharsets.UTF_8),p[3].getBytes(StandardCharsets.UTF_8)))return Optional.empty();
            if(Instant.now().getEpochSecond()>Long.parseLong(p[1]))return Optional.empty();
            return users.findById(Long.valueOf(p[0])).filter(u->u.active && java.util.Objects.equals(u.sessionKey,p[2]));
        }catch(Exception ex){return Optional.empty();}
    }
    @Bean SecurityFilterChain chain(HttpSecurity http)throws Exception{
        if(!demo&&(secret.length()<32||secret.contains("DEVELOPMENT_ONLY")))throw new IllegalStateException("A strong JWT_SECRET (32+ chars) must be configured before production startup");
        http.csrf(csrf->csrf.disable()).cors(cors->cors.configurationSource(corsSource()))
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex->ex.authenticationEntryPoint((req,res,authException)->res.sendError(HttpServletResponse.SC_UNAUTHORIZED,"Please sign in")))
            .authorizeHttpRequests(auth->auth.requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
                .requestMatchers("/api/health","/api/auth/login","/api/auth/register","/api/catalog/**").permitAll()
                .requestMatchers(HttpMethod.GET,"/api/checkout/*").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(new OncePerRequestFilter(){
                @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
                    String value=req.getHeader("Authorization");
                    if(value!=null&&value.startsWith("Bearer "))resolve(value.substring(7)).ifPresent(u->{
                        var roles=List.of(new SimpleGrantedAuthority(u.admin?"ROLE_ADMIN":"ROLE_USER"));
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(u,null,roles));
                    });
                    res.setHeader("X-Content-Type-Options","nosniff");
                    res.setHeader("Referrer-Policy","strict-origin-when-cross-origin");
                    res.setHeader("X-Frame-Options","DENY");
                    res.setHeader("Cache-Control","no-store");
                    chain.doFilter(req,res);
                }
            },UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
    @Bean UrlBasedCorsConfigurationSource corsSource(){
        CorsConfiguration config=new CorsConfiguration();
        if(demo) config.setAllowedOriginPatterns(List.of("http://localhost:*","http://127.0.0.1:*"));
        else config.setAllowedOrigins(List.of(origin));
        config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        UrlBasedCorsConfigurationSource source=new UrlBasedCorsConfigurationSource();source.registerCorsConfiguration("/**",config);return source;
    }
}
