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
 * 发票注册响应模型
 * 对应VeriFactu发票注册SOAP响应
 */
@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
@XmlAccessorType(XmlAccessType.FIELD)
public class VerifactuResponse {
    
    @XmlElement(name = "Header", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Header header;
    
    @XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
    private Body body;
    
    /**
     * 获取CSV值
     * 
     * @return CSV值
     */
    public String getCsv() {
        if (body != null && body.respuesta != null) {
            return body.respuesta.csv;
        }
        return null;
    }
    
    /**
     * 获取状态
     * 
     * @return 状态
     */
    public String getEstadoEnvio() {
        if (body != null && body.respuesta != null) {
            return body.respuesta.estadoEnvio;
        }
        return null;
    }

    /**
     * 获取错误码
     *
     * @return 状态
     */
    public String getCodigoErrorRegistro() {
        if (body != null && body.respuesta != null && body.respuesta.respuestaLineas!= null && !body.respuesta.respuestaLineas.isEmpty()) {
            return body.respuesta.respuestaLineas.get(0).codigoErrorRegistro;
        }
        return null;
    }

    /**
     * 获取错误描述
     *
     * @return 状态
     */
    public String getDescripcionErrorRegistro() {
        if (body != null && body.respuesta != null && body.respuesta.respuestaLineas!= null && !body.respuesta.respuestaLineas.isEmpty()) {
            return body.respuesta.respuestaLineas.get(0).descripcionErrorRegistro;
        }
        return null;
    }

    /**
     * 获取时间戳
     * 
     * @return 时间戳
     */
    public String getTimestamp() {
        if (body != null && body.respuesta != null && body.respuesta.datosPresentacion != null) {
            return body.respuesta.datosPresentacion.timestampPresentacion;
        }
        return null;
    }
    
    /**
     * 获取税号
     * 
     * @return 税号
     */
    public String getNifPresentador() {
        if (body != null && body.respuesta != null && body.respuesta.datosPresentacion != null) {
            return body.respuesta.datosPresentacion.nifPresentador;
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
        return "Correcto".equals(estado);
    }
    
    /**
     * 从XML字符串解析响应
     * 
     * @param xml XML字符串
     * @return 响应对象
     * @throws JAXBException 如果解析失败
     */
    public static VerifactuResponse fromXml(String xml) throws JAXBException {
        return XmlUtils.unmarshalWithNamespace(xml, VerifactuResponse.class);
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
        
        @XmlElement(name = "RespuestaRegFactuSistemaFacturacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private RespuestaRegFactuSistemaFacturacion respuesta;

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public RespuestaRegFactuSistemaFacturacion getRespuesta() {
            return this.respuesta;
        }

        public void setRespuesta(RespuestaRegFactuSistemaFacturacion respuesta) {
            this.respuesta = respuesta;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RespuestaRegFactuSistemaFacturacion {
        @XmlElement(name = "CSV", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private String csv;
        
        @XmlElement(name = "DatosPresentacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private DatosPresentacion datosPresentacion;
        
        @XmlElement(name = "Cabecera", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private Cabecera cabecera;
        
        @XmlElement(name = "TiempoEsperaEnvio", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private String tiempoEsperaEnvio;
        
        @XmlElement(name = "EstadoEnvio", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private String estadoEnvio;
        
        @XmlElement(name = "RespuestaLinea", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private List<RespuestaLinea> respuestaLineas;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DatosPresentacion {
        @XmlElement(name = "NIFPresentador", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nifPresentador;
        
        @XmlElement(name = "TimestampPresentacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String timestampPresentacion;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Cabecera {
        @XmlElement(name = "ObligadoEmision", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private ObligadoEmision obligadoEmision;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ObligadoEmision {
        @XmlElement(name = "NombreRazon", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nombreRazon;
        
        @XmlElement(name = "NIF", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String nif;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class RespuestaLinea {
        @XmlElement(name = "IDFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private IDFactura idFactura;
        
        @XmlElement(name = "Operacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private Operacion operacion;
        
        @XmlElement(name = "EstadoRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private String estadoRegistro;

        @XmlElement(name = "CodigoErrorRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private String codigoErrorRegistro;

        @XmlElement(name = "DescripcionErrorRegistro", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/RespuestaSuministro.xsd")
        private String descripcionErrorRegistro;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class IDFactura {
        @XmlElement(name = "IDEmisorFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String idEmisorFactura;
        
        @XmlElement(name = "NumSerieFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String numSerieFactura;
        
        @XmlElement(name = "FechaExpedicionFactura", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String fechaExpedicionFactura;
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Operacion {
        @XmlElement(name = "TipoOperacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String tipoOperacion;
        
        @XmlElement(name = "Subsanacion", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String subsanacion;
        
        @XmlElement(name = "RechazoPrevio", namespace = "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd")
        private String rechazoPrevio;
    }

    public Header getHeader() {
        return this.header;
    }

    public void setHeader(Header header) {
        this.header = header;
    }

    public Body getBody() {
        return this.body;
    }

    public void setBody(Body body) {
        this.body = body;
    }
}