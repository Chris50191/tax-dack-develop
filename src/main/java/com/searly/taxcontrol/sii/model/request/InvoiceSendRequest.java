package com.searly.taxcontrol.sii.model.request;

import com.searly.taxcontrol.sii.model.common.InvoiceData;

import java.io.InputStream;

/**
 * 发票发送请求参数封装类
 * 
 * 统一封装发票发送所需的所有参数，简化接口调用
 * 
 * @author SII Integration Team
 * @version 1.0
 * @since 2024-12
 */
public class InvoiceSendRequest {

    /**
     * 发送者RUT（不含验证位）
     */
    private String rutSender;

    /**
     * 发送者验证位
     */
    private String dvSender;

    /**
     * 公司RUT（不含验证位）
     */
    private String rutCompany;

    /**
     * 公司验证位
     */
    private String dvCompany;

    /**
     * 发票表头信息
     */
    private InvoiceData invoiceData;

    /**
     * CAF 文件
     */
    private InputStream cafFile;

    /**
     * 可选：Documento 签名使用的证书别名（若为空则按 RUT 自动选择）
     */
    private String aliasDocumento;

    /**
     * 可选：SetDTE 签名使用的证书别名（若为空则按 RUT 自动选择）
     */
    private String aliasSetDte;

    /**
     * 是否使用测试环境
     * 默认为true（测试环境）
     */
    private boolean testMode = true;


    /**
     * 请求得xml, 不是入参，是报错返回
     */
    private String requestJson;

    // 默认构造函数
    public InvoiceSendRequest() {
    }

    // 便捷构造函数
    public InvoiceSendRequest(String rutSender, String dvSender,
                              String rutCompany, String dvCompany,
                              InvoiceData invoiceData) {
        this.rutSender = rutSender;
        this.dvSender = dvSender;
        this.rutCompany = rutCompany;
        this.dvCompany = dvCompany;
        this.invoiceData = invoiceData;
    }

    // Getters and Setters
    public String getRutSender() {
        return rutSender;
    }

    public void setRutSender(String rutSender) {
        this.rutSender = rutSender;
    }

    public String getDvSender() {
        return dvSender;
    }

    public void setDvSender(String dvSender) {
        this.dvSender = dvSender;
    }

    public String getRutCompany() {
        return rutCompany;
    }

    public void setRutCompany(String rutCompany) {
        this.rutCompany = rutCompany;
    }

    public String getDvCompany() {
        return dvCompany;
    }

    public void setDvCompany(String dvCompany) {
        this.dvCompany = dvCompany;
    }

    public InvoiceData getInvoiceData() {
        return this.invoiceData;
    }

    public void setInvoiceData(InvoiceData invoiceData) {
        this.invoiceData = invoiceData;
    }

    public boolean getTestMode() {
        return this.testMode;
    }

    public boolean isTestMode() {
        return testMode;
    }

    public void setTestMode(boolean testMode) {
        this.testMode = testMode;
    }

    /**
     * 获取完整的发送者RUT（包含验证位）
     */
    public String getFullRutSender() {
        return rutSender + "-" + dvSender;
    }

    /**
     * 获取完整的公司RUT（包含验证位）
     */
    public String getFullRutCompany() {
        return rutCompany + "-" + dvCompany;
    }

    /**
     * 手动验证请求参数
     * 
     * @throws IllegalArgumentException 如果验证失败
     */
    public void validate() {
        if (rutSender == null || rutSender.trim().isEmpty()) {
            throw new IllegalArgumentException("发送者RUT不能为空");
        }
        if (dvSender == null || dvSender.trim().isEmpty()) {
            throw new IllegalArgumentException("发送者验证位不能为空");
        }
        if (rutCompany == null || rutCompany.trim().isEmpty()) {
            throw new IllegalArgumentException("公司RUT不能为空");
        }
        if (dvCompany == null || dvCompany.trim().isEmpty()) {
            throw new IllegalArgumentException("公司验证位不能为空");
        }
    }

    @Override
    public String toString() {
        return String.format("InvoiceSendRequest{rutSender='%s', rutCompany='%s', testMode=%s}", 
                           getFullRutSender(), getFullRutCompany(), testMode);
    }

    public InputStream getCafFile() {
        return this.cafFile;
    }

    public void setCafFile(InputStream cafFile) {
        this.cafFile = cafFile;
    }

    public String getAliasDocumento() {
        return aliasDocumento;
    }

    public void setAliasDocumento(String aliasDocumento) {
        this.aliasDocumento = aliasDocumento;
    }

    public String getAliasSetDte() {
        return aliasSetDte;
    }

    public void setAliasSetDte(String aliasSetDte) {
        this.aliasSetDte = aliasSetDte;
    }

    public String getRequestJson() {
        return this.requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }
}