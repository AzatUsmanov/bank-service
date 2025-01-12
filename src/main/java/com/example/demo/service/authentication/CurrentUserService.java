package com.example.demo.service.authentication;

import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;

public interface CurrentUserService {

    boolean userHasNoAuthorityToEdit(Integer userId);

    boolean userHasNoAuthorityToView(Integer userId);

    boolean userHasNoAuthorityToView(String username);

    Integer getCurrentUserId();

    User getCurrentUser();
}
