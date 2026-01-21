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

@XmlRootElement(name = "EnvioBOLETA")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"setDTE"})
public class EnvioBOLETA {

  @XmlElement(name = "SetDTE", required = true)
  private SetDTE setDTE;

  //    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#", required = true)
//    private Signature signature;

//  @XmlAttribute(name = "xmlns:ds")
//  private final String xmlnsDs = "http://www.w3.org/2000/09/xmldsig#";

  @XmlAttribute(name = "xmlns:xsi")
  private final String xmlnsXsi = "http://www.w3.org/2001/XMLSchema-instance";

  // SII要求EnvioBOLETA使用v11版本的Schema
  // schemaLocation格式：命名空间URI + 空格 + Schema文件名
  @XmlAttribute(name = "xsi:schemaLocation")
  private final String schemaLocation = "http://www.sii.cl/SiiDte EnvioBOLETA_v11.xsd";

  @XmlAttribute(name = "version", required = true)
  private final String version = "1.0";

  @XmlAttribute(name = "xmlns")
  private final String xmlns = "http://www.sii.cl/SiiDte";

  // 构造函数
  public EnvioBOLETA() {
  }

  public EnvioBOLETA(SetDTE setDTE) {
    this.setDTE = setDTE;
//        this.signature = signature;
  }

  // Getter和Setter方法
  public SetDTE getSetDTE() {
    return setDTE;
  }

  public void setSetDTE(SetDTE setDTE) {
    this.setDTE = setDTE;
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
    JAXBContext context = JAXBContext.newInstance(EnvioBOLETA.class);
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
