package com.searly.taxcontrol.verifactu.service;

import com.searly.taxcontrol.verifactu.api.VeriFactuApi;
import com.searly.taxcontrol.verifactu.config.VeriFactuConfig;
import com.searly.taxcontrol.verifactu.model.CancelInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.ConsultaInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.CorrectionInvoiceRequest;
import com.searly.taxcontrol.verifactu.model.InvoiceRegisterRequest;
import com.searly.taxcontrol.verifactu.model.InvoiceResponse;
import com.searly.taxcontrol.verifactu.model.VeriFactuException;
import com.searly.taxcontrol.verifactu.model.VerifactuConsultaResponse;
import com.searly.taxcontrol.verifactu.model.VerifactuErrorResponse;
import com.searly.taxcontrol.verifactu.model.VerifactuResponse;
import com.searly.taxcontrol.verifactu.utils.SSLUtils;
import com.searly.taxcontrol.verifactu.utils.XmlUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.net.ssl.SSLContext;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VeriFactu服务实现
 * 实现与AEAT税务系统的交互
 */
public class VeriFactuService implements VeriFactuApi {

  private static final Logger log = Logger.getLogger(VeriFactuService.class.getName());
  private static final String CONTENT_TYPE_XML = "application/xml; charset=utf-8";
  private static final String HEADER_ACCEPT = "Accept";
  private static final String HEADER_CONTENT_TYPE = "Content-Type";

  private final String apiBaseUrl;
  private final String validateUrl;
  private final CloseableHttpClient httpClient;
  private final boolean isTestEnvironment;
  private final SSLContext sslContext;

  /**
   * 构造函数
   *
   * @param config
   *     VeriFactu配置
   */
  public VeriFactuService(VeriFactuConfig config) throws VeriFactuException {
    this.apiBaseUrl = config.getApiUrl();
    this.validateUrl = config.getValidateUrl();
    this.isTestEnvironment = config.isTestEnvironment();

    // 配置HTTP客户端，启用自动重定向
    RequestConfig requestConfig = RequestConfig.custom()
        .setConnectTimeout(30000)
        .setSocketTimeout(60000)
        .setRedirectsEnabled(true)
        .setCircularRedirectsAllowed(true)
        .setMaxRedirects(10)
        .build();

    try {
      if (!Objects.isNull(config.getCertificatePath()) && config.getCertificatePath().length() > 0) {
        this.sslContext = SSLUtils.createSSLContext(
            config.getCertificatePath(),
            config.getCertificatePassword());
      } else {
        this.sslContext = SSLUtils.createSSLContext(
            config.getCertificate(),
            config.getCertificatePassword());
      }
    } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException |
             IOException | UnrecoverableKeyException | KeyManagementException e) {
      log.log(Level.SEVERE, "初始化SSL上下文失败", e);
      throw new VeriFactuException("initial SSL failure", e);
    }

    // 创建HTTP客户端，使用自定义的重定向策略
    final SSLConnectionSocketFactory sslFactory;
    if (!Objects.isNull(config.getCertificatePath()) && config.getCertificatePath().length() > 0) {
      sslFactory = SSLUtils.createSSLFactory(config.getCertificatePath(), config.getCertificatePassword());
    } else {
      sslFactory = SSLUtils.createSSLFactory(this.sslContext);
    }
    this.httpClient = HttpClients.custom()
        .setSSLSocketFactory(sslFactory)
        .setDefaultRequestConfig(requestConfig)
        .setRedirectStrategy(new LaxRedirectStrategy())
        .build();

