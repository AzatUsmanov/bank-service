package com.example.demo.service.currency;

import com.example.demo.client.CentralBankClient;
import com.example.demo.domain.model.Currency;
import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;


/**
 * Класс выполняющий бизнес-логику по получению актуальных курсов у ЦБ РФ
 */
@Service
@AllArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final static double EXCHANGE_RATE_RUBLE_TO_RUBLE = 1;

    private final CentralBankClient centralBankClient;

    /**
     * Метод, возвращающий актуальное отношение курсов валют
     * @param from {@link Currency} - исходный курс валюты
     * @param to {@link Currency} - курс валюты, отношение к которому нужно найти
     * @return отношение курса from к курсу to
     */
    @Override
    public BigDecimal getExchangeRate(Currency from, Currency to) {
        BigDecimal fromExchangeRate = getExchangeRateToRuble(from);
        BigDecimal toExchangeRate = getExchangeRateToRuble(to);
        return fromExchangeRate.divide(toExchangeRate, 4, RoundingMode.DOWN);
    }

    /**
     * Метод, возвращающий обменный курс к RUB для всех валют
     * @param currency {@link Currency} - курс, отношение к RUB которого нужно вернуть
     * @return - отношение currency к RUB
     */
    private BigDecimal getExchangeRateToRuble(Currency currency) {
        if (currency == Currency.RUB) {
            return new BigDecimal(EXCHANGE_RATE_RUBLE_TO_RUBLE);
        } else return getExchangeRateToRubleFromCentralBank(currency);
    }

    /**
     * Метод, делающий запрос к ЦБ РФ и возвращающий обменный курс к RUB для всех валют кроме RUB
     * @param currency {@link Currency} - курс, отношение к RUB которого нужно вернуть
     * @return - отношение currency к RUB
     */
    private BigDecimal getExchangeRateToRubleFromCentralBank(Currency currency) {
        String responseBody = centralBankClient.getExchangeRates();
        JSONObject responseBodyInJson = new JSONObject(responseBody);
        JSONObject currencyInfoList = responseBodyInJson.getJSONObject("Valute");
        JSONObject currencyInfo = currencyInfoList.getJSONObject(currency.toString());
        return currencyInfo.getBigDecimal("Value");
    }

}
