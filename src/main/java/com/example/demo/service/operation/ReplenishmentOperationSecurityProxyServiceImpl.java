package com.example.demo.service.operation;

import com.example.demo.dao.operation.OperationDao;
import com.example.demo.domain.dto.Account;
import com.example.demo.domain.dto.operation.ReplenishmentOperation;
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
 * Класс, реализующий логику проверки доступа к данным об операциях пополнения на счет {@link ReplenishmentOperation}
 */
@Service("ReplenishmentOperationSecurityProxyServiceImpl")
public class ReplenishmentOperationSecurityProxyServiceImpl implements OperationService<ReplenishmentOperation> {

    private final OperationService<ReplenishmentOperation> replenishmentOperationOperationService;

    private final AccountService accountService;

    private final CurrentUserService currentUserService;

    public ReplenishmentOperationSecurityProxyServiceImpl(@Qualifier("ReplenishmentOperationServiceImpl")
                                                          OperationService<ReplenishmentOperation> replenishmentOperationOperationService,
                                                          @Qualifier("AccountServiceImpl") AccountService accountService,
                                                          CurrentUserService currentUserService) {
        this.replenishmentOperationOperationService = replenishmentOperationOperationService;
        this.accountService = accountService;
        this.currentUserService = currentUserService;
    }

    /**
     * Метод, проверяющий доступ пользователя к зачислению средств на счет и вызывающий
     * метод, обрабатывающий операцию пополнения средств на счет {@link Account}
     * @param operation {@link ReplenishmentOperation} - информация о пополнении средств на счет
     */
    @Override
    @Transactional
    public void process(ReplenishmentOperation operation) throws NotEnoughFundsInAccount {
        if (!currentUserService.userHasAuthorityToEdit(operation.getUserId())) {
            throw new AccessDeniedException("Attempt to process an operation for another user");
        }
        replenishmentOperationOperationService.process(operation);
    }


    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях зачисления {@link ReplenishmentOperation} средств на счет
     * и вызывающий метод возвращающий данные об операции пополнения средств на счет {@link Account}
     * @param id - идентификатор операции, на чтение которой идет запрос
     * @return {@link ReplenishmentOperation} - данные об операции
     */
    @Override
    public ReplenishmentOperation getById(Integer id) {
        ReplenishmentOperation replenishmentOperation = replenishmentOperationOperationService.getById(id);
        if (!currentUserService.userHasAuthorityToView(replenishmentOperation.getUserId())) {
            throw new AccessDeniedException("Attempt to get an operation by id for another user");
        }
        return replenishmentOperation;
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях зачисления {@link ReplenishmentOperation} средств на счет
     * и вызывающий метод возвращающий информацию о всех операциях пополнения {@link ReplenishmentOperation} на счет {@link Account}
     * с идентификатором равным auditId
     * @param accountId - идентификатор счета, на информацию о пополнениях которого идет запрос
     * @return {@link List<ReplenishmentOperation>} - список операций пополнения
     */
    @Override
    public List<ReplenishmentOperation> getByAccountId(Integer accountId) {
        Account account = accountService.getById(accountId);
        if (!currentUserService.userHasAuthorityToView(account.getUserId())) {
            throw new AccessDeniedException("Attempt to get an operations by account id for another user");
        }
        return replenishmentOperationOperationService.getByAccountId(accountId);
    }

    /**
     * Метод, проверяющий доступ пользователя к чтению данных об операциях зачисления {@link ReplenishmentOperation} средств на счет
     * и вызывающий метод, возвращающий информацию о всех операциях пополнения {@link ReplenishmentOperation}, совершенных пользователем {@link User}
     * с идентификатором равным userId
     * @param userId идентификатор пользователя, на информацию об операциях пополнения которого идет запрос
     * @return {@link List<ReplenishmentOperation>} - список операций пополнения
     */
    @Override
    public List<ReplenishmentOperation> getByUserId(Integer userId) {
        if (!currentUserService.userHasAuthorityToView(userId)) {
            throw new AccessDeniedException("Attempt to get an operations by user id for another user");
        }
        return replenishmentOperationOperationService.getByUserId(userId);
    }

}
