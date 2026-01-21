package com.searly.taxcontrol.verifactu.model;

import com.searly.taxcontrol.verifactu.utils.HashGenerator;
import com.searly.taxcontrol.verifactu.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 注销发票请求实体类
 * 对应注销发票的请求格式
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    
    @XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"body"})
public class CancelInvoiceRequest {

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;

    public CancelInvoiceRequest() {
        this.body = new Body();
    }

    public CancelInvoiceRequest(String nif, String nombreRazon, String numSerieFactura, String fechaExpedicion) {
        this.body = new Body();
        this.body.regFactu.cabecera.obligadoEmision.nif = nif;
        this.body.regFactu.cabecera.obligadoEmision.nombreRazon = nombreRazon;
        
        this.body.regFactu.registroFactura.registroAnulacion.idFactura.idEmisorFacturaAnulada = nif;
        this.body.regFactu.registroFactura.registroAnulacion.idFactura.numSerieFacturaAnulada = numSerieFactura;
        this.body.regFactu.registroFactura.registroAnulacion.idFactura.fechaExpedicionFacturaAnulada = fechaExpedicion;
    }


    /**
     * 设置发票链接信息并计算哈希值
     *
     * @param previousInvoiceInfo 上一张发票的信息，如果是第一张发票则为null
     * @return 当前对象，用于链式调用
     */
    public CancelInvoiceRequest calculateAndSetHash(PreviousInvoiceInfo previousInvoiceInfo) {
        // 设置时间戳（马德里时区）
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
        body.regFactu.registroFactura.registroAnulacion.fechaHoraHusoGenRegistro =
                now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

        // 设置链接信息
        Encadenamiento encadenamiento = new Encadenamiento();
        if (previousInvoiceInfo != null) {
            encadenamiento.registroAnterior = new RegistroAnterior();
            encadenamiento.registroAnterior.idEmisorFactura = previousInvoiceInfo.getIdEmisorFactura();
            encadenamiento.registroAnterior.numSerieFactura = previousInvoiceInfo.getNumSerieFactura();
            encadenamiento.registroAnterior.fechaExpedicionFactura = previousInvoiceInfo.getFechaExpedicionFactura();
            encadenamiento.registroAnterior.huella = previousInvoiceInfo.getHuella();
            body.regFactu.registroFactura.registroAnulacion.encadenamiento = encadenamiento;
        }else {
            // 首次则为空
            encadenamiento.primerRegistro = "S";
            body.regFactu.registroFactura.registroAnulacion.encadenamiento = encadenamiento;
        }

        // 计算当前发票的哈希值 (这里需要实现哈希计算逻辑)
        String currentHash = HashGenerator.calculateRegistrationCancelHash(body.regFactu.registroFactura.registroAnulacion.idFactura.idEmisorFacturaAnulada,
                                                                     body.regFactu.registroFactura.registroAnulacion.idFactura.numSerieFacturaAnulada,
                                                                     body.regFactu.registroFactura.registroAnulacion.idFactura.fechaExpedicionFacturaAnulada,
                                                                     previousInvoiceInfo != null ? previousInvoiceInfo.getHuella() : null,
                                                                     body.regFactu.registroFactura.registroAnulacion.fechaHoraHusoGenRegistro);
        body.regFactu.registroFactura.registroAnulacion.huella = currentHash;
        body.regFactu.registroFactura.registroAnulacion.tipoHuella = "01";

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
    public CancelInvoiceRequest setSystemInfo(String systemNif, String systemName,
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

        body.regFactu.registroFactura.registroAnulacion.sistemaInformatico = sistemaInfo;

        return this;
    }


    /**
     * 将对象转换为XML字符串
     * @return XML字符串
     * @throws JAXBException 如果序列化失败
     */
    public String toXml() throws JAXBException {
        return XmlUtils.marshal(this);
    }

    /**
     * 获取发票哈希值
     *
     * @return 哈希值
     */
    public String getHuella() {
        if (body != null && body.regFactu != null && body.regFactu.registroFactura != null) {
            return body.regFactu.registroFactura.registroAnulacion.huella;
        }
        return null;
    }

        
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"regFactu"})
    public static class Body {
        @XmlElement(name = "RegFactuSistemaFacturacion")
        public RegFactuSistemaFacturacion regFactu = new RegFactuSistemaFacturacion();
    }

        
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"cabecera", "registroFactura"})
    public static class RegFactuSistemaFacturacion {
        @XmlElement(name = "Cabecera")
        public Cabecera cabecera = new Cabecera();

        @XmlElement(name = "RegistroFactura")
        public RegistroFactura registroFactura = new RegistroFactura();
    }


        
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"registroAnulacion"})
    public static class RegistroFactura {
        @XmlElement(name = "RegistroAnulacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private RegistroAnulacion registroAnulacion = new RegistroAnulacion();
    }

        
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {
            "idVersion", "idFactura",
            "encadenamiento", "sistemaInformatico", "fechaHoraHusoGenRegistro",
            "tipoHuella", "huella"
    })
    public static class RegistroAnulacion {
        @XmlElement(name = "IDVersion")
        private String idVersion = "1.0";

        @XmlElement(name = "IDFactura")
        private IDFactura idFactura = new IDFactura();

        @XmlElement(name = "Encadenamiento")
        public Encadenamiento encadenamiento = new Encadenamiento();

        @XmlElement(name = "SistemaInformatico")
        public SistemaInformatico sistemaInformatico = new SistemaInformatico();

        @XmlElement(name = "FechaHoraHusoGenRegistro")
        public String fechaHoraHusoGenRegistro;

        @XmlElement(name = "TipoHuella")
        public String tipoHuella;

        @XmlElement(name = "Huella")
        public String huella;
    }

    
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"idEmisorFacturaAnulada", "numSerieFacturaAnulada", "fechaExpedicionFacturaAnulada"})
    public static class IDFactura {
        @XmlElement(name = "IDEmisorFacturaAnulada", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd")
        private String idEmisorFacturaAnulada;

        @XmlElement(name = "NumSerieFacturaAnulada", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd")
        private String numSerieFacturaAnulada;

        @XmlElement(name = "FechaExpedicionFacturaAnulada", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd")
        private String fechaExpedicionFacturaAnulada;

        public IDFactura() {
        }
    }
} 