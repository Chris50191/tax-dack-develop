/**
 * 项目名：	tax-dack
 * 文件名：	Desglose.java
 * 模块说明：
 * 修改历史：
 * 2025/6/28 - cc - 创建。
 */
package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.List;

/**
 * @author cc
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"detalleDesglose"})
public class Desglose {
  @XmlElement(name = "DetalleDesglose")
  public List<DetalleDesglose> detalleDesglose;

  public List<DetalleDesglose> getDetalleDesglose() {
    return this.detalleDesglose;
  }

  public void setDetalleDesglose(List<DetalleDesglose> detalleDesglose) {
    this.detalleDesglose = detalleDesglose;
  }
}
