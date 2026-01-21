package com.searly.taxcontrol.verifactu.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 查询发票响应实体类
 * 对应查询发票的响应格式
 */
@XmlRootElement(name = "RespuestaConsultaFactuSistemaFacturacion")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsultaResponse {

    @XmlElement(name = "Cabecera")
    private Cabecera cabecera;

    @XmlElement(name = "DatosPresentacion")
    private DatosPresentacion datosPresentacion;

    @XmlElement(name = "EstadoEnvio")
    private String estadoEnvio;

    @XmlElement(name = "CSV")
    private String csv;

    @XmlElement(name = "DatosRegistroFacturacion")
    private List<DatosRegistroFacturacion> datosRegistroFacturacion;

    public ConsultaResponse() {
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Cabecera {
        @XmlElement(name = "ObligadoEmision")
        private ObligadoEmision obligadoEmision;

        public Cabecera() {
        }

        public ObligadoEmision getObligadoEmision() {
            return obligadoEmision;
        }

        public void setObligadoEmision(ObligadoEmision obligadoEmision) {
            this.obligadoEmision = obligadoEmision;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ObligadoEmision {
        @XmlElement(name = "NombreRazon")
        private String nombreRazon;

        @XmlElement(name = "NIF")
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
        @XmlElement(name = "NIFPresentador")
        private String nifPresentador;

        @XmlElement(name = "TimestampPresentacion")
        private String timestampPresentacion;

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
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DatosRegistroFacturacion {
        @XmlElement(name = "IDFactura")
        private IDFactura idFactura;

        @XmlElement(name = "TipoFactura")
        private String tipoFactura;

        @XmlElement(name = "FechaOperacion")
        private String fechaOperacion;

        @XmlElement(name = "DescripcionOperacion")
        private String descripcionOperacion;

        @XmlElement(name = "ImporteTotal")
        private String importeTotal;

        @XmlElement(name = "CuotaTotal")
        private String cuotaTotal;

        @XmlElement(name = "Huella")
        private String huella;

        @XmlElement(name = "TipoHuella")
        private String tipoHuella;

        @XmlElement(name = "FechaHoraRegistro")
        private String fechaHoraRegistro;

        @XmlElement(name = "EstadoRegistro")
        private String estadoRegistro;

        @XmlElement(name = "CSV")
        private String csv;

        @XmlElement(name = "QR")
        private String qr;

        public DatosRegistroFacturacion() {
        }

        public IDFactura getIdFactura() {
            return idFactura;
        }

        public void setIdFactura(IDFactura idFactura) {
            this.idFactura = idFactura;
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

        public String getHuella() {
            return huella;
        }

        public void setHuella(String huella) {
            this.huella = huella;
        }

        public String getTipoHuella() {
            return tipoHuella;
        }

        public void setTipoHuella(String tipoHuella) {
            this.tipoHuella = tipoHuella;
        }

        public String getFechaHoraRegistro() {
            return fechaHoraRegistro;
        }

        public void setFechaHoraRegistro(String fechaHoraRegistro) {
            this.fechaHoraRegistro = fechaHoraRegistro;
        }

        public String getEstadoRegistro() {
            return estadoRegistro;
        }

        public void setEstadoRegistro(String estadoRegistro) {
            this.estadoRegistro = estadoRegistro;
        }

        public String getCsv() {
            return csv;
        }

        public void setCsv(String csv) {
            this.csv = csv;
        }

        public String getQr() {
            return qr;
        }

        public void setQr(String qr) {
            this.qr = qr;
        }
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IDFactura {
        @XmlElement(name = "IDEmisorFactura")
        private String idEmisorFactura;

        @XmlElement(name = "NumSerieFactura")
        private String numSerieFactura;

        @XmlElement(name = "FechaExpedicionFactura")
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

    public Cabecera getCabecera() {
        return cabecera;
    }

    public void setCabecera(Cabecera cabecera) {
        this.cabecera = cabecera;
    }

    public DatosPresentacion getDatosPresentacion() {
        return datosPresentacion;
    }

    public void setDatosPresentacion(DatosPresentacion datosPresentacion) {
        this.datosPresentacion = datosPresentacion;
    }

    public String getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(String estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public String getCsv() {
        return csv;
    }

    public void setCsv(String csv) {
        this.csv = csv;
    }

    public List<DatosRegistroFacturacion> getDatosRegistroFacturacion() {
        return datosRegistroFacturacion;
    }

    public void setDatosRegistroFacturacion(List<DatosRegistroFacturacion> datosRegistroFacturacion) {
        this.datosRegistroFacturacion = datosRegistroFacturacion;
    }
} 