package com.example.demo.service.operation;

import com.example.demo.dao.operation.OperationDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
import com.example.demo.domain.dto.operation.WithdrawalOperation;
import com.example.demo.domain.model.Currency;
import com.example.demo.domain.model.User;
import com.example.demo.service.account.AccountService;
import com.example.demo.service.authentication.CurrentUserService;
import com.example.demo.service.currency.CurrencyService;
import com.example.demo.service.user.UserService;
import com.example.demo.tool.exception.NotEnoughFundsInAccount;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

/**
 * Класс, реализующий логику проверки доступа к данным об операциях снятия средств со счета {@link WithdrawalOperation}
 */
@Service("WithdrawalOperationSecurityProxyServiceImpl")
public class WithdrawalOperationSecurityProxyServiceImpl implements OperationService<WithdrawalOperation> {

    private final OperationService<WithdrawalOperation> withdrawalOperationOperationService;

    private final AccountService accountService;

    private final CurrentUserService currentUserService;


    public WithdrawalOperationSecurityProxyServiceImpl(@Qualifier("WithdrawalOperationServiceImpl")
                                                       OperationService<WithdrawalOperation> withdrawalOperationOperationService,
                                                       @Qualifier("AccountServiceImpl") AccountService accountService,
                                                       CurrentUserService currentUserService) {
        this.withdrawalOperationOperationService = withdrawalOperationOperationService;
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, проверяющий доступ пользователя к списыванию средств со счета и вызывающий
     * метод, обрабатывающий операцию снятия средств со счета {@link Account}
     * @param operation {@link WithdrawalOperation} - информация о снятия средств на счета
     */
    @Override
    @Transactional
    public void process(WithdrawalOperation operation) throws NotEnoughFundsInAccount {
        if (!currentUserService.userHasAuthorityToEdit(operation.getUserId())) {
            throw new AccessDeniedException("Attempt to process an operation for another user");
        }
        withdrawalOperationOperationService.process(operation);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях списывания {@link WithdrawalOperation} средств со счета
     * и вызывающий метод возвращающий данные об операции снятия средств {@link WithdrawalOperation} со счета {@link Account}
     * @param id - идентификатор операции, на чтение которой идет запрос
     * @return {@link WithdrawalOperation}  данные об операции
     */
    @Override
    public WithdrawalOperation getById(Integer id) {
        WithdrawalOperation withdrawalOperation = withdrawalOperationOperationService.getById(id);
        if (!currentUserService.userHasAuthorityToView(withdrawalOperation.getUserId())) {
            throw new AccessDeniedException("Attempt to get an operation by id for another user");
        }
        return withdrawalOperation;

    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях зачисления {@link ReplenishmentOperation} средств на счет
     * и вызывающий метод возвращающий информацию о всех операциях пополнения {@link ReplenishmentOperation} на счет {@link Account}
     * с идентификатором равным auditId
     * @param accountId - идентификатор счета, на информацию о списываниях которого идет запрос
     * @return {@link List<WithdrawalOperation>} - список операций пополнения
     */
    @Override
    public List<WithdrawalOperation> getByAccountId(Integer accountId) {
        Account account = accountService.getById(accountId);
        if (!currentUserService.userHasAuthorityToView(account.getUserId())) {
            throw new AccessDeniedException("Attempt to get an operations by account id for another user");
        }
        return withdrawalOperationOperationService.getByAccountId(accountId);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях списывания {@link WithdrawalOperation} средств со счета
     * и вызывающий метод, возвращающий информацию о всех операциях списывания {@link WithdrawalOperation} со счета {@link Account} с
     * идентификатором равным auditId
     * @param userId идентификатор пользователя, на информацию об операциях списываниях которого идет запрос
     * @return {@link List<WithdrawalOperation>} - список операций пополнения
     */
    @Override
    public List<WithdrawalOperation> getByUserId(Integer userId) {
        if (!currentUserService.userHasAuthorityToView(userId)) {
            throw new AccessDeniedException("Attempt to get an operations by user id for another user");
        }
        return withdrawalOperationOperationService.getByUserId(userId);
    }

}
