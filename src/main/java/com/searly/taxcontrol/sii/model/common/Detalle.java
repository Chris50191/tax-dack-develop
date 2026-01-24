package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"nroLinDet", "indExe", "nmbItem", "qtyItem", "unmdItem", "prcItem", "montoItem"})
public class Detalle {
    
    @XmlElement(name = "NroLinDet", required = true)
    private Integer nroLinDet;

    @XmlElement(name = "IndExe")
    private Integer indExe;
    
    @XmlElement(name = "NmbItem", required = true)
    private String nmbItem;
    
    @XmlElement(name = "QtyItem")
    private BigDecimal qtyItem;

    @XmlElement(name = "UnmdItem")
    private String unmdItem;

    @XmlElement(name = "PrcItem")
    private BigDecimal prcItem;
    
    @XmlElement(name = "MontoItem", required = true)
    private BigDecimal montoItem;
    
    // 构造函数
    public Detalle() {}
    
    public Detalle(Integer nroLinDet, String nmbItem, BigDecimal qtyItem,
                   BigDecimal prcItem, BigDecimal montoItem) {
        this.nroLinDet = nroLinDet;
        this.nmbItem = nmbItem;
        this.qtyItem = qtyItem;
        this.prcItem = prcItem;
        this.montoItem = montoItem;
    }

    public Detalle(Integer nroLinDet, String nmbItem, BigDecimal montoItem) {
        this.nroLinDet = nroLinDet;
        this.nmbItem = nmbItem;
        this.montoItem = montoItem;
    }

    // Getter和Setter方法
    public Integer getNroLinDet() {
        return nroLinDet;
    }
    
    public void setNroLinDet(Integer nroLinDet) {
        this.nroLinDet = nroLinDet;
    }

    public Integer getIndExe() {
        return indExe;
    }

    public void setIndExe(Integer indExe) {
        this.indExe = indExe;
    }
    
    public String getNmbItem() {
        return nmbItem;
    }
    
    public void setNmbItem(String nmbItem) {
        this.nmbItem = nmbItem;
    }

    public BigDecimal getQtyItem() {
        return this.qtyItem;
    }

    public void setQtyItem(BigDecimal qtyItem) {
        this.qtyItem = qtyItem;
    }

    public String getUnmdItem() {
        return unmdItem;
    }

    public void setUnmdItem(String unmdItem) {
        this.unmdItem = unmdItem;
    }

    public BigDecimal getPrcItem() {
        return this.prcItem;
    }

    public void setPrcItem(BigDecimal prcItem) {
        this.prcItem = prcItem;
    }

    public BigDecimal getMontoItem() {
        return this.montoItem;
    }

    public void setMontoItem(BigDecimal montoItem) {
        this.montoItem = montoItem;
    }
}
