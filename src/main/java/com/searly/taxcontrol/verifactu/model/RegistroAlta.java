/**
 * 项目名：	tax-dack
 * 文件名：	RegistroAlta.java
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
@XmlType(propOrder = {
        "idVersion", "idFactura", "nombreRazonEmisor", "subsanacion", "rechazoPrevio",
        "tipoFactura", "tipoRectificativa", "facturasRectificadas", "facturasSustituidas", "importeRectificacion", "fechaOperacion", "descripcionOperacion", "macrodato",
        "destinatarios", "cupon", "desglose", "cuotaTotal", "importeTotal",
        "encadenamiento", "sistemaInformatico", "fechaHoraHusoGenRegistro",
        "tipoHuella", "huella"
})
public class RegistroAlta {
  @XmlElement(name = "IDVersion")
  public String idVersion;

  @XmlElement(name = "IDFactura")
  public IDFactura idFactura = new IDFactura();

  @XmlElement(name = "NombreRazonEmisor")
  public String nombreRazonEmisor;

  @XmlElement(name = "Subsanacion")
  public String subsanacion;

  @XmlElement(name = "RechazoPrevio")
  public String rechazoPrevio;

  @XmlElement(name = "TipoFactura")
  public String tipoFactura;

  @XmlElement(name = "TipoRectificativa")
  protected String tipoRectificativa;

  @XmlElement(name = "FacturasRectificadas")
  protected FacturasRectificadas facturasRectificadas;

  @XmlElement(name = "FacturasSustituidas")
  public FacturasSustituidas facturasSustituidas;

  @XmlElement(name = "ImporteRectificacion")
  protected ImporteRectificacion importeRectificacion;

  @XmlElement(name = "FechaOperacion")
  public String fechaOperacion;

  @XmlElement(name = "DescripcionOperacion")
  public String descripcionOperacion;

  @XmlElement(name = "Macrodato")
  public String macrodato;

  @XmlElement(name = "Destinatarios")
  public List<Destinatario> destinatarios;

  @XmlElement(name = "Cupon")
  public String cupon;

  @XmlElement(name = "Desglose")
  public Desglose desglose;

  @XmlElement(name = "CuotaTotal")
  public String cuotaTotal;

  @XmlElement(name = "ImporteTotal")
  public String importeTotal;

  @XmlElement(name = "Encadenamiento")
  public Encadenamiento encadenamiento;

  @XmlElement(name = "SistemaInformatico")
  public SistemaInformatico sistemaInformatico;

  @XmlElement(name = "FechaHoraHusoGenRegistro")
  public String fechaHoraHusoGenRegistro;

  @XmlElement(name = "TipoHuella")
  public String tipoHuella;

  @XmlElement(name = "Huella")
  public String huella;

  public String getIdVersion() {
    return this.idVersion;
  }

  public void setIdVersion(String idVersion) {
    this.idVersion = idVersion;
  }

  public IDFactura getIdFactura() {
    return this.idFactura;
  }

  public void setIdFactura(IDFactura idFactura) {
    this.idFactura = idFactura;
  }

  public String getNombreRazonEmisor() {
    return this.nombreRazonEmisor;
  }

  public void setNombreRazonEmisor(String nombreRazonEmisor) {
    this.nombreRazonEmisor = nombreRazonEmisor;
  }

  public String getSubsanacion() {
    return this.subsanacion;
  }

  public void setSubsanacion(String subsanacion) {
    this.subsanacion = subsanacion;
  }

  public String getRechazoPrevio() {
    return this.rechazoPrevio;
  }

  public void setRechazoPrevio(String rechazoPrevio) {
    this.rechazoPrevio = rechazoPrevio;
  }

  public String getTipoFactura() {
    return this.tipoFactura;
  }

  public void setTipoFactura(String tipoFactura) {
    this.tipoFactura = tipoFactura;
  }

  public String getFechaOperacion() {
    return this.fechaOperacion;
  }

  public void setFechaOperacion(String fechaOperacion) {
    this.fechaOperacion = fechaOperacion;
  }

  public String getDescripcionOperacion() {
    return this.descripcionOperacion;
  }

  public void setDescripcionOperacion(String descripcionOperacion) {
    this.descripcionOperacion = descripcionOperacion;
  }

  public String getMacrodato() {
    return this.macrodato;
  }

  public void setMacrodato(String macrodato) {
    this.macrodato = macrodato;
  }

  public List<Destinatario> getDestinatarios() {
    return this.destinatarios;
  }

  public void setDestinatarios(List<Destinatario> destinatarios) {
    this.destinatarios = destinatarios;
  }

  public String getCupon() {
    return this.cupon;
  }

  public void setCupon(String cupon) {
    this.cupon = cupon;
  }

  public Desglose getDesglose() {
    return this.desglose;
  }

  public void setDesglose(Desglose desglose) {
    this.desglose = desglose;
  }

  public String getCuotaTotal() {
    return this.cuotaTotal;
  }

  public void setCuotaTotal(String cuotaTotal) {
    this.cuotaTotal = cuotaTotal;
  }

  public String getImporteTotal() {
    return this.importeTotal;
  }

  public void setImporteTotal(String importeTotal) {
    this.importeTotal = importeTotal;
  }

  public Encadenamiento getEncadenamiento() {
    return this.encadenamiento;
  }

  public void setEncadenamiento(Encadenamiento encadenamiento) {
    this.encadenamiento = encadenamiento;
  }

  public SistemaInformatico getSistemaInformatico() {
    return this.sistemaInformatico;
  }

  public void setSistemaInformatico(SistemaInformatico sistemaInformatico) {
    this.sistemaInformatico = sistemaInformatico;
  }

  public String getFechaHoraHusoGenRegistro() {
    return this.fechaHoraHusoGenRegistro;
  }

  public void setFechaHoraHusoGenRegistro(String fechaHoraHusoGenRegistro) {
    this.fechaHoraHusoGenRegistro = fechaHoraHusoGenRegistro;
  }

  public String getTipoHuella() {
    return this.tipoHuella;
  }

  public void setTipoHuella(String tipoHuella) {
    this.tipoHuella = tipoHuella;
  }

  public String getHuella() {
    return this.huella;
  }

  public void setHuella(String huella) {
    this.huella = huella;
  }
}
