package com.searly.taxcontrol.sii.model.response;


import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 发送状态查询响应模型类
 * 用于表示查询发票发送状态的详细响应数据
 * 包含发送者信息、跟踪ID、处理状态、统计信息和详细错误信息
 */
@XmlRootElement(name = "ResultadoEnvioDataRespuesta")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResultadoEnvioDataRespuesta {
    
    /**
     * 发票发送者RUT号码
     * 格式：RUT-DV，例如 45000054-K
     */
    @XmlElement(name = "rut_emisor")
    private String rutEmisor;
    
    /**
     * 实际发送者RUT号码
     * 格式：RUT-DV，例如 8315495-0
     */
    @XmlElement(name = "rut_envia")
    private String rutEnvia;
    
    /**
     * 跟踪ID
     * 用于跟踪发票处理状态的唯一标识符
     * 最大15位数字
     */
    @XmlElement(name = "trackid")
    private Long trackId;
    
    /**
     * 接收时间
     * 格式：DD/MM/YYYY HH:MM:SS
     */
    @XmlElement(name = "fecha_recepcion")
    private String fechaRecepcion;
    
    /**
     * 处理状态
     * 可能的状态包括：
     * - REC: 发送已接收
     * - EPR: 发送已处理
     * - CRT: 封面OK
     * - FOK: 发送签名已验证
     * - PRD: 发送正在处理
     * - RCH: 因信息错误被拒绝
     * - RCO: 因一致性被拒绝
     * - VOF: 未找到.xml文件
     * - RFR: 因签名错误被拒绝
     * - RPR: 接受但有修复
     * - RPT: 重复发送被拒绝
     * - RSC: 因Schema被拒绝
     * - SOK: Schema已验证
     * - RCT: 因封面错误被拒绝
     */
    @XmlElement(name = "estado")
    private String estado;
    
    /**
     * 统计信息列表
     * 包含不同类型发票的统计数据
     */
    @XmlElement(name = "estadistica")
    private List<ResultadoEnvioDataRespuestaEstadistica> estadistica;
    
    /**
     * 修复和拒绝详细信息列表
     * 包含被拒绝或需要修复的发票详细信息
     */
    @XmlElement(name = "detalle_rep_rech")
    private List<ResultadoEnvioDataRespuestaDetalleRepRech> detalleRepRech;

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

    public Long getTrackId() {
        return trackId;
    }

    public void setTrackId(Long trackId) {
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

    public List<ResultadoEnvioDataRespuestaEstadistica> getEstadistica() {
        return estadistica;
    }

    public void setEstadistica(List<ResultadoEnvioDataRespuestaEstadistica> estadistica) {
        this.estadistica = estadistica;
    }

    public List<ResultadoEnvioDataRespuestaDetalleRepRech> getDetalleRepRech() {
        return detalleRepRech;
    }

    public void setDetalleRepRech(List<ResultadoEnvioDataRespuestaDetalleRepRech> detalleRepRech) {
        this.detalleRepRech = detalleRepRech;
    }
} 