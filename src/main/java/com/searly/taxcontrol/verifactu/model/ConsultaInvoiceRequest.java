package com.searly.taxcontrol.verifactu.model;


import com.searly.taxcontrol.verifactu.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 发票查询请求实体类
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"header", "body"})
public class ConsultaInvoiceRequest {

    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header = new Header();

    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body = new Body();

    public ConsultaInvoiceRequest() {
    }

    public ConsultaInvoiceRequest(String nif, String nombreRazon, String numSerieFactura, String ejercicio, String periodo) {
        this.body.consultaFactu.cabecera.obligadoEmision.nif = nif;
        this.body.consultaFactu.cabecera.obligadoEmision.nombreRazon = nombreRazon;
        this.body.consultaFactu.filtroConsulta.numSerieFactura = numSerieFactura;
        this.body.consultaFactu.filtroConsulta.periodoImputacion.ejercicio = ejercicio;
        this.body.consultaFactu.filtroConsulta.periodoImputacion.periodo = periodo;
    }

    public ConsultaInvoiceRequest(String nif, String nombreRazon, String ejercicio, String periodo, String fechaDate,String pageSize) {
        this.body.consultaFactu.cabecera.obligadoEmision.nif = nif;
        this.body.consultaFactu.cabecera.obligadoEmision.nombreRazon = nombreRazon;
        this.body.consultaFactu.filtroConsulta.periodoImputacion.ejercicio = ejercicio;
        this.body.consultaFactu.filtroConsulta.periodoImputacion.periodo = periodo;
        this.body.consultaFactu.filtroConsulta.clavePaginacion.idEmisorFactura = nif;
        this.body.consultaFactu.filtroConsulta.clavePaginacion.fechaExpedicionFactura = fechaDate;
        this.body.consultaFactu.filtroConsulta.clavePaginacion.numSerieFactura = pageSize;
    }

    public String toXml() throws JAXBException {
        return XmlUtils.marshal(this);
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Header {
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Body {
        @XmlElement(name = "ConsultaFactuSistemaFacturacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd")
        private ConsultaFactuSistemaFacturacion consultaFactu = new ConsultaFactuSistemaFacturacion();

        public ConsultaFactuSistemaFacturacion getConsultaFactu() {
            return consultaFactu;
        }

        public void setConsultaFactu(ConsultaFactuSistemaFacturacion consultaFactu) {
            this.consultaFactu = consultaFactu;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"cabecera", "filtroConsulta"})
    public static class ConsultaFactuSistemaFacturacion {
        @XmlElement(name = "Cabecera", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd")
        private Cabecera cabecera = new Cabecera();

        @XmlElement(name = "FiltroConsulta", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd")
        private FiltroConsulta filtroConsulta = new FiltroConsulta();

        public Cabecera getCabecera() {
            return cabecera;
        }

        public void setCabecera(Cabecera cabecera) {
            this.cabecera = cabecera;
        }

        public FiltroConsulta getFiltroConsulta() {
            return filtroConsulta;
        }

        public void setFiltroConsulta(FiltroConsulta filtroConsulta) {
            this.filtroConsulta = filtroConsulta;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"idVersion", "obligadoEmision"})
    public static class Cabecera {
        @XmlElement(name = "IDVersion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idVersion = "1.0";

        @XmlElement(name = "ObligadoEmision", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private ObligadoEmision obligadoEmision = new ObligadoEmision();

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
    @XmlType(propOrder = {"nombreRazon", "nif"})
    public static class ObligadoEmision {
        @XmlElement(name = "NombreRazon", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nombreRazon;

        @XmlElement(name = "NIF", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nif;

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
    @XmlType(propOrder = {"periodoImputacion", "clavePaginacion", "numSerieFactura"})
    public static class FiltroConsulta {
        @XmlElement(name = "PeriodoImputacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd")
        private PeriodoImputacion periodoImputacion = new PeriodoImputacion();

        @XmlElement(name = "ClavePaginacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd")
        private ClavePaginacion clavePaginacion = new ClavePaginacion();

        @XmlElement(name = "NumSerieFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd")
        private String numSerieFactura;

        public PeriodoImputacion getPeriodoImputacion() {
            return periodoImputacion;
        }

        public void setPeriodoImputacion(PeriodoImputacion periodoImputacion) {
            this.periodoImputacion = periodoImputacion;
        }

        public String getNumSerieFactura() {
            return numSerieFactura;
        }

        public void setNumSerieFactura(String numSerieFactura) {
            this.numSerieFactura = numSerieFactura;
        }

        public ClavePaginacion getClavePaginacion() {
            return this.clavePaginacion;
        }

        public void setClavePaginacion(ClavePaginacion clavePaginacion) {
            this.clavePaginacion = clavePaginacion;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(propOrder = {"ejercicio", "periodo"})
    public static class PeriodoImputacion {
        @XmlElement(name = "Ejercicio", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String ejercicio;

        @XmlElement(name = "Periodo", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String periodo;

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
    @XmlType(propOrder = {"idEmisorFactura", "numSerieFactura", "fechaExpedicionFactura"})
    public static class ClavePaginacion {
        @XmlElement(name = "IDEmisorFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idEmisorFactura;

        @XmlElement(name = "NumSerieFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String numSerieFactura;

        @XmlElement(name = "FechaExpedicionFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String fechaExpedicionFactura;

        public String getIdEmisorFactura() {
            return this.idEmisorFactura;
        }

        public void setIdEmisorFactura(String idEmisorFactura) {
            this.idEmisorFactura = idEmisorFactura;
        }

        public String getNumSerieFactura() {
            return this.numSerieFactura;
        }

        public void setNumSerieFactura(String numSerieFactura) {
            this.numSerieFactura = numSerieFactura;
        }

        public String getFechaExpedicionFactura() {
            return this.fechaExpedicionFactura;
        }

        public void setFechaExpedicionFactura(String fechaExpedicionFactura) {
            this.fechaExpedicionFactura = fechaExpedicionFactura;
        }
    }

    // Getters and Setters for root class
    public Header getHeader() {
        return header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
} 