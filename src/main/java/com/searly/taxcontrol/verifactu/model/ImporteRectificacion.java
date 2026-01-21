
package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * Desglose de Base y Cuota sustituida en las Facturas Rectificativas sustitutivas
 *
 * <p>DesgloseRectificacionType complex type的 Java 类。
 *
 * <p>以下模式片段指定包含在此类中的预期内容。
 *
 * <pre>
 * &lt;complexType name="DesgloseRectificacionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="BaseRectificada" type="{https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd}ImporteSgn12.2Type"/>
 *         &lt;element name="CuotaRectificada" type="{https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd}ImporteSgn12.2Type"/>
 *         &lt;element name="CuotaRecargoRectificado" type="{https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd}ImporteSgn12.2Type" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ImporteRectificacion", propOrder = {
        "baseRectificada",
        "cuotaRectificada",
        "cuotaRecargoRectificado"
})
public class ImporteRectificacion {

  @XmlElement(name = "BaseRectificada", required = true)
  protected String baseRectificada;
  @XmlElement(name = "CuotaRectificada", required = true)
  protected String cuotaRectificada;
  @XmlElement(name = "CuotaRecargoRectificado")
  protected String cuotaRecargoRectificado;

  /**
   * 获取baseRectificada属性的值。
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getBaseRectificada() {
    return baseRectificada;
  }

  /**
   * 设置baseRectificada属性的值。
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setBaseRectificada(String value) {
    this.baseRectificada = value;
  }

  /**
   * 获取cuotaRectificada属性的值。
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getCuotaRectificada() {
    return cuotaRectificada;
  }

  /**
   * 设置cuotaRectificada属性的值。
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setCuotaRectificada(String value) {
    this.cuotaRectificada = value;
  }

  /**
   * 获取cuotaRecargoRectificado属性的值。
   *
   * @return
   *     possible object is
   *     {@link String }
   *
   */
  public String getCuotaRecargoRectificado() {
    return cuotaRecargoRectificado;
  }

  /**
   * 设置cuotaRecargoRectificado属性的值。
   *
   * @param value
   *     allowed object is
   *     {@link String }
   *
   */
  public void setCuotaRecargoRectificado(String value) {
    this.cuotaRecargoRectificado = value;
  }

}
