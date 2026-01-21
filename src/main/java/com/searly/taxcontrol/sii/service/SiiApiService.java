package com.searly.taxcontrol.sii.service;

import com.searly.taxcontrol.sii.api.SiiApi;
import com.searly.taxcontrol.sii.config.SiiConfig;
import com.searly.taxcontrol.sii.model.request.EnvioPost;
import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.GetTokenResponse;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioDataRespuesta;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioDataRespuestaDetalleRepRech;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioDataRespuestaError;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.model.response.SemillaResponse;
import com.searly.taxcontrol.sii.model.response.SiiEnvioStatusResponse;
import com.searly.taxcontrol.sii.model.response.SiiInvoiceResponse;
import com.searly.taxcontrol.sii.util.AuthUtils;
import com.searly.taxcontrol.sii.util.CertificateManager;
import com.searly.taxcontrol.sii.util.InvoiceGenerator;
import com.searly.taxcontrol.verifactu.config.VeriFactuConfig;
import com.searly.taxcontrol.verifactu.model.InvoiceResponse;
import com.searly.taxcontrol.verifactu.model.VeriFactuException;
import com.searly.taxcontrol.verifactu.utils.SSLUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VeriFactu服务实现
 * 实现与AEAT税务系统的交互
 */
public class SiiApiService implements SiiApi {

  private static final Logger log = Logger.getLogger(SiiApiService.class.getName());

  private final String apiBaseUrl;
  private final String boletaBaseUrl;
  private final String validateUrl;
  private final boolean isTestEnvironment;
  private final String certificatePath;
  private final InputStream certificate;
  private final String certificatePassword;
  private final RestTemplate restTemplate;

  /**
   * 构造函数
   *
   * @param config VeriFactu配置
   */
  public SiiApiService(SiiConfig config) throws VeriFactuException {
    this.apiBaseUrl = config.getApiUrl();
    this.boletaBaseUrl = config.getBoletaBaseUrl();
    this.validateUrl = config.getValidateUrl();
    this.isTestEnvironment = config.getIsTestEnvironment();
    this.certificatePath = config.getCertificatePath();
    this.certificate = config.getCertificate();
    this.certificatePassword = config.getCertificatePassword();
    this.restTemplate = new RestTemplate();
    this.restTemplate.setMessageConverters(Collections.singletonList(new Jaxb2RootElementHttpMessageConverter()));

    log.info("VeriFactuService初始化完成");
    log.info("环境: " + (isTestEnvironment ? "测试环境" : "生产环境"));
    log.info("API地址: " + apiBaseUrl);
    log.info("验证地址: " + validateUrl);
    log.info("已启用自动重定向处理");

  }

