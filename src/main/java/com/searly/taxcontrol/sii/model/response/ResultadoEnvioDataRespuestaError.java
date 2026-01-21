package com.searly.taxcontrol.sii.model.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 发票发送结果错误信息模型类
 * 用于表示发票发送过程中的错误详情，包含错误的类型、位置和描述信息
 */
@XmlRootElement(name = "error")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultadoEnvioDataRespuestaError {
    
    /**
     * 错误部分
     * ENV: 环境相关错误 - 与系统环境配置相关的错误
     * CRT: 证书相关错误 - 与数字证书相关的错误
     * TED: 电子文档相关错误 - 与电子文档格式相关的错误
     * CAF: 授权文件相关错误 - 与授权文件相关的错误
     * DTE: 电子发票相关错误 - 与发票内容相关的错误
     * HED: 头部信息相关错误 - 与发票头部信息相关的错误
     * DET: 明细信息相关错误 - 与发票明细信息相关的错误
     * REF: 引用信息相关错误 - 与发票引用信息相关的错误
     * DRG: 其他相关错误 - 其他类型的错误
     */
    @XmlElement(name = "seccion")
    private String seccion;
    
    /**
     * 错误行号
     * 错误发生的具体行号，用于定位错误在XML文档中的位置
     */
    @XmlElement(name = "linea")
    private Integer linea;
    
    /**
     * 错误级别
     * 1: 轻微警告 - 不影响发票处理的警告信息
     * 2: 警告 - 需要关注但不影响发票处理的警告
     * 3: 拒绝 - 导致发票被拒绝的错误
     */
    @XmlElement(name = "nivel")
    private Integer nivel;
    
    /**
     * 错误代码
     * SII系统定义的错误代码，用于标识具体的错误类型
     */
    @XmlElement(name = "codigo")
    private Integer codigo;
    
    /**
     * 错误描述
     * 错误的简要说明，提供错误的基本信息
     */
    @XmlElement(name = "descripcion")
    private String descripcion;
    
    /**
     * 错误详情
     * 错误的详细说明，包含具体的错误原因和可能的解决方案
     */
    @XmlElement(name = "detalle")
    private String detalle;
    
    public String getSeccion() {
        return seccion;
    }
    
    public void setSeccion(String seccion) {
        this.seccion = seccion;
    }
    
    public Integer getLinea() {
        return linea;
    }
    
    public void setLinea(Integer linea) {
        this.linea = linea;
    }
    
    public Integer getNivel() {
        return nivel;
    }
    
    public void setNivel(Integer nivel) {
        this.nivel = nivel;
    }
    
    public Integer getCodigo() {
        return codigo;
    }
    
    public void setCodigo(Integer codigo) {
        this.codigo = codigo;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDetalle() {
        return detalle;
    }
    
    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }
} 