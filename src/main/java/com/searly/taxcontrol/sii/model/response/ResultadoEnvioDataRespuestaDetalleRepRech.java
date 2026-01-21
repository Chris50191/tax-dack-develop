package com.searly.taxcontrol.sii.model.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 发票发送结果警告和拒绝详情模型类
 * 用于表示发票发送后的警告和拒绝详细信息，包含每个发票的处理状态和错误信息
 */
@XmlRootElement(name = "detalle_rep_rech")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultadoEnvioDataRespuestaDetalleRepRech {
    
    /**
     * 发票号码
     * 被警告或拒绝的发票号码，用于唯一标识一张发票
     */
    @XmlElement(name = "folio")
    private Integer folio;
    
    /**
     * 文档类型
     * 39: 电子发票
     * 41: 免税电子发票
     * 用于标识发票的类型
     */
    @XmlElement(name = "tipo")
    private Integer tipo;
    
    /**
     * 处理状态
     * DOK: 已接受 - 发票已被SII系统接受
     * RPR: 警告 - 发票存在警告信息
     * RCH: 拒绝 - 发票被SII系统拒绝
     * RLV: 已解决 - 警告或拒绝的问题已解决
     */
    @XmlElement(name = "estado")
    private String estado;
    
    /**
     * 状态描述
     * 对当前状态的详细说明，包括警告或拒绝的具体原因
     */
    @XmlElement(name = "descripcion")
    private String descripcion;
    
    /**
     * 错误信息列表
     * 包含所有相关的错误信息，每个错误都包含具体的错误代码和描述
     */
    @XmlElement(name = "error")
    private List<ResultadoEnvioDataRespuestaError> errors;
    
    public Integer getFolio() {
        return folio;
    }
    
    public void setFolio(Integer folio) {
        this.folio = folio;
    }
    
    public Integer getTipo() {
        return tipo;
    }
    
    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public List<ResultadoEnvioDataRespuestaError> getErrors() {
        return errors;
    }
    
    public void setErrors(List<ResultadoEnvioDataRespuestaError> errors) {
        this.errors = errors;
    }
} 