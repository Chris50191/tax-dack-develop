package com.searly.taxcontrol.verifactu.model;

import com.searly.taxcontrol.verifactu.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 发票注册响应模型
 * 对应VeriFactu发票注册SOAP响应
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class VerifactuErrorResponse {
    
    @XmlElement(name = "Body",namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;


    /**
     * 获取错误代码
     *
     * @return 状态
     */
    public String getFaultCode() {
        if (body != null && body.fault != null) {
            return body.fault.faultcode;
        }
        return null;
    }

    /**
     * 获取错误代码
     *
     * @return 状态
     */
    public String getFaultString() {
        if (body != null && body.fault != null) {
            return body.fault.faultstring;
        }
        return null;
    }

    /**
     * 获取错误代码
     *
     * @return 状态
     */
    public String getFaultDetail() {
        if (body != null && body.fault != null) {
            return body.fault.detail;
        }
        return null;
    }


    /**
     * 从XML字符串解析响应
     * 
     * @param xml XML字符串
     * @return 响应对象
     * @throws JAXBException 如果解析失败
     */
    public static VerifactuErrorResponse fromXml(String xml) throws JAXBException {
        return XmlUtils.unmarshalWithNamespace(xml, VerifactuErrorResponse.class);
    }
    
    // 内部类定义
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        
        @XmlElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
        private Fault fault;

        public Fault getFault() {
            return this.fault;
        }

        public void setFault(Fault fault) {
            this.fault = fault;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Fault {
        @XmlElement(name = "faultcode",  namespace = "")
        private String faultcode;

        @XmlElement(name = "faultstring",  namespace = "")
        private String faultstring;

        @XmlElement(name = "detail",  namespace = "")
        private String detail;

        public String getFaultcode() {
            return this.faultcode;
        }

        public void setFaultcode(String faultcode) {
            this.faultcode = faultcode;
        }

        public String getFaultstring() {
            return this.faultstring;
        }

        public void setFaultstring(String faultstring) {
            this.faultstring = faultstring;
        }

        public String getDetail() {
            return this.detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }
    }

    public Body getBody() {
        return this.body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}