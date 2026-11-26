package com.authapp.athentication.service;

public interface AppUserservice {

    void registerOrUpdate(
            String provider,
            String email,
            String name,
            String picture
    );
}
