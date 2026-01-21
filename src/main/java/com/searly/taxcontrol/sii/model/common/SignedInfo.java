package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"canonicalizationMethod", "signatureMethod", "reference"})
public class SignedInfo {
    
    @XmlElement(name = "CanonicalizationMethod", required = true)
    private CanonicalizationMethod canonicalizationMethod;
    
    @XmlElement(name = "SignatureMethod", required = true)
    private SignatureMethod signatureMethod;
    
    @XmlElement(name = "Reference", required = true)
    private Reference reference;
    
    // 构造函数
    public SignedInfo() {}
    
    public SignedInfo(CanonicalizationMethod canonicalizationMethod, 
                     SignatureMethod signatureMethod, Reference reference) {
        this.canonicalizationMethod = canonicalizationMethod;
        this.signatureMethod = signatureMethod;
        this.reference = reference;
    }
    
    // Getter和Setter方法
    public CanonicalizationMethod getCanonicalizationMethod() {
        return canonicalizationMethod;
    }
    
    public void setCanonicalizationMethod(CanonicalizationMethod canonicalizationMethod) {
        this.canonicalizationMethod = canonicalizationMethod;
    }
    
    public SignatureMethod getSignatureMethod() {
        return signatureMethod;
    }
    
    public void setSignatureMethod(SignatureMethod signatureMethod) {
        this.signatureMethod = signatureMethod;
    }
    
    public Reference getReference() {
        return reference;
    }
    
    public void setReference(Reference reference) {
        this.reference = reference;
    }
}
