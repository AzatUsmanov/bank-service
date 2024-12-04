package com.example.demo.domain.model;

import lombok.Getter;

@Getter
public enum Authority {

    USER_VIEW((short) 1),
    USER_EDIT((short) 2),
    ADMIN_VIEW((short) 3),
    ADMIN_EDIT((short) 4);

    private final short number;

    Authority(short number) {
        this.number = number;
    }

}
