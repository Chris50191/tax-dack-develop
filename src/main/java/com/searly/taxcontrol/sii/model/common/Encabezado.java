package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"idDoc", "emisor", "receptor", "totales"})
public class Encabezado {
    
    @XmlElement(name = "IdDoc", required = true)
    private IdDoc idDoc;
    
    @XmlElement(name = "Emisor", required = true)
    private Emisor emisor;
    
    @XmlElement(name = "Receptor", required = true)
    private Receptor receptor;
    
    @XmlElement(name = "Totales", required = true)
    private Totales totales;
    
    // 构造函数
    public Encabezado() {}
    
    public Encabezado(IdDoc idDoc, Emisor emisor, Receptor receptor, Totales totales) {
        this.idDoc = idDoc;
        this.emisor = emisor;
        this.receptor = receptor;
        this.totales = totales;
    }
    
    // Getter和Setter方法
    public IdDoc getIdDoc() {
        return idDoc;
    }
    
    public void setIdDoc(IdDoc idDoc) {
        this.idDoc = idDoc;
    }
    
    public Emisor getEmisor() {
        return emisor;
    }
    
    public void setEmisor(Emisor emisor) {
        this.emisor = emisor;
    }
    
    public Receptor getReceptor() {
        return receptor;
    }
    
    public void setReceptor(Receptor receptor) {
        this.receptor = receptor;
    }
    
    public Totales getTotales() {
        return totales;
    }
    
    public void setTotales(Totales totales) {
        this.totales = totales;
    }
}
