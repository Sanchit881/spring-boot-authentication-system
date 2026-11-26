package com.authapp.athentication.service.Imp;

import com.authapp.athentication.entites.Provider;
import com.authapp.athentication.entites.Role;
import com.authapp.athentication.entites.User;
import com.authapp.athentication.repository.RoleRepository;
import com.authapp.athentication.repository.UserRepository;
import com.authapp.athentication.service.AppUserservice;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
public class AppUserserviceImpl implements AppUserservice {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AppUserserviceImpl(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public void registerOrUpdate(   String provider,
                                    String email,
                                    String name,
                                    String picture) {

        // 1. Check if user already exists
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setImage(picture);
                    newUser.setProvider(Provider.GOOGLE);
                    newUser.setEnable(true);
                    return newUser;
                });

        // 2. Update user details
        user.setName(name);
        user.setImage(picture);
        user.setProvider(Provider.GOOGLE);
        user.setEnable(true);

        // 3. Assign default role
        Role role = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() ->
                        new RuntimeException("ROLE_USER not found in database"));

        user.getRoles().add(role);

        // 4. Save user
        userRepository.save(user);
    }


}
