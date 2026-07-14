package com.example.attendance.auth;

import com.example.attendance.employee.EmployeeRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final EmployeeRepository employeeRepository;

    public CustomUserDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String employeeCode) throws UsernameNotFoundException {
        var employee = employeeRepository.findByEmployeeCode(employeeCode)
                .filter(e -> e.isActive())
                .orElseThrow(() -> new UsernameNotFoundException("認証に失敗しました"));

        return new User(
                employee.getEmployeeCode(),
                employee.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + employee.getRole().name()))
        );
    }
}
