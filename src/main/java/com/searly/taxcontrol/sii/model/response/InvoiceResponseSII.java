package com.searly.taxcontrol.sii.model.response;


import com.searly.taxcontrol.verifactu.model.InvoiceResponse;
import com.searly.taxcontrol.verifactu.model.VerifactuConsultaResponse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import java.util.ArrayList;
import java.util.List;

/**
 * 发票操作响应
 * 包含注册、查询或作废操作的响应结果
 */
//@Data
public class InvoiceResponseSII extends InvoiceResponse {

    private String request;

    private Long trackId;

    /**
     * 响应状态
     */
    public enum Status {
        REC, //发送已接收
        EPR, //发送已处理
        CRT, //封面OK
        FOK, //发送签名已验证
        PRD, //发送正在处理
        RCH, //因信息错误被拒绝
        RCO, //因一致性被拒绝
        VOF, //未找到.xml文件
        RFR, //因签名错误被拒绝
        RPR, //接受但有修复
        RPT, //重复发送被拒绝
        RSC, //因Schema被拒绝
        SOK, //Schema已验证
        RCT, //因封面错误被拒绝
    }

    /**
     * 是否成功
     * @return 如果操作成功返回true
     */
    public boolean isSuccess() {
        return Status.REC.name().equals(this.getStatus());
    }

    public String getRequest() {
        return this.request;
    }

    public void setRequest(String request) {
        this.request = request;
    }

    public Long getTrackId() {
        return this.trackId;
    }

    public void setTrackId(Long trackId) {
        this.trackId = trackId;
    }
}