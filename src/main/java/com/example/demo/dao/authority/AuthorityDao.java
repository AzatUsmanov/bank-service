package com.example.demo.dao.authority;

import com.example.demo.domain.model.Authority;

import java.sql.SQLException;
import java.util.List;

public interface AuthorityDao {


    List<Authority> getByUserId(Integer userId);

    void saveByUserId(Authority authority, Integer userId);
}
