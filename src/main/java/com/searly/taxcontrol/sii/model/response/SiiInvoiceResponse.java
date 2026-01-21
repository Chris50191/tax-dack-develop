package com.searly.taxcontrol.sii.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SII发票发送JSON响应模型类
 * 专门用于处理SII系统返回的JSON格式响应
 * 
 * 与ResultadoEnvioPost不同，这个类使用Jackson注解处理JSON
 */
public class SiiInvoiceResponse {
    
    /**
     * 发票发送者RUT号码
     */
    @JsonProperty("rut_emisor")
    private String rutEmisor;
    
    /**
     * 实际发送者RUT号码
     */
    @JsonProperty("rut_envia")
    private String rutEnvia;
    
    /**
     * 跟踪ID
     */
    @JsonProperty("trackid")
    private Long trackId;
    
    /**
     * 接收时间
     */
    @JsonProperty("fecha_recepcion")
    private String fechaRecepcion;
    
    /**
     * 处理状态
     */
    @JsonProperty("estado")
    private String estado;
    
    /**
     * 发票文件标识
     */
    @JsonProperty("file")
    private String file;

    // 默认构造函数
    public SiiInvoiceResponse() {
    }

    // Getters and Setters
    public String getRutEmisor() {
        return rutEmisor;
    }

    public void setRutEmisor(String rutEmisor) {
        this.rutEmisor = rutEmisor;
    }

    public String getRutEnvia() {
        return rutEnvia;
    }

    public void setRutEnvia(String rutEnvia) {
        this.rutEnvia = rutEnvia;
    }

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }

    public String getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(String fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "SiiInvoiceResponse{" +
                "rutEmisor='" + rutEmisor + '\'' +
                ", rutEnvia='" + rutEnvia + '\'' +
                ", trackId=" + trackId +
                ", fechaRecepcion='" + fechaRecepcion + '\'' +
                ", estado='" + estado + '\'' +
                ", file='" + file + '\'' +
                '}';
    }
} 