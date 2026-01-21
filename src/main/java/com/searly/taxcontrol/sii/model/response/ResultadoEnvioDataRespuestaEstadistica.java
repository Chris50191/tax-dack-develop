package com.searly.taxcontrol.sii.model.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 发票发送结果统计信息模型类
 * 用于表示发票发送后的统计信息，包括各类状态的数量统计
 * 用于跟踪和监控发票发送的整体情况
 */
@XmlRootElement(name = "estadistica")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultadoEnvioDataRespuestaEstadistica {
    
    /**
     * 文档类型
     * 39: 电子发票 - 标准电子发票
     * 41: 免税电子发票 - 免税类型的电子发票
     * 用于区分不同类型的发票
     */
    @XmlElement(name = "tipo")
    private Integer tipo;
    
    /**
     * 已通知数量
     * 已发送到SII系统的发票数量
     * 表示已提交处理的发票总数
     */
    @XmlElement(name = "informados")
    private Integer informados;
    
    /**
     * 已接受数量
     * SII系统已接受的发票数量
     * 表示成功处理的发票数量
     */
    @XmlElement(name = "aceptados")
    private Integer aceptados;
    
    /**
     * 已拒绝数量
     * SII系统拒绝的发票数量
     * 表示处理失败的发票数量
     */
    @XmlElement(name = "rechazados")
    private Integer rechazados;
    
    /**
     * 警告数量
     * SII系统发出警告的发票数量
     * 表示需要关注的发票数量
     */
    @XmlElement(name = "reparos")
    private Integer reparos;
    
    public Integer getTipo() {
        return tipo;
    }
    
    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }
    
    public Integer getInformados() {
        return informados;
    }
    
    public void setInformados(Integer informados) {
        this.informados = informados;
    }
    
    public Integer getAceptados() {
        return aceptados;
    }
    
    public void setAceptados(Integer aceptados) {
        this.aceptados = aceptados;
    }
    
    public Integer getRechazados() {
        return rechazados;
    }
    
    public void setRechazados(Integer rechazados) {
        this.rechazados = rechazados;
    }
    
    public Integer getReparos() {
        return reparos;
    }
    
    public void setReparos(Integer reparos) {
        this.reparos = reparos;
    }
} 