package com.searly.taxcontrol.sii.config;

import javax.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class SiiConfig {
    private static final Logger logger = Logger.getLogger(SiiConfig.class.getName());
    private static final String CONFIG_FILE = "/config/sii.properties";

    private Properties properties;
    private String apiUrl;
    private String boletaBaseUrl;
    private String validateUrl;
    private String certificatePath;
    private InputStream certificate;
    private String certificatePassword;
    private boolean isTestEnvironment;

    /**
     * 构造函数
     * 默认从classpath加载配置
     */
    public SiiConfig() {
        // 初始化，但不立即加载配置
        this.properties = new Properties();
    }

    @PostConstruct
    public void init() {
        loadConfig();
    }

    /**
     * 构造函数
     *
     * @param apiUrl API URL
     * @param boletaBaseUrl API URL
     * @param validateUrl 验证URL
     * @param certificatePath 证书路径
     * @param certificatePassword 证书密码
     * @param isTestEnvironment 是否为测试环境
     */
    public SiiConfig(String apiUrl, String boletaBaseUrl, String validateUrl, String certificatePath,
                     String certificatePassword, boolean isTestEnvironment) {
        this.apiUrl = apiUrl;
        this.boletaBaseUrl = boletaBaseUrl;
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
    public SiiConfig(String apiUrl,  String boletaBaseUrl, String validateUrl, InputStream certificate,
                           String certificatePassword, boolean isTestEnvironment) {
        this.apiUrl = apiUrl;
        this.boletaBaseUrl = boletaBaseUrl;
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
                String environment = properties.getProperty("sii.environment", "test");
                isTestEnvironment = "test".equalsIgnoreCase(environment);

                // 根据环境选择URL
                if (isTestEnvironment) {
                    apiUrl = properties.getProperty("sii.api.url.test");
                    boletaBaseUrl = properties.getProperty("sii.boleta.url.test");
                    validateUrl = properties.getProperty("sii.validate.url.test");
                    logger.info("已加载测试环境配置");
                } else {
                    apiUrl = properties.getProperty("sii.api.url.prod");
                    boletaBaseUrl = properties.getProperty("sii.boleta.url.prod");
                    validateUrl = properties.getProperty("sii.validate.url.prod");
                    logger.info("已加载生产环境配置");
                }

                // 获取证书路径并处理
                certificatePath = properties.getProperty("sii.certificate.path");

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

                certificatePassword = properties.getProperty("sii.certificate.password");

                logger.info("已加载sii配置");
                logger.info("证书路径: " + certificatePath);
            } else {
                logger.warning("未找到配置文件: " + CONFIG_FILE);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "加载配置文件失败", e);
        }
    }


    @Bean
    public RestTemplate taxDackTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 添加 JAXB 消息转换器
        restTemplate.setMessageConverters(Collections.singletonList(new Jaxb2RootElementHttpMessageConverter()));
        return restTemplate;
    }

    public boolean getIsTestEnvironment() {
        return this.isTestEnvironment;
    }

    public void setIsTestEnvironment(boolean testEnvironment) {
        this.isTestEnvironment = testEnvironment;
    }

    public Properties getProperties() {
        return this.properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getApiUrl() {
        return this.apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getValidateUrl() {
        return this.validateUrl;
    }

    public void setValidateUrl(String validateUrl) {
        this.validateUrl = validateUrl;
    }

    public String getCertificatePath() {
        return this.certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public InputStream getCertificate() {
        return this.certificate;
    }

    public void setCertificate(InputStream certificate) {
        this.certificate = certificate;
    }

    public String getCertificatePassword() {
        return this.certificatePassword;
    }

    public void setCertificatePassword(String certificatePassword) {
        this.certificatePassword = certificatePassword;
    }

    public String getBoletaBaseUrl() {
        return this.boletaBaseUrl;
    }

    public void setBoletaBaseUrl(String boletaBaseUrl) {
        this.boletaBaseUrl = boletaBaseUrl;
    }
}