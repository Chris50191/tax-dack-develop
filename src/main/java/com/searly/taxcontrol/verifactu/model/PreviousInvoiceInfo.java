/**
 * 项目名：	tax-dack
 * 文件名：	PreviousInvoiceInfo.java
 * 模块说明：
 * 修改历史：
 * 2025/6/30 - cc - 创建。
 */
package com.searly.taxcontrol.verifactu.model;

/**
 * 上一张发票信息的数据类
 * @author cc
 */
public class PreviousInvoiceInfo {
  private String idEmisorFactura;
  private String numSerieFactura;
  private String fechaExpedicionFactura;
  private String huella;

  public PreviousInvoiceInfo(String idEmisorFactura, String numSerieFactura,
                             String fechaExpedicionFactura, String huella) {
    this.idEmisorFactura = idEmisorFactura;
    this.numSerieFactura = numSerieFactura;
    this.fechaExpedicionFactura = fechaExpedicionFactura;
    this.huella = huella;
  }

  public String getIdEmisorFactura() { return idEmisorFactura; }
  public String getNumSerieFactura() { return numSerieFactura; }
  public String getFechaExpedicionFactura() { return fechaExpedicionFactura; }
  public String getHuella() { return huella; }
}
