package com.searly.taxcontrol.verifactu.model;

/**
 * VeriFactu系统异常类
 * 用于封装所有与VeriFactu相关的异常
 */
public class VeriFactuException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误代码
     */
    private String errorCode;
    
    /**
     * 构造函数
     * @param message 错误消息
     */
    public VeriFactuException(String message) {
        super(message);
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param cause 原始异常
     */
    public VeriFactuException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param errorCode 错误代码
     */
    public VeriFactuException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     * @param message 错误消息
     * @param cause 原始异常
     * @param errorCode 错误代码
     */
    public VeriFactuException(String message, Throwable cause, String errorCode) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * 获取错误代码
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    /**
     * 设置错误代码
     * @param errorCode 错误代码
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
} 