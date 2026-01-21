package com.searly.taxcontrol.sii.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * SII发送状态查询JSON响应模型类
 * 专门用于处理SII系统返回的发送状态查询JSON格式响应
 * 
 * 与ResultadoEnvioDataRespuesta不同，这个类使用Jackson注解处理JSON
 */
public class SiiEnvioStatusResponse {
    
    /**
     * 发票发送者RUT号码
     */
    @JsonProperty("rut_emisor")
    private String rutEmisor;
    
    /**
     * 实际发送者RUT号码
     */
    @JsonProperty("rut_envia")
    private String rutEnvia;
    
    /**
     * 跟踪ID
     */
    @JsonProperty("trackid")
    private String trackId;
    
    /**
     * 接收时间
     */
    @JsonProperty("fecha_recepcion")
    private String fechaRecepcion;
    
    /**
     * 处理状态
     */
    @JsonProperty("estado")
    private String estado;
    
    /**
     * 统计信息列表
     */
    @JsonProperty("estadistica")
    private List<SiiEstadistica> estadistica;
    
    /**
     * 修复和拒绝详细信息列表
     */
    @JsonProperty("detalle_rep_rech")
    private List<SiiDetalleRepRech> detalleRepRech;

    // 默认构造函数
    public SiiEnvioStatusResponse() {
    }

    // Getters and Setters
    public String getRutEmisor() {
        return rutEmisor;
    }

    public void setRutEmisor(String rutEmisor) {
        this.rutEmisor = rutEmisor;
    }

    public String getRutEnvia() {
        return rutEnvia;
    }

    public void setRutEnvia(String rutEnvia) {
        this.rutEnvia = rutEnvia;
    }

    public String getTrackId() {
        return trackId;
    }

    public void setTrackId(String trackId) {
        this.trackId = trackId;
    }

    public String getFechaRecepcion() {
        return fechaRecepcion;
    }

    public void setFechaRecepcion(String fechaRecepcion) {
        this.fechaRecepcion = fechaRecepcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public List<SiiEstadistica> getEstadistica() {
        return estadistica;
    }

    public void setEstadistica(List<SiiEstadistica> estadistica) {
        this.estadistica = estadistica;
    }

    public List<SiiDetalleRepRech> getDetalleRepRech() {
        return detalleRepRech;
    }

    public void setDetalleRepRech(List<SiiDetalleRepRech> detalleRepRech) {
        this.detalleRepRech = detalleRepRech;
    }

    @Override
    public String toString() {
        return "SiiEnvioStatusResponse{" +
                "rutEmisor='" + rutEmisor + '\'' +
                ", rutEnvia='" + rutEnvia + '\'' +
                ", trackId='" + trackId + '\'' +
                ", fechaRecepcion='" + fechaRecepcion + '\'' +
                ", estado='" + estado + '\'' +
                ", estadistica=" + estadistica +
                ", detalleRepRech=" + detalleRepRech +
                '}';
    }

    /**
     * 统计信息内部类
     */
    public static class SiiEstadistica {
        @JsonProperty("tipo")
        private String tipo;
        
        @JsonProperty("informados")
        private Integer informados;
        
        @JsonProperty("aceptados")
        private Integer aceptados;
        
        @JsonProperty("rechazados")
        private Integer rechazados;
        
        @JsonProperty("reparos")
        private Integer reparos;

        // Getters and Setters
        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public Integer getInformados() {
            return informados;
        }

        public void setInformados(Integer informados) {
            this.informados = informados;
        }

        public Integer getAceptados() {
            return aceptados;
        }

        public void setAceptados(Integer aceptados) {
            this.aceptados = aceptados;
        }

        public Integer getRechazados() {
            return rechazados;
        }

        public void setRechazados(Integer rechazados) {
            this.rechazados = rechazados;
        }

        public Integer getReparos() {
            return reparos;
        }

        public void setReparos(Integer reparos) {
            this.reparos = reparos;
        }
    }

    /**
     * 详细错误信息内部类
     */
    public static class SiiDetalleRepRech {
        @JsonProperty("tipo")
        private String tipo;
        
        @JsonProperty("folio")
        private String folio;
        
        @JsonProperty("estado")
        private String estado;
        
        @JsonProperty("descripcion")
        private String descripcion;
        
        @JsonProperty("error")
        private List<SiiError> error;

        // Getters and Setters
        public String getTipo() {
            return tipo;
        }

        public void setTipo(String tipo) {
            this.tipo = tipo;
        }

        public String getFolio() {
            return folio;
        }

        public void setFolio(String folio) {
            this.folio = folio;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public List<SiiError> getError() {
            return error;
        }

        public void setError(List<SiiError> error) {
            this.error = error;
        }
    }

    /**
     * 错误详情内部类
     */
    public static class SiiError {
        @JsonProperty("seccion")
        private String seccion;
        
        @JsonProperty("linea")
        private Integer linea;
        
        @JsonProperty("nivel")
        private Integer nivel;
        
        @JsonProperty("codigo")
        private Integer codigo;
        
        @JsonProperty("descripcion")
        private String descripcion;
        
        @JsonProperty("detalle")
        private String detalle;

        // Getters and Setters
        public String getSeccion() {
            return seccion;
        }

        public void setSeccion(String seccion) {
            this.seccion = seccion;
        }

        public Integer getLinea() {
            return linea;
        }

        public void setLinea(Integer linea) {
            this.linea = linea;
        }

        public Integer getNivel() {
            return nivel;
        }

        public void setNivel(Integer nivel) {
            this.nivel = nivel;
        }

        public Integer getCodigo() {
            return codigo;
        }

        public void setCodigo(Integer codigo) {
            this.codigo = codigo;
        }

        public String getDescripcion() {
            return descripcion;
        }

        public void setDescripcion(String descripcion) {
            this.descripcion = descripcion;
        }

        public String getDetalle() {
            return detalle;
        }

        public void setDetalle(String detalle) {
            this.detalle = detalle;
        }
    }
} 