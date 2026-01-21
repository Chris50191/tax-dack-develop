/**
 * 项目名：	tax-dack
 * 文件名：	SistemaInformatico.java
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
@XmlType(propOrder = {
        "nombreRazon", "nif", "nombreSistemaInformatico",
        "idSistemaInformatico", "version", "numeroInstalacion",
        "tipoUsoPosibleSoloVerifactu", "tipoUsoPosibleMultiOT", "indicadorMultiplesOT"
})
public class SistemaInformatico {
  @XmlElement(name = "NombreRazon")
  public String nombreRazon;

  @XmlElement(name = "NIF")
  public String nif;

  @XmlElement(name = "NombreSistemaInformatico")
  public String nombreSistemaInformatico;

  @XmlElement(name = "IdSistemaInformatico")
  public String idSistemaInformatico;

  @XmlElement(name = "Version")
  public String version;

  @XmlElement(name = "NumeroInstalacion")
  public String numeroInstalacion;

  @XmlElement(name = "TipoUsoPosibleSoloVerifactu")
  public String tipoUsoPosibleSoloVerifactu;

  @XmlElement(name = "TipoUsoPosibleMultiOT")
  public String tipoUsoPosibleMultiOT;

  @XmlElement(name = "IndicadorMultiplesOT")
  public String indicadorMultiplesOT;

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

  public String getNombreSistemaInformatico() {
    return this.nombreSistemaInformatico;
  }

  public void setNombreSistemaInformatico(String nombreSistemaInformatico) {
    this.nombreSistemaInformatico = nombreSistemaInformatico;
  }

  public String getIdSistemaInformatico() {
    return this.idSistemaInformatico;
  }

  public void setIdSistemaInformatico(String idSistemaInformatico) {
    this.idSistemaInformatico = idSistemaInformatico;
  }

  public String getVersion() {
    return this.version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getNumeroInstalacion() {
    return this.numeroInstalacion;
  }

  public void setNumeroInstalacion(String numeroInstalacion) {
    this.numeroInstalacion = numeroInstalacion;
  }

  public String getTipoUsoPosibleSoloVerifactu() {
    return this.tipoUsoPosibleSoloVerifactu;
  }

  public void setTipoUsoPosibleSoloVerifactu(String tipoUsoPosibleSoloVerifactu) {
    this.tipoUsoPosibleSoloVerifactu = tipoUsoPosibleSoloVerifactu;
  }

  public String getTipoUsoPosibleMultiOT() {
    return this.tipoUsoPosibleMultiOT;
  }

  public void setTipoUsoPosibleMultiOT(String tipoUsoPosibleMultiOT) {
    this.tipoUsoPosibleMultiOT = tipoUsoPosibleMultiOT;
  }

  public String getIndicadorMultiplesOT() {
    return this.indicadorMultiplesOT;
  }

  public void setIndicadorMultiplesOT(String indicadorMultiplesOT) {
    this.indicadorMultiplesOT = indicadorMultiplesOT;
  }
}
