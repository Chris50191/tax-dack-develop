package com.searly.taxcontrol.sii.api;

import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioDataRespuesta;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.model.response.SiiEnvioStatusResponse;
import com.searly.taxcontrol.verifactu.model.InvoiceResponse;

public interface SiiApi {
  /**
   * 注册发票
   * 向税务机关提交新发票
   *
   * @param request 发票数据
   * @return 操作响应
   */
  ResultadoEnvioPost registerInvoice(InvoiceSendRequest request);

  /**
   * 查询发票
   * 通过发票号查询已注册的发票
   *
   * @param rut 公司RUT
   * @param dv 验证码
   * @param trackId 跟踪ID
   * @return 操作响应
   */
  SiiEnvioStatusResponse queryInvoice(String rut, String dv, Long trackId);
} 