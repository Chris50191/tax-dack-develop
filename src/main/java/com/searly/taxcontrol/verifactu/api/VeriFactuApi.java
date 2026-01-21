package com.searly.taxcontrol.verifactu.api;

import com.searly.taxcontrol.verifactu.model.CancelInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.ConsultaInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.CorrectionInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.InvoiceRegisterRequest;
import com.searly.taxcontrol.verifactu.model.InvoiceResponse;
import com.searly.taxcontrol.verifactu.model.VeriFactuException;

/**
 * VeriFactu API接口
 * 定义与税务系统交互的主要操作
 */
public interface VeriFactuApi {
    
    /**
     * 注册发票
     * 向税务机关提交新发票
     * 
     * @param request 发票数据
     * @return 操作响应
     * @throws VeriFactuException 如果操作失败
     */
    InvoiceResponse registerInvoice(InvoiceRegisterRequest request) throws VeriFactuException;
    
    /**
     * 查询发票
     * 通过发票号查询已注册的发票
     * 
     * @param request 发票查询参数
     * @return 操作响应
     * @throws VeriFactuException 如果操作失败
     */
    InvoiceResponse queryInvoice(ConsultaInvoiceRequest request) throws VeriFactuException;
    
    /**
     * 作废发票
     * 取消已注册的发票
     * 
     * @param request 发票数据
     * @return 操作响应
     * @throws VeriFactuException 如果操作失败
     */
    InvoiceResponse cancelInvoice(CancelInvoiceRequest request) throws VeriFactuException;

    /**
     * 修正发票
     * 修正已注册的发票
     *
     * @param request 发票数据
     * @return 操作响应
     * @throws VeriFactuException 如果操作失败
     */
    InvoiceResponse correctionInvoice(CorrectionInvoiceRequest request) throws VeriFactuException;

} 