package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"signedInfo", "signatureValue", "keyInfo"})
public class Signature {
    
    @XmlElement(name = "SignedInfo", required = true)
    private SignedInfo signedInfo;
    
    @XmlElement(name = "SignatureValue", required = true)
    private String signatureValue;
    
    @XmlElement(name = "KeyInfo", required = true)
    private KeyInfo keyInfo;
    
    // 构造函数
    public Signature() {}
    
    public Signature(SignedInfo signedInfo, String signatureValue, KeyInfo keyInfo) {
        this.signedInfo = signedInfo;
        this.signatureValue = signatureValue;
        this.keyInfo = keyInfo;
    }
    
    // Getter和Setter方法
    public SignedInfo getSignedInfo() {
        return signedInfo;
    }
    
    public void setSignedInfo(SignedInfo signedInfo) {
        this.signedInfo = signedInfo;
    }
    
    public String getSignatureValue() {
        return signatureValue;
    }
    
    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
    }
    
    public KeyInfo getKeyInfo() {
        return keyInfo;
    }
    
    public void setKeyInfo(KeyInfo keyInfo) {
        this.keyInfo = keyInfo;
    }
}
