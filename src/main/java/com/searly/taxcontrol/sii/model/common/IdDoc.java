package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tipoDTE", "folio", "fchEmis", "indServicio"})
public class IdDoc {
    
    @XmlElement(name = "TipoDTE", required = true)
    private Integer tipoDTE;
    
    @XmlElement(name = "Folio", required = true)
    private String folio;
    
    @XmlElement(name = "FchEmis", required = true)
    private String fchEmis;
    
    @XmlElement(name = "IndServicio", required = true)
    private Integer indServicio;
    
    // 构造函数
    public IdDoc() {}
    
    public IdDoc(Integer tipoDTE, String folio, String fchEmis, Integer indServicio) {
        this.tipoDTE = tipoDTE;
        this.folio = folio;
        this.fchEmis = fchEmis;
        this.indServicio = indServicio;
    }
    
    // Getter和Setter方法
    public Integer getTipoDTE() {
        return tipoDTE;
    }
    
    public void setTipoDTE(Integer tipoDTE) {
        this.tipoDTE = tipoDTE;
    }
    
    public String getFolio() {
        return folio;
    }
    
    public void setFolio(String folio) {
        this.folio = folio;
    }
    
    public String getFchEmis() {
        return fchEmis;
    }
    
    public void setFchEmis(String fchEmis) {
        this.fchEmis = fchEmis;
    }
    
    public Integer getIndServicio() {
        return indServicio;
    }
    
    public void setIndServicio(Integer indServicio) {
        this.indServicio = indServicio;
    }
}
