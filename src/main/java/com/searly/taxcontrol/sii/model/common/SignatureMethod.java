package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class SignatureMethod {
    
    @XmlAttribute(name = "Algorithm", required = true)
    private final String algorithm = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
    
    // 构造函数
    public SignatureMethod() {}
    
    // Getter方法
    public String getAlgorithm() {
        return algorithm;
    }
}
