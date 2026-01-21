package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"transforms", "digestMethod", "digestValue"})
public class Reference {
    
    @XmlElement(name = "Transforms")
    private Transforms transforms;
    
    @XmlElement(name = "DigestMethod", required = true)
    private DigestMethod digestMethod;
    
    @XmlElement(name = "DigestValue", required = true)
    private String digestValue;
    
    @XmlAttribute(name = "URI", required = true)
    private String uri = "";
    
    // 构造函数
    public Reference() {}
    
    public Reference(Transforms transforms, DigestMethod digestMethod, String digestValue) {
        this.transforms = transforms;
        this.digestMethod = digestMethod;
        this.digestValue = digestValue;
    }
    
    // Getter和Setter方法
    public Transforms getTransforms() {
        return transforms;
    }
    
    public void setTransforms(Transforms transforms) {
        this.transforms = transforms;
    }
    
    public DigestMethod getDigestMethod() {
        return digestMethod;
    }
    
    public void setDigestMethod(DigestMethod digestMethod) {
        this.digestMethod = digestMethod;
    }
    
    public String getDigestValue() {
        return digestValue;
    }
    
    public void setDigestValue(String digestValue) {
        this.digestValue = digestValue;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
}
