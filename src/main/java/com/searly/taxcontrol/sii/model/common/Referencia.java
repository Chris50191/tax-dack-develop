package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"nroLinRef", "tpoDocRef", "folioRef", "fchRef","codRef", "razonRef", "codVndor", "codCaja"})
public class Referencia {
    
    @XmlElement(name = "NroLinRef", required = true)
    private Integer nroLinRef;
    
    @XmlElement(name = "TpoDocRef")
    private String tpoDocRef;
    
    @XmlElement(name = "FolioRef")
    private String folioRef;

    @XmlElement(name = "FchRef")
    private String fchRef;

    @XmlElement(name = "CodRef")
    private String codRef;
    
    @XmlElement(name = "RazonRef")
    private String razonRef;
    
    @XmlElement(name = "CodVndor")
    private String codVndor;
    
    @XmlElement(name = "CodCaja")
    private String codCaja;
    
    // 构造函数
    public Referencia() {}

    public Referencia(Integer nroLinRef, String tpoDocRef, String folioRef, String fchRef, String codRef, String razonRef) {
        this.nroLinRef = nroLinRef;
        this.tpoDocRef = tpoDocRef;
        this.folioRef = folioRef;
        this.fchRef = fchRef;
        this.codRef = codRef;
        this.razonRef = razonRef;
    }

    // Getter和Setter方法
    public Integer getNroLinRef() {
        return nroLinRef;
    }
    
    public void setNroLinRef(Integer nroLinRef) {
        this.nroLinRef = nroLinRef;
    }
    
    public String getTpoDocRef() {
        return tpoDocRef;
    }
    
    public void setTpoDocRef(String tpoDocRef) {
        this.tpoDocRef = tpoDocRef;
    }
    
    public String getFolioRef() {
        return folioRef;
    }
    
    public void setFolioRef(String folioRef) {
        this.folioRef = folioRef;
    }
    
    public String getCodRef() {
        return codRef;
    }
    
    public void setCodRef(String codRef) {
        this.codRef = codRef;
    }
    
    public String getRazonRef() {
        return razonRef;
    }
    
    public void setRazonRef(String razonRef) {
        this.razonRef = razonRef;
    }
    
    public String getCodVndor() {
        return codVndor;
    }
    
    public void setCodVndor(String codVndor) {
        this.codVndor = codVndor;
    }
    
    public String getCodCaja() {
        return codCaja;
    }
    
    public void setCodCaja(String codCaja) {
        this.codCaja = codCaja;
    }

    public void setFchRef(String fchRef) {
        this.fchRef = fchRef;
    }
}
