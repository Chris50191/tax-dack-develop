package com.searly.taxcontrol.verifactu.utils;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * XML工具类
 * 提供XML解析和处理的实用方法
 */
public class XmlUtils {
    
    private static final Logger log = Logger.getLogger(XmlUtils.class.getName());
    
    /**
     * 自定义命名空间前缀映射器
     */
    private static class CustomNamespacePrefixMapper extends NamespacePrefixMapper {
        @Override
        public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
            if ("http://schemas.xmlsoap.org/soap/envelope/".equals(namespaceUri)) {
                return "SOAP-ENV";
            } else if ("http://www.w3.org/2001/XMLSchema".equals(namespaceUri)) {
                return "xsd";
            } else if ("http://www.w3.org/2001/XMLSchema-instance".equals(namespaceUri)) {
                return "xsi";
            } else if ("https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd".equals(namespaceUri)) {
                return "";  // 默认命名空间，无前缀
            } else if ("https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd".equals(namespaceUri)) {
                return "";  // 默认命名空间，无前缀
            } else if ("https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd".equals(namespaceUri)) {
                return "con";  // ConsultaLR命名空间使用con前缀
            }
            return suggestion;
        }

        @Override
        public String[] getPreDeclaredNamespaceUris() {
            return new String[] {
                "http://schemas.xmlsoap.org/soap/envelope/",
                "http://www.w3.org/2001/XMLSchema",
                "http://www.w3.org/2001/XMLSchema-instance",
                "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd",
                "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd",
                "https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd"
            };
        }
    }

    /**
     * 将对象序列化为XML字符串
     *
     * @param object 要序列化的对象
     * @return XML字符串
     * @throws JAXBException 如果序列化失败
     */
    public static String marshal(Object object) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = context.createMarshaller();
        
        // 设置输出格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        
        // 设置命名空间前缀映射器
        try {
            marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new CustomNamespacePrefixMapper());
        } catch (PropertyException e) {
            log.log(Level.WARNING, "命名空间前缀映射器设置失败", e);
        }

        StringWriter writer = new StringWriter();
        marshaller.marshal(object, writer);
        
        // 处理生成的XML，调整命名空间声明位置
        String xml = writer.toString();

        // 移除所有ns前缀
        xml = xml.replaceAll("ns\\d+:", "");

        // 移除根元素上的默认命名空间声明
        xml = xml.replaceAll("xmlns=\"https://www2\\.agenciatributaria\\.gob\\.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR\\.xsd\"", "");

        // 移除根元素上的SuministroInformacion命名空间声明
        xml = xml.replaceAll("xmlns:ns5=\"https://www2\\.agenciatributaria\\.gob\\.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion\\.xsd\"", "");
        
        // 移除根元素上的ConsultaLR命名空间声明
        xml = xml.replaceAll("xmlns:ns\\d+=\"https://www2\\.agenciatributaria\\.gob\\.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR\\.xsd\"", "");

        // 在RegFactuSistemaFacturacion元素上添加默认命名空间声明
        xml = xml.replace("<RegFactuSistemaFacturacion>",
                         "<RegFactuSistemaFacturacion xmlns=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroLR.xsd\">");
                         
        // 在ConsultaFactuSistemaFacturacion元素上添加默认命名空间声明
        xml = xml.replace("<ConsultaFactuSistemaFacturacion>",
                         "<ConsultaFactuSistemaFacturacion xmlns=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/ConsultaLR.xsd\">");

        // 在ObligadoEmision和RegistroAlta元素上添加默认命名空间声明
        xml = xml.replace("<ObligadoEmision>",
                         "<ObligadoEmision xmlns=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd\">");

        xml = xml.replace("<RegistroAlta>",
                         "<RegistroAlta xmlns=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd\">");
        xml = xml.replace("<RegistroAnulacion>",
                         "<RegistroAnulacion xmlns=\"https://www2.agenciatributaria.gob.es/static_files/common/internet/dep/aplicaciones/es/aeat/tike/cont/ws/SuministroInformacion.xsd\">");
        // 移除空属性
        xml = xml.replaceAll("<con:ClavePaginacion/>", "");
        xml = xml.replaceAll("<TipoRecargoEquivalencia/>", "");
        xml = xml.replaceAll("<CuotaRecargoEquivalencia/>", "");

        return xml;
    }
    
    /**
     * 将XML字符串转换为对象
     * 
     * @param <T> 对象类型
     * @param xml XML字符串
     * @param type 对象类
     * @return 转换后的对象
     * @throws JAXBException 如果反序列化失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(String xml, Class<T> type) throws JAXBException {
        if (xml == null || xml.trim().isEmpty()) {
            return null;
        }
        
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }
    
    /**
     * 将带命名空间的XML字符串转换为对象
     * 
     * @param <T> 对象类型
     * @param xml XML字符串
     * @param type 对象类
     * @return 转换后的对象
     * @throws JAXBException 如果反序列化失败
     */
    @SuppressWarnings("unchecked")
    public static <T> T unmarshalWithNamespace(String xml, Class<T> type) throws JAXBException {
        if (xml == null || xml.trim().isEmpty()) {
            return null;
        }
        
        JAXBContext context = JAXBContext.newInstance(type);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StreamSource(new StringReader(xml)));
    }
    
    /**
     * 解析XML字符串为Document对象
     * 
     * @param xmlString XML字符串
     * @return Document对象，解析失败则返回null
     */
    public static Document parseXmlString(String xmlString) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        } catch (Exception e) {
            log.log(Level.SEVERE, "XML解析失败", e);
            return null;
        }
    }
    
    /**
     * 查找具有指定标签名的元素
     * 
     * @param document Document对象
     * @param tagName 标签名
     * @return 找到的第一个元素，未找到则返回null
     */
    public static Element findElementByTagName(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes != null && nodes.getLength() > 0) {
            Node node = nodes.item(0);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                return (Element) node;
            }
        }
        return null;
    }
    
    /**
     * 查找具有指定标签名的所有元素
     * 
     * @param document Document对象
     * @param tagName 标签名
     * @return 元素数组
     */
    public static Element[] findElementsByTagName(Document document, String tagName) {
        NodeList nodes = document.getElementsByTagName(tagName);
        if (nodes != null && nodes.getLength() > 0) {
            Element[] elements = new Element[nodes.getLength()];
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    elements[i] = (Element) node;
                }
            }
            return elements;
        }
        return new Element[0];
    }
    
    /**
     * 从元素中获取文本内容
     * 
     * @param element 元素
     * @return 文本内容，未找到则返回空字符串
     */
    public static String getTextContent(Element element) {
        if (element != null) {
            return element.getTextContent();
        }
        return "";
    }
    
    /**
     * 从Document中查找指定标签名的元素，并获取其文本内容
     * 
     * @param document Document对象
     * @param tagName 标签名
     * @return 文本内容，未找到则返回空字符串
     */
    public static String getElementTextContent(Document document, String tagName) {
        Element element = findElementByTagName(document, tagName);
        return getTextContent(element);
    }
    
    /**
     * 查找具有指定XPath的元素
     * 注意：此简单实现不支持完整的XPath语法，仅支持简单的标签路径
     * 
     * @param document Document对象
     * @param xpath 简化的XPath表达式，如 "Envelope/Body/Response"
     * @return 找到的第一个元素，未找到则返回null
     */
    public static Element findElementBySimplePath(Document document, String xpath) {
        String[] paths = xpath.split("/");
        Element current = document.getDocumentElement();
        
        for (int i = 0; i < paths.length; i++) {
            if (i == 0 && paths[0].equals(current.getTagName())) {
                continue;
            }
            
            NodeList childNodes = current.getChildNodes();
            current = null;
            
            for (int j = 0; j < childNodes.getLength(); j++) {
                Node node = childNodes.item(j);
                if (node.getNodeType() == Node.ELEMENT_NODE && node.getNodeName().equals(paths[i])) {
                    current = (Element) node;
                    break;
                }
            }
            
            if (current == null) {
                return null;
            }
        }
        
        return current;
    }
    
    /**
     * 获取元素的属性值
     * 
     * @param element 元素
     * @param attributeName 属性名
     * @return 属性值，未找到则返回空字符串
     */
    public static String getAttributeValue(Element element, String attributeName) {
        if (element != null && element.hasAttribute(attributeName)) {
            return element.getAttribute(attributeName);
        }
        return "";
    }
} 