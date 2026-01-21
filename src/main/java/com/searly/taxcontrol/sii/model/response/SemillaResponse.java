package com.searly.taxcontrol.sii.model.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * 种子响应模型类
 * 用于表示从SII系统获取种子值的响应
 * 包含响应头和响应体两部分
 */
@XmlRootElement(name = "RESPUESTA", namespace = "http://www.sii.cl/XMLSchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class SemillaResponse {

    /**
     * 响应头
     * 包含响应的状态信息
     * 用于指示请求是否成功处理
     */
    @XmlElement(name = "RESP_HDR", namespace = "http://www.sii.cl/XMLSchema")
    private ResponseHeader responseHeader;
    
    /**
     * 响应体
     * 包含实际的响应数据
     * 在这里主要是种子值
     */
    @XmlElement(name = "RESP_BODY", namespace = "http://www.sii.cl/XMLSchema")
    private ResponseBody responseBody;

    public ResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(ResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public ResponseBody getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(ResponseBody responseBody) {
        this.responseBody = responseBody;
    }

    /**
     * 响应头内部类
     * 用于封装响应头信息
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResponseHeader {
        /**
         * 响应状态
         * 0: 成功
         * 1: 错误
         * 用于指示请求处理的结果
         */
        @XmlElement(name = "ESTADO", namespace = "")
        private String estado;

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }

    /**
     * 响应体内部类
     * 用于封装响应体数据
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResponseBody {
        /**
         * 种子值
         * 用于生成数字签名的随机数
         * 每次请求都会生成新的种子值
         */
        @XmlElement(name = "SEMILLA", namespace = "")
        private String semilla;

        public String getSemilla() {
            return semilla;
        }

        public void setSemilla(String semilla) {
            this.semilla = semilla;
        }
    }

    @XmlRegistry
    public static class ObjectFactory {
        @XmlElementDecl(namespace = "http://www.sii.cl/XMLSchema", name = "RESPUESTA")
        public SemillaResponse createSemillaResponse() {
            return new SemillaResponse();
        }
    }
} 