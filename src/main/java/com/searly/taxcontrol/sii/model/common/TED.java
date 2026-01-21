package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"dd", "frmt"})
public class TED {
    
    @XmlElement(name = "DD", required = true)
    private DD dd;
    
    @XmlElement(name = "FRMT", required = true)
    private FRMT frmt;
    
    @XmlAttribute(name = "version", required = true)
    private final String version = "1.0";
    
    // 构造函数
    public TED() {}
    
    public TED(DD dd, FRMT frmt) {
        this.dd = dd;
        this.frmt = frmt;
    }
    
    // Getter和Setter方法
    public DD getDd() {
        return dd;
    }
    
    public void setDd(DD dd) {
        this.dd = dd;
    }
    
    public FRMT getFrmt() {
        return frmt;
    }
    
    public void setFrmt(FRMT frmt) {
        this.frmt = frmt;
    }
    
    public String getVersion() {
        return version;
    }
}
