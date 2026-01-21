package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"caratula", "dte"})
public class SetDTE {
    
    @XmlElement(name = "Caratula", required = true)
    private Caratula caratula;
    
    @XmlElement(name = "DTE", required = true)
    private List<DTE> dte;
    
    @XmlAttribute(name = "ID", required = true)
    private String id;
    
    // 构造函数
    public SetDTE() {}
    
    public SetDTE(Caratula caratula, List<DTE> dte, String id) {
        this.caratula = caratula;
        this.dte = dte;
        this.id = id;
    }
    
    // Getter和Setter方法
    public Caratula getCaratula() {
        return caratula;
    }
    
    public void setCaratula(Caratula caratula) {
        this.caratula = caratula;
    }
    
    public List<DTE> getDte() {
        return dte;
    }
    
    public void setDte(List<DTE> dte) {
        this.dte = dte;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
}
