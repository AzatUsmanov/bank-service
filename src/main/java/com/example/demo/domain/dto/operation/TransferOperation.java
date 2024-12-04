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
 * Класс, описывающий операцию перевода средств между счетами
 */
@Data
@SuperBuilder
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TransferOperation extends Operation {

    private final static int MIN_FUNDS_VALUE = 0;

    @Schema(description = "Идентификатор пользователя, выполняющего операцию", example = "21")
    @NotNull(message = "from user id is empty")
    private Integer fromUserId;

    @Schema(description = "Идентификатор пользователя, на счет которого переводятся деньги", example = "21")
    @NotNull(message = "to user id is empty")
    private Integer toUserId;

    @Schema(description = "Идентификатор счета, с которого снимают средства", example = "12")
    @NotNull(message = "from account id is empty")
    private Integer fromAccountId;

    @Schema(description = "Идентификатор счета, на который мы переводим средства", example = "12")
    @NotNull(message = "to account id is empty")
    private Integer toAccountId;

    @Schema(description = "Количество средств, которые мы переводим", example = "10.00")
    @Min(value = MIN_FUNDS_VALUE, message = "funds in the account cannot be negative")
    private BigDecimal funds;

    @Schema(description = "Валюта, в которой проходит операция перевода. Равна валюте счета, с которого совершается перевод", example = "USD")
    @NotNull(message = "account id is empty")
    private Currency fromAccountCurrency;

}
