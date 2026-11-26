package com.authapp.athentication.service.Imp;

import com.authapp.athentication.dtos.RoleDto;
import com.authapp.athentication.entites.Role;
import com.authapp.athentication.repository.RoleRepository;
import com.authapp.athentication.service.RoleService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImp implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    public RoleServiceImp(RoleRepository roleRepository,
                          ModelMapper modelMapper) {
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        Role role = modelMapper.map(roleDto, Role.class);

        Role savedRole = roleRepository.save(role);

        return modelMapper.map(savedRole, RoleDto.class);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .toList();
    }
}
