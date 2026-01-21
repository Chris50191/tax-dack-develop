/**
 * 项目名：	tax-dack
 * 文件名：	Encadenamiento.java
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
@XmlType(propOrder = {"registroAnterior", "primerRegistro"})
public class Encadenamiento {
  @XmlElement(name = "RegistroAnterior")
  public RegistroAnterior registroAnterior;

  @XmlElement(name = "PrimerRegistro")
  public String primerRegistro;

  public RegistroAnterior getRegistroAnterior() {
    return this.registroAnterior;
  }

  public void setRegistroAnterior(RegistroAnterior registroAnterior) {
    this.registroAnterior = registroAnterior;
  }

  public String getPrimerRegistro() {
    return this.primerRegistro;
  }

  public void setPrimerRegistro(String primerRegistro) {
    this.primerRegistro = primerRegistro;
  }
}
