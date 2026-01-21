package com.searly.taxcontrol.verifactu.model;

import com.searly.taxcontrol.verifactu.utils.HashGenerator;
import com.searly.taxcontrol.verifactu.utils.QRGenerator;
import com.searly.taxcontrol.verifactu.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 发票注册请求模型
 * 对应VeriFactu发票注册SOAP请求
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"body"})
public class InvoiceRegisterRequest {

  @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
  public Body body = new Body();

  /**
   * 获取发票编号
   *
   * @return 发票编号
   */
  public String getInvoiceNumber() {
    if (body != null && body.regFactu != null && body.regFactu.registroFactura != null &&
        body.regFactu.registroFactura.registroAlta != null && body.regFactu.registroFactura.registroAlta.idFactura != null) {
      return body.regFactu.registroFactura.registroAlta.idFactura.numSerieFactura;
    }
    return null;
  }

  /**
   * 获取发票哈希值
   *
   * @return 哈希值
   */
  public String getHuella() {
    if (body != null && body.regFactu != null && body.regFactu.registroFactura != null) {
      return body.regFactu.registroFactura.registroAlta.huella;
    }
    return null;
  }

  /**
   * 获取上一次哈希值
   *
   * @return 哈希值
   */
  public String getPreHuella() {
    if (body != null &&
        body.regFactu != null &&
        body.regFactu.registroFactura != null &&
        body.regFactu.registroFactura.registroAlta != null &&
        body.regFactu.registroFactura.registroAlta.encadenamiento != null &&
        !Objects.equals(body.regFactu.registroFactura.registroAlta.encadenamiento.primerRegistro, "S")
    ) {
      return body.regFactu.registroFactura.registroAlta.encadenamiento.registroAnterior.huella;
    }
    return null;
  }

  /**
   * 获取安装编号
   *
   * @return 安装编号
   */
  public String getSystemId() {
    if (body != null &&
        body.regFactu != null &&
        body.regFactu.registroFactura != null &&
        body.regFactu.registroFactura.registroAlta != null &&
        body.regFactu.registroFactura.registroAlta.sistemaInformatico != null
    ) {
      return body.regFactu.registroFactura.registroAlta.sistemaInformatico.numeroInstalacion;

    }
    return null;
  }

  /**
   * 获取签发日期
   *
   * @return 签发日期
   */
  public String getIssueDate() {
    if (body != null && body.regFactu != null && body.regFactu.registroFactura != null &&
        body.regFactu.registroFactura.registroAlta != null && body.regFactu.registroFactura.registroAlta.idFactura != null) {
      return body.regFactu.registroFactura.registroAlta.idFactura.fechaExpedicionFactura;
    }
    return null;
  }

  /**
   * 获取总金额
   *
   * @return 总金额
   */
  public String getTotalAmount() {
    if (body != null && body.regFactu != null && body.regFactu.registroFactura != null &&
        body.regFactu.registroFactura.registroAlta != null) {
      return body.regFactu.registroFactura.registroAlta.importeTotal;
    }
    return null;
  }

  /**
   * 获取总税额
   *
   * @return 总税额
   */
  public String getTotalTax() {
    if (body != null && body.regFactu != null && body.regFactu.registroFactura != null &&
        body.regFactu.registroFactura.registroAlta != null) {
      return body.regFactu.registroFactura.registroAlta.cuotaTotal;
    }
    return null;
  }

