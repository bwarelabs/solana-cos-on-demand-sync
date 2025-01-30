package com.bwarelabs.solana_cos_on_demand_sync.exceptions;

/**
 * This exception is used to indicate internal system failures that
 * are not directly caused by user actions.
 */
public class InternalErrorException extends Exception {
    public InternalErrorException(String message, Throwable cause) {
        super(message, cause);
    }
}
