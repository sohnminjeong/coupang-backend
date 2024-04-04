package com.kh.coupang.controller;

import com.kh.coupang.config.TokenProvider;
import com.kh.coupang.domain.User;
import com.kh.coupang.domain.UserDTO;
import com.kh.coupang.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class UserController {

    @Autowired
    private UserService serivce;

    @Autowired
    private TokenProvider tokenProvider;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();  // 비밀번호 암호화


    // 회원가입
    @PostMapping("/signUp")
    public ResponseEntity create(@RequestBody User vo){

        // User Entity에서 @Builder 사용하기 위해 User.builder().build()사용
        // 들어가길 원하는 값은 builder와build사이에 입력
        // password의 경우 암호화때문에 vo.getPassword를 encode처리 해야 함
        User user = User.builder()
                .id(vo.getId())
                .password(passwordEncoder.encode(vo.getPassword()))
                .name(vo.getName())
                .phone(vo.getPhone())
                .email(vo.getEmail())
                .address(vo.getAddress())
                .role("ROLE_USER")   // db에 값이 null로 들어가기 때문에
                .build();

        User result = serivce.create(user);
        UserDTO responseDTO = UserDTO.builder()
                                .id(result.getId())
                                .name(result.getName())
                                .build();
        return ResponseEntity.ok().body(responseDTO);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity login(@RequestBody User vo){
        User user = serivce.login(vo.getId(), vo.getPassword(), passwordEncoder);
        if(user!=null){
            // 로그인 성공 -> 토큰 생성 (TokenProvider 생성)
            String token = tokenProvider.create(user);
            log.info("token1 : " + token);
            UserDTO responseDTO = UserDTO.builder()
                    .id(user.getId())
                    .name(user.getName())
                    .token(token)
                    .build();
            log.info("responseDTO : " + responseDTO);
            return ResponseEntity.ok().body(responseDTO);
        } else {
            // 로그인 실패
            return ResponseEntity.badRequest().build();
        }
    }
}
