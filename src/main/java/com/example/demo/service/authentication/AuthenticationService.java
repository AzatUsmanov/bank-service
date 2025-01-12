package com.example.demo.service.authentication;


import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

public interface AuthenticationService {

    void signUp(SignUpRequest request) throws NotUniqueEmailException, NotUniqueUsernameException;

}
