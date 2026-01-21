package com.searly.taxcontrol.verifactu.model;


import com.searly.taxcontrol.verifactu.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 发票查询响应模型
 * 对应VeriFactu发票注册SOAP响应
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class VerifactuConsultaResponse {

    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;

    /**
     * 获取状态
     *
     * @return 状态
     */
    public String getEstadoEnvio() {
        if (body != null && body.respuesta != null) {
            return body.respuesta.resultadoConsulta;
        }
        return null;
    }

    /**
     * 获取时间戳
     *
     * @return 时间戳
     */
    public IDFactura getIDFactura() {
        if (body != null && body.respuesta != null && body.respuesta.registros != null && !body.respuesta.registros.isEmpty()) {
            return body.respuesta.registros.get(0).idFactura;
        }
        return null;
    }

    /**
     * 获取哈希值
     *
     * @return 税号
     */
    public String getHuella() {
        if (body != null && body.respuesta != null && body.respuesta.registros != null && !body.respuesta.registros.isEmpty()) {
            return body.respuesta.registros.get(0).datosRegistroFacturacion.huella;
        }
        return null;
    }
    /**
     * 获取发票状态
     *
     * @return 税号
     */
    public String getEstadoRegistro() {
        if (body != null && body.respuesta != null && body.respuesta.registros != null && !body.respuesta.registros.isEmpty() && body.respuesta.registros.get(0).estadoRegistro!=null) {
            return body.respuesta.registros.get(0).estadoRegistro.estadoRegistro;
        }
        return null;
    }

    /**
     * 判断是否成功
     *
     * @return 是否成功
     */
    public boolean isSuccess() {
        String estado = getEstadoEnvio();
        return "ConDatos".equals(estado);
    }

    /**
     * 从XML字符串解析响应
     *
     * @param xml XML字符串
     * @return 响应对象
     * @throws JAXBException 如果解析失败
     */
    public static VerifactuConsultaResponse fromXml(String xml) throws JAXBException {
        return XmlUtils.unmarshalWithNamespace(xml, VerifactuConsultaResponse.class);
    }

    // 内部类定义

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
        // 响应头部，通常为空
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        @XmlAttribute(name = "Id")
        private String id;

        @XmlElement(name = "RespuestaConsultaFactuSistemaFacturacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private RespuestaRegFactuSistemaFacturacion respuesta;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RespuestaRegFactuSistemaFacturacion {

        @XmlElement(name = "Cabecera", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private Cabecera cabecera;

        @XmlElement(name = "PeriodoImputacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private PeriodoImputacion periodoImputacion;

        @XmlElement(name = "IndicadorPaginacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String indicadorPaginacion;

        @XmlElement(name = "ResultadoConsulta", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String resultadoConsulta;

        @XmlElement(name = "RegistroRespuestaConsultaFactuSistemaFacturacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private List<RegistroRespuestaConsulta> registros;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Cabecera {
        @XmlElement(name = "IDVersion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idVersion;

        @XmlElement(name = "ObligadoEmision", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private ObligadoEmision obligadoEmision;

        public Cabecera() {
        }

        public String getIdVersion() {
            return idVersion;
        }

        public void setIdVersion(String idVersion) {
            this.idVersion = idVersion;
        }

        public ObligadoEmision getObligadoEmision() {
            return obligadoEmision;
        }

        public void setObligadoEmision(ObligadoEmision obligadoEmision) {
            this.obligadoEmision = obligadoEmision;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class PeriodoImputacion {
        @XmlElement(name = "Ejercicio", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String ejercicio;

        @XmlElement(name = "Periodo", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String periodo;

        public PeriodoImputacion() {
        }

        public String getEjercicio() {
            return ejercicio;
        }

        public void setEjercicio(String ejercicio) {
            this.ejercicio = ejercicio;
        }

        public String getPeriodo() {
            return periodo;
        }

        public void setPeriodo(String periodo) {
            this.periodo = periodo;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ObligadoEmision {
        @XmlElement(name = "NombreRazon", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nombreRazon;

        @XmlElement(name = "NIF", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nif;

        public ObligadoEmision() {
        }

        public String getNombreRazon() {
            return nombreRazon;
        }

        public void setNombreRazon(String nombreRazon) {
            this.nombreRazon = nombreRazon;
        }

        public String getNif() {
            return nif;
        }

        public void setNif(String nif) {
            this.nif = nif;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DatosPresentacion {
        @XmlElement(name = "NIFPresentador", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nifPresentador;

        @XmlElement(name = "TimestampPresentacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String timestampPresentacion;

        @XmlElement(name = "IdPeticion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idPeticion;

        public DatosPresentacion() {
        }

        public String getNifPresentador() {
            return nifPresentador;
        }

        public void setNifPresentador(String nifPresentador) {
            this.nifPresentador = nifPresentador;
        }

        public String getTimestampPresentacion() {
            return timestampPresentacion;
        }

        public void setTimestampPresentacion(String timestampPresentacion) {
            this.timestampPresentacion = timestampPresentacion;
        }

        public String getIdPeticion() {
            return idPeticion;
        }

        public void setIdPeticion(String idPeticion) {
            this.idPeticion = idPeticion;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegistroRespuestaConsulta {
        @XmlElement(name = "IDFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private IDFactura idFactura;

        @XmlElement(name = "DatosRegistroFacturacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private DatosRegistroFacturacion datosRegistroFacturacion;

        @XmlElement(name = "DatosPresentacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private DatosPresentacion datosPresentacion;

        @XmlElement(name = "EstadoRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private EstadoRegistroInfo estadoRegistro;

        public RegistroRespuestaConsulta() {
        }

        public IDFactura getIdFactura() {
            return idFactura;
        }

        public void setIdFactura(IDFactura idFactura) {
            this.idFactura = idFactura;
        }

        public DatosRegistroFacturacion getDatosRegistroFacturacion() {
            return datosRegistroFacturacion;
        }

        public void setDatosRegistroFacturacion(DatosRegistroFacturacion datosRegistroFacturacion) {
            this.datosRegistroFacturacion = datosRegistroFacturacion;
        }

        public DatosPresentacion getDatosPresentacion() {
            return datosPresentacion;
        }

        public void setDatosPresentacion(DatosPresentacion datosPresentacion) {
            this.datosPresentacion = datosPresentacion;
        }

        public EstadoRegistroInfo getEstadoRegistro() {
            return estadoRegistro;
        }

        public void setEstadoRegistro(EstadoRegistroInfo estadoRegistro) {
            this.estadoRegistro = estadoRegistro;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class EstadoRegistroInfo {
        @XmlElement(name = "TimestampUltimaModificacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String timestampUltimaModificacion;

        @XmlElement(name = "EstadoRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String estadoRegistro;

        @XmlElement(name = "CodigoErrorRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String codigoErrorRegistro;

        @XmlElement(name = "DescripcionErrorRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String descripcionErrorRegistro;

        public EstadoRegistroInfo() {
        }

        public String getTimestampUltimaModificacion() {
            return timestampUltimaModificacion;
        }

        public void setTimestampUltimaModificacion(String timestampUltimaModificacion) {
            this.timestampUltimaModificacion = timestampUltimaModificacion;
        }

        public String getEstadoRegistro() {
            return estadoRegistro;
        }

        public void setEstadoRegistro(String estadoRegistro) {
            this.estadoRegistro = estadoRegistro;
        }

        public String getCodigoErrorRegistro() {
            return codigoErrorRegistro;
        }

        public void setCodigoErrorRegistro(String codigoErrorRegistro) {
            this.codigoErrorRegistro = codigoErrorRegistro;
        }

        public String getDescripcionErrorRegistro() {
            return descripcionErrorRegistro;
        }

        public void setDescripcionErrorRegistro(String descripcionErrorRegistro) {
            this.descripcionErrorRegistro = descripcionErrorRegistro;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DatosRegistroFacturacion {
        @XmlElement(name = "Subsanacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String subsanacion;

        @XmlElement(name = "RechazoPrevio", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String rechazoPrevio;

        @XmlElement(name = "TipoFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String tipoFactura;

        @XmlElement(name = "FechaOperacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String fechaOperacion;

        @XmlElement(name = "DescripcionOperacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String descripcionOperacion;

        @XmlElement(name = "FacturaSimplificadaArt7273", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String facturaSimplificadaArt7273;

        @XmlElement(name = "FacturaSinIdentifDestinatarioArt61d", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String facturaSinIdentifDestinatarioArt61d;

        @XmlElement(name = "Macrodato", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String macrodato;

        @XmlElement(name = "Destinatarios", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private Destinatarios destinatarios;

        @XmlElement(name = "Cupon", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String cupon;

        @XmlElement(name = "Desglose", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private Desglose desglose;

        @XmlElement(name = "ImporteTotal", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String importeTotal;

        @XmlElement(name = "CuotaTotal", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String cuotaTotal;

        @XmlElement(name = "Encadenamiento", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private Encadenamiento encadenamiento;

        @XmlElement(name = "FechaHoraHusoGenRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String fechaHoraHusoGenRegistro;

        @XmlElement(name = "TipoHuella", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String tipoHuella;

        @XmlElement(name = "Huella", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String huella;

        public DatosRegistroFacturacion() {
        }

        public String getSubsanacion() {
            return subsanacion;
        }

        public void setSubsanacion(String subsanacion) {
            this.subsanacion = subsanacion;
        }

        public String getRechazoPrevio() {
            return rechazoPrevio;
        }

        public void setRechazoPrevio(String rechazoPrevio) {
            this.rechazoPrevio = rechazoPrevio;
        }

        public String getTipoFactura() {
            return tipoFactura;
        }

        public void setTipoFactura(String tipoFactura) {
            this.tipoFactura = tipoFactura;
        }

        public String getFechaOperacion() {
            return fechaOperacion;
        }

        public void setFechaOperacion(String fechaOperacion) {
            this.fechaOperacion = fechaOperacion;
        }

        public String getDescripcionOperacion() {
            return descripcionOperacion;
        }

        public void setDescripcionOperacion(String descripcionOperacion) {
            this.descripcionOperacion = descripcionOperacion;
        }

        public String getFacturaSimplificadaArt7273() {
            return facturaSimplificadaArt7273;
        }

        public void setFacturaSimplificadaArt7273(String facturaSimplificadaArt7273) {
            this.facturaSimplificadaArt7273 = facturaSimplificadaArt7273;
        }

        public String getFacturaSinIdentifDestinatarioArt61d() {
            return facturaSinIdentifDestinatarioArt61d;
        }

        public void setFacturaSinIdentifDestinatarioArt61d(String facturaSinIdentifDestinatarioArt61d) {
            this.facturaSinIdentifDestinatarioArt61d = facturaSinIdentifDestinatarioArt61d;
        }

        public String getMacrodato() {
            return macrodato;
        }

        public void setMacrodato(String macrodato) {
            this.macrodato = macrodato;
        }

        public Destinatarios getDestinatarios() {
            return destinatarios;
        }

        public void setDestinatarios(Destinatarios destinatarios) {
            this.destinatarios = destinatarios;
        }

        public String getCupon() {
            return cupon;
        }

        public void setCupon(String cupon) {
            this.cupon = cupon;
        }

        public Desglose getDesglose() {
            return desglose;
        }

        public void setDesglose(Desglose desglose) {
            this.desglose = desglose;
        }

        public String getImporteTotal() {
            return importeTotal;
        }

        public void setImporteTotal(String importeTotal) {
            this.importeTotal = importeTotal;
        }

        public String getCuotaTotal() {
            return cuotaTotal;
        }

        public void setCuotaTotal(String cuotaTotal) {
            this.cuotaTotal = cuotaTotal;
        }

        public Encadenamiento getEncadenamiento() {
            return encadenamiento;
        }

        public void setEncadenamiento(Encadenamiento encadenamiento) {
            this.encadenamiento = encadenamiento;
        }

        public String getFechaHoraHusoGenRegistro() {
            return fechaHoraHusoGenRegistro;
        }

        public void setFechaHoraHusoGenRegistro(String fechaHoraHusoGenRegistro) {
            this.fechaHoraHusoGenRegistro = fechaHoraHusoGenRegistro;
        }

        public String getTipoHuella() {
            return tipoHuella;
        }

        public void setTipoHuella(String tipoHuella) {
            this.tipoHuella = tipoHuella;
        }

        public String getHuella() {
            return huella;
        }

        public void setHuella(String huella) {
            this.huella = huella;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Destinatarios {
        @XmlElement(name = "IDDestinatario", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private IDDestinatario idDestinatario;

        public Destinatarios() {
        }

        public IDDestinatario getIdDestinatario() {
            return idDestinatario;
        }

        public void setIdDestinatario(IDDestinatario idDestinatario) {
            this.idDestinatario = idDestinatario;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IDDestinatario {
        @XmlElement(name = "NombreRazon", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nombreRazon;

        @XmlElement(name = "NIF", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nif;

        public IDDestinatario() {
        }

        public String getNombreRazon() {
            return nombreRazon;
        }

        public void setNombreRazon(String nombreRazon) {
            this.nombreRazon = nombreRazon;
        }

        public String getNif() {
            return nif;
        }

        public void setNif(String nif) {
            this.nif = nif;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Desglose {
        @XmlElement(name = "DetalleDesglose", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private DetalleDesglose detalleDesglose;

        public Desglose() {
        }

        public DetalleDesglose getDetalleDesglose() {
            return detalleDesglose;
        }

        public void setDetalleDesglose(DetalleDesglose detalleDesglose) {
            this.detalleDesglose = detalleDesglose;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DetalleDesglose {
        @XmlElement(name = "Impuesto", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String impuesto;

        @XmlElement(name = "ClaveRegimen", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String claveRegimen;

        @XmlElement(name = "CalificacionOperacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String calificacionOperacion;

        @XmlElement(name = "TipoImpositivo", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String tipoImpositivo;

        @XmlElement(name = "BaseImponibleOimporteNoSujeto", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String baseImponibleOimporteNoSujeto;

        @XmlElement(name = "CuotaRepercutida", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String cuotaRepercutida;

        public DetalleDesglose() {
        }

        public String getImpuesto() {
            return impuesto;
        }

        public void setImpuesto(String impuesto) {
            this.impuesto = impuesto;
        }

        public String getClaveRegimen() {
            return claveRegimen;
        }

        public void setClaveRegimen(String claveRegimen) {
            this.claveRegimen = claveRegimen;
        }

        public String getCalificacionOperacion() {
            return calificacionOperacion;
        }

        public void setCalificacionOperacion(String calificacionOperacion) {
            this.calificacionOperacion = calificacionOperacion;
        }

        public String getTipoImpositivo() {
            return tipoImpositivo;
        }

        public void setTipoImpositivo(String tipoImpositivo) {
            this.tipoImpositivo = tipoImpositivo;
        }

        public String getBaseImponibleOimporteNoSujeto() {
            return baseImponibleOimporteNoSujeto;
        }

        public void setBaseImponibleOimporteNoSujeto(String baseImponibleOimporteNoSujeto) {
            this.baseImponibleOimporteNoSujeto = baseImponibleOimporteNoSujeto;
        }

        public String getCuotaRepercutida() {
            return cuotaRepercutida;
        }

        public void setCuotaRepercutida(String cuotaRepercutida) {
            this.cuotaRepercutida = cuotaRepercutida;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Encadenamiento {
        @XmlElement(name = "PrimerRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private String primerRegistro;

        @XmlElement(name = "RegistroAnterior", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaConsultaLR.xsd")
        private RegistroAnterior registroAnterior;

        public Encadenamiento() {
        }

        public String getPrimerRegistro() {
            return primerRegistro;
        }

        public void setPrimerRegistro(String primerRegistro) {
            this.primerRegistro = primerRegistro;
        }

        public RegistroAnterior getRegistroAnterior() {
            return registroAnterior;
        }

        public void setRegistroAnterior(RegistroAnterior registroAnterior) {
            this.registroAnterior = registroAnterior;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RegistroAnterior {
        @XmlElement(name = "IDEmisorFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idEmisorFactura;

        @XmlElement(name = "NumSerieFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String numSerieFactura;

        @XmlElement(name = "FechaExpedicionFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String fechaExpedicionFactura;

        @XmlElement(name = "Huella", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String huella;

        public RegistroAnterior() {
        }

        public String getIdEmisorFactura() {
            return idEmisorFactura;
        }

        public void setIdEmisorFactura(String idEmisorFactura) {
            this.idEmisorFactura = idEmisorFactura;
        }

        public String getNumSerieFactura() {
            return numSerieFactura;
        }

        public void setNumSerieFactura(String numSerieFactura) {
            this.numSerieFactura = numSerieFactura;
        }

        public String getFechaExpedicionFactura() {
            return fechaExpedicionFactura;
        }

        public void setFechaExpedicionFactura(String fechaExpedicionFactura) {
            this.fechaExpedicionFactura = fechaExpedicionFactura;
        }

        public String getHuella() {
            return huella;
        }

        public void setHuella(String huella) {
            this.huella = huella;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IDFactura {
        @XmlElement(name = "IDEmisorFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idEmisorFactura;

        @XmlElement(name = "NumSerieFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String numSerieFactura;

        @XmlElement(name = "FechaExpedicionFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String fechaExpedicionFactura;

        public IDFactura() {
        }

        public String getIdEmisorFactura() {
            return idEmisorFactura;
        }

        public void setIdEmisorFactura(String idEmisorFactura) {
            this.idEmisorFactura = idEmisorFactura;
        }

        public String getNumSerieFactura() {
            return numSerieFactura;
        }

        public void setNumSerieFactura(String numSerieFactura) {
            this.numSerieFactura = numSerieFactura;
        }

        public String getFechaExpedicionFactura() {
            return fechaExpedicionFactura;
        }

        public void setFechaExpedicionFactura(String fechaExpedicionFactura) {
            this.fechaExpedicionFactura = fechaExpedicionFactura;
        }
    }
} 