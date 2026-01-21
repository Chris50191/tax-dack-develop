package com.searly.taxcontrol.verifactu.config;


import com.searly.taxcontrol.verifactu.api.VeriFactuApi;
import com.searly.taxcontrol.verifactu.service.VeriFactuService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VeriFactu配置类
 * 负责加载配置并创建服务实例
 */
public class VeriFactuConfig {
    private static final Logger logger = Logger.getLogger(VeriFactuConfig.class.getName());
    private static final String CONFIG_FILE = "/config/verifactu.properties";
    
    private Properties properties;
    private String apiUrl;
    private String validateUrl;
    private String certificatePath;
    private InputStream certificate;
    private String certificatePassword;
    private boolean isTestEnvironment;
    
    /**
     * 构造函数
     * 默认从classpath加载配置
     */
    public VeriFactuConfig() {
        // 初始化，但不立即加载配置
        this.properties = new Properties();
    }
    
    /**
     * 构造函数
     * 
     * @param apiUrl API URL
     * @param validateUrl 验证URL
     * @param certificatePath 证书路径
     * @param certificatePassword 证书密码
     * @param isTestEnvironment 是否为测试环境
     */
    public VeriFactuConfig(String apiUrl, String validateUrl, String certificatePath,
                           String certificatePassword, boolean isTestEnvironment) {
        this.apiUrl = apiUrl;
        this.validateUrl = validateUrl;
        this.certificatePath = certificatePath;
        this.certificatePassword = certificatePassword;
        this.isTestEnvironment = isTestEnvironment;
    }

    /**
     * 构造函数
     *
     * @param apiUrl API URL
     * @param validateUrl 验证URL
     * @param certificate 证书输入流
     * @param certificatePassword 证书密码
     * @param isTestEnvironment 是否为测试环境
     */
    public VeriFactuConfig(String apiUrl, String validateUrl, InputStream certificate,
                           String certificatePassword, boolean isTestEnvironment) {
        this.apiUrl = apiUrl;
        this.validateUrl = validateUrl;
        this.certificate = certificate;
        this.certificatePassword = certificatePassword;
        this.isTestEnvironment = isTestEnvironment;
    }

    /**
     * 加载配置
     * 从配置文件加载配置信息
     */
    public void loadConfig() {
        loadProperties();
    }
    
    /**
     * 加载配置文件
     */
    private void loadProperties() {
        properties = new Properties();
        try (InputStream is = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (is != null) {
                properties.load(is);
                
                // 确定环境
                String environment = properties.getProperty("verifactu.environment", "test");
                isTestEnvironment = "test".equalsIgnoreCase(environment);
                
                // 根据环境选择URL
                if (isTestEnvironment) {
                    apiUrl = properties.getProperty("verifactu.api.url.test");
                    validateUrl = properties.getProperty("verifactu.validate.url.test");
                    logger.info("已加载测试环境配置");
                } else {
                    apiUrl = properties.getProperty("verifactu.api.url.prod");
                    validateUrl = properties.getProperty("verifactu.validate.url.prod");
                    logger.info("已加载生产环境配置");
                }
                
                // 获取证书路径并处理
                certificatePath = properties.getProperty("verifactu.certificate.path");
                
                // 检查证书路径是否为相对路径，如果是，则转换为绝对路径
                if (certificatePath != null && !certificatePath.startsWith("/") && !new File(certificatePath).isAbsolute()) {
                    // 获取项目根目录路径
                    String userDir = System.getProperty("user.dir");
                    File certFile = new File(userDir, certificatePath);
                    
                    if (certFile.exists()) {
                        certificatePath = certFile.getAbsolutePath();
                        logger.info("证书路径已转换为绝对路径: " + certificatePath);
                    } else {
                        // 如果文件不存在，尝试在classpath中查找
                        String classpathCert = "/" + certificatePath;
                        if (getClass().getResource(classpathCert) != null) {
                            // 保持原路径，后续会通过classpath加载
                            logger.info("证书将从classpath加载: " + certificatePath);
                        } else {
                            logger.warning("警告：证书文件不存在: " + certificatePath);
                            // 尝试在resources目录中查找
                            classpathCert = "/src/main/resources/config/keystore.p12";
                            if (getClass().getResource(classpathCert) != null) {
                                certificatePath = "src/main/resources/config/keystore.p12";
                                logger.info("已找到证书在resources目录: " + certificatePath);
                            }
                        }
                    }
                }
                
                certificatePassword = properties.getProperty("verifactu.certificate.password");
                
                logger.info("已加载VeriFactu配置");
                logger.info("证书路径: " + certificatePath);
            } else {
                logger.warning("未找到配置文件: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "加载配置文件失败", e);
        }
    }
    
    /**
     * 创建VeriFactu API服务实例
     * 
     * @return API服务实例
     */
    public VeriFactuApi createApiService() {
        validateConfig();
        return new VeriFactuService(this);
    }
    
    /**
     * 验证配置是否完整
     */
    private void validateConfig() {
        if (apiUrl == null || apiUrl.trim().isEmpty()) {
            throw new IllegalStateException("未配置API URL");
        }
        if (validateUrl == null || validateUrl.trim().isEmpty()) {
            throw new IllegalStateException("未配置验证URL");
        }
        if (certificatePath == null || certificatePath.trim().isEmpty()) {
            throw new IllegalStateException("未配置证书路径");
        }
        if (certificatePassword == null) {
            throw new IllegalStateException("未配置证书密码");
        }
    }
    
    /**
     * 获取API URL
     * @return API URL
     */
    public String getApiUrl() {
        return apiUrl;
    }
    
    /**
     * 设置API URL
     * @param apiUrl API URL
     */
    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }
    
    /**
     * 获取验证URL
     * @return 验证URL
     */
    public String getValidateUrl() {
        return validateUrl;
    }
    
    /**
     * 设置验证URL
     * @param validateUrl 验证URL
     */
    public void setValidateUrl(String validateUrl) {
        this.validateUrl = validateUrl;
    }
    
    /**
     * 获取证书路径
     * @return 证书路径
     */
    public String getCertificatePath() {
        return certificatePath;
    }
    
    /**
     * 设置证书路径
     * @param certificatePath 证书路径
     */
    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }
    
    /**
     * 获取证书密码
     * @return 证书密码
     */
    public String getCertificatePassword() {
        return certificatePassword;
    }
    
    /**
     * 设置证书密码
     * @param certificatePassword 证书密码
     */
    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }
    
    /**
     * 是否为测试环境
     * @return 如果是测试环境返回true，否则返回false
     */
    public boolean isTestEnvironment() {
        return isTestEnvironment;
    }
    
    /**
     * 设置是否为测试环境
     * @param testEnvironment 是否为测试环境
     */
    public void setTestEnvironment(boolean testEnvironment) {
        isTestEnvironment = testEnvironment;
    }

    public InputStream getCertificate() {
        return this.certificate;
    }

    public void setCertificate(InputStream certificate) {
        this.certificate = certificate;
    }
}