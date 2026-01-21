/**
 * 项目名：	tax-dack
 * 文件名：	DetalleDesglose.java
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
        "impuesto", "claveRegimen", "calificacionOperacion",
        "tipoImpositivo", "baseImponibleOimporteNoSujeto", "cuotaRepercutida", "tipoRecargoEquivalencia","cuotaRecargoEquivalencia"
})
public class DetalleDesglose {
  @XmlElement(name = "Impuesto")
  public String impuesto;

  @XmlElement(name = "ClaveRegimen")
  public String claveRegimen;

  @XmlElement(name = "CalificacionOperacion")
  public String calificacionOperacion;

  @XmlElement(name = "TipoImpositivo")
  public String tipoImpositivo;

  @XmlElement(name = "BaseImponibleOimporteNoSujeto")
  public String baseImponibleOimporteNoSujeto;

  @XmlElement(name = "CuotaRepercutida")
  public String cuotaRepercutida;

  @XmlElement(name = "TipoRecargoEquivalencia")
  public String tipoRecargoEquivalencia;

  @XmlElement(name = "CuotaRecargoEquivalencia")
  public String cuotaRecargoEquivalencia;

  public String getImpuesto() {
    return this.impuesto;
  }

  public void setImpuesto(String impuesto) {
    this.impuesto = impuesto;
  }

  public String getClaveRegimen() {
    return this.claveRegimen;
  }

  public void setClaveRegimen(String claveRegimen) {
    this.claveRegimen = claveRegimen;
  }

  public String getCalificacionOperacion() {
    return this.calificacionOperacion;
  }

  public void setCalificacionOperacion(String calificacionOperacion) {
    this.calificacionOperacion = calificacionOperacion;
  }

  public String getTipoImpositivo() {
    return this.tipoImpositivo;
  }

  public void setTipoImpositivo(String tipoImpositivo) {
    this.tipoImpositivo = tipoImpositivo;
  }

  public String getBaseImponibleOimporteNoSujeto() {
    return this.baseImponibleOimporteNoSujeto;
  }

  public void setBaseImponibleOimporteNoSujeto(String baseImponibleOimporteNoSujeto) {
    this.baseImponibleOimporteNoSujeto = baseImponibleOimporteNoSujeto;
  }

  public String getCuotaRepercutida() {
    return this.cuotaRepercutida;
  }

  public void setCuotaRepercutida(String cuotaRepercutida) {
    this.cuotaRepercutida = cuotaRepercutida;
  }

  public String getTipoRecargoEquivalencia() {
    return this.tipoRecargoEquivalencia;
  }

  public void setTipoRecargoEquivalencia(String tipoRecargoEquivalencia) {
    this.tipoRecargoEquivalencia = tipoRecargoEquivalencia;
  }

  public String getCuotaRecargoEquivalencia() {
    return this.cuotaRecargoEquivalencia;
  }

  public void setCuotaRecargoEquivalencia(String cuotaRecargoEquivalencia) {
    this.cuotaRecargoEquivalencia = cuotaRecargoEquivalencia;
  }
}
