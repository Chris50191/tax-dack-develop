package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class FRMT {
    
    @XmlValue
    private String value;
    
    @XmlAttribute(name = "algoritmo", required = true)
    private final String algoritmo = "SHA1withRSA";
    
    // 构造函数
    public FRMT() {}
    
    public FRMT(String value) {
        this.value = value;
    }
    
    // Getter和Setter方法
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    public String getAlgoritmo() {
        return algoritmo;
    }
}
