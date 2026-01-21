/**
 * 项目名：	tax-dack
 * 文件名：	IDDestinatario.java
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
@XmlType(propOrder = {"nombreRazon", "nif"})
public class IDDestinatario {
  @XmlElement(name = "NombreRazon")
  public String nombreRazon;

  @XmlElement(name = "NIF")
  public String nif;

  public String getNombreRazon() {
    return this.nombreRazon;
  }

  public void setNombreRazon(String nombreRazon) {
    this.nombreRazon = nombreRazon;
  }

  public String getNif() {
    return this.nif;
  }

  public void setNif(String nif) {
    this.nif = nif;
  }
}
