package com.bwarelabs.solana_cos_on_demand_sync.exceptions;

/**
 * This exception is used to indicate user-related errors, such as
 * missing objects (404) or access issues (403).
 */
public class UserTypeException extends Exception {
    public UserTypeException(String message) {
        super(message);
    }
}
