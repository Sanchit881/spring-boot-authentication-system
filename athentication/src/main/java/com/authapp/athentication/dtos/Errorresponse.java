package com.authapp.athentication.dtos;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
public class Errorresponse {
    String message;
    HttpStatus status;
    private String path;
    private Instant timestamp;

    public Errorresponse(String message, HttpStatus status, String path, Instant timestamp) {
        this.message = message;
        this.status = status;
        this.path = path;
        this.timestamp = timestamp;
    }
}