  /**
   * 设置发票基本信息
   *
   * @param sellerNif
   *     卖方税号
   * @param sellerName
   *     卖方名称
   * @param invoiceNumber
   *     发票编号
   * @param issueDate
   *     签发日期 (格式: dd-MM-yyyy)
   * @param invoiceType
   *     发票类型
   * @param description
   *     描述
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest setBasicInfo(String sellerNif, String sellerName,
                                             String invoiceNumber, String issueDate,
                                             String invoiceType, String description) {
    // 设置卖方信息
    body.regFactu.cabecera.obligadoEmision.nif = sellerNif;
    body.regFactu.cabecera.obligadoEmision.nombreRazon = sellerName;

    // 设置发票标识
    body.regFactu.registroFactura.registroAlta.idFactura.idEmisorFactura = sellerNif;
    body.regFactu.registroFactura.registroAlta.idFactura.numSerieFactura = invoiceNumber;
    body.regFactu.registroFactura.registroAlta.idFactura.fechaExpedicionFactura = issueDate;

    // 设置其他基本信息
    body.regFactu.registroFactura.registroAlta.nombreRazonEmisor = sellerName;
    body.regFactu.registroFactura.registroAlta.tipoFactura = invoiceType;
    body.regFactu.registroFactura.registroAlta.fechaOperacion = issueDate;
    body.regFactu.registroFactura.registroAlta.descripcionOperacion = description;

    return this;
  }

  /**
   * 设置发票描述信息
   *
   * @param description
   *     描述
   */
  public InvoiceRegisterRequest setDescription(String description) {
    body.regFactu.registroFactura.registroAlta.descripcionOperacion = description;
    return this;
  }

  /**
   * 设置买方信息
   *
   * @param buyerNif
   *     买方税号
   * @param buyerName
   *     买方名称
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest setBuyerInfo(String buyerNif, String buyerName) {
    IDDestinatario idDestinatario = new IDDestinatario();
    idDestinatario.nif = buyerNif;
    idDestinatario.nombreRazon = buyerName;

    Destinatario destinatario = new Destinatario();
    destinatario.idDestinatario = idDestinatario;

    body.regFactu.registroFactura.registroAlta.destinatarios = new ArrayList<>();
    body.regFactu.registroFactura.registroAlta.destinatarios.add(destinatario);
    body.regFactu.registroFactura.registroAlta.tipoFactura = "F1";
    return this;
  }

  /**
   * 获取买方信息
   *
   * @return 当前对象，用于链式调用
   */
  public IDDestinatario getBuyerInfo() {
    List<Destinatario> destinatarios = body.regFactu.registroFactura.registroAlta.destinatarios;
    if (destinatarios == null) {
      return null;
    }
    IDDestinatario idDestinatario = new IDDestinatario();
    idDestinatario.nif = destinatarios.get(0).idDestinatario.nif;
    idDestinatario.nombreRazon = destinatarios.get(0).idDestinatario.nombreRazon;
    return idDestinatario;
  }


  /**
   * 设置买方信息,通过替换发票
   *
   * @param buyerNif
   *     买方税号
   * @param buyerName
   *     买方名称
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest setBuyerInfoByReplace(String buyerNif, String buyerName, String sellerNif,
                                                      String invoiceNumber, String issueDate) {
    IDDestinatario idDestinatario = new IDDestinatario();
    idDestinatario.nif = buyerNif;
    idDestinatario.nombreRazon = buyerName;

    Destinatario destinatario = new Destinatario();
    destinatario.idDestinatario = idDestinatario;

    body.regFactu.registroFactura.registroAlta.destinatarios = new ArrayList<>();
    body.regFactu.registroFactura.registroAlta.destinatarios.add(destinatario);
    body.regFactu.registroFactura.registroAlta.tipoFactura = "F3";

    FacturasSustituidas facturasSustituidas = new FacturasSustituidas();
    facturasSustituidas.setIdFacturaSustituida(Arrays.asList(new IDFacturaARType(sellerNif, invoiceNumber, issueDate)));
    body.regFactu.registroFactura.registroAlta.facturasSustituidas = facturasSustituidas;
    return this;
  }

  /**
   * 设置发票更正源 - 有原单 - 替换
   *
   * @param sellerNif
   * @param invoiceNumber
   * @param issueDate
   * @param invoiceType
   *     发票类型
   * @param :
   *     S -替换
   * @return
   */
  public InvoiceRegisterRequest setRectificada(String sellerNif, String invoiceNumber, String issueDate, String invoiceType, BigDecimal baseAmount, BigDecimal taxAmount, BigDecimal insTaxAmount) {
    FacturasRectificadas facturasRectificadas = new FacturasRectificadas();
    facturasRectificadas.setIdFacturaRectificada(Arrays.asList(new IDFacturaARType(sellerNif, invoiceNumber, issueDate)));
    body.regFactu.registroFactura.registroAlta.facturasRectificadas = facturasRectificadas;
    body.regFactu.registroFactura.registroAlta.tipoFactura = invoiceType;

    ImporteRectificacion importeRectificacion = new ImporteRectificacion();
    importeRectificacion.setBaseRectificada(baseAmount.toString());
    importeRectificacion.setCuotaRectificada(taxAmount.toString());
    importeRectificacion.setCuotaRecargoRectificado(insTaxAmount.toString());
    body.regFactu.registroFactura.registroAlta.importeRectificacion = importeRectificacion;

    body.regFactu.registroFactura.registroAlta.tipoRectificativa = "S";
    return this;
  }

