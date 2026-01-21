package com.searly.taxcontrol.sii.model.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.io.StringWriter;

@XmlRootElement(name = "DTE")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"documento"})
public class DTE {
    
    @XmlElement(name = "Documento", required = true)
    private Documento documento;
    
//    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#", required = true)
//    private Signature signature;
    
    @XmlAttribute(name = "version", required = true)
    private final String version = "1.0";
    
    // 构造函数
    public DTE() {}
    
    public DTE(Documento documento) {
        this.documento = documento;
//        this.signature = signature;
    }
    
    // Getter和Setter方法
    public Documento getDocumento() {
        return documento;
    }
    
    public void setDocumento(Documento documento) {
        this.documento = documento;
    }
    
//    public Signature getSignature() {
//        return signature;
//    }
//
//    public void setSignature(Signature signature) {
//        this.signature = signature;
//    }
    
    public String getVersion() {
        return version;
    }

    public String toXml() throws JAXBException {
        // 使用JAXB生成XML
        JAXBContext context = JAXBContext.newInstance(DTE.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false); // 改为紧凑格式
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

        StringWriter writer = new StringWriter();
        marshaller.marshal(this, writer);

        // 添加XML声明
        String xmlContent = writer.toString();
        return xmlContent;
    }
}
