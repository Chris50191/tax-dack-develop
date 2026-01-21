package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"re", "td", "f", "fe", "rr", "rsr", "mnt", "it1", "caf", "tsted"})
public class DD {
    
    @XmlElement(name = "RE", required = true)
    private String re;
    
    @XmlElement(name = "TD", required = true)
    private Integer td;
    
    @XmlElement(name = "F", required = true)
    private String f;
    
    @XmlElement(name = "FE", required = true)
    private String fe;
    
    @XmlElement(name = "RR", required = true)
    private String rr;
    
    @XmlElement(name = "RSR", required = true)
    private String rsr;
    
    @XmlElement(name = "MNT", required = true)
    private Long mnt;
    
    @XmlElement(name = "IT1", required = true)
    private String it1;
    
    @XmlElement(name = "CAF", required = true)
    private CAF caf;
    
    @XmlElement(name = "TSTED", required = true)
    private String tsted;
    
    // 构造函数
    public DD() {}
    
    public DD(String re, Integer td, String f, String fe, String rr,
              String rsr, Long mnt, String it1, CAF caf, String tsted) {
        this.re = re;
        this.td = td;
        this.f = f;
        this.fe = fe;
        this.rr = rr;
        this.rsr = rsr;
        this.mnt = mnt;
        this.it1 = it1;
        this.caf = caf;
        this.tsted = tsted;
    }
    
    // Getter和Setter方法
    public String getRe() {
        return re;
    }
    
    public void setRe(String re) {
        this.re = re;
    }
    
    public Integer getTd() {
        return td;
    }
    
    public void setTd(Integer td) {
        this.td = td;
    }
    
    public String getF() {
        return f;
    }
    
    public void setF(String f) {
        this.f = f;
    }
    
    public String getFe() {
        return fe;
    }
    
    public void setFe(String fe) {
        this.fe = fe;
    }
    
    public String getRr() {
        return rr;
    }
    
    public void setRr(String rr) {
        this.rr = rr;
    }
    
    public String getRsr() {
        return rsr;
    }
    
    public void setRsr(String rsr) {
        this.rsr = rsr;
    }
    
    public Long getMnt() {
        return mnt;
    }
    
    public void setMnt(Long mnt) {
        this.mnt = mnt;
    }
    
    public String getIt1() {
        return it1;
    }
    
    public void setIt1(String it1) {
        this.it1 = it1;
    }
    
    public CAF getCaf() {
        return caf;
    }
    
    public void setCaf(CAF caf) {
        this.caf = caf;
    }
    
    public String getTsted() {
        return tsted;
    }
    
    public void setTsted(String tsted) {
        this.tsted = tsted;
    }
}
