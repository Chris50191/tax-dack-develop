/**
 * 项目名：	tax-dack
 * 文件名：	RegistroAnterior.java
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
@XmlType(propOrder = {"idEmisorFactura", "numSerieFactura", "fechaExpedicionFactura", "huella"})
public class RegistroAnterior {
  @XmlElement(name = "IDEmisorFactura")
  public String idEmisorFactura;

  @XmlElement(name = "NumSerieFactura")
  public String numSerieFactura;

  @XmlElement(name = "FechaExpedicionFactura")
  public String fechaExpedicionFactura;

  @XmlElement(name = "Huella")
  public String huella;

  public String getIdEmisorFactura() {
    return this.idEmisorFactura;
  }

  public void setIdEmisorFactura(String idEmisorFactura) {
    this.idEmisorFactura = idEmisorFactura;
  }

  public String getNumSerieFactura() {
    return this.numSerieFactura;
  }

  public void setNumSerieFactura(String numSerieFactura) {
    this.numSerieFactura = numSerieFactura;
  }

  public String getFechaExpedicionFactura() {
    return this.fechaExpedicionFactura;
  }

  public void setFechaExpedicionFactura(String fechaExpedicionFactura) {
    this.fechaExpedicionFactura = fechaExpedicionFactura;
  }

  public String getHuella() {
    return this.huella;
  }

  public void setHuella(String huella) {
    this.huella = huella;
  }
}