    log.info("VeriFactuService初始化完成");
    log.info("环境: " + (isTestEnvironment ? "测试环境" : "生产环境"));
    log.info("API地址: " + apiBaseUrl);
    log.info("验证地址: " + validateUrl);
    log.info("已启用自动重定向处理");

  }

  /**
   * 修正发票（直接使用CorrectionInvoiceRequest）
   *
   * @param request
   *     修正发票请求
   * @return 发票响应
   * @throws VeriFactuException
   *     如果修正失败
   */
  public InvoiceResponse correctionInvoice(CorrectionInvoiceRequest request) throws VeriFactuException {
    try {
      // 包装为SOAP请求
      String soapRequest = request.toXml();
      log.info("发送发票修正请求: " + soapRequest);

      // 发送请求并获取响应
      String soapResponse = sendSoapRequest(apiBaseUrl, soapRequest);
      log.info("收到发票修正响应: " + soapResponse);

      // 解析响应
      VerifactuResponse registerResponse;
      try {
        registerResponse = VerifactuResponse.fromXml(soapResponse);
      } catch (JAXBException e) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析", e);
        return parseResponse(soapResponse);
      }

      if (registerResponse == null || registerResponse.getHeader() == null || registerResponse.getBody() == null || registerResponse.getBody().getId() == null) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析");
        VerifactuErrorResponse errorResponse = VerifactuErrorResponse.fromXml(soapResponse);
        if (errorResponse != null && errorResponse.getFaultCode() != null) {
          InvoiceResponse response = new InvoiceResponse();
          response.setCsv(soapResponse);
          response.setEstadoRegistro(InvoiceResponse.Status.Incorrecto.name());
          response.setCodigoRespuesta(errorResponse.getFaultCode());
          response.setDescripcionRespuesta(errorResponse.getFaultString());
          response.setStatus(response.getEstadoRegistro());
          return response;
        }
      }

      // 转换为通用响应格式
      InvoiceResponse response = new InvoiceResponse();
      response.setCsv(soapResponse);
      response.setEstadoRegistro(registerResponse.getEstadoEnvio());
      response.setTimestamp(registerResponse.getTimestamp());
      response.setNif(registerResponse.getNifPresentador());
      response.setCodigoRespuesta(registerResponse.getCodigoErrorRegistro());
      response.setDescripcionRespuesta(registerResponse.getDescripcionErrorRegistro());
      response.setStatus(registerResponse.getEstadoEnvio());

      return response;
    } catch (UnknownHostException e) {
      log.log(Level.SEVERE, "找不到域名主机", e);
      throw new VeriFactuException("correct invoice failure: UnknownHostName");
    } catch (Exception e) {
      log.log(Level.SEVERE, "修正发票失败", e);
      throw new VeriFactuException("correct invoice failure: " + e.getMessage(), e);
    }
  }

  /**
   * 注册发票（直接使用InvoiceRegisterRequest）
   *
   * @param request
   *     发票注册请求
   * @return 发票响应
   * @throws VeriFactuException
   *     如果注册失败
   */
  public InvoiceResponse registerInvoice(InvoiceRegisterRequest request) throws VeriFactuException {
    try {
      // 生成XML请求
      String soapRequest = request.toXml();
      log.info("发送发票注册请求: " + soapRequest);

      // 发送请求并获取响应
      String soapResponse = sendSoapRequest(apiBaseUrl, soapRequest);
      log.info("收到发票注册响应: " + soapResponse);

      // 解析响应
      VerifactuResponse registerResponse;
      try {
        registerResponse = VerifactuResponse.fromXml(soapResponse);
      } catch (JAXBException e) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析", e);
        return parseResponse(soapResponse);
      }
      if (registerResponse == null || registerResponse.getHeader() == null || registerResponse.getBody() == null || registerResponse.getBody().getId() == null) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析");
        VerifactuErrorResponse errorResponse = VerifactuErrorResponse.fromXml(soapResponse);
        if (errorResponse != null && errorResponse.getFaultCode() != null) {
          InvoiceResponse response = new InvoiceResponse();
          response.setCsv(soapResponse);
          response.setEstadoRegistro(InvoiceResponse.Status.Incorrecto.name());
          response.setCodigoRespuesta(errorResponse.getFaultCode());
          response.setDescripcionRespuesta(errorResponse.getFaultString());
          response.setStatus(response.getEstadoRegistro());
          return response;
        }
      }

      // 转换为通用响应格式
      InvoiceResponse response = new InvoiceResponse();
      response.setCsv(soapResponse);
      response.setEstadoRegistro(registerResponse.getEstadoEnvio());
      response.setTimestamp(registerResponse.getTimestamp());
      response.setNif(registerResponse.getNifPresentador());
      response.setCodigoRespuesta(registerResponse.getCodigoErrorRegistro());
      response.setDescripcionRespuesta(registerResponse.getDescripcionErrorRegistro());
      response.setStatus(registerResponse.getEstadoEnvio());

      return response;

    } catch (UnknownHostException e) {
      log.log(Level.SEVERE, "找不到域名主机", e);
      throw new VeriFactuException("correct invoice failure: UnknownHostName");
    } catch (Exception e) {
      log.log(Level.SEVERE, "注册发票失败", e);
      throw new VeriFactuException("register invoice failure: " + e.getMessage(), e);
    }
  }

  @Override
  public InvoiceResponse queryInvoice(ConsultaInvoiceRequest request) throws VeriFactuException {
    try {
      // 将对象转换为XML
      String xmlRequest = request.toXml();
      log.info("发送发票查询请求: " + xmlRequest);

      // 发送请求并获取响应
      String soapResponse = sendSoapRequest(apiBaseUrl, xmlRequest);
//      log.info("收到发票查询响应: /n" + soapResponse);
      System.out.println("发票查询响应：\n" +soapResponse);

      // 解析响应
      VerifactuConsultaResponse consultaResponse;
      try {
        consultaResponse = VerifactuConsultaResponse.fromXml(soapResponse);
      } catch (JAXBException e) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析", e);
        return parseResponse(soapResponse);
      }

      // 转换为通用响应格式
      InvoiceResponse response = new InvoiceResponse();
      response.setCsv(soapResponse);
      response.setEstadoRegistro(consultaResponse.getEstadoEnvio());
      response.setConsultaIDFactura(consultaResponse.getIDFactura());
      response.setConsultaHuella(consultaResponse.getHuella());
      response.setConsultaEstadoRegistro(consultaResponse.getEstadoRegistro());
      response.setStatus(consultaResponse.isSuccess() ? InvoiceResponse.Status.Correcto.name() : InvoiceResponse.Status.Incorrecto.name());

      return response;

    } catch (UnknownHostException e) {
      log.log(Level.SEVERE, "找不到域名主机", e);
      throw new VeriFactuException("correct invoice failure: UnknownHostName");
    } catch (Exception e) {
      log.log(Level.SEVERE, "查询发票失败", e);
      throw new VeriFactuException("query invoice failure: " + e.getMessage(), e);
    }
  }

  @Override
  public InvoiceResponse cancelInvoice(CancelInvoiceRequest request) throws VeriFactuException {
    try {
      // 将对象转换为XML
      String xmlRequest = request.toXml();
      log.info("发送发票注销请求: " + xmlRequest);

      // 发送请求并获取响应
      String soapResponse = sendSoapRequest(apiBaseUrl, xmlRequest);
      log.info("收到发票注销响应: " + soapResponse);

      // 解析响应
      VerifactuResponse registerResponse;
      try {
        registerResponse = VerifactuResponse.fromXml(soapResponse);
      } catch (JAXBException e) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析", e);
        return parseResponse(soapResponse);
      }

      if (registerResponse == null || registerResponse.getHeader() == null || registerResponse.getBody() == null || registerResponse.getBody().getId() == null) {
        log.log(Level.WARNING, "使用JAXB解析响应失败，尝试使用DOM解析");
        VerifactuErrorResponse errorResponse = VerifactuErrorResponse.fromXml(soapResponse);
        if (errorResponse != null && errorResponse.getFaultCode() != null) {
          InvoiceResponse response = new InvoiceResponse();
          response.setCsv(soapResponse);
          response.setEstadoRegistro(InvoiceResponse.Status.Incorrecto.name());
          response.setCodigoRespuesta(errorResponse.getFaultCode());
          response.setDescripcionRespuesta(errorResponse.getFaultString());
          response.setStatus(response.getEstadoRegistro());
          return response;
        }
      }

      // 转换为通用响应格式
      InvoiceResponse response = new InvoiceResponse();
      response.setCsv(soapResponse);
      response.setEstadoRegistro(registerResponse.getEstadoEnvio());
      response.setTimestamp(registerResponse.getTimestamp());
      response.setNif(registerResponse.getNifPresentador());
      response.setCodigoRespuesta(registerResponse.getCodigoErrorRegistro());
      response.setDescripcionRespuesta(registerResponse.getDescripcionErrorRegistro());
      response.setStatus(registerResponse.getEstadoEnvio());

      return response;
    } catch (UnknownHostException e) {
      log.log(Level.SEVERE, "找不到域名主机", e);
      throw new VeriFactuException("correct invoice failure: UnknownHostName");
    } catch (Exception e) {
      log.log(Level.SEVERE, "注销发票失败", e);
      throw new VeriFactuException("cancel invoice failure: " + e.getMessage(), e);
    }
  }

  private String sendSoapRequest(String endpointUrl, String soapRequest) throws IOException {
    HttpPost httpPost = new HttpPost(endpointUrl);
    httpPost.setHeader(HEADER_ACCEPT, CONTENT_TYPE_XML);
    httpPost.setHeader(HEADER_CONTENT_TYPE, CONTENT_TYPE_XML);
    httpPost.setEntity(new StringEntity(soapRequest, StandardCharsets.UTF_8));

    try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
      HttpEntity entity = response.getEntity();
      String responseContent = EntityUtils.toString(entity, StandardCharsets.UTF_8);

      int statusCode = response.getStatusLine().getStatusCode();
      if (statusCode >= 200 && statusCode < 300) {
        return responseContent;
      } else {
        throw new IOException("HTTP Error: " + statusCode + ", Error Message: " + responseContent);
      }
    }
  }

  private InvoiceResponse parseResponse(String soapResponse) throws VeriFactuException {
    // 创建响应对象
    InvoiceResponse response = new InvoiceResponse();

    try {
      // 解析XML字符串
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(soapResponse)));

      // 提取CSV值
      Element csvElement = XmlUtils.findElementByTagName(document, "CSV");
      if (csvElement != null) {
        response.setCsv(csvElement.getTextContent());
      }

      // 提取状态
      Element estadoElement = XmlUtils.findElementByTagName(document, "EstadoEnvio");
      if (estadoElement != null) {
        String estadoValue = estadoElement.getTextContent();
        response.setEstadoRegistro(estadoValue);
        response.setStatus(estadoValue);
      }

      // 提取时间戳
      Element timestampElement = XmlUtils.findElementByTagName(document, "TimestampPresentacion");
      if (timestampElement != null) {
        response.setTimestamp(timestampElement.getTextContent());
      }

      // 提取卖方税号
      Element nifElement = XmlUtils.findElementByTagName(document, "NIFPresentador");
      if (nifElement != null) {
        response.setNif(nifElement.getTextContent());
      }

      // 提取错误代码
      Element faultcode = XmlUtils.findElementByTagName(document, "faultcode");
      if (faultcode != null) {
        response.setNif(faultcode.getTextContent());
      }

      // 提取错误信息
      Element faultstring = XmlUtils.findElementByTagName(document, "faultstring");
      if (faultstring != null) {
        response.setNif(faultstring.getTextContent());
      }

      return response;
    } catch (Exception e) {
      log.log(Level.SEVERE, "解析响应失败", e);
      throw new VeriFactuException("parse invoice failure: " + e.getMessage(), e);
    }
  }

  private InvoiceResponse parseConsultaResponse(String soapResponse) throws VeriFactuException {
    // 与parseResponse类似，但针对查询响应的特定结构进行解析
    InvoiceResponse response = new InvoiceResponse();

    try {
      // 解析XML字符串
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.parse(new org.xml.sax.InputSource(new java.io.StringReader(soapResponse)));

      // 提取CSV值
      Element csvElement = XmlUtils.findElementByTagName(document, "CSV");
      if (csvElement != null) {
        response.setCsv(csvElement.getTextContent());
      }

      // 提取状态
      Element estadoElement = XmlUtils.findElementByTagName(document, "EstadoEnvio");
      if (estadoElement != null) {
        String estadoValue = estadoElement.getTextContent();
        response.setEstadoRegistro(estadoValue);
        response.setStatus(estadoValue);
      }

      // 提取时间戳
      Element timestampElement = XmlUtils.findElementByTagName(document, "TimestampPresentacion");
      if (timestampElement != null) {
        response.setTimestamp(timestampElement.getTextContent());
      }

      return response;
    } catch (Exception e) {
      log.log(Level.SEVERE, "解析查询响应失败", e);
      throw new VeriFactuException("parse query invoice failure: " + e.getMessage(), e);
    }
  }
} 