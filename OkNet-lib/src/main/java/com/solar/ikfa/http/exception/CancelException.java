package com.solar.ikfa.http.exception;

/**
 * @author wujunjie
 * @date 16/5/31
 */
public class CancelException extends Exception {

    public CancelException(){

    }

    public CancelException(String message) {
        super(message);
    }
}
