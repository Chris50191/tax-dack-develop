package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"transform"})
public class Transforms {
    
    @XmlElement(name = "Transform", required = true)
    private List<Transform> transform;
    
    // 构造函数
    public Transforms() {}
    
    public Transforms(List<Transform> transform) {
        this.transform = transform;
    }
    
    // Getter和Setter方法
    public List<Transform> getTransform() {
        return transform;
    }
    
    public void setTransform(List<Transform> transform) {
        this.transform = transform;
    }
}