  /**
   * 设置发票更正源 - 有原单
   *
   * @param sellerNif
   * @param invoiceNumber
   * @param issueDate
   * @param invoiceType
   *     发票类型
   * @param modifyType
   *     : S -替换，I -增量
   * @return
   */
  public InvoiceRegisterRequest setRectificada(String sellerNif, String invoiceNumber, String issueDate, String invoiceType, String modifyType) {
    FacturasRectificadas facturasRectificadas = new FacturasRectificadas();
    facturasRectificadas.setIdFacturaRectificada(Arrays.asList(new IDFacturaARType(sellerNif, invoiceNumber, issueDate)));
    body.regFactu.registroFactura.registroAlta.facturasRectificadas = facturasRectificadas;
    body.regFactu.registroFactura.registroAlta.tipoFactura = invoiceType;
    body.regFactu.registroFactura.registroAlta.tipoRectificativa = modifyType;
    return this;
  }

  /**
   * 设置发票更正源 - 无原单
   *
   * @param invoiceType
   *     发票类型
   * @param modifyType
   *     : S -替换，I -增量
   * @return
   */
  public InvoiceRegisterRequest setRectificada(String invoiceType, String modifyType) {
    body.regFactu.registroFactura.registroAlta.tipoFactura = invoiceType;
    body.regFactu.registroFactura.registroAlta.tipoRectificativa = modifyType;
    return this;
  }

  /**
   * 获取税收明细
   *
   * @return 当前对象，用于链式调用
   */
  public List<DetalleDesglose> getDetalleDesglose() {
    final List<DetalleDesglose> detalleDesglose = body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose;
    return detalleDesglose;
  }

  /**
   * 获取税收明细
   *
   * @return 当前对象，用于链式调用
   */
  public void setDetalleDesglose(List<DetalleDesglose> detalleDesglose) {
    body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose = detalleDesglose;
  }


