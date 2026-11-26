package com.authapp.athentication.security;


import com.authapp.athentication.entites.User;
import com.authapp.athentication.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService  implements UserDetailsService {
    private final UserRepository userRepository;


    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String useremail)
            throws UsernameNotFoundException {

        User user = userRepository.findByEmail(useremail)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + useremail
                        )
                );

        return new CustomUserDetails(user);
    }
}
