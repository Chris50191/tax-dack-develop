/**
 * 项目名：	tax-dack
 * 文件名：	AuthUtils.java
 * 模块说明：
 * 修改历史：
 * 2025/8/22 - cc - 创建。
 */
package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.model.request.GetTokenRequest;
import com.searly.taxcontrol.sii.model.response.GetTokenResponse;
import com.searly.taxcontrol.sii.model.response.SemillaResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author cc
 */
public class AuthUtils {
  private static final Logger log = Logger.getLogger(AuthUtils.class.getName());

  private static final Map<String, SeedInfo> seedCache = new ConcurrentHashMap<>();
  private static final Map<String, TokenInfo> tokenCache = new ConcurrentHashMap<>();

  private static class SeedInfo {
    String seed;
    long expireAt;
  }

  private static class TokenInfo {
    String token;
    long expireAt;
  }

  public static synchronized String getSemilla(String baseUrl, String rut, RestTemplate restTemplate) {
    try {
      // 先从缓存取
      SeedInfo info = seedCache.get(rut);
      long now = System.currentTimeMillis();
      // 判断缓存是否过期
      if (info != null && info.expireAt > now) {
        return info.seed;
      }

      String url = baseUrl + "/boleta.electronica.semilla";
      log.log(Level.INFO, "获取SII认证种子: {}", url);

      // 使用 ResponseExtractor 处理原始响应
      String rawResponse = restTemplate.execute(url, HttpMethod.GET, null, response -> {
        // 打印状态码和响应头
        log.log(Level.INFO, "HTTP状态码: {}", response.getStatusCode());
        log.log(Level.INFO, "响应头: {}", response.getHeaders());
        // 读取响应体
        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
        StringBuilder responseBody = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
          responseBody.append(line);
        }
        return responseBody.toString();
      });

      // 打印原始XML响应
      log.log(Level.INFO, "原始XML响应: {}", rawResponse);
      // 创建JAXB上下文
      JAXBContext jaxbContext = JAXBContext.newInstance(SemillaResponse.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      // 解析XML
      StringReader reader = new StringReader(rawResponse);
      SemillaResponse response = (SemillaResponse) unmarshaller.unmarshal(reader);

      // 验证解析结果
      if (response != null) {
        log.info("XML解析成功");
        if (response.getResponseHeader() != null) {
          log.log(Level.INFO, "响应状态码: {}", response.getResponseHeader().getEstado());
        } else {
          throw new RuntimeException("响应头为空");
        }

        if (response.getResponseBody() != null) {
          log.log(Level.INFO, "种子值: {}", response.getResponseBody().getSemilla());
        } else {
          throw new RuntimeException("响应体为空");
        }
      } else {
        throw new RuntimeException("XML解析结果为空");
      }

      SeedInfo newInfo = new SeedInfo();
      newInfo.seed = response.getResponseBody().getSemilla();
      newInfo.expireAt = now + (10 * 60 * 1000) - 5000; // 10 分钟 - 提前5秒
      seedCache.put(rut, newInfo);

      return newInfo.seed;
    } catch (Exception e) {
      log.log(Level.INFO, "获取SII认证种子失败", e);
      throw new RuntimeException("获取SII认证种子失败: " + e.getMessage());
    }
  }

