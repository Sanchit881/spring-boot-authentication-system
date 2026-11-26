package com.authapp.athentication.service;

import com.authapp.athentication.dtos.RoleDto;

import java.util.List;

public interface RoleService {
    RoleDto createRole(RoleDto roleDto);

    List<RoleDto> getAllRoles();
}
