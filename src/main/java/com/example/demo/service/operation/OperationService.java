package com.example.demo.service.operation;

import com.example.demo.domain.dto.operation.Operation;
import com.example.demo.tool.exception.NotEnoughFundsInAccount;

import java.util.List;

public interface OperationService <T extends Operation> {

    void process(T operation) throws NotEnoughFundsInAccount;

    T getById(Integer id);

    List<T> getByAccountId(Integer accountId);

    List<T> getByUserId(Integer userId);

}