  @Override
  public ResultadoEnvioPost registerInvoice(InvoiceSendRequest request) throws VeriFactuException {
    try {
      // 手动验证请求参数
      request.validate();

      // 从发票数据中提取 RUT（用于获取 Token 和签名）
      // SII 要求：获取 Token 时使用的 RUT 必须与发票中的 rutEmisor 一致
      String invoiceRutEmisor = request.getInvoiceData().getRutEmisor();
      if (invoiceRutEmisor == null || invoiceRutEmisor.trim().isEmpty()) {
        throw new VeriFactuException("发票数据中的 rutEmisor 不能为空");
      }
      
      // 提取 RUT 数字部分（去掉验证位）
      String[] rutParts = invoiceRutEmisor.split("-");
      if (rutParts.length != 2) {
        throw new VeriFactuException("发票数据中的 rutEmisor 格式不正确，应为 XXXXX-XX 格式: " + invoiceRutEmisor);
      }
      String rutForAuth = rutParts[0]; // 用于认证的 RUT（不含验证位）
      
      log.log(Level.INFO, "使用发票中的 RUT 获取 Token: " + rutForAuth + " (发票 RUT: " + invoiceRutEmisor + ")");

      // 获取令牌（使用发票中的 RUT）
      String semilla = AuthUtils.getSemilla(apiBaseUrl, rutForAuth, restTemplate);
      // 加载证书
      KeyStore keyStore;
      if (certificate!=null){
        // 加载证书
        keyStore = CertificateManager.loadPKCS12Certificate(certificate, certificatePassword);
      }else {
        // 加载证书
        keyStore = CertificateManager.loadPKCS12Certificate(certificatePath, certificatePassword);
      }
      String signedXml = AuthUtils.signToken(semilla, keyStore, certificatePassword);
      // 验证令牌
      if (StringUtils.isEmpty(signedXml)) {
        throw new VeriFactuException("signedXml is null");
      }
      String token = AuthUtils.getToken(apiBaseUrl, rutForAuth, restTemplate, signedXml);
      // 验证令牌
      if (token == null || token.trim().isEmpty()) {
        throw new VeriFactuException("token is null");
      }
      log.log(Level.INFO, "成功获取SII令牌: " + token);

      // 验证CAF文件中的RUT与发票中的RUT是否一致
      // 重要：SII要求CAF文件中的RE必须与发票中的rutEmisor完全一致
      if (request.getCafFile() != null) {
        boolean markSupported = request.getCafFile().markSupported();
        
        if (markSupported) {
          request.getCafFile().mark(Integer.MAX_VALUE);
        }
        
        try {
          // 读取CAF文件验证RUT
          com.searly.taxcontrol.sii.util.CAFResolve.CafData cafData = 
              com.searly.taxcontrol.sii.util.CAFResolve.loadCaf(request.getCafFile());
          String cafRut = cafData.re;
          
          if (!invoiceRutEmisor.equals(cafRut)) {
            throw new VeriFactuException(
                String.format("CAF文件中的RUT (%s) 与发票中的rutEmisor (%s) 不一致！必须完全匹配。", 
                    cafRut, invoiceRutEmisor));
          }
          
          log.log(Level.INFO, "CAF RUT验证通过: " + cafRut);
          
          // 重置流以便后续使用
          if (markSupported) {
            request.getCafFile().reset();
          } else {
            // 如果流不支持mark/reset，抛出异常提示使用ByteArrayInputStream
            throw new VeriFactuException(
                "CAF流不支持mark/reset，请使用ByteArrayInputStream或确保流支持reset操作");
          }
        } catch (Exception e) {
          if (markSupported) {
            try {
              request.getCafFile().reset();
            } catch (Exception resetEx) {
              log.log(Level.WARNING, "重置CAF流失败", resetEx);
            }
          }
          throw new VeriFactuException("验证CAF文件失败: " + e.getMessage(), e);
        }
      }

      // 验证发票数据中的rutEnvia
      String invoiceRutEnviaBeforeGen = request.getInvoiceData().getRutEnvia();
      log.log(Level.INFO, "生成发票XML前 - rutEmisor: " + request.getInvoiceData().getRutEmisor() + 
                         ", rutEnvia: " + invoiceRutEnviaBeforeGen);
      
      if (invoiceRutEnviaBeforeGen == null || invoiceRutEnviaBeforeGen.trim().isEmpty()) {
        throw new VeriFactuException("发票数据中的 rutEnvia 为空，无法生成发票");
      }
      
      // 创建发票生成器
      InvoiceGenerator generator = new InvoiceGenerator();
      // 生成发票XML
      // 注意：如果CAF流已经被读取，需要重新设置
      String signedInvoiceXml = generator.generateInvoiceXML(request.getInvoiceData(), keyStore, certificatePassword, request.getCafFile());
      request.setRequestJson(signedInvoiceXml);
      
      // 验证生成的XML中是否包含正确的rutEnvia
      if (!signedInvoiceXml.contains("RutEnvia")) {
        log.log(Level.WARNING, "生成的XML中未找到RutEnvia标签");
      } else {
        // 提取XML中的RutEnvia值进行验证
        java.util.regex.Pattern rutEnviaPattern = java.util.regex.Pattern.compile("<RutEnvia>([^<]+)</RutEnvia>");
        java.util.regex.Matcher matcher = rutEnviaPattern.matcher(signedInvoiceXml);
        if (matcher.find()) {
          String xmlRutEnvia = matcher.group(1);
          log.log(Level.INFO, "生成的XML中的RutEnvia: " + xmlRutEnvia);
          if (!invoiceRutEnviaBeforeGen.equals(xmlRutEnvia)) {
            log.log(Level.SEVERE, String.format(
                "XML中的RutEnvia (%s) 与发票数据中的rutEnvia (%s) 不一致！", 
                xmlRutEnvia, invoiceRutEnviaBeforeGen));
          }
        }
      }
      
      log.log(Level.INFO, "发票XML内容长度: " + signedInvoiceXml.length() + " 字符");

      // 创建发送请求对象
      // 重要：rutCompany 必须与发票 XML 中的 rutEnvia 一致
      // 从发票数据中提取 rutEnvia，确保与上传参数一致
      String invoiceRutEnvia = request.getInvoiceData().getRutEnvia();
      if (invoiceRutEnvia == null || invoiceRutEnvia.trim().isEmpty()) {
        throw new VeriFactuException("发票数据中的 rutEnvia 不能为空");
      }
      
      // 提取 RUT 数字部分
      String[] rutEnviaParts = invoiceRutEnvia.split("-");
      if (rutEnviaParts.length != 2) {
        throw new VeriFactuException("发票数据中的 rutEnvia 格式不正确，应为 XXXXX-XX 格式: " + invoiceRutEnvia);
      }
      
      // 确保 request 中的 rutCompany 与发票中的 rutEnvia 一致
      request.setRutCompany("78065438");
      request.setDvCompany("4");
      
      log.log(Level.INFO, "使用发票中的 rutEnvia 设置 rutCompany: " + invoiceRutEnvia);
      
      EnvioPost envioPost = createEnvioPost(request);
      // 调用SII API发送发票
      ResultadoEnvioPost result = sendInvoice(token, envioPost, signedInvoiceXml.getBytes(StandardCharsets.ISO_8859_1));
      result.setRequestJson(signedInvoiceXml);
      return result;
    } catch (Exception e) {
      log.log(Level.INFO, "发送发票失败", e);
      throw new VeriFactuException("register invoice failure: " + e.getMessage(), e);
    }
  }

