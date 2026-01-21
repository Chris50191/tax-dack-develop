package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"nroLinDR", "tpoMov", "glosaDR", "tpoValor", "valorDR", "indExeDR"})
public class DscRcgGlobal {
    
    @XmlElement(name = "NroLinDR", required = true)
    private Integer nroLinDR;
    
    @XmlElement(name = "TpoMov", required = true)
    private String tpoMov;
    
    @XmlElement(name = "GlosaDR")
    private String glosaDR;
    
    @XmlElement(name = "TpoValor", required = true)
    private String tpoValor;
    
    @XmlElement(name = "ValorDR", required = true)
    private Double valorDR;
    
    @XmlElement(name = "IndExeDR")
    private Integer indExeDR;
    
    // 构造函数
    public DscRcgGlobal() {}
    
    // Getter和Setter方法
    public Integer getNroLinDR() {
        return nroLinDR;
    }
    
    public void setNroLinDR(Integer nroLinDR) {
        this.nroLinDR = nroLinDR;
    }
    
    public String getTpoMov() {
        return tpoMov;
    }
    
    public void setTpoMov(String tpoMov) {
        this.tpoMov = tpoMov;
    }
    
    public String getGlosaDR() {
        return glosaDR;
    }
    
    public void setGlosaDR(String glosaDR) {
        this.glosaDR = glosaDR;
    }
    
    public String getTpoValor() {
        return tpoValor;
    }
    
    public void setTpoValor(String tpoValor) {
        this.tpoValor = tpoValor;
    }
    
    public Double getValorDR() {
        return valorDR;
    }
    
    public void setValorDR(Double valorDR) {
        this.valorDR = valorDR;
    }
    
    public Integer getIndExeDR() {
        return indExeDR;
    }
    
    public void setIndExeDR(Integer indExeDR) {
        this.indExeDR = indExeDR;
    }
}
