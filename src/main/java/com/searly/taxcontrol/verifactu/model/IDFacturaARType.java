
package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 *  Datos de identificación de factura sustituida o rectificada. El NIF se cogerá del NIF indicado en el bloque IDFactura
 * 
 * <p>IDFacturaARType complex type的 Java 类。
 * 
 * <p>以下模式片段指定包含在此类中的预期内容。
 * 
 * <pre>
 * &lt;complexType name="IDFacturaARType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="IDEmisorFactura" type="{https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd}NIFType"/>
 *         &lt;element name="NumSerieFactura" type="{https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd}TextoIDFacturaType"/>
 *         &lt;element name="FechaExpedicionFactura" type="{https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd}fecha"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IDFacturaARType", propOrder = {
    "idEmisorFactura",
    "numSerieFactura",
    "fechaExpedicionFactura"
})
public class IDFacturaARType {

    @XmlElement(name = "IDEmisorFactura", required = true)
    protected String idEmisorFactura;
    @XmlElement(name = "NumSerieFactura", required = true)
    protected String numSerieFactura;
    @XmlElement(name = "FechaExpedicionFactura", required = true)
    protected String fechaExpedicionFactura;

    public IDFacturaARType() {
    }

    public IDFacturaARType(String idEmisorFactura, String numSerieFactura, String fechaExpedicionFactura) {
        this.idEmisorFactura = idEmisorFactura;
        this.numSerieFactura = numSerieFactura;
        this.fechaExpedicionFactura = fechaExpedicionFactura;
    }

    /**
     * 获取idEmisorFactura属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIDEmisorFactura() {
        return idEmisorFactura;
    }

    /**
     * 设置idEmisorFactura属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIDEmisorFactura(String value) {
        this.idEmisorFactura = value;
    }

    /**
     * 获取numSerieFactura属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumSerieFactura() {
        return numSerieFactura;
    }

    /**
     * 设置numSerieFactura属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumSerieFactura(String value) {
        this.numSerieFactura = value;
    }

    /**
     * 获取fechaExpedicionFactura属性的值。
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFechaExpedicionFactura() {
        return fechaExpedicionFactura;
    }

    /**
     * 设置fechaExpedicionFactura属性的值。
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFechaExpedicionFactura(String value) {
        this.fechaExpedicionFactura = value;
    }

}