  /**
   * 使用签名后的XML获取SII认证令牌
   *
   * @param signedXml 签名后的XML字符串
   * @return 包含令牌的响应
   */
  public static synchronized String getToken(String baseUrl, String rut, RestTemplate restTemplate, String signedXml) {
    try {
      // 先从缓存取
//      TokenInfo info = tokenCache.get(rut);
      long now = System.currentTimeMillis();
      // 判断缓存是否过期
//      if (info != null && info.expireAt > now) {
//        return info.token;
//      }

      String url = baseUrl + "/boleta.electronica.token";
      log.log(Level.INFO, "使用签名XML获取SII认证令牌: {}", url);
      log.log(Level.INFO, "签名XML: {}", signedXml);

      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_XML);

      // 使用 ResponseExtractor 处理原始响应
      String rawResponse = restTemplate.execute(url, HttpMethod.POST,
                                                requestCallback -> {
                                                  requestCallback.getHeaders().setContentType(MediaType.APPLICATION_XML);
                                                  requestCallback.getBody().write(signedXml.getBytes(StandardCharsets.UTF_8));
                                                },
                                                response -> {
                                                  // 打印状态码和响应头
                                                  log.log(Level.INFO, "HTTP状态码: {}", response.getStatusCode());
                                                  log.log(Level.INFO, "响应头: {}", response.getHeaders());
                                                  // 读取响应体
                                                  BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody(), StandardCharsets.UTF_8));
                                                  StringBuilder responseBody = new StringBuilder();
                                                  String line;
                                                  while ((line = reader.readLine()) != null) {
                                                    responseBody.append(line);
                                                  }
                                                  return responseBody.toString();
                                                });

      // 打印原始XML响应
      log.log(Level.INFO, "原始XML响应: {}", rawResponse);

      // 创建JAXB上下文
      JAXBContext jaxbContext = JAXBContext.newInstance(GetTokenResponse.class);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      // 解析XML
      StringReader reader = new StringReader(rawResponse);
      GetTokenResponse response = (GetTokenResponse) unmarshaller.unmarshal(reader);

      // 验证解析结果
      if (response != null) {
        log.log(Level.INFO, "XML解析成功");
        if (response.getResponseHeader() != null) {
          log.log(Level.INFO, "响应状态码: {}", response.getResponseHeader().getEstado());
          log.log(Level.INFO, "响应描述: {}", response.getResponseHeader().getGlosa());
        } else {
          throw new RuntimeException("响应头为空");
        }

        if (response.getResponseBody() != null) {
          log.log(Level.INFO, "令牌值: {}", response.getResponseBody().getToken());
        } else {
          throw new RuntimeException( "响应体为空");
        }
      } else {
        throw new RuntimeException("XML解析结果为空");
      }

      TokenInfo newInfo = new TokenInfo();
      newInfo.token = response.getResponseBody().getToken();
      newInfo.expireAt = now + (60 * 60 * 1000) - (5 * 60 * 1000); // 1 小时 - 提前 5 分钟
      tokenCache.put(rut, newInfo);

      return response.getResponseBody().getToken();
    } catch (Exception e) {
      log.log(Level.INFO, "使用签名XML获取SII认证令牌失败", e);
      throw new RuntimeException("使用签名XML获取SII认证令牌失败: " + e.getMessage());
    }
  }

  public static String signToken(String semilla, KeyStore keyStore, String password) {
    try {
      // 创建GetToken请求
      GetTokenRequest tokenRequest = new GetTokenRequest();
      tokenRequest.setItem(new GetTokenRequest.Item());
      tokenRequest.getItem().setSemilla(semilla);

      log.info("使用指定证书对GetToken请求进行数字签名");

      // 确定证书别名
      String actualAlias = CertificateManager.getFirstAlias(keyStore);

      // 获取证书和私钥
      X509Certificate certificate = CertificateManager.getCertificate(keyStore, actualAlias);
      PrivateKey privateKey = CertificateManager.getPrivateKey(keyStore, actualAlias, password);

      Document unsignedDoc = buildUnsignedGetTokenDoc(semilla);
      signGetTokenDoc(unsignedDoc, privateKey, certificate);
      String signedXml = domToTwoLines(unsignedDoc);

      log.info("=== Final XML to send ===");
      log.info(signedXml);

      return signedXml;

    } catch (Exception e) {
      log.log(Level.INFO, "GetToken请求签名失败", e);
      throw new RuntimeException("GetToken请求签名失败: " + e.getMessage(), e);
    }
  }

  private static Document buildUnsignedGetTokenDoc(String semilla) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document doc = db.newDocument();

    Element getToken = doc.createElement("getToken");
    doc.appendChild(getToken);

    Element item = doc.createElement("item");
    getToken.appendChild(item);

    Element semillaElem = doc.createElement("Semilla");
    semillaElem.setTextContent(semilla);
    item.appendChild(semillaElem);

    return doc;
  }

  // 签名 XML，KeyInfo 包含 KeyValue + X509Data
  private static void signGetTokenDoc(Document doc, PrivateKey privateKey, X509Certificate cert) throws Exception {
    XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

    CanonicalizationMethod cm = fac.newCanonicalizationMethod(
            CanonicalizationMethod.INCLUSIVE,
            (C14NMethodParameterSpec) null
                                                             );
    SignatureMethod sm = fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null);
    DigestMethod dm = fac.newDigestMethod(DigestMethod.SHA1, null);
    Transform envTransform = fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null);

    Reference ref = fac.newReference(
            "",
            dm,
            Collections.singletonList(envTransform),
            null,
            null
                                    );

    SignedInfo si = fac.newSignedInfo(cm, sm, Collections.singletonList(ref));

    KeyInfoFactory kif = fac.getKeyInfoFactory();
    KeyValue kv = kif.newKeyValue(cert.getPublicKey());
    X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));

    // KeyInfo 包含 KeyValue 和 X509Data
    KeyInfo ki = kif.newKeyInfo(Arrays.asList(kv, x509Data));

    DOMSignContext signContext = new DOMSignContext(privateKey, doc.getDocumentElement());
    XMLSignature signature = fac.newXMLSignature(si, ki);
    signature.sign(signContext);

    // 确保 X509Certificate Base64 是单行
    NodeList certNodes = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "X509Certificate");
    for (int i = 0; i < certNodes.getLength(); i++) {
      Node node = certNodes.item(i);
      node.setTextContent(node.getTextContent().replaceAll("\\s+", ""));
    }
  }

  // DOM 转为两行字符串
  private static String domToTwoLines(Document doc) throws TransformerException {
    TransformerFactory tf = TransformerFactory.newInstance();
    Transformer transformer = tf.newTransformer();
    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
    transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    transformer.setOutputProperty(OutputKeys.INDENT, "no");

    StringWriter sw = new StringWriter();
    transformer.transform(new DOMSource(doc), new StreamResult(sw));
    String out = sw.toString().replaceAll("[\r\n]", "");
    int idx = out.indexOf("?>");
    if (idx != -1) {
      return out.substring(0, idx + 2) + "\n" + out.substring(idx + 2).trim();
    }
    return out;
  }
}
