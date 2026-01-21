package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"encabezado", "detalle", "subTotInfo", "dscRcgGlobal", "referencia", "ted", "tmstFirma"})
public class Documento {
    
    @XmlElement(name = "Encabezado", required = true)
    private Encabezado encabezado;
    
    @XmlElement(name = "Detalle")
    private List<Detalle> detalle;
    
    @XmlElement(name = "SubTotInfo")
    private List<SubTotInfo> subTotInfo;
    
    @XmlElement(name = "DscRcgGlobal")
    private List<DscRcgGlobal> dscRcgGlobal;
    
    @XmlElement(name = "Referencia")
    private List<Referencia> referencia;
    
    @XmlElement(name = "TED", required = true)
    private TED ted;
    
    @XmlElement(name = "TmstFirma", required = true)
    private String tmstFirma;
    
    @XmlAttribute(name = "ID", required = true)
    private String id;
    
    // 构造函数
    public Documento() {}
    
    public Documento(Encabezado encabezado, List<Detalle> detalle, List<Referencia> referencia, String tmstFirma, String id) {
        this.encabezado = encabezado;
        this.detalle = detalle;
        this.referencia = referencia;
        this.tmstFirma = tmstFirma;
        this.id = id;
    }

    public Documento(Encabezado encabezado, List<Detalle> detalle, String tmstFirma, String id) {
        this.encabezado = encabezado;
        this.detalle = detalle;
        this.tmstFirma = tmstFirma;
        this.id = id;
    }

    // Getter和Setter方法
    public Encabezado getEncabezado() {
        return encabezado;
    }
    
    public void setEncabezado(Encabezado encabezado) {
        this.encabezado = encabezado;
    }
    
    public List<Detalle> getDetalle() {
        return detalle;
    }
    
    public void setDetalle(List<Detalle> detalle) {
        this.detalle = detalle;
    }
    
    public List<SubTotInfo> getSubTotInfo() {
        return subTotInfo;
    }
    
    public void setSubTotInfo(List<SubTotInfo> subTotInfo) {
        this.subTotInfo = subTotInfo;
    }
    
    public List<DscRcgGlobal> getDscRcgGlobal() {
        return dscRcgGlobal;
    }
    
    public void setDscRcgGlobal(List<DscRcgGlobal> dscRcgGlobal) {
        this.dscRcgGlobal = dscRcgGlobal;
    }
    
    public List<Referencia> getReferencia() {
        return referencia;
    }
    
    public void setReferencia(List<Referencia> referencia) {
        this.referencia = referencia;
    }
    
    public TED getTed() {
        return ted;
    }
    
    public void setTed(TED ted) {
        this.ted = ted;
    }
    
    public String getTmstFirma() {
        return tmstFirma;
    }
    
    public void setTmstFirma(String tmstFirma) {
        this.tmstFirma = tmstFirma;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
}
