/**
 * 项目名：	tax-dack
 * 文件名：	Destinatario.java
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
@XmlType(propOrder = {"idDestinatario"})
public class Destinatario {
  @XmlElement(name = "IDDestinatario")
  public IDDestinatario idDestinatario;

  public IDDestinatario getIdDestinatario() {
    return this.idDestinatario;
  }

  public void setIdDestinatario(IDDestinatario idDestinatario) {
    this.idDestinatario = idDestinatario;
  }
}
