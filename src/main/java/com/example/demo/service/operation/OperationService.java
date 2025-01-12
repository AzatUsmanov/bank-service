package com.example.demo.service.operation;

import com.example.demo.domain.dto.operation.Operation;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;
import com.example.demo.tool.exception.TransferToNonExistentAccountException;
import com.example.demo.tool.exception.TransferToSameAccountException;

import java.util.List;

public interface OperationService <T extends Operation> {

    void process(T operation) throws NotEnoughFundsInAccountException, TransferToNonExistentAccountException, TransferToSameAccountException;

    T getById(Integer id);

    List<T> getByAccountId(Integer accountId);

    List<T> getByUserId(Integer userId);

}
