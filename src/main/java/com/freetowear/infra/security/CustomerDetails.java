package com.freetowear.infra.security;

import com.freetowear.entity.Customer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class CustomerDetails implements UserDetails, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Customer customer;

    public CustomerDetails(Customer customer) {
        this.customer = customer;
    }

    @Override
    public String getUsername() {
        return customer.getEmail();
    }

    @Override
    public String getPassword() {
        return customer.getPassword();
    }

    public String getId() {
        return customer.getId();
    }

    public String getName() {
        return customer.getName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(customer.getRole().name()));
    }

    @Override
    public boolean isEnabled() {
        return customer.getActive();
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }
}