package com.example.demo.service.authentication;

import com.example.demo.domain.dto.authentication.JwtAuthenticationResponse;
import com.example.demo.domain.dto.authentication.SignInRequest;
import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;

public interface AuthenticationService {

    JwtAuthenticationResponse signUp(SignUpRequest request) throws NotUniqueEmailException, NotUniqueUsernameException;

    JwtAuthenticationResponse signIn(SignInRequest request);

}
