package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"nroSTI", "glosaSTI", "ordenSTI", "subTotNetoSTI", "subTotIVASTI", "subTotAdicSTI", "subTotExeSTI", "valSubtotSTI", "lineasDeta"})
public class SubTotInfo {
    
    @XmlElement(name = "NroSTI", required = true)
    private Integer nroSTI;
    
    @XmlElement(name = "GlosaSTI")
    private String glosaSTI;
    
    @XmlElement(name = "OrdenSTI")
    private Integer ordenSTI;
    
    @XmlElement(name = "SubTotNetoSTI")
    private Double subTotNetoSTI;
    
    @XmlElement(name = "SubTotIVASTI")
    private Double subTotIVASTI;
    
    @XmlElement(name = "SubTotAdicSTI")
    private Double subTotAdicSTI;
    
    @XmlElement(name = "SubTotExeSTI")
    private Double subTotExeSTI;
    
    @XmlElement(name = "ValSubtotSTI")
    private Double valSubtotSTI;
    
    @XmlElement(name = "LineasDeta")
    private Integer lineasDeta;
    
    // 构造函数
    public SubTotInfo() {}
    
    // Getter和Setter方法
    public Integer getNroSTI() {
        return nroSTI;
    }
    
    public void setNroSTI(Integer nroSTI) {
        this.nroSTI = nroSTI;
    }
    
    public String getGlosaSTI() {
        return glosaSTI;
    }
    
    public void setGlosaSTI(String glosaSTI) {
        this.glosaSTI = glosaSTI;
    }
    
    public Integer getOrdenSTI() {
        return ordenSTI;
    }
    
    public void setOrdenSTI(Integer ordenSTI) {
        this.ordenSTI = ordenSTI;
    }
    
    public Double getSubTotNetoSTI() {
        return subTotNetoSTI;
    }
    
    public void setSubTotNetoSTI(Double subTotNetoSTI) {
        this.subTotNetoSTI = subTotNetoSTI;
    }
    
    public Double getSubTotIVASTI() {
        return subTotIVASTI;
    }
    
    public void setSubTotIVASTI(Double subTotIVASTI) {
        this.subTotIVASTI = subTotIVASTI;
    }
    
    public Double getSubTotAdicSTI() {
        return subTotAdicSTI;
    }
    
    public void setSubTotAdicSTI(Double subTotAdicSTI) {
        this.subTotAdicSTI = subTotAdicSTI;
    }
    
    public Double getSubTotExeSTI() {
        return subTotExeSTI;
    }
    
    public void setSubTotExeSTI(Double subTotExeSTI) {
        this.subTotExeSTI = subTotExeSTI;
    }
    
    public Double getValSubtotSTI() {
        return valSubtotSTI;
    }
    
    public void setValSubtotSTI(Double valSubtotSTI) {
        this.valSubtotSTI = valSubtotSTI;
    }
    
    public Integer getLineasDeta() {
        return lineasDeta;
    }
    
    public void setLineasDeta(Integer lineasDeta) {
        this.lineasDeta = lineasDeta;
    }
}
