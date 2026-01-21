package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class CanonicalizationMethod {
    
    @XmlAttribute(name = "Algorithm", required = true)
    private final String algorithm = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
    
    // 构造函数
    public CanonicalizationMethod() {}
    
    // Getter方法
    public String getAlgorithm() {
        return algorithm;
    }
}
