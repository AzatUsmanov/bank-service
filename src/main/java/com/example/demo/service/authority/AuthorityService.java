package com.example.demo.service.authority;

import com.example.demo.domain.model.Authority;

import java.util.List;

public interface AuthorityService {


    List<Authority> getByUserId(Integer userId);

    void saveByUserId(List<Authority> authorities, Integer userId);
}
