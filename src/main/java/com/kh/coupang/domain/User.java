package com.kh.coupang.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder  // setter대신 사용! (ex. controller에서 ~.set() 대신)
public class User implements UserDetails {

    @Id
    private String id;

    @Column
    private String password;

    @Column
    private String name;

    @Column
    private String phone;

    @Column
    private String address;

    @Column
    private String email;

    @Column
    private String role;  // 권한 관련 컬럼

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { //권한 관련
        ArrayList<GrantedAuthority> authList = new ArrayList<GrantedAuthority>();
        authList.add(new SimpleGrantedAuthority(role));  //권한 관련 컬럼 넣고 리턴
        return authList;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {  // 계정 만료 관련
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {   // 계정 잠금 관련
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {  // 비밀번호 관련
        return true;
    }

    @Override
    public boolean isEnabled() {  // 활성화
        return true;
    }
}