  /**
   * 添加税收明细
   *
   * @param taxType
   *     税种 (01=增值税)
   * @param regimeCode
   *     税收制度代码
   * @param operationType
   *     操作类型
   * @param taxRate
   *     税率
   * @param baseAmount
   *     基础金额
   * @param taxAmount
   *     税额
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest addTaxDetail(String taxType, String regimeCode,
                                             String operationType, String taxRate,
                                             BigDecimal baseAmount, BigDecimal taxAmount) {
    DetalleDesglose detalle = new DetalleDesglose();
    detalle.impuesto = taxType;
    detalle.claveRegimen = regimeCode;
    detalle.calificacionOperacion = operationType;
    detalle.tipoImpositivo = taxRate;
    detalle.baseImponibleOimporteNoSujeto = baseAmount.toString();
    detalle.cuotaRepercutida = taxAmount.toString();

    if (body.regFactu.registroFactura.registroAlta.desglose == null) {
      body.regFactu.registroFactura.registroAlta.desglose = new Desglose();
      body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose = new ArrayList<>();
    }

    body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose.add(detalle);

    return this;
  }

  /**
   * 添加税收明细带附加税得
   *
   * @param taxType
   *     税种 (01=增值税)
   * @param regimeCode
   *     税收制度代码
   * @param operationType
   *     操作类型
   * @param taxRate
   *     增值税率
   * @param insRate
   *     附加税率
   * @param baseAmount
   *     基础金额
   * @param taxAmount
   *     税额
   * @param insTaxAmount
   *     附加税额
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest addTaxDetail(String taxType, String regimeCode,
                                             String operationType, String taxRate, String insRate,
                                             BigDecimal baseAmount, BigDecimal taxAmount, BigDecimal insTaxAmount) {
    DetalleDesglose detalle = new DetalleDesglose();
    detalle.impuesto = taxType;
    detalle.claveRegimen = regimeCode;
    detalle.calificacionOperacion = operationType;
    detalle.tipoImpositivo = taxRate;
    detalle.baseImponibleOimporteNoSujeto = baseAmount.toString();
    detalle.cuotaRepercutida = taxAmount.toString();
    detalle.tipoRecargoEquivalencia = insRate;
    detalle.cuotaRecargoEquivalencia = insTaxAmount.toString();

    if (body.regFactu.registroFactura.registroAlta.desglose == null) {
      body.regFactu.registroFactura.registroAlta.desglose = new Desglose();
      body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose = new ArrayList<>();
    }

    body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose.add(detalle);

    return this;
  }

  /**
   * 添加税收明细-免税操作
   *
   * @param taxType
   *     税种 (01=增值税)
   * @param operationType
   *     操作类型: N2
   * @param baseAmount
   *     基础金额
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest addTaxDetail(String taxType, String operationType, BigDecimal baseAmount) {
    DetalleDesglose detalle = new DetalleDesglose();
    detalle.impuesto = taxType;
    detalle.calificacionOperacion = operationType;
    detalle.baseImponibleOimporteNoSujeto = baseAmount.toString();

    if (body.regFactu.registroFactura.registroAlta.desglose == null) {
      body.regFactu.registroFactura.registroAlta.desglose = new Desglose();
      body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose = new ArrayList<>();
    }

    body.regFactu.registroFactura.registroAlta.desglose.detalleDesglose.add(detalle);

    return this;
  }

  /**
   * 设置总金额
   *
   * @param totalTax
   *     总税额
   * @param totalAmount
   *     总金额
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest setTotalAmounts(BigDecimal totalTax, BigDecimal totalAmount) {
    body.regFactu.registroFactura.registroAlta.cuotaTotal = totalTax.toString();
    body.regFactu.registroFactura.registroAlta.importeTotal = totalAmount.toString();
    return this;
  }

  /**
   * 设置系统信息
   *
   * @param systemNif
   *     系统提供商税号
   * @param systemName
   *     系统提供商名称
   * @param systemSoftwareName
   *     系统软件名称
   * @param systemId
   *     系统ID
   * @param version
   *     版本
   * @param installationNumber
   *     安装编号
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest setSystemInfo(String systemNif, String systemName,
                                              String systemSoftwareName, String systemId,
                                              String version, String installationNumber) {
    SistemaInformatico sistemaInfo = new SistemaInformatico();
    sistemaInfo.nif = systemNif;
    sistemaInfo.nombreRazon = systemName;
    sistemaInfo.nombreSistemaInformatico = systemSoftwareName;
    sistemaInfo.idSistemaInformatico = systemId;
    sistemaInfo.version = version;
    sistemaInfo.numeroInstalacion = installationNumber;
    sistemaInfo.tipoUsoPosibleSoloVerifactu = "N";
    sistemaInfo.tipoUsoPosibleMultiOT = "S";
    sistemaInfo.indicadorMultiplesOT = "S";

    body.regFactu.registroFactura.registroAlta.sistemaInformatico = sistemaInfo;

    return this;
  }

  /**
   * 设置发票链接信息并计算哈希值
   *
   * @param previousInvoiceInfo
   *     上一张发票的信息，如果是第一张发票则为null
   * @return 当前对象，用于链式调用
   */
  public InvoiceRegisterRequest calculateAndSetHash(PreviousInvoiceInfo previousInvoiceInfo, String nowDate) {
    // 设置时间戳（马德里时区）
    body.regFactu.registroFactura.registroAlta.fechaHoraHusoGenRegistro = nowDate;

    // 设置链接信息
    Encadenamiento encadenamiento = new Encadenamiento();
    if (previousInvoiceInfo != null) {
      encadenamiento.registroAnterior = new RegistroAnterior();
      encadenamiento.registroAnterior.idEmisorFactura = previousInvoiceInfo.getIdEmisorFactura();
      encadenamiento.registroAnterior.numSerieFactura = previousInvoiceInfo.getNumSerieFactura();
      encadenamiento.registroAnterior.fechaExpedicionFactura = previousInvoiceInfo.getFechaExpedicionFactura();
      encadenamiento.registroAnterior.huella = previousInvoiceInfo.getHuella();
      body.regFactu.registroFactura.registroAlta.encadenamiento = encadenamiento;
    } else {
      // 首次则为空
      encadenamiento.primerRegistro = "S";
      body.regFactu.registroFactura.registroAlta.encadenamiento = encadenamiento;
    }

    // 计算当前发票的哈希值 (这里需要实现哈希计算逻辑)
    String currentHash = HashGenerator.calculateRegistrationHash(body.regFactu.registroFactura.registroAlta.idFactura.idEmisorFactura,
        body.regFactu.registroFactura.registroAlta.idFactura.numSerieFactura,
        body.regFactu.registroFactura.registroAlta.idFactura.fechaExpedicionFactura,
        body.regFactu.registroFactura.registroAlta.tipoFactura,
        body.regFactu.registroFactura.registroAlta.cuotaTotal,
        body.regFactu.registroFactura.registroAlta.importeTotal,
        previousInvoiceInfo != null ? previousInvoiceInfo.getHuella() : null,
        body.regFactu.registroFactura.registroAlta.fechaHoraHusoGenRegistro);
    body.regFactu.registroFactura.registroAlta.huella = currentHash;
    body.regFactu.registroFactura.registroAlta.tipoHuella = "01";

    return this;
  }


