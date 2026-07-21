package com.banking.exceptions;

public class CreditScoreInsufficientException extends BusinessException {
    public CreditScoreInsufficientException(String message) {
        super(406, message);
    }
}
