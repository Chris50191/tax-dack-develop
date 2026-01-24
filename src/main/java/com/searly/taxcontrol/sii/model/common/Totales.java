package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"mntNeto", "mntExe", "iva", "mntAdic", "mntTotal"})
public class Totales {
    
    @XmlElement(name = "MntNeto")
    private BigDecimal mntNeto;

    @XmlElement(name = "MntExe")
    private BigDecimal mntExe;
    
    @XmlElement(name = "IVA")
    private BigDecimal iva;

    @XmlElement(name = "MntAdic")
    private BigDecimal mntAdic;

    @XmlElement(name = "MntTotal", required = true)
    private BigDecimal mntTotal;
    
    // 构造函数
    public Totales() {}
    
    public Totales(BigDecimal mntNeto, BigDecimal mntExe, BigDecimal iva, BigDecimal mntAdic,BigDecimal mntTotal) {
        this.mntNeto = mntNeto;
        this.mntExe = mntExe;
        this.iva = iva;
        this.mntAdic = mntAdic;
        this.mntTotal = mntTotal;
    }
    
    // Getter和Setter方法

    public BigDecimal getMntNeto() {
        return this.mntNeto;
    }

    public void setMntNeto(BigDecimal mntNeto) {
        this.mntNeto = mntNeto;
    }

    public BigDecimal getMntExe() {
        return this.mntExe;
    }

    public void setMntExe(BigDecimal mntExe) {
        this.mntExe = mntExe;
    }

    public BigDecimal getIva() {
        return this.iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getMntTotal() {
        return this.mntTotal;
    }

    public void setMntTotal(BigDecimal mntTotal) {
        this.mntTotal = mntTotal;
    }

    public BigDecimal getMntAdic() {
        return this.mntAdic;
    }

    public void setMntAdic(BigDecimal mntAdic) {
        this.mntAdic = mntAdic;
    }
}