  /**
   * 创建具有默认值的请求对象
   *
   * @return 请求对象
   */
  public static InvoiceRegisterRequest createDefault() {
    InvoiceRegisterRequest request = new InvoiceRegisterRequest();

    // 设置默认值
    request.body.regFactu.registroFactura.registroAlta.idVersion = "1.0";
    request.body.regFactu.registroFactura.registroAlta.subsanacion = "N";
    request.body.regFactu.registroFactura.registroAlta.rechazoPrevio = "N";
    request.body.regFactu.registroFactura.registroAlta.macrodato = "N";
    request.body.regFactu.registroFactura.registroAlta.cupon = "N";
    request.body.regFactu.registroFactura.registroAlta.tipoHuella = "01";
    request.body.regFactu.registroFactura.registroAlta.tipoFactura = "F2";
    return request;
  }

  /**
   * 生成SOAP请求XML
   *
   * @return SOAP请求XML字符串
   * @throws JAXBException
   *     如果生成XML失败
   */
  public String toXml() throws JAXBException {
    return XmlUtils.marshal(this);
  }

  /**
   * 获取二维码链接
   * baseUrl
   *
   * @return 二维码链接 qrUrl
   * @throws JAXBException
   *     如果生成XML失败
   */
  public String getQrCode(String baseUrl) {
    final String qrUrl = QRGenerator.buildQRUrl(baseUrl,
        this.body.regFactu.cabecera.obligadoEmision.nif,
        this.body.regFactu.registroFactura.registroAlta.idFactura.numSerieFactura,
        this.body.regFactu.registroFactura.registroAlta.idFactura.fechaExpedicionFactura,
        this.body.regFactu.registroFactura.registroAlta.importeTotal);

    return qrUrl;
  }


  @XmlAccessorType(XmlAccessType.FIELD)
  @XmlType(propOrder = {"regFactu"})
  public static class Body {
    @XmlElement(name = "RegFactuSistemaFacturacion")
    public RegFactuSistemaFacturacion regFactu = new RegFactuSistemaFacturacion();

    public RegFactuSistemaFacturacion getRegFactu() {
      return this.regFactu;
    }

    public void setRegFactu(RegFactuSistemaFacturacion regFactu) {
      this.regFactu = regFactu;
    }
  }

  public Body getBody() {
    return this.body;
  }

  public void setBody(Body body) {
    this.body = body;
  }
}