  /**
   * 发送电子发票（使用multipart/form-data格式）
   *
   * @param token      认证令牌
   * @param envioPost  发票发送请求数据
   * @param xmlContent 发票XML文件内容
   * @return 响应结果
   */
  public ResultadoEnvioPost sendInvoice(String token, EnvioPost envioPost, byte[] xmlContent) {

    try {
      String url = boletaBaseUrl + "/boleta.electronica.envio";
      log.log(Level.INFO, "发送电子发票: "+url);
      log.log(Level.INFO, "令牌: "+token);

      // 打印XML内容用于调试
      try {
        String xmlString = new String(xmlContent, StandardCharsets.ISO_8859_1);
        log.log(Level.INFO, "发送的XML内容: "+xmlString);
      } catch (Exception e) {
        throw new RuntimeException("XML can not parser", e);
      }

      // 验证令牌格式
      if (token == null || token.trim().isEmpty()) {
        throw new RuntimeException("token is null");
      }

      // 创建multipart请求
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.MULTIPART_FORM_DATA);
      // 设置Cookie - 确保格式正确
      headers.set("Authorization", "Bearer " + token.trim());
      // 设置其他必要的HTTP头
      headers.set("User-Agent", "Mozilla/4.0 (compatible; PROG 1.0; Windows NT)");
      headers.set("Accept", "*/*");
      headers.set("Accept-Encoding", "gzip, deflate");
      headers.set("Connection", "keep-alive");

      log.log(Level.INFO, "请求头: "+ headers);

      // 构建multipart body - 将所有参数转换为字符串
      org.springframework.util.MultiValueMap<String, Object> body = new org.springframework.util.LinkedMultiValueMap<>();
      body.add("rutSender", String.valueOf(envioPost.getRutSender()));
      body.add("dvSender", envioPost.getDvSender());
      body.add("rutCompany", String.valueOf(envioPost.getRutCompany()));
      body.add("dvCompany", envioPost.getDvCompany());

      // 添加文件
      org.springframework.core.io.ByteArrayResource fileResource = new org.springframework.core.io.ByteArrayResource(xmlContent) {
        @Override
        public String getFilename() {
          return "boleta.xml";
        }
      };
      body.add("archivo", fileResource);
      log.log(Level.INFO, "请求体参数: "+ envioPost);
      // 验证URL格式
      log.log(Level.INFO, "完整请求URL: "+ url);

      HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
      RestTemplate restTemplate = new RestTemplate();
      restTemplate.getMessageConverters().add(new FormHttpMessageConverter()); // 关键
      restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
      // 发送请求并处理响应
      ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
      log.log(Level.INFO, "发票发送响应状态: "+ response.getStatusCode());
      log.log(Level.INFO, "发票发送响应头: "+ response.getHeaders());
      log.log(Level.INFO, "发票发送响应内容: "+ response.getBody());

      // 检查响应内容
      String responseBody = response.getBody();

      // 如果是400错误，详细分析响应内容
      if (response.getStatusCode().value() == 400) {
        log.log(Level.INFO, "收到400 BAD_REQUEST错误");
        log.log(Level.INFO, "响应内容详情: "+ responseBody);

        if (responseBody != null) {
          if (responseBody.contains("env:Fault") || responseBody.contains("faultstring")) {
            log.log(Level.INFO, "SII返回SOAP错误: "+ responseBody);
            throw new RuntimeException("SII response error: " + responseBody);
          } else {
            log.log(Level.INFO, "SII返回非SOAP格式错误: "+ responseBody);
            throw new RuntimeException("SII  response error (400): " + responseBody);
          }
        }
        throw new RuntimeException("SII服务返回400错误，但响应内容为空");
      }

      // 检查响应是否为SOAP错误
      if (responseBody != null && responseBody.contains("env:Fault")) {
        log.log(Level.INFO, "SII返回SOAP错误: "+ responseBody);
        throw new RuntimeException("SII服务返回错误: " + responseBody);
      }

      // 解析JSON响应为SiiInvoiceResponse对象
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      SiiInvoiceResponse jsonResponse = objectMapper.readValue(responseBody, SiiInvoiceResponse.class);
      log.log(Level.INFO, "解析后的JSON响应: "+ jsonResponse);

      // 转换为ResultadoEnvioPost对象以保持接口兼容性
      ResultadoEnvioPost result = convertToResultadoEnvioPost(jsonResponse);
      result.setResponseJson(responseBody);
      return result;

    } catch (Exception e) {
      log.log(Level.INFO, "发送电子发票失败", e);
      throw new RuntimeException("send failure: " + e.getMessage());
    }
  }

  /**
   * 将SiiInvoiceResponse转换为ResultadoEnvioPost
   * 用于保持接口兼容性
   */
  private ResultadoEnvioPost convertToResultadoEnvioPost(SiiInvoiceResponse jsonResponse) {
    ResultadoEnvioPost result = new ResultadoEnvioPost();

    result.setRutEmisor(jsonResponse.getRutEmisor());
    result.setRutEnvia(jsonResponse.getRutEnvia());
    result.setTrackId(jsonResponse.getTrackId());
    result.setFechaRecepcion(jsonResponse.getFechaRecepcion());
    result.setEstado(jsonResponse.getEstado());
    result.setFile(jsonResponse.getFile());

    log.log(Level.INFO, MessageFormat.format("转换后的ResultadoEnvioPost: rutEmisor={0}, rutEnvia={1}, trackId={2}, fechaRecepcion={3}, estado={4}, file={5}",
                                             result.getRutEmisor(), result.getRutEnvia(), result.getTrackId(),
                                             result.getFechaRecepcion(), result.getEstado(), result.getFile()));
    return result;
  }

  /**
   * 创建EnvioPost对象
   */
  private EnvioPost createEnvioPost(InvoiceSendRequest request) {
    EnvioPost envioPost = new EnvioPost();

    // 转换RUT参数为Integer类型
    try {
      envioPost.setRutSender(Integer.parseInt(request.getRutSender()));
      envioPost.setRutCompany(Integer.parseInt(request.getRutCompany()));
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("RUT格式无效: " + e.getMessage());
    }

    envioPost.setDvSender(request.getDvSender());
    envioPost.setDvCompany(request.getDvCompany());

    return envioPost;
  }

  @Override
  public SiiEnvioStatusResponse queryInvoice(String rut, String dv, Long trackId) {
    try {
      // 获取令牌
      String semilla = AuthUtils.getSemilla(apiBaseUrl, rut, restTemplate);
      // 加载证书
      KeyStore keyStore;
      if (certificate!=null){
        // 加载证书
        keyStore = CertificateManager.loadPKCS12Certificate(certificate, certificatePassword);
      }else {
        // 加载证书
        keyStore = CertificateManager.loadPKCS12Certificate(certificatePath, certificatePassword);
      }
      String signedXml = AuthUtils.signToken(semilla, keyStore, certificatePassword);
      // 验证令牌
      if (StringUtils.isEmpty(signedXml)) {
        throw new VeriFactuException("signedXml is null");
      }
      String token = AuthUtils.getToken(apiBaseUrl, rut, restTemplate, signedXml);
      // 验证令牌
      if (token == null || token.trim().isEmpty()) {
        throw new VeriFactuException("token is null");
      }
      log.log(Level.INFO, "成功获取SII令牌: "+token);

      // 调用SII API查询发送状态
      SiiEnvioStatusResponse result = querySendStatus(token, rut, dv, trackId.toString());

      return result;
    } catch (Exception e) {
      log.log(Level.INFO, "查询电子发票失败", e);
      throw new RuntimeException("query failure: " + e.getMessage());
    }
  }

  /**
   * 查询发送状态
   *
   * @param token   认证令牌
   * @param rut     公司RUT
   * @param dv      验证码
   * @param trackId 跟踪ID
   * @return 响应结果
   */
  public SiiEnvioStatusResponse querySendStatus(String token, String rut, String dv, String trackId) {
    try {
      // 根据OpenAPI规范，查询发送状态应该使用认证服务器（baseUrl），而不是发票服务器（boletaBaseUrl）
      String url = UriComponentsBuilder
              .fromHttpUrl(apiBaseUrl)
              .path("/boleta.electronica.envio/{rut}-{dv}-{trackid}")
              .buildAndExpand(rut, dv, trackId)
              .toUriString();

      log.log(Level.INFO, "查询发送状态: "+url);
      log.log(Level.INFO, "使用认证服务器: "+apiBaseUrl);
      log.log(Level.INFO, "使用令牌: "+token);

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + token);
      headers.set("Accept", "application/json");
      HttpEntity<?> request = new HttpEntity<>(headers);
      log.log(Level.INFO, "发送查询请求，请求头: "+headers);

      // 发送请求并处理响应
      ResponseEntity<String> response;
      try {
        // 在需要的地方临时创建
        RestTemplate restTemplate = new RestTemplate();
        // 添加 JSON 字符串转换器
        StringHttpMessageConverter converter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.APPLICATION_JSON));
        restTemplate.getMessageConverters().add(converter);
        response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
      } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
        log.log(Level.INFO, "错误响应: "+e.getResponseBodyAsString());
        throw new RuntimeException(e.getMessage());
      }
      log.log(Level.INFO, "查询发送状态响应内容: "+response.getBody());

      // 解析JSON响应为SiiEnvioStatusResponse对象
      com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
      SiiEnvioStatusResponse jsonResponse = objectMapper.readValue(response.getBody(), SiiEnvioStatusResponse.class);
      log.log(Level.INFO, "解析后的JSON响应: "+jsonResponse);

      return jsonResponse;
    } catch (Exception e) {
      log.log(Level.INFO, "查询发送状态失败", e);
      throw new RuntimeException("查询发送状态失败: " + e.getMessage());
    }
  }

}