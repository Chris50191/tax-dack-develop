package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * DA - Datos de Autorizacion de Folios
 * 代表授权数据，包含发行者信息、授权范围和公钥信息
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"re", "rs", "td", "rng", "fa", "rsapk", "idk"})
public class DA {
    
    @XmlElement(name = "RE", required = true)
    private String re; // RUT Emisor
    
    @XmlElement(name = "RS", required = true)
    private String rs; // Razon Social Emisor
    
    @XmlElement(name = "TD", required = true)
    private Integer td; // Tipo DTE
    
    @XmlElement(name = "RNG", required = true)
    private RNG rng; // Rango Autorizado de Folios
    
    @XmlElement(name = "FA", required = true)
    private String fa; // Fecha Autorizacion
    
    @XmlElement(name = "RSAPK")
    private RSAPK rsapk; // Clave Publica RSA del Solicitante
    
    @XmlElement(name = "IDK", required = true)
    private Long idk; // Identificador de Llave
    
    // 构造函数
    public DA() {}
    
    public DA(String re, String rs, Integer td, RNG rng, String fa, RSAPK rsapk, Long idk) {
        this.re = re;
        this.rs = rs;
        this.td = td;
        this.rng = rng;
        this.fa = fa;
        this.rsapk = rsapk;
        this.idk = idk;
    }
    
    // Getter 和 Setter 方法
    public String getRe() {
        return re;
    }
    
    public void setRe(String re) {
        this.re = re;
    }
    
    public String getRs() {
        return rs;
    }
    
    public void setRs(String rs) {
        this.rs = rs;
    }
    
    public Integer getTd() {
        return td;
    }
    
    public void setTd(Integer td) {
        this.td = td;
    }
    
    public RNG getRng() {
        return rng;
    }
    
    public void setRng(RNG rng) {
        this.rng = rng;
    }
    
    public String getFa() {
        return fa;
    }
    
    public void setFa(String fa) {
        this.fa = fa;
    }
    
    public RSAPK getRsapk() {
        return rsapk;
    }
    
    public void setRsapk(RSAPK rsapk) {
        this.rsapk = rsapk;
    }
    
    public Long getIdk() {
        return idk;
    }
    
    public void setIdk(Long idk) {
        this.idk = idk;
    }
    
    @Override
    public String toString() {
        return "DA{" +
                "re='" + re + '\'' +
                ", rs='" + rs + '\'' +
                ", td=" + td +
                ", rng=" + rng +
                ", fa=" + fa +
                ", rsapk=" + rsapk +
                ", idk=" + idk +
                '}';
    }
}
