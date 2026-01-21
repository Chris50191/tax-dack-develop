package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"rsaKeyValue"})
public class KeyValue {
    
    @XmlElement(name = "RSAKeyValue")
    private RSAKeyValue rsaKeyValue;
    
    // 构造函数
    public KeyValue() {}
    
    public KeyValue(RSAKeyValue rsaKeyValue) {
        this.rsaKeyValue = rsaKeyValue;
    }
    
    // Getter和Setter方法
    public RSAKeyValue getRsaKeyValue() {
        return rsaKeyValue;
    }
    
    public void setRsaKeyValue(RSAKeyValue rsaKeyValue) {
        this.rsaKeyValue = rsaKeyValue;
    }
}
