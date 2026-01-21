package com.searly.taxcontrol.verifactu.model;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发票操作响应
 * 包含注册、查询或作废操作的响应结果
 */
//@Data
@XmlRootElement(name = "Respuesta")
@XmlAccessorType(XmlAccessType.FIELD)
public class InvoiceResponse {
    /**
     * 响应状态
     */
    public enum Status {
        REC, // 已接受待处理
        Correcto, // 成功
        Incorrecto, // 失败
        ParcialmenteCorrecto  // 成功接收但是有错误
    }
    
    /**
     * 状态码
     */
    @XmlElement(name = "CodigoResultado")
    private String resultCode;

    /**
     * 描述信息
     */
    @XmlElement(name = "Descripcion")
    private String description;
    
    /**
     * 操作状态
     */
    @XmlElement(name = "Estado")
    private String status;
    
    /**
     * 注册编号/引用号
     */
    @XmlElement(name = "IdRegistro")
    private String registrationId;
    
    /**
     * 时间戳
     */
    @XmlElement(name = "Timestamp")
    private String timestamp;
    
    /**
     * CSV代码(用于文档验证)
     */
    @XmlElement(name = "CSV")
    private String csv;
    
    /**
     * 二维码数据(Base64编码)
     */
    @XmlElement(name = "QRData")
    private String qrData;
    
    /**
     * NIF
     */
    private String nif;
    
    /**
     * ID版本
     */
    private String idVersion;
    
    /**
     * 注册状态
     */
    private String estadoRegistro;
    
    /**
     * 响应代码
     */
    private String codigoRespuesta;
    
    /**
     * 响应描述
     */
    private String descripcionRespuesta;

    /**
     * 查询的发票号描述
     */
    private VerifactuConsultaResponse.IDFactura consultaIDFactura;

    /**
     * 查询的发票号哈希
     */
    private String consultaHuella;

    /**
     * 查询的发票号状态
     */
    private String consultaEstadoRegistro;

    public String getConsultaEstadoRegistro() {
        return this.consultaEstadoRegistro;
    }

    public void setConsultaEstadoRegistro(String consultaEstadoRegistro) {
        this.consultaEstadoRegistro = consultaEstadoRegistro;
    }

    public VerifactuConsultaResponse.IDFactura getConsultaIDFactura() {
        return this.consultaIDFactura;
    }

    public void setConsultaIDFactura(VerifactuConsultaResponse.IDFactura consultaIDFactura) {
        this.consultaIDFactura = consultaIDFactura;
    }

    public String getConsultaHuella() {
        return this.consultaHuella;
    }

    public void setConsultaHuella(String consultaHuella) {
        this.consultaHuella = consultaHuella;
    }

    /**
     * 验证错误列表
     */
    @XmlElementWrapper(name = "Errores")
    @XmlElement(name = "Error")
    private List<ValidationError> errors = new ArrayList<>();
    
    /**
     * 是否成功
     * @return 如果操作成功返回true
     */
    @XmlTransient
    public boolean isSuccess() {
        return Status.Correcto.name().equals(status)||Status.ParcialmenteCorrecto.name().equals(status);
    }
    
    /**
     * 获取注册状态
     * @return 注册状态
     */
    public String getEstadoRegistro() {
        return estadoRegistro;
    }
    
    /**
     * 获取响应代码
     * @return 响应代码
     */
    public String getCodigoRespuesta() {
        return codigoRespuesta;
    }
    
    /**
     * 获取响应描述
     * @return 响应描述
     */
    public String getDescripcionRespuesta() {
        return descripcionRespuesta;
    }

    /**
     * 验证错误类
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ValidationError {
        /**
         * 错误代码
         */
        @XmlElement(name = "Codigo")
        private String code;
        
        /**
         * 错误消息
         */
        @XmlElement(name = "Mensaje")
        private String message;
        
        /**
         * 错误位置/字段
         */
        @XmlElement(name = "Campo")
        private String field;
    }

    public String getResultCode() {
        return this.resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRegistrationId() {
        return this.registrationId;
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getCsv() {
        return this.csv;
    }

    public void setCsv(String csv) {
        this.csv = csv;
    }

    public String getQrData() {
        return this.qrData;
    }

    public void setQrData(String qrData) {
        this.qrData = qrData;
    }

    public String getNif() {
        return this.nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getIdVersion() {
        return this.idVersion;
    }

    public void setIdVersion(String idVersion) {
        this.idVersion = idVersion;
    }

    public void setEstadoRegistro(String estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }

    public void setCodigoRespuesta(String codigoRespuesta) {
        this.codigoRespuesta = codigoRespuesta;
    }

    public void setDescripcionRespuesta(String descripcionRespuesta) {
        this.descripcionRespuesta = descripcionRespuesta;
    }

    public List<ValidationError> getErrors() {
        return this.errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors;
    }
}