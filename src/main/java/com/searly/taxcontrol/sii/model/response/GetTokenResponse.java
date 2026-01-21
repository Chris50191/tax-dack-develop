package com.searly.taxcontrol.sii.model.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 获取令牌响应模型类
 * 用于表示从SII系统获取令牌的响应
 * 包含响应头和响应体两部分
 */
@XmlRootElement(name = "RESPUESTA", namespace = "http://www.sii.cl/XMLSchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetTokenResponse {

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
     * 在这里主要是令牌值
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
         * 00: 成功 - Token Creado
         * 01-12, 21, -3, -07: 各种错误状态
         * 用于指示请求处理的结果
         */
        @XmlElement(name = "ESTADO", namespace = "")
        private String estado;
        
        /**
         * 状态描述
         * 对响应状态的详细说明
         * 当发生错误时，包含错误的具体原因
         */
        @XmlElement(name = "GLOSA", namespace = "")
        private String glosa;

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }

        public String getGlosa() {
            return glosa;
        }

        public void setGlosa(String glosa) {
            this.glosa = glosa;
        }
    }

    /**
     * 响应体内部类
     * 用于封装响应体数据
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ResponseBody {
        /**
         * 访问令牌
         * 用于后续请求的身份验证
         * 有效期为1小时，每次使用会自动续期
         */
        @XmlElement(name = "TOKEN", namespace = "")
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }

    /**
     * 检查响应是否成功
     * @return 如果响应状态为00则返回true，否则返回false
     */
    public boolean isSuccess() {
        return responseHeader != null && "00".equals(responseHeader.getEstado());
    }
    
    /**
     * 获取错误信息
     * @return 如果发生错误则返回错误描述，否则返回null
     */
    public String getErrorMessage() {
        return responseHeader != null ? responseHeader.getGlosa() : null;
    }
    
    /**
     * 获取访问令牌
     * @return 响应中的访问令牌，如果不存在则返回null
     */
    public String getToken() {
        return responseBody != null ? responseBody.getToken() : null;
    }

    /**
     * 获取状态码
     * @return 响应状态码
     */
    public String getEstado() {
        return responseHeader != null ? responseHeader.getEstado() : null;
    }
} 