package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"da", "frma"})
public class CAF {
    
    @XmlElement(name = "DA", required = true)
    private DA da;
    
    @XmlElement(name = "FRMA", required = true)
    private FRMA frma;
    
    @XmlAttribute(name = "version", required = true)
    private final String version = "1.0";
    
    // 构造函数
    public CAF() {}
    
    public CAF(DA da, FRMA frma) {
        this.da = da;
        this.frma = frma;
    }
    
    // Getter和Setter方法
    public DA getDa() {
        return da;
    }
    
    public void setDa(DA da) {
        this.da = da;
    }
    
    public FRMA getFrma() {
        return frma;
    }
    
    public void setFrma(FRMA frma) {
        this.frma = frma;
    }
    
    public String getVersion() {
        return version;
    }
}
