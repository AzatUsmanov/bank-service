package com.example.demo.service.authentication;

import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;

public interface CurrentUserService {
    String getCurrentUsername();

    User getCurrentUser();

    Integer getCurrentUserId();

    boolean userHasNoAuthorityToEdit(Integer userId);

    boolean userHasNoAuthorityToView(Integer userId);

    boolean userHasAuthorityToView(String username);

    boolean notEqualToCurrentUserId(Integer id);

    boolean equalToCurrentUsername(String username);

    boolean currentUserHasAuthority(Authority authority);
}
