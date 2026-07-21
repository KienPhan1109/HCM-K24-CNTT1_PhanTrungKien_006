package com.banking.exceptions;

public class BadDebtCustomerException extends BusinessException {
    public BadDebtCustomerException(String message) {
        super(406, message);
    }
}
