package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"keyValue", "x509Data"})
public class KeyInfo {
    
    @XmlElement(name = "KeyValue")
    private KeyValue keyValue;
    
    @XmlElement(name = "X509Data")
    private X509Data x509Data;
    
    // 构造函数
    public KeyInfo() {}
    
    public KeyInfo(KeyValue keyValue, X509Data x509Data) {
        this.keyValue = keyValue;
        this.x509Data = x509Data;
    }
    
    // Getter和Setter方法
    public KeyValue getKeyValue() {
        return keyValue;
    }
    
    public void setKeyValue(KeyValue keyValue) {
        this.keyValue = keyValue;
    }
    
    public X509Data getX509Data() {
        return x509Data;
    }
    
    public void setX509Data(X509Data x509Data) {
        this.x509Data = x509Data;
    }
}
