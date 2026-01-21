package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class Transform {
    
    @XmlAttribute(name = "Algorithm", required = true)
    private final String algorithm = "http://www.w3.org/2000/09/xmldsig#enveloped-signature";
    
    // 构造函数
    public Transform() {}
    
    // Getter方法
    public String getAlgorithm() {
        return algorithm;
    }
}
