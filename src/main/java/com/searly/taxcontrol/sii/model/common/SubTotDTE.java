package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tpoDTE", "nroDTE"})
public class SubTotDTE {
    
    @XmlElement(name = "TpoDTE", required = true)
    private Integer tpoDTE;
    
    @XmlElement(name = "NroDTE", required = true)
    private Integer nroDTE;
    
    // 构造函数
    public SubTotDTE() {}
    
    public SubTotDTE(Integer tpoDTE, Integer nroDTE) {
        this.tpoDTE = tpoDTE;
        this.nroDTE = nroDTE;
    }
    
    // Getter和Setter方法
    public Integer getTpoDTE() {
        return tpoDTE;
    }
    
    public void setTpoDTE(Integer tpoDTE) {
        this.tpoDTE = tpoDTE;
    }
    
    public Integer getNroDTE() {
        return nroDTE;
    }
    
    public void setNroDTE(Integer nroDTE) {
        this.nroDTE = nroDTE;
    }
}
