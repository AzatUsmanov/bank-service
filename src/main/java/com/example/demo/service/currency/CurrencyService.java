package com.example.demo.service.currency;

import com.example.demo.domain.model.Currency;

import java.math.BigDecimal;

public interface CurrencyService {

    BigDecimal getExchangeRate(Currency from, Currency to);

}
