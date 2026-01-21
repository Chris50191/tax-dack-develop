package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"rutEmisor", "rznSocEmisor", "giroEmisor", "dirOrigen", "cmnaOrigen", "ciudadOrigen"})
public class Emisor {
    
    @XmlElement(name = "RUTEmisor", required = true)
    private String rutEmisor;
    
    @XmlElement(name = "RznSocEmisor")
    private String rznSocEmisor;
    
    @XmlElement(name = "GiroEmisor")
    private String giroEmisor;
    
    @XmlElement(name = "DirOrigen")
    private String dirOrigen;
    
    @XmlElement(name = "CmnaOrigen")
    private String cmnaOrigen;
    
    @XmlElement(name = "CiudadOrigen")
    private String ciudadOrigen;
    
    // 构造函数
    public Emisor() {}
    
    public Emisor(String rutEmisor, String rznSocEmisor, String giroEmisor, 
                  String dirOrigen, String cmnaOrigen, String ciudadOrigen) {
        this.rutEmisor = rutEmisor;
        this.rznSocEmisor = rznSocEmisor;
        this.giroEmisor = giroEmisor;
        this.dirOrigen = dirOrigen;
        this.cmnaOrigen = cmnaOrigen;
        this.ciudadOrigen = ciudadOrigen;
    }
    
    // Getter和Setter方法
    public String getRutEmisor() {
        return rutEmisor;
    }
    
    public void setRutEmisor(String rutEmisor) {
        this.rutEmisor = rutEmisor;
    }
    
    public String getRznSocEmisor() {
        return rznSocEmisor;
    }
    
    public void setRznSocEmisor(String rznSocEmisor) {
        this.rznSocEmisor = rznSocEmisor;
    }
    
    public String getGiroEmisor() {
        return giroEmisor;
    }
    
    public void setGiroEmisor(String giroEmisor) {
        this.giroEmisor = giroEmisor;
    }
    
    public String getDirOrigen() {
        return dirOrigen;
    }
    
    public void setDirOrigen(String dirOrigen) {
        this.dirOrigen = dirOrigen;
    }
    
    public String getCmnaOrigen() {
        return cmnaOrigen;
    }
    
    public void setCmnaOrigen(String cmnaOrigen) {
        this.cmnaOrigen = cmnaOrigen;
    }
    
    public String getCiudadOrigen() {
        return ciudadOrigen;
    }
    
    public void setCiudadOrigen(String ciudadOrigen) {
        this.ciudadOrigen = ciudadOrigen;
    }
}
