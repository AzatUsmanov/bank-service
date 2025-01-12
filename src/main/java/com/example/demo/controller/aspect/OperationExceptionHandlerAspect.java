package com.example.demo.controller.aspect;

import com.example.demo.domain.dto.operation.TransferOperation;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;
import com.example.demo.tool.exception.TransferToNonExistentAccountException;
import com.example.demo.tool.exception.TransferToSameAccountException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;


@Aspect
@Component
public class OperationExceptionHandlerAspect {

    @Around("execution(org.springframework.web.servlet.ModelAndView" +
            " com.example.demo.controller.WithdrawalOperationController.process(..))")
    public ModelAndView handleProcessWithdrawalOperationException(ProceedingJoinPoint joinPoint) throws Throwable {
        var operation = ((WithdrawalOperation) joinPoint.getArgs()[0]);
        ModelAndView modelAndView = new ModelAndView("withdrawal/create-form.html", HttpStatus.BAD_REQUEST);
        modelAndView.getModel().put("operation",
                WithdrawalOperation.builder()
                .accountId(operation.getAccountId())
                .build());

        try {
            return (ModelAndView) joinPoint.proceed();
        } catch (NotEnoughFundsInAccountException e) {
            modelAndView.getModel().put("globalError", "Not enough funds in account");
        }
        return modelAndView;
    }

    @Around("execution(org.springframework.web.servlet.ModelAndView" +
            " com.example.demo.controller.TransferOperationController.process(..))")
    public ModelAndView handleProcessTransferOperationException(ProceedingJoinPoint joinPoint) throws Throwable {
        var operation = ((TransferOperation) joinPoint.getArgs()[0]);
        ModelAndView modelAndView = new ModelAndView("transfer/create-form.html", HttpStatus.BAD_REQUEST);
        modelAndView.getModel().put("operation",
                TransferOperation.builder()
                        .fromAccountId(operation.getFromAccountId())
                        .build());

        try {
            return (ModelAndView) joinPoint.proceed();
        } catch (TransferToNonExistentAccountException e) {
            modelAndView.getModel().put("globalError", "Attempt to send to non-existent account");
        }  catch (TransferToSameAccountException e) {
            modelAndView.getModel().put("globalError", "Attempt to send to same account");
        } catch (NotEnoughFundsInAccountException e) {
            modelAndView.getModel().put("globalError", "Not enough funds in account");
        }
        return modelAndView;
    }

}
