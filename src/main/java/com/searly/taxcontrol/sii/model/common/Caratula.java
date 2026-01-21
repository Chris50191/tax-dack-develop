package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"rutEmisor", "rutEnvia", "rutReceptor", "fchResol", "nroResol", "tmstFirmaEnv", "subTotDTE"})
public class Caratula {
    
    @XmlElement(name = "RutEmisor", required = true)
    private String rutEmisor;
    
    @XmlElement(name = "RutEnvia", required = true)
    private String rutEnvia;
    
    @XmlElement(name = "RutReceptor", required = true)
    private String rutReceptor;
    
    @XmlElement(name = "FchResol", required = true)
    private String fchResol;
    
    @XmlElement(name = "NroResol", required = true)
    private Integer nroResol;
    
    @XmlElement(name = "TmstFirmaEnv", required = true)
    private String tmstFirmaEnv;
    
    @XmlElement(name = "SubTotDTE", required = true)
    private List<SubTotDTE> subTotDTE;
    
    @XmlAttribute(name = "version", required = true)
    private final String version = "1.0";
    
    // 构造函数
    public Caratula() {}
    
    public Caratula(String rutEmisor, String rutEnvia, String rutReceptor, 
                    String fchResol, Integer nroResol, String tmstFirmaEnv, 
                    List<SubTotDTE> subTotDTE) {
        this.rutEmisor = rutEmisor;
        this.rutEnvia = rutEnvia;
        this.rutReceptor = rutReceptor;
        this.fchResol = fchResol;
        this.nroResol = nroResol;
        this.tmstFirmaEnv = tmstFirmaEnv;
        this.subTotDTE = subTotDTE;
    }
    
    // Getter和Setter方法
    public String getRutEmisor() {
        return rutEmisor;
    }
    
    public void setRutEmisor(String rutEmisor) {
        this.rutEmisor = rutEmisor;
    }
    
    public String getRutEnvia() {
        return rutEnvia;
    }
    
    public void setRutEnvia(String rutEnvia) {
        this.rutEnvia = rutEnvia;
    }
    
    public String getRutReceptor() {
        return rutReceptor;
    }
    
    public void setRutReceptor(String rutReceptor) {
        this.rutReceptor = rutReceptor;
    }
    
    public String getFchResol() {
        return fchResol;
    }
    
    public void setFchResol(String fchResol) {
        this.fchResol = fchResol;
    }
    
    public Integer getNroResol() {
        return nroResol;
    }
    
    public void setNroResol(Integer nroResol) {
        this.nroResol = nroResol;
    }
    
    public String getTmstFirmaEnv() {
        return tmstFirmaEnv;
    }
    
    public void setTmstFirmaEnv(String tmstFirmaEnv) {
        this.tmstFirmaEnv = tmstFirmaEnv;
    }
    
    public List<SubTotDTE> getSubTotDTE() {
        return subTotDTE;
    }
    
    public void setSubTotDTE(List<SubTotDTE> subTotDTE) {
        this.subTotDTE = subTotDTE;
    }
    
    public String getVersion() {
        return version;
    }
}
