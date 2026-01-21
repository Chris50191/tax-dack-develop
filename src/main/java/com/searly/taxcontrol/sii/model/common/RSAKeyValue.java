package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"modulus", "exponent"})
public class RSAKeyValue {
    
    @XmlElement(name = "Modulus", required = true)
    private String modulus;
    
    @XmlElement(name = "Exponent", required = true)
    private String exponent;
    
    // 构造函数
    public RSAKeyValue() {}
    
    public RSAKeyValue(String modulus, String exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
    }
    
    // Getter和Setter方法
    public String getModulus() {
        return modulus;
    }
    
    public void setModulus(String modulus) {
        this.modulus = modulus;
    }
    
    public String getExponent() {
        return exponent;
    }
    
    public void setExponent(String exponent) {
        this.exponent = exponent;
    }
}
