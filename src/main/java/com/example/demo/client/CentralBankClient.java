package com.example.demo.client;

import com.google.gson.JsonObject;
import feign.Response;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "central-bank",
        url = "${client.central-bank.url}")
public interface CentralBankClient {

    @GetMapping
    String getExchangeRates();

}
