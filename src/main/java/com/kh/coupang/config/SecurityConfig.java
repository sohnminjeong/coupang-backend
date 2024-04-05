package com.kh.coupang.config;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.CorsFilter;

@Configuration  // 설정파일 이라는 뜻
@EnableWebSecurity  // 설정 파일 중 security와 관련되어 있음
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;


    // 특정 http 요청에 대한 웹 기반 보안 구성. 인증/인가 및 로그아웃 설정

    /*
    @Bean  // 빈 등록
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 기본적으로 시큐리티에서 제공되는 것을 사용하지 않음을 의미
        http.csrf(csrf -> csrf.disable());//바깥공격 어떻게 처리할건지
        http.httpBasic(basic -> basic.disable());  // 기본으로 제공하는 보안 사용 x
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));  //쓰지 않겠음 의미
        // 인증된 사람만 허용하는 것 막기(전체 허용 시킴)
        http.authorizeHttpRequests(authorize ->
            authorize
                .requestMatchers("/signUp").permitAll()  // signUp은 허용
                .anyRequest().authenticated() // 인증된 애들만 허용시키겠음
        );
        return http.build();
    }*/

    //위와 동일(http뒤 전부 .으로 연결)
    @Bean  // 빈 등록
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
        // 기본적으로 시큐리티에서 제공되는 것을 사용하지 않음을 의미
                    .csrf(csrf -> csrf.disable())//바깥공격 어떻게 처리할건지
                    .httpBasic(basic -> basic.disable())  // 기본으로 제공하는 보안 사용 x
                    .sessionManagement(session
                            -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    )  //쓰지 않겠음 의미
                    // 인증된 사람만 허용하는 것 막기(전체 허용 시킴)
                    .authorizeHttpRequests(authorize ->
                            authorize
                                .requestMatchers("/signUp", "/login", "/api/public/**").permitAll()  // signUp은 허용
                                    .requestMatchers("/api/product").hasRole("USER")
                                    .anyRequest().authenticated() // 인증된 애들만 허용시키겠음
                    )
                .addFilterAfter(jwtAuthenticationFilter, CorsFilter.class)
                .build();
    }
}
