package com.authapp.athentication.security;

import com.authapp.athentication.service.AppUserservice;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;


@Service
public class CustomOidcService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final OidcUserService oidcUserService = new OidcUserService();
    private final AppUserservice appUserservice;

    public CustomOidcService(AppUserservice appUserservice) {
        this.appUserservice = appUserservice;
    }

    @Override
    public @Nullable OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {

        // Load user details from Google using Spring's default implementation
        OidcUser oidcUser = oidcUserService.loadUser(userRequest);

        String provider = userRequest
                .getClientRegistration()
                .getRegistrationId();

        System.out.println(provider);

        // 2. Extract details from Google
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        String picture = oidcUser.getPicture();


        System.out.println("Email: " + email);
        System.out.println("Name: " + name);
        System.out.println("Picture: " + picture);



        appUserservice.registerOrUpdate(provider, email,name,picture);

        // 4. Return the authenticated Google user to Spring Security
        return oidcUser;
    }
}
