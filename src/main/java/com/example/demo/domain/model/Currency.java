package com.example.demo.domain.model;


import lombok.Getter;

import java.util.Arrays;

@Getter
public enum Currency {
    RUB((short) 1),
    USD((short) 2),
    EUR((short) 3);

    private final Short number;

    Currency(Short number) {
        this.number = number;
    }

    public static Currency getByNumber(Short number) {
        return Arrays
                .stream(Currency.values())
                .filter(x -> x.getNumber().equals(number))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
