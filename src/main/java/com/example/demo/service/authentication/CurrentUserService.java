package com.example.demo.service.authentication;

import com.example.demo.domain.model.Authority;
import com.example.demo.domain.model.User;

public interface CurrentUserService {
    String getCurrentUsername();

    User getCurrentUser();

    Integer getCurrentUserId();

    boolean userHasAuthorityToEdit(Integer userId);

    boolean userHasAuthorityToView(Integer userId);

    boolean userHasAuthorityToView(String username);

    boolean equalToCurrentUserId(Integer id);

    boolean equalToCurrentUsername(String username);

    boolean currentUserHasAuthority(Authority authority);
}
