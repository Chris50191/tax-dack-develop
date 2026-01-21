package com.searly.taxcontrol.verifactu.model;


import com.searly.taxcontrol.verifactu.utils.HashGenerator;
import com.searly.taxcontrol.verifactu.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * 修正发票请求实体类
 * 对应修正/更新发票的请求格式
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"body"})
public class CorrectionInvoiceRequest {

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
     * @param sellerNif 卖方税号
     * @param sellerName 卖方名称
     * @param invoiceNumber 发票编号
     * @param issueDate 签发日期 (格式: dd-MM-yyyy)
     * @param invoiceType 发票类型
     * @param description 描述
     * @return 当前对象，用于链式调用
     */
    public CorrectionInvoiceRequest setBasicInfo(String sellerNif, String sellerName,
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
     * 设置买方信息
     *
     * @param buyerNif 买方税号
     * @param buyerName 买方名称
     * @return 当前对象，用于链式调用
     */
    public CorrectionInvoiceRequest setBuyerInfo(String buyerNif, String buyerName) {
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
     * 添加税收明细
     *
     * @param taxType 税种 (01=增值税)
     * @param regimeCode 税收制度代码
     * @param operationType 操作类型
     * @param taxRate 税率
     * @param baseAmount 基础金额
     * @param taxAmount 税额
     * @return 当前对象，用于链式调用
     */
    public CorrectionInvoiceRequest addTaxDetail(String taxType, String regimeCode,
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
     * 设置总金额
     *
     * @param totalTax 总税额
     * @param totalAmount 总金额
     * @return 当前对象，用于链式调用
     */
    public CorrectionInvoiceRequest setTotalAmounts(BigDecimal totalTax, BigDecimal totalAmount) {
        body.regFactu.registroFactura.registroAlta.cuotaTotal = totalTax.toString();
        body.regFactu.registroFactura.registroAlta.importeTotal = totalAmount.toString();
        return this;
    }

    /**
     * 设置系统信息
     *
     * @param systemNif 系统提供商税号
     * @param systemName 系统提供商名称
     * @param systemSoftwareName 系统软件名称
     * @param systemId 系统ID
     * @param version 版本
     * @param installationNumber 安装编号
     * @return 当前对象，用于链式调用
     */
    public CorrectionInvoiceRequest setSystemInfo(String systemNif, String systemName,
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
     * @param previousInvoiceInfo 上一张发票的信息，如果是第一张发票则为null
     * @return 当前对象，用于链式调用
     */
    public CorrectionInvoiceRequest calculateAndSetHash(PreviousInvoiceInfo previousInvoiceInfo) {
        // 设置时间戳（马德里时区）
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
        body.regFactu.registroFactura.registroAlta.fechaHoraHusoGenRegistro =
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

        // 设置链接信息
        Encadenamiento encadenamiento = new Encadenamiento();
        if (previousInvoiceInfo != null) {
            encadenamiento.registroAnterior = new RegistroAnterior();
            encadenamiento.registroAnterior.idEmisorFactura = previousInvoiceInfo.getIdEmisorFactura();
            encadenamiento.registroAnterior.numSerieFactura = previousInvoiceInfo.getNumSerieFactura();
            encadenamiento.registroAnterior.fechaExpedicionFactura = previousInvoiceInfo.getFechaExpedicionFactura();
            encadenamiento.registroAnterior.huella = previousInvoiceInfo.getHuella();
            body.regFactu.registroFactura.registroAlta.encadenamiento = encadenamiento;
        }else {
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
    public static CorrectionInvoiceRequest createDefault() {
        CorrectionInvoiceRequest request = new CorrectionInvoiceRequest();

        // 设置默认值
        request.body.regFactu.registroFactura.registroAlta.idVersion = "1.0";
        request.body.regFactu.registroFactura.registroAlta.subsanacion = "S";
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
     * @throws JAXBException 如果生成XML失败
     */
    public String toXml() throws JAXBException {
        return XmlUtils.marshal(this);
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