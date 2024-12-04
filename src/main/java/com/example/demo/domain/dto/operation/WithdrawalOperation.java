package com.example.demo.domain.dto.operation;

import com.example.demo.domain.model.Currency;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * Класс, описывающий операцию списывания средств со счета
 */
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WithdrawalOperation extends Operation {

    private final static int MIN_FUNDS_VALUE = 0;

    @Schema(description = "Идентификатор пользователя, выполняющего операцию", example = "21")
    @NotNull(message = "user id is empty")
    private Integer userId;

    @Schema(description = "Идентификатор счета, с которого снимают средства", example = "12")
    @NotNull(message = "account id is empty")
    private Integer accountId;

    @Schema(description = "Количество средств, которые мы хотим снять со счета", example = "10.00")
    @Min(value = MIN_FUNDS_VALUE, message = "funds in the account cannot be negative")
    private BigDecimal funds;

    @Schema(description = "Валюта, в которой проводится операция", example = "USD")
    @NotNull(message = "account id is empty")
    private Currency currency;

}
