/**
 * 项目名：	tax-dack
 * 文件名：	FacturasRectificadas.java
 * 模块说明：
 * 修改历史：
 * 2025/7/22 - cc - 创建。
 */
package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

/**
 * @author cc
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "idFacturaRectificada"
})
public class FacturasRectificadas {

  @XmlElement(name = "IDFacturaRectificada", required = true)
  protected List<IDFacturaARType> idFacturaRectificada;

  public List<IDFacturaARType> getIDFacturaRectificada() {
    if (idFacturaRectificada == null) {
      idFacturaRectificada = new ArrayList<IDFacturaARType>();
    }
    return this.idFacturaRectificada;
  }

  public void setIdFacturaRectificada(List<IDFacturaARType> idFacturaRectificada) {
    this.idFacturaRectificada = idFacturaRectificada;
  }
}
