package com.searly.taxcontrol.sii.model.request;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 获取令牌请求模型类
 * 用于表示向SII系统请求令牌的XML格式
 * 包含种子值和数字签名
 * 根据OpenAPI规范，getToken元素不应该有命名空间
 * 注意：SII要求的是简单的两行XML格式，不使用命名空间
 */
@XmlRootElement(name = "getToken")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetTokenRequest {

    /**
     * 请求项
     * 包含种子值
     */
    @XmlElement(name = "item")
    private Item item;

    /**
     * 数字签名
     * 用于验证请求的真实性
     */
    @XmlElement(name = "Signature", namespace = "http://www.w3.org/2000/09/xmldsig#")
    private Object signature;

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Object getSignature() {
        return signature;
    }

    public void setSignature(Object signature) {
        this.signature = signature;
    }

    /**
     * 请求项内部类
     * 包含种子值
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Item {
        /**
         * 种子值
         * 从getSemilla接口获取的种子值
         * 用于生成数字签名
         */
        @XmlElement(name = "Semilla")
        private String semilla;

        public String getSemilla() {
            return semilla;
        }

        public void setSemilla(String semilla) {
            this.semilla = semilla;
        }
    }

    /**
     * 便捷构造方法
     * @param semilla 种子值
     * @return GetTokenRequest实例
     */
    public static GetTokenRequest create(String semilla) {
        GetTokenRequest request = new GetTokenRequest();
        Item item = new Item();
        item.setSemilla(semilla);
        request.setItem(item);
        return request;
    }

    /**
     * 获取种子值
     * @return 种子值
     */
    public String getSemilla() {
        return item != null ? item.getSemilla() : null;
    }


    /**
     * 生成符合SII OpenAPI规范的简单XML字符串
     * 格式：<getToken><item><Semilla>种子值</Semilla></item></getToken>
     * @return 简单的XML字符串，不包含命名空间
     */
    public String toSimpleXml() {
        if (item == null || item.getSemilla() == null) {
            throw new IllegalStateException("种子值不能为空");
        }
        return String.format("<getToken><item><Semilla>%s</Semilla></item></getToken>", 
                           item.getSemilla());
    }

    /**
     * 生成完整的两行XML格式（符合SII要求）
     * @return 包含XML声明的完整XML字符串
     */
    public String toCompleteXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + toSimpleXml();
    }

    /**
     * 生成用于签名的XML格式（不包含换行符）
     * 根据SII文档要求，签名前的XML应该是两行格式
     * @return 用于签名的XML字符串
     */
    public String toSignableXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + toSimpleXml();
    }
} 