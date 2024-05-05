package com.attica.athens.security;

import com.attica.athens.user.domain.BaseUser;
import com.attica.athens.user.domain.User;
import java.util.ArrayList;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class CustomUserDetails implements UserDetails {

    private final BaseUser baseUser;

    public CustomUserDetails(BaseUser baseUser) {
        this.baseUser = baseUser;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        Collection<GrantedAuthority> collection = new ArrayList<>();

        collection.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {

                return baseUser.getRole().name();
            }
        });

        return collection;
    }

    @Override
    public String getPassword() {

        if (baseUser instanceof User) {
            return ((User) baseUser).getPassword();
        }

        return "";
    }

    @Override
    public String getUsername() {

        if (baseUser instanceof User) {
            return ((User) baseUser).getUsername();
        }

        return baseUser.getId();
    }

    @Override
    public boolean isAccountNonExpired() {

        return true;
    }

    @Override
    public boolean isAccountNonLocked() {

        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {

        return true;
    }

    @Override
    public boolean isEnabled() {

        return true;
    }
}