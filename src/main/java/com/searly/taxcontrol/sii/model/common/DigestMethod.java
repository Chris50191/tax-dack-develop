package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * DigestMethod - 摘要方法
 * 指定用于计算摘要的算法
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {})
public class DigestMethod {
    
    @XmlAttribute(name = "Algorithm", required = true)
    private final String algorithm = "http://www.w3.org/2000/09/xmldsig#sha1";
    
    // 构造函数
    public DigestMethod() {}
    
    // Getter 方法
    public String getAlgorithm() {
        return algorithm;
    }
    
    @Override
    public String toString() {
        return "DigestMethod{algorithm='" + algorithm + "'}";
    }
}
