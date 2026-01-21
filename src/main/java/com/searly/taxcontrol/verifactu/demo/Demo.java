package com.searly.taxcontrol.verifactu.demo;

import com.searly.taxcontrol.verifactu.model.CancelInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.ConsultaInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.CorrectionInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.InvoiceRegisterRequest;
import com.searly.taxcontrol.verifactu.model.InvoiceResponse;
import com.searly.taxcontrol.verifactu.model.PreviousInvoiceInfo;
import com.searly.taxcontrol.verifactu.config.VeriFactuConfig;
import com.searly.taxcontrol.verifactu.model.VeriFactuException;
import com.searly.taxcontrol.verifactu.service.VeriFactuService;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 主类
 * 包含示例代码，演示VeriFactu API的使用
 */
public class Demo {

  public static void main(String[] args) {
    try {
      // 加载配置
      VeriFactuConfig config = new VeriFactuConfig();
      config.loadConfig();

      // 创建服务实例
      VeriFactuService service = new VeriFactuService(config);
      System.out.println("服务初始化完成，开始演示...");

      // 根据选择执行不同的演示
      int demoType = 2; // 切换演示类型: 1=注册发票, 2=查询发票, 3=修正发票, 4=注销发票

      switch (demoType) {
        case 1:
          // 注册发票
          registerInvoiceDemo(service, config);
          break;
        case 2:
          // 查询发票
          queryInvoiceDemo(service, config);
          break;
        case 3:
          // 修正发票
          correctionInvoiceDemo(service, config);
          break;
        case 4:
          // 注销发票
          cancelInvoiceDemo(service, config);
        case 5:
          // 替换发票
          replaceInvoiceDemo(service, config);
        case 6:
          // 退货更正发票 R1
          RectifiInvoiceDemo(service, config);
        case 7:
          // 退货更正发票 R5
          RectifiInvoiceDemo(service, config);
          break;
        default:
          System.out.println("无效的演示类型");
      }

    } catch (Exception e) {
      System.err.println("发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void RectifiInvoiceDemo(VeriFactuService service, VeriFactuConfig config) {
    System.out.println("\n===== 发票注册演示 =====");
    try {
      // 创建发票注册请求
      InvoiceRegisterRequest request = InvoiceRegisterRequest.createDefault();
      // 设置基本信息
      request.setBasicInfo(
              "B56803240",  // B56803240
              "SOFTCRAFTER S.L.", // SOFTCRAFTER S.L.
              "FACT2024-052",
              "18-07-2025",
              "R5",
              "修正发票"
                          );
      // 设置买方信息，如果发票类型为F3(标准发票)则需要设置买方信息
//      request.setBuyerInfo("B56803273", "MIDIAMADRID");
      request.setRectificada("B56803240", "FACT2024-051", "18-07-2025", "R5","I");
      // 添加税收明细
      BigDecimal baseAmount = new BigDecimal("-100");
      BigDecimal taxAmount = new BigDecimal("-21");
//      BigDecimal insTaxAmount = new BigDecimal("52");
      BigDecimal totalAmount = new BigDecimal("-121");
      request.addTaxDetail(
              "01", // 增值税
              "01", // 一般税制
              "S1", // 标准
              "21", // 21%税率
              baseAmount, // 基础金额
              taxAmount   // 税额
                          );
//      request.addTaxDetail(
//              "01", // 增值税
//              "18", // 一般税制
//              "S1", // 标准
//              "21", // 21%税率
//              "5.2",
//              baseAmount, // 基础金额
//              taxAmount,  // 税额
//              insTaxAmount);
//      request.addTaxDetail(
//              "01", // 增值税
//              "18", // 一般税制
//              "S1", // 标准
//              "21", // 21%税率
//              "1.75",
//              baseAmount, // 基础金额
//              taxAmount,  // 税额
//              new BigDecimal(17.5));
      // 设置总金额
      request.setTotalAmounts(
              taxAmount,  // 总税额
              totalAmount // 总金额
                             );
      // 设置系统信息
      request.setSystemInfo(
              "B12959755",       // B12959755
              "VERIFACTU JAVA CLIENT",      // VERIFACTU JAVA CLIENT
              "VeriFactu-Java",
              "01",        // 01
              "1.0.0",   // 1.0.0
              "01" // 01
                           );
      // 设置上一张
      PreviousInvoiceInfo previousInvoiceInfo = new PreviousInvoiceInfo("B56803240", "FACT2024-018", "29-06-2025", "7ED45ACAE4E315008229DA8251F74D9FF0C6EF155259D1E909592945C2E2AC61");

      ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
      String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
      // 计算并设置哈希值
      request.calculateAndSetHash(previousInvoiceInfo, nowDate); // 首次注册，无前序哈希
      // 直接使用新的registerInvoice方法
      InvoiceResponse response = service.registerInvoice(request);

      // 输出结果
      if (response.isSuccess()) {
        System.out.println("发票注册成功!");
        System.out.println("CSV: " + response.getCsv());
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("时间戳: " + response.getTimestamp());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      } else {
        System.out.println("发票注册失败!");
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      }

    } catch (Exception e) {
      System.err.println("注册发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  private static void replaceInvoiceDemo(VeriFactuService service, VeriFactuConfig config) {
    System.out.println("\n===== 发票注册演示 =====");
    try {
      // 创建发票注册请求
      InvoiceRegisterRequest request = InvoiceRegisterRequest.createDefault();
      // 设置基本信息
      request.setBasicInfo(
              "B56803240",  // B56803240
              "SOFTCRAFTER S.L.", // SOFTCRAFTER S.L.
              "FACT2024-043",
              "18-07-2025",
              "F3",
              "注册发票"
                          );
      // 设置买方信息，如果发票类型为F3(标准发票)则需要设置买方信息
      request.setBuyerInfoByReplace("B56803273", "MIDIAMADRID","B56803240", "FACT2024-042","18-07-2025");
      // 添加税收明细
      BigDecimal baseAmount = new BigDecimal("100");
      BigDecimal taxAmount = new BigDecimal("21");
//      BigDecimal insTaxAmount = new BigDecimal("52");
      BigDecimal totalAmount = new BigDecimal("121");
      request.addTaxDetail(
              "01", // 增值税
              "01", // 一般税制
              "S1", // 标准
              "21", // 21%税率
              baseAmount, // 基础金额
              taxAmount   // 税额
                          );
//      request.addTaxDetail(
//              "01", // 增值税
//              "18", // 一般税制
//              "S1", // 标准
//              "21", // 21%税率
//              "5.2",
//              baseAmount, // 基础金额
//              taxAmount,  // 税额
//              insTaxAmount);
//      request.addTaxDetail(
//              "01", // 增值税
//              "18", // 一般税制
//              "S1", // 标准
//              "21", // 21%税率
//              "1.75",
//              baseAmount, // 基础金额
//              taxAmount,  // 税额
//              new BigDecimal(17.5));
      // 设置总金额
      request.setTotalAmounts(
              taxAmount,  // 总税额
              totalAmount // 总金额
                             );
      // 设置系统信息
      request.setSystemInfo(
              "B12959755",       // B12959755
              "VERIFACTU JAVA CLIENT",      // VERIFACTU JAVA CLIENT
              "VeriFactu-Java",
              "01",        // 01
              "1.0.0",   // 1.0.0
              "01" // 01
                           );
      // 设置上一张
      PreviousInvoiceInfo previousInvoiceInfo = new PreviousInvoiceInfo("B56803240", "FACT2024-018", "29-06-2025", "7ED45ACAE4E315008229DA8251F74D9FF0C6EF155259D1E909592945C2E2AC61");

      ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
      String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
      // 计算并设置哈希值
      request.calculateAndSetHash(previousInvoiceInfo, nowDate); // 首次注册，无前序哈希
      // 直接使用新的registerInvoice方法
      InvoiceResponse response = service.registerInvoice(request);

      // 输出结果
      if (response.isSuccess()) {
        System.out.println("发票注册成功!");
        System.out.println("CSV: " + response.getCsv());
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("时间戳: " + response.getTimestamp());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      } else {
        System.out.println("发票注册失败!");
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      }

    } catch (Exception e) {
      System.err.println("注册发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 发票注册演示
   */
  private static void registerInvoiceDemo(VeriFactuService service, VeriFactuConfig config) {
    System.out.println("\n===== 发票注册演示 =====");
    try {
      // 创建发票注册请求
      InvoiceRegisterRequest request = InvoiceRegisterRequest.createDefault();
      // 设置基本信息
      request.setBasicInfo(
              "B56803240",  // B56803240
              "SOFTCRAFTER S.L.", // SOFTCRAFTER S.L.
              "FACT2024-059",
              "18-07-2025",
              "F2",
              "注册发票"
                          );
      // 设置买方信息，如果发票类型为F1(标准发票)则需要设置买方信息
//      request.setBuyerInfo("B56803273", "MIDIAMADRID");
      // 添加税收明细
      BigDecimal baseAmount = new BigDecimal("100");
      BigDecimal taxAmount = new BigDecimal("21");
//      BigDecimal insTaxAmount = new BigDecimal("52");
      BigDecimal totalAmount = new BigDecimal("121");
      request.addTaxDetail(
              "01", // 增值税
              "01", // 一般税制
              "S1", // 标准
              "21", // 21%税率
              baseAmount, // 基础金额
              taxAmount   // 税额
                          );
//      request.addTaxDetail(
//              "01", // 增值税
//              "18", // 一般税制
//              "S1", // 标准
//              "21", // 21%税率
//              "5.2",
//              baseAmount, // 基础金额
//              taxAmount,  // 税额
//              insTaxAmount);
//      request.addTaxDetail(
//              "01", // 增值税
//              "18", // 一般税制
//              "S1", // 标准
//              "21", // 21%税率
//              "1.75",
//              baseAmount, // 基础金额
//              taxAmount,  // 税额
//              new BigDecimal(17.5));
      // 设置总金额
      request.setTotalAmounts(
              taxAmount,  // 总税额
              totalAmount // 总金额
                             );
      // 设置系统信息
      request.setSystemInfo(
              "B12959755",       // B12959755
              "VERIFACTU JAVA CLIENT",      // VERIFACTU JAVA CLIENT
              "VeriFactu-Java",
              "01",        // 01
              "1.0.0",   // 1.0.0
              "01" // 01
                           );
      // 设置上一张
      PreviousInvoiceInfo previousInvoiceInfo = new PreviousInvoiceInfo("B56803240", "FACT2024-044", "25-06-2025", "10365005C691EE41B996971ED0F7E64FDA6D62BAB9689E7244FD9881EF671726");

      ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
      String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
      // 计算并设置哈希值
      request.calculateAndSetHash(previousInvoiceInfo, nowDate); // 首次注册，无前序哈希
      // 直接使用新的registerInvoice方法
      InvoiceResponse response = service.registerInvoice(request);

      // 输出结果
      if (response.isSuccess()) {
        System.out.println("发票注册成功!");
        System.out.println("CSV: " + response.getCsv());
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("时间戳: " + response.getTimestamp());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      } else {
        System.out.println("发票注册失败!");
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      }

    } catch (Exception e) {
      System.err.println("注册发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 发票查询演示
   */
  private static void queryInvoiceDemo(VeriFactuService service, VeriFactuConfig config) {
    System.out.println("\n===== 发票查询演示 =====");
    try {
      // 创建发票注册请求,查全部
      ConsultaInvoiceRequest request = new ConsultaInvoiceRequest("B56803240", "SOFTCRAFTER S.L.", "2025", "08", "25-07-2025", "10000");
      // 查询某个发票
//      ConsultaInvoiceRequest request = new ConsultaInvoiceRequest("B56803240", "SOFTCRAFTER S.L.", "FACT2024-020","2025", "06");
      System.out.println("发送查询请求...");
      InvoiceResponse response = service.queryInvoice(request);

      // 处理响应
      if (response.isSuccess()) {
        System.out.println("查询成功!");
        System.out.println("CSV: " + response.getCsv());
        System.out.println("最新一单: " + response.getConsultaIDFactura().getIdEmisorFactura());
        System.out.println("最新一单: " + response.getConsultaIDFactura().getNumSerieFactura());
        System.out.println("最新一单: " + response.getConsultaIDFactura().getFechaExpedicionFactura());
        System.out.println("最新一单: " + response.getConsultaEstadoRegistro());
        System.out.println("最新一单halle: " + response.getConsultaHuella());
      } else {
        System.out.println("查询失败!");
        System.out.println("错误: " + response.getEstadoRegistro());
      }

    } catch (VeriFactuException e) {
      System.err.println("查询发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 修正发票演示
   */
  private static void correctionInvoiceDemo(VeriFactuService service, VeriFactuConfig config) {
    System.out.println("\n===== 修正发票演示 =====");
    try {
      CorrectionInvoiceRequest request = CorrectionInvoiceRequest.createDefault();
      // 设置基本信息
      request.setBasicInfo(
              "B56803240",  // B56803240
              "SOFTCRAFTER S.L.", // SOFTCRAFTER S.L.
              "FACT2024-036",
              "18-07-2025",
              "F2",
              "发票修正为F1"
                          );
      // 设置买方信息，如果发票类型为F1(标准发票)则需要设置买方信息
      request.setBuyerInfo("B56803273", "MIDIAMADRID");
      // 添加税收明细
      BigDecimal baseAmount = new BigDecimal("-0.1");
      BigDecimal taxAmount = new BigDecimal("-0.02");
      BigDecimal totalAmount = new BigDecimal("-0.12");
      request.addTaxDetail(
              "01", // 增值税
              "01", // 一般税制
              "S1", // 标准
              "21", // 21%税率
              baseAmount, // 基础金额
              taxAmount   // 税额
                          );
      // 设置总金额
      request.setTotalAmounts(
              taxAmount,  // 总税额
              totalAmount  // 总金额
                             );
      // 设置系统信息
      request.setSystemInfo(
              "B12959755",       // B12959755
              "VERIFACTU JAVA CLIENT",      // VERIFACTU JAVA CLIENT
              "VeriFactu-Java",
              "01",        // 01
              "1.0.0",   // 1.0.0
              "01" // 01
                           );
      // 设置上一张
      PreviousInvoiceInfo previousInvoiceInfo = new PreviousInvoiceInfo("B56803240","FACT2024-014","29-06-2025","7ED45ACAE4E315008229DA8251F74D9FF0C6EF155259D1E909592945C2E2AC62");

      // 计算并设置哈希值
      request.calculateAndSetHash(previousInvoiceInfo); // 首次注册，无前序哈希

      // 修正发票
      InvoiceResponse response = service.correctionInvoice(request);

      // 输出结果
      if (response.isSuccess()) {
        System.out.println("发票修正成功!");
        System.out.println("CSV: " + response.getCsv());
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("时间戳: " + response.getTimestamp());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      } else {
        System.out.println("发票注册失败!");
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      }

    } catch (VeriFactuException e) {
      System.err.println("修正发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 发票注销演示
   */
  private static void cancelInvoiceDemo(VeriFactuService service, VeriFactuConfig config) {
    System.out.println("\n===== 发票注销演示 =====");
    try {
      CancelInvoiceRequest request = new CancelInvoiceRequest("B56803240", "SOFTCRAFTER S.L.", "FACT2024-019", "29-06-2025");
      // 设置系统信息
      request.setSystemInfo(
              "B12959755",       // B12959755
              "VERIFACTU JAVA CLIENT",      // VERIFACTU JAVA CLIENT
              "VeriFactu-Java",
              "01",        // 01
              "1.0.0",   // 1.0.0
              "01" // 01
                           );
      // 设置上一张
      PreviousInvoiceInfo previousInvoiceInfo = new PreviousInvoiceInfo("B56803240","FACT2024-018","29-06-2025","7ED45ACAE4E315008229DA8251F74D9FF0C6EF155259D1E909592945C2E2AC61");
      // 计算并设置哈希值
      request.calculateAndSetHash(previousInvoiceInfo); // 首次注册，无前序哈希

      // 注销发票
      System.out.println("发送注销请求...");
      InvoiceResponse response = service.cancelInvoice(request);

      // 处理响应
      if (response.isSuccess()) {
        System.out.println("发票取消成功!");
        System.out.println("CSV: " + response.getCsv());
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("时间戳: " + response.getTimestamp());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      } else {
        System.out.println("发票注册失败!");
        System.out.println("状态: " + response.getEstadoRegistro());
        System.out.println("错误: " + response.getCodigoRespuesta());
        System.out.println("描述: " + response.getDescripcionRespuesta());
      }

    } catch (VeriFactuException e) {
      System.err.println("注销发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }
} 