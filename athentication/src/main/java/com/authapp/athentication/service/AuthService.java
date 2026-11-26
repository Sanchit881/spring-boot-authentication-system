package com.authapp.athentication.service;

import com.authapp.athentication.dtos.ResponseDto;
import com.authapp.athentication.dtos.UserDto;

public interface AuthService {

    ResponseDto RegisterUser(UserDto userDto);

    //login

    void forgotPassword(String email);

    void resetPassword(String token, String newPassword);
}
