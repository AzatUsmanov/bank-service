package com.example.demo.domain.dto.operation;

import com.example.demo.domain.model.Currency;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Класс, описывающий операцию пополнения средств на счет
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Операция пополнения денег на счет")
public class ReplenishmentOperation extends Operation {

    private final static int MIN_FUNDS_VALUE = 0;

    @Schema(description = "Идентификатор пользователя, выполняющего операцию", example = "21")
    private Integer userId;

    @Schema(description = "Идентификатор счета, на который зачисляют средства", example = "12")
    private Integer accountId;

    @Schema(description = "Количество средств, которые мы хотим положить на счет", example = "10.00")
    @Min(value = MIN_FUNDS_VALUE, message = "funds in the account cannot be negative")
    @NotNull(message = "funds is empty")
    private BigDecimal funds;

    @Schema(description = "Валюта, в которой проводится операция", example = "USD")
    private Currency currency;

}
