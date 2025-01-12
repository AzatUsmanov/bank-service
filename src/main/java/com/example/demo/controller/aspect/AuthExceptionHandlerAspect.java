package com.example.demo.controller.aspect;

import com.example.demo.domain.dto.authentication.SignUpRequest;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.tool.exception.NotEnoughFundsInAccountException;
import com.example.demo.tool.exception.NotUniqueEmailException;
import com.example.demo.tool.exception.NotUniqueUsernameException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;

@Aspect
@Component
public class AuthExceptionHandlerAspect {

    @Around("execution(org.springframework.web.servlet.ModelAndView" +
            " com.example.demo.controller.AuthenticationController.signUp(..))")
    public ModelAndView handleProcessWithdrawalOperationException(ProceedingJoinPoint joinPoint) throws Throwable {
        var operation = ((SignUpRequest) joinPoint.getArgs()[0]);
        ModelAndView modelAndView = new ModelAndView("auth/sign-up.html", HttpStatus.BAD_REQUEST);
        modelAndView.getModel().put("request", new SignUpRequest());

        try {
            return (ModelAndView) joinPoint.proceed();
        } catch (NotUniqueUsernameException e) {
            modelAndView.getModel().put("globalError", "Not unique username");
        } catch (NotUniqueEmailException e) {
            modelAndView.getModel().put("globalError", "Not unique email");
        }
        return modelAndView;
    }


}
