package com.example.emailservice.utils;

public class ExceptionUtil {

    /**
     * Traverses the exception chain to find the original cause of the error.
     */
    public static Throwable getRootCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        return (cause != null && cause != throwable) ? getRootCause(cause) : throwable;
    }
}

