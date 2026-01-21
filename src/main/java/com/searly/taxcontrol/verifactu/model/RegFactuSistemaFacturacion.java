/**
 * 项目名：	tax-dack
 * 文件名：	RegFactuSistemaFacturacion.java
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
@XmlType(propOrder = {"cabecera", "registroFactura"})
public class RegFactuSistemaFacturacion {
  @XmlElement(name = "Cabecera")
  public Cabecera cabecera = new Cabecera();

  @XmlElement(name = "RegistroFactura")
  public RegistroFactura registroFactura = new RegistroFactura();

  public Cabecera getCabecera() {
    return this.cabecera;
  }

  public void setCabecera(Cabecera cabecera) {
    this.cabecera = cabecera;
  }

  public RegistroFactura getRegistroFactura() {
    return this.registroFactura;
  }

  public void setRegistroFactura(RegistroFactura registroFactura) {
    this.registroFactura = registroFactura;
  }
}
