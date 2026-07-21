package com.banking.exceptions;

public class LoanApplicationNotFoundException extends BusinessException {
    public LoanApplicationNotFoundException(String message) {
        super(404, message);
    }
}
