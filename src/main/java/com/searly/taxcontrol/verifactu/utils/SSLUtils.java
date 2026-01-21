package com.searly.taxcontrol.verifactu.utils;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * SSL工具类
 * 提供SSL相关功能，如证书加载等
 */
public class SSLUtils {
    private static final Logger logger = Logger.getLogger(SSLUtils.class.getName());
    
    /**
     * 创建SSL工厂
     * 
     * @param certificatePath 证书路径
     * @param certificatePassword 证书密码
     * @return SSL连接套接字工厂
     */
    public static SSLConnectionSocketFactory createSSLFactory(String certificatePath, String certificatePassword) {
        try {
            // 创建SSL上下文
            SSLContext sslContext = createSSLContext(certificatePath, certificatePassword);
            
            // 创建SSL连接工厂
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1.2"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());
            
            logger.info("SSL连接工厂创建成功");
            return sslFactory;
            
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | 
                CertificateException | UnrecoverableKeyException | KeyManagementException e) {
            logger.log(Level.SEVERE, "SSL配置错误: " + e.getMessage(), e);
            throw new RuntimeException("SSL配置错误: " + e.getMessage(), e);
        }
    }

    /**
     * 创建SSL连接工厂
     *
     * @param sslContext 证书路径
     * @return SSL连接套接字工厂
     */
    public static SSLConnectionSocketFactory createSSLFactory(SSLContext sslContext) {
        try {
            // 创建SSL连接工厂
            SSLConnectionSocketFactory sslFactory = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[]{"TLSv1.2"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());

            logger.info("SSL连接工厂创建成功");
            return sslFactory;

        } catch (Exception e) {
            logger.log(Level.SEVERE, "SSL配置错误: " + e.getMessage(), e);
            throw new RuntimeException("SSL配置错误: " + e.getMessage(), e);
        }
    }

    /**
     * 创建SSL上下文
     *
     * @param certificatePath 证书路径
     * @param certificatePassword 证书密码
     * @return SSL上下文
     * @throws KeyStoreException 密钥库异常
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 算法异常
     * @throws CertificateException 证书异常
     * @throws UnrecoverableKeyException 密钥不可恢复异常
     * @throws KeyManagementException 密钥管理异常
     */
    public static SSLContext createSSLContext(String certificatePath, String certificatePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException, KeyManagementException {
        
        // 加载证书
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        
        // 尝试多种方式加载证书
        boolean loaded = false;
        
        // 1. 尝试作为文件系统路径加载
        File certFile = new File(certificatePath);
        if (certFile.exists()) {
            logger.info("从文件系统加载证书: " + certificatePath);
            try (FileInputStream instream = new FileInputStream(certFile)) {
                keyStore.load(instream, certificatePassword.toCharArray());
                loaded = true;
                logger.info("成功从文件系统加载证书: " + certFile.getAbsolutePath());
            } catch (IOException e) {
                logger.log(Level.WARNING, "从文件系统加载证书失败: " + e.getMessage(), e);
            }
        } else {
            logger.warning("证书文件不存在: " + certFile.getAbsolutePath());
        }
        
        if (!loaded) {
            throw new IOException("无法加载证书，已尝试多种路径: " + certificatePath);
        }
        
        // 创建SSL上下文
        SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, certificatePassword.toCharArray())
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();

        
        logger.info("SSL上下文创建成功");
        return sslContext;
    }

    /**
     * 创建SSL上下文
     *
     * @param certificate 证书输入流
     * @param certificatePassword 证书密码
     * @return SSL上下文
     * @throws KeyStoreException 密钥库异常
     * @throws IOException IO异常
     * @throws NoSuchAlgorithmException 算法异常
     * @throws CertificateException 证书异常
     * @throws UnrecoverableKeyException 密钥不可恢复异常
     * @throws KeyManagementException 密钥管理异常
     */
    public static SSLContext createSSLContext(InputStream certificate, String certificatePassword)
            throws KeyStoreException, IOException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException, KeyManagementException {

        // 加载证书
        KeyStore keyStore = KeyStore.getInstance("PKCS12");

        // 尝试多种方式加载证书
        boolean loaded = false;

        // 1. 尝试作为输入流加载
        if (certificate !=null) {
            logger.info("从输入流加载证书" );
            try  {
                keyStore.load(certificate, certificatePassword.toCharArray());
                loaded = true;
                logger.info("成功从输入流加载证书加载证书");
            } catch (IOException e) {
                logger.log(Level.WARNING, "从文件系统加载证书失败: " + e.getMessage(), e);
            }finally {
                certificate.close();
            }
        } else {
            logger.warning("证书文件不存在" );
        }

        if (!loaded) {
            throw new IOException("无法加载证书，已尝试多种路径");
        }

        // 创建SSL上下文
        SSLContext sslContext = SSLContexts.custom()
                    .loadKeyMaterial(keyStore, certificatePassword.toCharArray())
                    .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                    .build();


        logger.info("SSL上下文创建成功");
        return sslContext;
    }
}