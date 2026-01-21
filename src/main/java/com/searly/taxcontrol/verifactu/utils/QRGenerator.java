package com.searly.taxcontrol.verifactu.utils;

import java.net.URLEncoder;
import java.text.DecimalFormat;

public class QRGenerator {

  // 测试环境（可验证发票）
  static String testUrl = "https://prewww2.aeat.es/wlpl/TIKE-CONT/ValidarQR?";
  // 生产环境（不可验证发票）
  static String prodUrl = "https://www2.agenciatributaria.gob.es/wlpl/TIKE-CONT/ValidarQRNoVerifactu?";


  public static void main(String[] args) throws Exception {
    double amount = 121;
    DecimalFormat df = new DecimalFormat("0.00");
    String importe = df.format(amount);

    // 示例调用（生产环境-可验证发票）
    String url = buildQRUrl(
            testUrl,
            "B56803240",
            "FACT2024-009",
            "25-06-2025",
            importe
                           );
    System.out.println("生成URL: " + url);
  }

  /**
   * 构建AEAT合规的QR码URL
   * @param baseUrl 基础URL（根据发票类型选择）
   * @param nif 税号
   * @param numSerie 发票序列号
   * @param fecha 开票日期（DD-MM-AAAA）
   * @param importe 总金额（带小数点格式）
   */
  public static String buildQRUrl(String baseUrl, String nif, String numSerie, String fecha, String importe) {
    StringBuilder url = new StringBuilder(baseUrl);
    try {
      url.append("nif=").append(encodeParam(nif)).append("&")
              .append("numserie=").append(encodeParam(numSerie)).append("&")
              .append("fecha=").append(encodeParam(fecha)).append("&")  // 修正参数名fecha（原文档错误写为fecho）
              .append("importe=").append(encodeParam(importe));
    } catch (Exception e) {
      throw new RuntimeException("URL构建失败: " + e.getMessage());
    }
    return url.toString();
  }

  /** UTF-8 URL编码 */
  private static String encodeParam(String param) {
    try {
      return URLEncoder.encode(param, "UTF-8")
              .replace("+", "%20");  // 空格特殊处理
    } catch (Exception e) {
      throw new RuntimeException("参数编码失败: " + param);
    }
  }
}