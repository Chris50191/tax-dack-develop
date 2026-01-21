package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"x509Certificate"})
public class X509Data {
    
    @XmlElement(name = "X509Certificate", required = true)
    private String x509Certificate;
    
    // 构造函数
    public X509Data() {}
    
    public X509Data(String x509Certificate) {
        this.x509Certificate = x509Certificate;
    }
    
    // Getter和Setter方法
    public String getX509Certificate() {
        return x509Certificate;
    }
    
    public void setX509Certificate(String x509Certificate) {
        this.x509Certificate = x509Certificate;
    }
}
