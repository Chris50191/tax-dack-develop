package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"d", "h"})
public class RNG {
    
    @XmlElement(name = "D", required = true)
    private Integer d;
    
    @XmlElement(name = "H", required = true)
    private Integer h;
    
    // 构造函数
    public RNG() {}
    
    public RNG(Integer d, Integer h) {
        this.d = d;
        this.h = h;
    }
    
    // Getter和Setter方法
    public Integer getD() {
        return d;
    }
    
    public void setD(Integer d) {
        this.d = d;
    }
    
    public Integer getH() {
        return h;
    }
    
    public void setH(Integer h) {
        this.h = h;
    }
}
