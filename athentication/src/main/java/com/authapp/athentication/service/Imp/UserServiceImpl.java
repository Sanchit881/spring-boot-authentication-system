package com.authapp.athentication.service.Imp;

import com.authapp.athentication.dtos.UserDto;
import com.authapp.athentication.entites.Provider;
import com.authapp.athentication.entites.Role;
import com.authapp.athentication.entites.User;
import com.authapp.athentication.exception.ResourceNotFoundException;
import com.authapp.athentication.helper.UserHelper;
import com.authapp.athentication.repository.RoleRepository;
import com.authapp.athentication.repository.UserRepository;
import com.authapp.athentication.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;


@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final ModelMapper modelMapper;
    private final RoleRepository  roleRepository;

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.roleRepository = roleRepository;
    }


    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {

            if (userDto.getEmail() == null || userDto.getEmail().isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }
            if (userRepository.existsByEmail(userDto.getEmail())) {
                throw new IllegalArgumentException("User with given email already exists");
            }
            // if you have extra checks __put here...
            User user = modelMapper.map(userDto, User.class);
            //role assign here to user___for authorization
            //TODO:

            User savedUser = userRepository.save(user);
            return modelMapper.map(savedUser, UserDto.class);

    }

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository
                .findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given email id "));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDto updateUser(UserDto userDto, String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User existingUser = userRepository
                .findById(uId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));

        //we are not going to change email id for this project.
        if (userDto.getName() != null) existingUser.setName(userDto.getName());
        if (userDto.getImage() != null) existingUser.setImage(userDto.getImage());
        if (userDto.getPassword() != null) existingUser.setPassword(userDto.getPassword());
        existingUser.setUpdatedAt(Instant.now());
        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDto.class);
    }

    @Override
    public void deleteUser(String userId) {
        UUID uId = UserHelper.parseUUID(userId);
        User user = userRepository.findById(uId).orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        userRepository.delete(user);
    }

    @Override
    public UserDto getUserById(String userId) {
        User user = userRepository.findById(UserHelper.parseUUID(userId)).orElseThrow(() -> new ResourceNotFoundException("User not found with given id"));
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    @Transactional
    public Iterable<UserDto> getAllUsers() {


            return userRepository
                    .findAll()
                    .stream()
                    .map(user -> modelMapper.map(user, UserDto.class))
                    .toList();
    }
}
