/**
 * 项目名：	tax-dack
 * 文件名：	FacturasSustituidas.java
 * 模块说明：
 * 修改历史：
 * 2025/7/18 - cc - 创建。
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
        "idFacturaSustituida"
})
public class FacturasSustituidas {

  @XmlElement(name = "IDFacturaSustituida", required = true)
  protected List<IDFacturaARType> idFacturaSustituida;

  public List<IDFacturaARType> getIDFacturaSustituida() {
    if (idFacturaSustituida == null) {
      idFacturaSustituida = new ArrayList<IDFacturaARType>();
    }
    return this.idFacturaSustituida;
  }

  public void setIdFacturaSustituida(List<IDFacturaARType> idFacturaSustituida) {
    this.idFacturaSustituida = idFacturaSustituida;
  }
}
