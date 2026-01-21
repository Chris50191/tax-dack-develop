package com.searly.taxcontrol.sii.demo;

import com.searly.taxcontrol.sii.config.SiiConfig;
import com.searly.taxcontrol.sii.model.common.DTE;
import com.searly.taxcontrol.sii.model.common.EnvioBOLETA;
import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.model.response.SiiEnvioStatusResponse;
import com.searly.taxcontrol.sii.service.SiiApiService;
import com.searly.taxcontrol.sii.util.InvoiceGenerator;
import com.searly.taxcontrol.sii.util.PDF417Generator;
import com.searly.taxcontrol.verifactu.model.VeriFactuException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 * 主类
 * 包含示例代码，演示VeriFactu API的使用
 */
public class Demo {

  public static void main(String[] args) {
    try {
      // 加载配置
      SiiConfig config = new SiiConfig();
      config.loadConfig();

      // 创建服务实例
      SiiApiService service = new SiiApiService(config);
      System.out.println("服务初始化完成，开始演示...");

      // 独立查询模式：传入 rutEmisor 和 trackId
      if (args != null && args.length >= 2) {
        String rutEmisor = args[0];
        String trackId = args[1];
        queryByTrackId(service, rutEmisor, trackId);
        return;
      }

      ResultadoEnvioPost resultadoEnvioPost = registerInvoiceDemo(service);
      Thread.sleep(10000);
      // 查询发票
      queryInvoiceDemo(service, resultadoEnvioPost);

    } catch (Exception e) {
      System.err.println("发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 发票注册演示
   */
  private static ResultadoEnvioPost registerInvoiceDemo(SiiApiService service) {
    System.out.println("\n===== 发票注册演示 =====");
    ResultadoEnvioPost resultadoEnvioPost = null;
    try {
      // 设置CAF 文件路径（根据实际CAF文件名修改）
      // 重要：请确保CAF文件路径正确，CAF文件中的RE必须与要使用的RUT一致
      File cafFile = new File("src/main/resources/config/78065438-4-CAF.xml");
      if (!cafFile.exists()) {
        System.err.println("警告：未找到 78065438-4-CAF.xml，尝试查找其他CAF文件...");
        // 尝试其他可能的文件名
        File altCafFile = new File("src/main/resources/config/24529296-1-CAF.xml");
        if (altCafFile.exists()) {
          cafFile = altCafFile;
          System.err.println("找到备用CAF文件: " + altCafFile.getPath());
          System.err.println("警告：此CAF文件可能包含错误的RUT，请确认！");
        } else {
          throw new FileNotFoundException("未找到CAF文件！请确保CAF文件存在于: " + 
              "src/main/resources/config/78065438-4-CAF.xml 或 " +
              "src/main/resources/config/24529296-1-CAF.xml");
        }
      } else {
        System.out.println("找到CAF文件: " + cafFile.getAbsolutePath());
      }
      
      // 从CAF文件中读取RE（RUT Emisor）
      // SII要求：CAF文件中的RE必须与发票中的rutEmisor一致
      String cafRut = null;
      String envia = "24529296-1";
      try (InputStream cafStream = Files.newInputStream(cafFile.toPath())) {
        com.searly.taxcontrol.sii.util.CAFResolve.CafData cafData = 
            com.searly.taxcontrol.sii.util.CAFResolve.loadCaf(cafStream);
        cafRut = cafData.re; // 从CAF中获取RUT，例如 "78065438-4"
        System.out.println("========== CAF文件信息 ==========");
        System.out.println("从CAF文件读取的RUT (RE): " + cafRut);
        System.out.println("CAF文件中的公司名称 (RS): " + cafData.rs);
        System.out.println("CAF文件中的发票类型 (TD): " + cafData.td);
        System.out.println("CAF文件中的Folio范围: " + cafData.rngD + " - " + cafData.rngH);
        System.out.println("=================================");
      }
      
      if (cafRut == null || cafRut.trim().isEmpty()) {
        throw new IllegalArgumentException("无法从CAF文件中读取RUT (RE字段)");
      }
      
      // 验证CAF RUT格式
      if (!cafRut.contains("-")) {
        throw new IllegalArgumentException("CAF文件中的RUT格式不正确: " + cafRut);
      }
      
      // 验证CAF RUT不是旧的"24529296-1"
      // 如果CAF中的RUT是"24529296-1"，说明使用了错误的CAF文件
      if ("24529296-1".equals(cafRut)) {
        String errorMsg = String.format(
            "错误：CAF文件中的RUT是 '24529296-1'，但应该使用 '78065438-4'！\n" +
            "请检查CAF文件路径，确保使用正确的CAF文件。\n" +
            "当前CAF文件: %s\n" +
            "CAF文件中的RE: %s",
            cafFile.getAbsolutePath(), cafRut);
        System.err.println(errorMsg);
        throw new IllegalArgumentException(errorMsg);
      }
      
      // 验证CAF RUT必须是"78065438-4"
      if (!"78065438-4".equals(cafRut)) {
        String errorMsg = String.format(
            "错误：CAF文件中的RUT (%s) 不是预期的 '78065438-4'！\n" +
            "当前CAF文件: %s\n" +
            "请使用正确的CAF文件。",
            cafRut, cafFile.getAbsolutePath());
        System.err.println(errorMsg);
        throw new IllegalArgumentException(errorMsg);
      }
      
      
      // 创建发票数据，使用CAF中的RUT
      InvoiceData invoiceData = createSampleInvoiceData();
      System.out.println("创建发票数据后 - rutEmisor: " + invoiceData.getRutEmisor() + 
                        ", rutEnvia: " + invoiceData.getRutEnvia());
      
      // 重要：发票中的rutEmisor必须与CAF中的RE一致
      invoiceData.setRutEmisor(cafRut);  // 设置为CAF中的RUT
      invoiceData.setRutEnvia(envia);   // 设置为CAF中的RUT
      
      // 验证设置是否成功
      System.out.println("========== 设置发票RUT ==========");
      System.out.println("使用CAF中的RUT设置发票: " + cafRut);
      System.out.println("验证 - invoiceData.getRutEmisor(): " + invoiceData.getRutEmisor());
      System.out.println("验证 - invoiceData.getRutEnvia(): " + invoiceData.getRutEnvia());
      System.out.println("=================================");
      

      if (!cafRut.equals(invoiceData.getRutEmisor())) {
        throw new IllegalStateException(
            String.format("rutEmisor设置失败！期望: %s, 实际: %s", 
                cafRut, invoiceData.getRutEmisor()));
      }
      
      InvoiceSendRequest request = new InvoiceSendRequest();
      // 使用CAF中的RUT进行认证
      request.setRutSender("24529296");
      request.setDvSender("1");
      request.setRutCompany("78065438");
      request.setDvCompany("4");
      request.setInvoiceData(invoiceData);

      // 读取CAF文件内容到字节数组（支持多次使用）
      byte[] cafBytes = Files.readAllBytes(cafFile.toPath());
      
      // 创建支持reset的输入流
      java.io.ByteArrayInputStream cafStream = new java.io.ByteArrayInputStream(cafBytes);
      request.setCafFile(cafStream);
      
      resultadoEnvioPost = service.registerInvoice(request);

      // 创建发票对象
      EnvioBOLETA envioBOLETA = new InvoiceGenerator().createEnvioBOLETA(request.getInvoiceData());
      final DTE dte = envioBOLETA.getSetDTE().getDte().get(0);
      final InputStream generate = PDF417Generator.generate(dte.toXml());
      final File pdf417 = PDF417Generator.getFile(generate);

      System.out.println("发票注册!");
      System.out.println("发票发送者RUT号码: " + resultadoEnvioPost.getRutEmisor());
      System.out.println("发票发送方法人RUT号码: " + resultadoEnvioPost.getRutEnvia());
      System.out.println("跟踪ID: " + resultadoEnvioPost.getTrackId());
      System.out.println("接收时间: " + resultadoEnvioPost.getFechaRecepcion());
      System.out.println("处理状态: " + resultadoEnvioPost.getEstado());
      System.out.println("pdf417: " + pdf417.getPath());

    } catch (Exception e) {
      System.err.println("注册发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
    return resultadoEnvioPost;
  }

  public static final String CHILE = "America/Santiago";

  /**
   * 创建示例发票数据
   * 基于成功过示例.xml中的数据
   */
  private static InvoiceData createSampleInvoiceData() {
    InvoiceData data = new InvoiceData();

    ZonedDateTime now = ZonedDateTime.now(ZoneId.of(CHILE));
    String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    // 基本信息
    data.setTipoDTE(39);                    // 电子发票
    data.setFolio("50");              // 发票号码
    data.setFchEmis(nowDate);          // 发票日期
    data.setIndServicio(3);                  // 销售和服务发票
    data.setDocumentId("BoletaElectronica_1832");   // 文档ID

    // 发送方信息
    // 注意：rutEmisor 和 rutEnvia 应该在调用此方法后从CAF文件中读取并设置
    // 这里设置默认值，但会被CAF中的RUT覆盖
    data.setRutEmisor("78065438-4");        // 发送方RUT（将从CAF文件读取）
    data.setRutEnvia("78065438-4");        // 发送方法人RUT（将从CAF文件读取）
    data.setRznSocEmisor("XIAOQI SPA");     // 发送方公司名称
    data.setGiroEmisor("VENTA AL POR MENOR EN COMERCIO NO ESPECIALIZADO"); // 发送方业务类型
//    data.setDirOrigen("222"); // 发送方地址
//    data.setCmnaOrigen("SANTA MARIA");      // 发送方社区
//    data.setCiudadOrigen("VALPARAISO");     // 发送方城市

    // 接收方信息
    data.setRutReceptor("60803000-K");      // 接收方RUT
    data.setRznSocReceptor("seve"); // 接收方公司名称
//    data.setDirRecep("12312312"); // 接收方地址
//    data.setCmnaRecep("ESTACION CENTRAL");  // 接收方社区
//    data.setCiudadRecep("SANTIAGO");        // 接收方城市

    // 金额信息
    data.setMntNeto(BigDecimal.valueOf(10));                   // 净额
    data.setIva(BigDecimal.valueOf(2));                        // IVA税额
//    data.setMntAdic(BigDecimal.valueOf(5));                        // IVA税额
    data.setMntTotal(BigDecimal.valueOf(12));                  // 总金额

    // 商品信息
    InvoiceData.Product product = new InvoiceData.Product();
    product.setNmbItem("EJEMPLO DE PRODUCTO");                // 商品名称
    product.setQtyItem(BigDecimal.valueOf(1));                     // 商品数量
    product.setPrcItem(BigDecimal.valueOf(10));                   // 商品单价
    product.setMontoItem(BigDecimal.valueOf(10));                 // 商品金额
    data.setProducts(Arrays.asList(product));

    // 退货来源发票
//    InvoiceData.Reference reference = new InvoiceData.Reference();
//    reference.setType("39");                // 原发票类型
//    reference.setBillId("1");                     // 原发票号
//    reference.setInvoiceDate("2025-05-28");                   // 原发票开票日期
//    reference.setReasonType("3");                 // 修改原因类型
//    reference.setResson("Devolución de mercadería");                 // 修改原因
//    data.setIsReferences(Arrays.asList(reference));

    // 授权信息
    data.setFchResol("2025-08-13");        // 授权日期
    data.setNroResol(100);                   // 授权号码

    // 时间戳
    String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
    data.setTmstFirmaEnv(currentTime);      // 发送签名时间戳
    data.setTmstFirma(currentTime);         // 文档签名时间戳

    return data;
  }

  /**
   * 发票查询演示
   */
  private static void queryInvoiceDemo(SiiApiService service, ResultadoEnvioPost resultadoEnvioPost) {
    System.out.println("\n===== 发票查询演示 =====");
    try {
      // 空值检查
      if (resultadoEnvioPost == null) {
        System.err.println("错误：发票注册结果为空，无法查询");
        return;
      }
      
      String rutEmisor = resultadoEnvioPost.getRutEmisor();
      Long trackId = resultadoEnvioPost.getTrackId();
      
      if (rutEmisor == null || trackId == null) {
        System.err.println("错误：缺少必要的查询参数");
        return;
      }
      
      // 验证 RUT 格式
      if (!rutEmisor.contains("-")) {
        System.err.println("错误：RUT 格式不正确，应为 XXXXX-XX 格式");
        return;
      }
      
      final String[] split = rutEmisor.split("-");
      if (split.length != 2) {
        System.err.println("错误：RUT 格式不正确");
        return;
      }
      
      SiiEnvioStatusResponse siiEnvioStatusResponse = service.queryInvoice(split[0], split[1], trackId);
      System.out.println("发票查詢!");
      System.out.println("发票发送者RUT号码: " + siiEnvioStatusResponse.getRutEmisor());
      System.out.println("实际发送者RUT号码: " + siiEnvioStatusResponse.getRutEnvia());
      System.out.println("跟踪ID: " + siiEnvioStatusResponse.getTrackId());
      System.out.println("接收时间: " + siiEnvioStatusResponse.getFechaRecepcion());
      System.out.println("处理状态: " + siiEnvioStatusResponse.getEstado());

    } catch (VeriFactuException e) {
      System.err.println("查询发票时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * 独立查询：根据 RUT + trackId 查询状态
   */
  private static void queryByTrackId(SiiApiService service, String rutEmisor, String trackId) {
    System.out.println("\n===== 独立查询演示 =====");
    try {
      if (rutEmisor == null || rutEmisor.trim().isEmpty()) {
        System.err.println("错误：rutEmisor 不能为空");
        return;
      }
      if (!rutEmisor.contains("-")) {
        System.err.println("错误：rutEmisor 格式不正确，应为 XXXXX-XX");
        return;
      }
      String[] parts = rutEmisor.split("-");
      if (parts.length != 2) {
        System.err.println("错误：rutEmisor 格式不正确");
        return;
      }

      if (trackId == null || trackId.trim().isEmpty()) {
        System.err.println("错误：trackId 不能为空");
        return;
      }

      Long trackIdValue;
      try {
        trackIdValue = Long.parseLong(trackId.trim());
      } catch (NumberFormatException e) {
        System.err.println("错误：trackId 必须是数字");
        return;
      }

      SiiEnvioStatusResponse response = service.queryInvoice(parts[0], parts[1], trackIdValue);
      System.out.println("发票查询成功!");
      System.out.println("发票发送者RUT号码: " + response.getRutEmisor());
      System.out.println("实际发送者RUT号码: " + response.getRutEnvia());
      System.out.println("跟踪ID: " + response.getTrackId());
      System.out.println("接收时间: " + response.getFechaRecepcion());
      System.out.println("处理状态: " + response.getEstado());
    } catch (VeriFactuException e) {
      System.err.println("独立查询时发生错误: " + e.getMessage());
      e.printStackTrace();
    }
  }

//  public static void main(String[] args) {
//    //      // 加载配置
//      SiiConfig config = new SiiConfig();
//      config.loadConfig();
//
//      // 创建服务实例
//      SiiApiService service = new SiiApiService(config);
//    ResultadoEnvioPost resultadoEnvioPost = new ResultadoEnvioPost();
//    resultadoEnvioPost.setRutEmisor("78064518-0");
//    resultadoEnvioPost.setTrackId(25272618L);
//    queryInvoiceDemo(service, resultadoEnvioPost);
//  }
} 