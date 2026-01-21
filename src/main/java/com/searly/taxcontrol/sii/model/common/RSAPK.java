package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"m", "e"})
public class RSAPK {
    
    @XmlElement(name = "M", required = true)
    private String m;
    
    @XmlElement(name = "E", required = true)
    private String e;
    
    // 构造函数
    public RSAPK() {}
    
    public RSAPK(String m, String e) {
        this.m = m;
        this.e = e;
    }
    
    // Getter和Setter方法
    public String getM() {
        return m;
    }
    
    public void setM(String m) {
        this.m = m;
    }
    
    public String getE() {
        return e;
    }
    
    public void setE(String e) {
        this.e = e;
    }
}
