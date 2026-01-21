package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"rutRecep", "rznSocRecep", "dirRecep", "cmnaRecep", "ciudadRecep"})
public class Receptor {
    
    @XmlElement(name = "RUTRecep", required = true)
    private String rutRecep;
    
    @XmlElement(name = "RznSocRecep")
    private String rznSocRecep;
    
    @XmlElement(name = "DirRecep")
    private String dirRecep;
    
    @XmlElement(name = "CmnaRecep")
    private String cmnaRecep;
    
    @XmlElement(name = "CiudadRecep")
    private String ciudadRecep;
    
    // 构造函数
    public Receptor() {}
    
    public Receptor(String rutRecep, String rznSocRecep, String dirRecep, 
                   String cmnaRecep, String ciudadRecep) {
        this.rutRecep = rutRecep;
        this.rznSocRecep = rznSocRecep;
        this.dirRecep = dirRecep;
        this.cmnaRecep = cmnaRecep;
        this.ciudadRecep = ciudadRecep;
    }
    
    // Getter和Setter方法
    public String getRutRecep() {
        return rutRecep;
    }
    
    public void setRutRecep(String rutRecep) {
        this.rutRecep = rutRecep;
    }
    
    public String getRznSocRecep() {
        return rznSocRecep;
    }
    
    public void setRznSocRecep(String rznSocRecep) {
        this.rznSocRecep = rznSocRecep;
    }
    
    public String getDirRecep() {
        return dirRecep;
    }
    
    public void setDirRecep(String dirRecep) {
        this.dirRecep = dirRecep;
    }
    
    public String getCmnaRecep() {
        return cmnaRecep;
    }
    
    public void setCmnaRecep(String cmnaRecep) {
        this.cmnaRecep = cmnaRecep;
    }
    
    public String getCiudadRecep() {
        return ciudadRecep;
    }
    
    public void setCiudadRecep(String ciudadRecep) {
        this.ciudadRecep = ciudadRecep;
    }
}
