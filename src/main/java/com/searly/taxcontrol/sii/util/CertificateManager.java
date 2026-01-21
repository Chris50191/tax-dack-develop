package com.searly.taxcontrol.sii.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SII证书管理工具类
 * 专门用于SII电子发票系统的证书加载和管理
 */
public class CertificateManager {
  private static final Logger logger = Logger.getLogger(CertificateManager.class.getName());

  /**
   * 从文件加载PKCS12证书
   *
   * @param certificatePath 证书文件路径
   * @param password        证书密码
   * @return KeyStore对象
   */
  public static KeyStore loadPKCS12Certificate(String certificatePath, String password) {
    try {
      logger.log(Level.INFO, "加载PKCS12证书: {}", certificatePath);

      KeyStore keyStore = KeyStore.getInstance("PKCS12");
      try (InputStream is = new FileInputStream(certificatePath)) {
        keyStore.load(is, password.toCharArray());
      }

      logger.info("PKCS12证书加载成功");
      return keyStore;
    } catch (Exception e) {
      logger.log(Level.INFO, "加载PKCS12证书失败: {}", e);
      throw new RuntimeException("加载PKCS12证书失败: " + e.getMessage(), e);
    }
  }

  /**
   * 从文件加载PKCS12证书
   *
   * @param certificateStream 证书文件路径
   * @param password          证书密码
   * @return KeyStore对象
   */
  public static KeyStore loadPKCS12Certificate(InputStream certificateStream, String password) throws Exception {
    logger.log(Level.INFO, "加载PKCS12证书流");

    KeyStore keyStore = KeyStore.getInstance("PKCS12");
    // 1. 尝试作为输入流加载
    if (certificateStream != null) {
      try {
        keyStore.load(certificateStream, password.toCharArray());
      } catch (Exception e) {
        logger.log(Level.INFO, "加载PKCS12证书失败: {}", e);
        throw new Exception("加载PKCS12证书失败: " + e.getMessage(), e);
      } finally {
        certificateStream.close();
      }
    }
    logger.info("PKCS12证书加载成功");
    return keyStore;
  }

  /**
   * 获取证书的第一个别名
   *
   * @param keyStore KeyStore对象
   * @return 证书别名
   */
  public static String getFirstAlias(KeyStore keyStore) {
    try {
      return keyStore.aliases().nextElement();
    } catch (Exception e) {
      logger.log(Level.INFO, "获取证书别名失败", e);
      throw new RuntimeException("获取证书别名失败: " + e.getMessage(), e);
    }
  }

  /**
   * 从KeyStore获取X509证书
   *
   * @param keyStore KeyStore对象
   * @param alias    证书别名
   * @return X509Certificate对象
   */
  public static X509Certificate getCertificate(KeyStore keyStore, String alias) {
    try {
      return (X509Certificate) keyStore.getCertificate(alias);
    } catch (Exception e) {
      logger.log(Level.INFO, "获取X509证书失败", e);
      throw new RuntimeException("获取X509证书失败: " + e.getMessage(), e);
    }
  }

  /**
   * 从KeyStore获取私钥
   *
   * @param keyStore KeyStore对象
   * @param alias    证书别名
   * @param password 私钥密码
   * @return PrivateKey对象
   */
  public static PrivateKey getPrivateKey(KeyStore keyStore, String alias, String password) {
    try {
      return (PrivateKey) keyStore.getKey(alias, password.toCharArray());
    } catch (Exception e) {
      logger.log(Level.INFO, "获取私钥失败", e);
      throw new RuntimeException("获取私钥失败: " + e.getMessage(), e);
    }
  }

  /**
   * 验证证书是否有效
   *
   * @param certificate X509证书
   * @return 是否有效
   */
  public boolean isCertificateValid(X509Certificate certificate) {
    try {
      certificate.checkValidity();
      logger.info("证书有效期验证通过");
      return true;
    } catch (Exception e) {
      logger.log(Level.INFO, "证书有效期验证失败: {}", e.getMessage());
      return false;
    }
  }

}