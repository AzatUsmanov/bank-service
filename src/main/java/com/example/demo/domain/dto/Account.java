package com.example.demo.domain.dto;

import com.example.demo.domain.model.Currency;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Класс, описывающий информацию о счете
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    public final static int MIN_FUNDS_VALUE = 0;

    @Schema(description = "идентификатор", example = "312")
    Integer id;

    @Schema(description = "Идентификатор пользователя, выполняющего операцию", example = "21")
    @NotNull(message = "account id is empty")
    Integer userId;

    @Schema(description = "Дата создания операции", example = "2011-11-11")
    @NotNull(message = "empty date of creation")
    Date dateOfCreation;

    @Schema(description = "Количество средств, которые хранятся на счету", example = "10.00")
    @Min(value = MIN_FUNDS_VALUE, message = "funds in the account cannot be negative")
    BigDecimal funds;

    @Schema(description = "Валюта, в которой хранятся средства на счету", example = "USD")
    @NotNull(message = "currency is empty")
    Currency currency;

}


/*
{
    "id" : 1,
    "userId" : 1,
    "dateOfCreation" : "2012-12-31",
    "funds" : 12.12,
    "currency" : "USD"
}
 */
