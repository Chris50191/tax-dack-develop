package com.searly.taxcontrol.verifactu.utils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class HashGenerator {

  // 生成SHA-256指纹（十六进制大写）
  public static String getHashVerifactu(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
      return bytesToHex(hashBytes).toUpperCase();
    } catch (Exception e) {
      throw new IllegalArgumentException("Error al generar la huella SHA", e);
    }
  }

  // 构建发票注册记录字符串 -- 注册
  public static String buildRegistrationString(
          String issuerTaxId,
          String invoiceNumber,
          String issueDate,
          String invoiceType,
          String totalTax,
          String totalAmount,
          String previousHash,
          String timestamp
                                              ) {
    StringBuilder sb = new StringBuilder();
    sb.append(buildField("IDEmisorFactura", issuerTaxId, true));
    sb.append(buildField("NumSerieFactura", invoiceNumber, true));
    sb.append(buildField("FechaExpedicionFactura", issueDate, true));
    sb.append(buildField("TipoFactura", invoiceType, true));
    sb.append(buildField("CuotaTotal", totalTax, true));
    sb.append(buildField("ImporteTotal", totalAmount, true));
    sb.append(buildField("Huella", previousHash, true));
    sb.append(buildField("FechaHoraHusoGenRegistro", timestamp, false));
    return sb.toString();
  }

  // 构建发票注册记录字符串 --取消
  public static String buildRegistrationStringCancel(
          String issuerTaxId,
          String invoiceNumber,
          String issueDate,
          String previousHash,
          String timestamp
                                              ) {
    StringBuilder sb = new StringBuilder();
    sb.append(buildField("IDEmisorFacturaAnulada", issuerTaxId, true));
    sb.append(buildField("NumSerieFacturaAnulada", invoiceNumber, true));
    sb.append(buildField("FechaExpedicionFacturaAnulada", issueDate, true));
    sb.append(buildField("Huella", previousHash, true));
    sb.append(buildField("FechaHoraHusoGenRegistro", timestamp, false));
    return sb.toString();
  }

  // 构建字段键值对
  private static String buildField(String fieldName, String value, boolean addSeparator) {
    String normalizedValue = (value == null) ? "" : value.trim();
    return fieldName + "=" + normalizedValue + (addSeparator ? "&" : "");
  }

  // 数值标准化（去除多余小数位）
  private static String normalizeNumber(String number) {
    if (number == null || number.isEmpty()) return "";
    try {
      return new BigDecimal(number).stripTrailingZeros().toPlainString();
    } catch (NumberFormatException e) {
      return number.trim(); // 非数字保持原样
    }
  }

  // 字节数组转十六进制
  private static String bytesToHex(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : bytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  // 计算发票注册指纹（主入口）-注册发票
  public static String calculateRegistrationHash(
          String issuerTaxId,
          String invoiceNumber,
          String issueDate,
          String invoiceType,
          String totalTax,
          String totalAmount,
          String previousHash,
          String timestamp
                                                ) {
    String dataString = buildRegistrationString(
            issuerTaxId, invoiceNumber, issueDate,
            invoiceType, totalTax, totalAmount,
            previousHash, timestamp
                                               );
    System.out.println(dataString);
    return getHashVerifactu(dataString);
  }

  // 计算发票注册指纹（主入口）- 取消发票
  public static String calculateRegistrationCancelHash(
          String issuerTaxId,
          String invoiceNumber,
          String issueDate,
          String previousHash,
          String timestamp
                                                ) {
    String dataString = buildRegistrationStringCancel(
            issuerTaxId, invoiceNumber, issueDate,
            previousHash, timestamp
                                               );
    System.out.println(dataString);
    return getHashVerifactu(dataString);
  }

  public static void main(String[] args) {
    // 首次发票注册（无前序指纹）
//    String hash = HashGenerator.calculateRegistrationHash(
//            "89890001K",         // 发行方税号
//            "12345678/G33",      // 发票序列号
//            "01-01-2024",        // 发行日期
//            "F1",                // 发票类型
//            "12.35",             // 总税额
//            "123.45",            // 总金额
//            null,                // 前序指纹（首张发票为空）
//            "2024-01-01T19:20:30+01:00" // 时间戳
//                                                         );
    ZoneId spainZone = ZoneId.of("Europe/Madrid");
    ZonedDateTime now = ZonedDateTime.now(spainZone);
    String timestamp = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    System.out.println(timestamp);
    String hash = HashGenerator.calculateRegistrationHash(
            "B56803240",         // 发行方税号
            "FACT2024-009",      // 发票序列号
            "25-06-2025",        // 发行日期
            "F1",                // 发票类型
            "21.00",             // 总税额
            "121.00",            // 总金额
            "A20CC5655C4E477539908F84C5472A2669086E81C41BA8FCAA3F29859D12E1DD",                // 前序指纹（首张发票为空）
            timestamp // 时间戳
                                                         );

//    String hash = HashGenerator.calculateRegistrationCancelHash(
//            "B56803240",         // 发行方税号
//            "FACT2024-010",      // 发票序列号
//            "25-06-2025",        // 发行日期
//            "F2CC09124833B90BEE17384C9E52710D4C16B77F52535CEF42EB3CDDE0201A55", // 前序指纹（首张发票为空）
//            timestamp // 时间戳
//                                                         );
    System.out.println(hash);

// 输出：3C464DAF61ACB827C65FDA19F352A4E3BDC2C640E9E9FC4CC058073F38F12F60
  }
}