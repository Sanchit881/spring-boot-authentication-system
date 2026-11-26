package com.authapp.athentication.dtos;

import lombok.*;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseDto {

    private UUID id;
    private String name;
    private String email;
    private String image;
    private Set<RoleDto> roles;
}
