package com.searly.taxcontrol.sii.model.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 发票发送结果模型类
 * 用于表示向SII系统发送发票后的响应结果
 * 包含发送者信息、跟踪ID、接收时间、处理状态等信息
 */
@XmlRootElement(name = "ResultadoEnvioPost")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultadoEnvioPost {
    /**
     * 响应状态
     */
    public enum Status {
        REC, // 已接受待处理
        EPR, // 处理中
        RFR, // 格式拒收,XML 格式有效
        RCH,  // 拒收,一般是业务规则校验失败
        ACD,  // 已接受
        FIR,  // 签名拒收
        PRC,  // 部分接受
        DUP,  // 重复
    }

    
    /**
     * 发票发送者RUT号码
     * 表示发送发票的公司或个人的RUT号码
     * 包含校验位
     */
    @XmlElement(name = "rut_emisor")
    private String rutEmisor;
    
    /**
     * c
     * 表示实际执行发送操作的公司或个人的RUT号码
     * 可能与发票发送者不同，例如通过第三方发送
     */
    @XmlElement(name = "rut_envia")
    private String  rutEnvia;
    
    /**
     * 跟踪ID
     * 用于跟踪发票处理状态的唯一标识符
     * 可用于后续查询发票处理结果
     */
    @XmlElement(name = "trackid")
    private Long trackId;
    
    /**
     * 接收时间
     * SII系统接收发票的时间
     * 格式：YYYY-MM-DD HH:mm:ss
     */
    @XmlElement(name = "fecha_recepcion")
    private String fechaRecepcion;
    
    /**
     * 处理状态
     * 表示发票在SII系统中的处理状态
     * 可能的状态包括：处理中、已完成、已拒绝等
     */
    @XmlElement(name = "estado")
    private String estado;
    
    /**
     * 发票文件标识
     * 用于标识发送的发票文件
     * 通常是一个唯一的文件名或标识符
     */
    @XmlElement(name = "file")
    private String file;

    // 请求体
    private String requestJson;
    // 返回体
    private String responseJson;

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

    public String getRequestJson() {
        return this.requestJson;
    }

    public void setRequestJson(String requestJson) {
        this.requestJson = requestJson;
    }

    public String getResponseJson() {
        return this.responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }
}