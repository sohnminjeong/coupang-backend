package com.kh.coupang.service;

import com.kh.coupang.domain.User;
import com.kh.coupang.repo.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    // 회원가입
    public User create(User user){
        return userDAO.save(user);
    }

    // 로그인
    public User login(String id, String password, PasswordEncoder encoder){
        User user = userDAO.findById(id).orElse(null);
        if(user!=null && encoder.matches(password, user.getPassword())){
            return user;
        }
        return null;
    }
}
