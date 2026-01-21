/**
 * 项目名：	tax-dack
 * 文件名：	RegistroFactura.java
 * 模块说明：
 * 修改历史：
 * 2025/6/28 - cc - 创建。
 */
package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author cc
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"registroAlta"})
public class RegistroFactura {
  @XmlElement(name = "RegistroAlta", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
  public RegistroAlta registroAlta = new RegistroAlta();

  public RegistroAlta getRegistroAlta() {
    return this.registroAlta;
  }

  public void setRegistroAlta(RegistroAlta registroAlta) {
    this.registroAlta = registroAlta;
  }
}
