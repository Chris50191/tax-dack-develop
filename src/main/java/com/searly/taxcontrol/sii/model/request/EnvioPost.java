package com.searly.taxcontrol.sii.model.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 发票发送请求模型类
 * 用于表示向SII系统发送发票的请求数据
 * 包含发送者信息、公司信息和发票文件数据
 */
@XmlRootElement(name = "EnvioPost")
@XmlAccessorType(XmlAccessType.FIELD)
public class EnvioPost {
    
    /**
     * 发送者RUT号码
     * RUT是智利纳税人的唯一标识号
     * 不包含校验位
     */
    @XmlElement(name = "rutSender")
    private Integer rutSender;
    
    /**
     * 发送者RUT校验位
     * 用于验证RUT号码的有效性
     * 通常是一个数字或字母K
     */
    @XmlElement(name = "dvSender")
    private String dvSender;
    
    /**
     * 公司RUT号码
     * 接收发票的公司的RUT号码
     * 不包含校验位
     */
    @XmlElement(name = "rutCompany")
    private Integer rutCompany;
    
    /**
     * 公司RUT校验位
     * 用于验证公司RUT号码的有效性
     * 通常是一个数字或字母K
     */
    @XmlElement(name = "dvCompany")
    private String dvCompany;
    
    /**
     * 发票文件数据
     * 包含发票XML文件的Base64编码字符串
     * 用于传输发票的具体内容
     */
    @XmlElement(name = "archivo")
    private String archivo;

    public Integer getRutSender() {
        return rutSender;
    }

    public void setRutSender(Integer rutSender) {
        this.rutSender = rutSender;
    }

    public String getDvSender() {
        return dvSender;
    }

    public void setDvSender(String dvSender) {
        this.dvSender = dvSender;
    }

    public Integer getRutCompany() {
        return rutCompany;
    }

    public void setRutCompany(Integer rutCompany) {
        this.rutCompany = rutCompany;
    }

    public String getDvCompany() {
        return dvCompany;
    }

    public void setDvCompany(String dvCompany) {
        this.dvCompany = dvCompany;
    }

    public String getArchivo() {
        return archivo;
    }

    public void setArchivo(String archivo) {
        this.archivo = archivo;
    }
} 