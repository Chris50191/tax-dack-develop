package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.model.common.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 发票XML生成器
 * 基于智利SII的发票标准生成电子发票XML
 */
public class InvoiceGenerator {

    private static final String PREFIX = "BOLETA_ENVIO_";
    // 日期格式化模板（SII 要求的 YYYYMMDD）
    private static final DateTimeFormatter DATE_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    // 智利默认时区（圣地亚哥，支持夏令时自动切换）
    private static final ZoneId CHILE_DEFAULT_ZONE = ZoneId.of("America/Santiago");

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * 生成发票XML
     *
     * @param invoiceData 发票数据
     * @return 生成的XML字符串
     * @throws JAXBException 如果XML生成失败
     */
    public String generateInvoiceXML(InvoiceData invoiceData, KeyStore ks, String pfxPassword, InputStream cafFile) throws Exception {
        // 验证发票数据中的关键字段
        if (invoiceData.getRutEnvia() == null || invoiceData.getRutEnvia().trim().isEmpty()) {
            throw new IllegalArgumentException("发票数据中的 rutEnvia 不能为空");
        }
        if (invoiceData.getRutEmisor() == null || invoiceData.getRutEmisor().trim().isEmpty()) {
            throw new IllegalArgumentException("发票数据中的 rutEmisor 不能为空");
        }

        //1. 创建发票对象
        EnvioBOLETA envioBOLETA = createEnvioBOLETA(invoiceData);

        // 使用JAXB生成XML
        JAXBContext context = JAXBContext.newInstance(EnvioBOLETA.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false); // 必须关闭，由后续逻辑手动换行
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

        StringWriter writer = new StringWriter();
        marshaller.marshal(envioBOLETA, writer);

        // 【关键】统一换行符并转为 DOM
        String xmlContent = writer.toString().replace("\r\n", "\n").replace("\n", "").trim();
        // 2. String 转 DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.ISO_8859_1)));
        
        // 在 DOM 中手动注入换行符，确保这些换行符作为“内容”参与签名计算，从而稳定摘要
//        addNewlineNodes(doc);

        // 在根节点显式声明 ds 命名空间
        doc.getDocumentElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", "http://www.w3.org/2000/09/xmldsig#");
        
        // 生成文件前缀（使用Folio和时间戳）
        String filePrefix = generateFilePrefix(invoiceData);

        // 保存步骤1：初始XML（插入TED前）
        saveXmlDocument(doc, filePrefix + "_01_初始XML_插入TED前.xml");

        //3. 生成TED
        TedGenerator.insertTed(doc, cafFile);

        // 保存步骤2：插入TED后的XML
        saveXmlDocument(doc, filePrefix + "_02_插入TED后.xml");

        doc = normalizeDocument(doc);

        String alias = ks.aliases().nextElement();
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, pfxPassword.toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

        // ======== 内部签名（签 Documento 节点） ========
        signXmlForSii(doc, "Documento", privateKey, cert);

        // 保存步骤3：内部签名后的XML
        saveXmlDocument(doc, filePrefix + "_03_内部签名Documento后.xml");

        NodeList dteSignatures = doc.getElementsByTagName("DTE").item(0).getChildNodes();
        for(int i=0; i<dteSignatures.getLength(); i++) {
            Node n = dteSignatures.item(i);
            if(n.getNodeName().equals("Signature") || n.getNodeName().endsWith(":Signature")) {
                formatSignatureNode((Element) n);
                break; // 假设 DTE 下只有一个签名
            }
        }

        // ======== 外部签名（签 SetDTE 节点） ========
        // Schema要求EnvioBOLETA在SetDTE后有ds:Signature
        signXmlForSiiOut(doc, "SetDTE", privateKey, cert);


        // 保存步骤4：外部签名后的XML
        saveXmlDocument(doc, filePrefix + "_04_外部签名SetDTE后_最终XML.xml");

        // 自动验签（本地诊断，签名失败时输出详细原因）
        validateSignatures(doc);

        // 5. 输出最终 XML
        // 注意：由于已移除DOM中的空白节点，使用INDENT="no"不会产生额外空白
        // 但如果完全压缩为一尼，长Base64内容可能超过SII的行长度限制（4090字符）
        // 因此需要在输出后对长内容进行适当的换行处理
        String finalXml = serializeDocument(doc);

        if (!finalXml.startsWith("<?xml")) {
            finalXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" + finalXml; // 注意这里加了一个换行，SII通常接受
        }

        // 再次确保没有 &#13; 这种脏数据混入 (双重保险)
        finalXml = finalXml.replace("&#13;", "").replace("&#10;", "");
        // 签名完成后不再插入任何换行，避免摘要不一致

        if (!finalXml.startsWith("<?xml")) {
            // XML声明后添加换行符，确保符合SII Schema要求
            finalXml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" + finalXml;
        }

        // 严禁在此处进行任何 wrapLongBase64Lines 操作，因为换行符已在签名前被算入摘要
        // finalXml = wrapLongBase64Lines(finalXml);

        // 保存最终发送XML，方便与Schema逐行比对
        saveXmlString(finalXml, filePrefix + "_05_最终XML_发送.xml");
        validateAgainstBoletaSchema(finalXml);
        return finalXml;
    }

    private void validateAgainstBoletaSchema(String xml) {
        try {
            Path schemaPath = Paths.get("output", "schema_envio_bol_720", "EnvioBOLETA_v11.xsd");
            if (!Files.exists(schemaPath)) {
                return;
            }
            String schemaContent = new String(Files.readAllBytes(schemaPath), StandardCharsets.ISO_8859_1);
            // 修正常见的PctType最小值冲突，避免Schema解析失败（仅用于本地诊断）
            schemaContent = schemaContent.replace("<xs:minInclusive value=\"0.01\"/>",
                                                 "<xs:minInclusive value=\"0.00\"/>");

            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            javax.xml.transform.stream.StreamSource schemaSource =
                new javax.xml.transform.stream.StreamSource(new StringReader(schemaContent));
            schemaSource.setSystemId(schemaPath.toUri().toString());
            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(new javax.xml.transform.stream.StreamSource(new StringReader(xml)));
        } catch (Exception e) {
            System.err.println("本地XSD校验失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void saveXmlString(String xml, String fileName) {
        try {
            Path outputDir = Paths.get("output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            Path filePath = outputDir.resolve(fileName);
            Files.write(filePath, xml.getBytes(StandardCharsets.ISO_8859_1));
            System.out.println("已保存最终XML文件: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("保存最终XML文件失败 [" + fileName + "]: " + e.getMessage());
        }
    }


    /**
     * 修复XML中的命名空间问题
     *
     * @param xmlContent 原始XML内容
     * @return 修复后的XML内容
     */
    private String fixNamespaceIssues(String xmlContent) {
        // 替换ns2和ns3前缀为ds前缀
//    xmlContent = xmlContent.replaceAll("ns2:", "ds:");
//    xmlContent = xmlContent.replaceAll("ns3:", "");
//
//    // 移除多余的命名空间声明
//    xmlContent = xmlContent.replaceAll("xmlns:ns2=\"[^\"]*\"", "");
//    xmlContent = xmlContent.replaceAll("xmlns:ds=\"[^\"]*\"", "");
//    xmlContent = xmlContent.replaceAll("xmlns:ns3=\"[^\"]*\"", "");

        xmlContent = xmlContent.replaceAll("<ns2:EnvioBOLETA ", "<EnvioBOLETA ");
        xmlContent = xmlContent.replaceAll("</ns2:EnvioBOLETA>", "</EnvioBOLETA>");

        // 将ds:Signature替换为Signature xmlns="http://www.w3.org/2000/09/xmldsig#"
//    xmlContent = xmlContent.replaceAll("<ds:Signature>", "<Signature xmlns=\"http://www.w3.org/2000/09/xmldsig#\">");
//    xmlContent = xmlContent.replaceAll("</ds:Signature>", "</Signature>");
//    // 去掉换行符
//    xmlContent = xmlContent.replace("&#13;", "");


        return xmlContent;
    }

    /**
     * 创建EnvioBOLETA对象
     */
    public EnvioBOLETA createEnvioBOLETA(InvoiceData invoiceData) {
        // 创建SetDTE
        SetDTE setDTE = createSetDTE(invoiceData);

        // 创建Signature
//        Signature signature = createSignature(invoiceData);

        return new EnvioBOLETA(setDTE);
    }

    /**
     * 创建SetDTE对象
     */
    private SetDTE createSetDTE(InvoiceData invoiceData) {
        // 创建Caratula
        Caratula caratula = createCaratula(invoiceData);

        // 创建DTE列表
        List<DTE> dteList = new ArrayList<>();
        DTE dte = createDTE(invoiceData);
        dteList.add(dte);

        return new SetDTE(caratula, dteList, generateSetDteIdWithChileTime(invoiceData.getRutEmisor()));
    }

    /**
     * 创建Caratula对象
     */
    private Caratula createCaratula(InvoiceData invoiceData) {
        // 验证关键字段
        String rutEmisor = invoiceData.getRutEmisor();
        String rutEnvia = invoiceData.getRutEnvia();

        if (rutEnvia == null || rutEnvia.trim().isEmpty()) {
            throw new IllegalArgumentException("发票数据中的 rutEnvia 不能为空");
        }

        if (rutEmisor == null || rutEmisor.trim().isEmpty()) {
            throw new IllegalArgumentException("发票数据中的 rutEmisor 不能为空");
        }

        // 创建SubTotDTE
        List<SubTotDTE> subTotDTEList = new ArrayList<>();
        SubTotDTE subTotDTE = new SubTotDTE(invoiceData.getTipoDTE(), 1);
        subTotDTEList.add(subTotDTE);

        Caratula caratula = new Caratula(
                rutEmisor,
                rutEnvia,
                invoiceData.getRutReceptor(),
                invoiceData.getFchResol(),
                invoiceData.getNroResol(),
                invoiceData.getTmstFirmaEnv(),
                subTotDTEList
        );

        // 验证Caratula中的值
        if (!rutEnvia.equals(caratula.getRutEnvia())) {
            throw new IllegalStateException(
                    String.format("Caratula中的rutEnvia设置失败！期望: %s, 实际: %s",
                            rutEnvia, caratula.getRutEnvia()));
        }

        return caratula;
    }

    /**
     * 创建DTE对象
     */
    private DTE createDTE(InvoiceData invoiceData) {
        // 创建Documento
        Documento documento = createDocumento(invoiceData);

        // 创建Signature
//        Signature signature = createDocumentSignature(invoiceData);

        return new DTE(documento);
    }

    /**
     * 创建Documento对象
     */
    private Documento createDocumento(InvoiceData invoiceData) {
        // 创建Encabezado
        Encabezado encabezado = createEncabezado(invoiceData);

        // 创建Detalle
        List<Detalle> detalleList = new ArrayList<>();
        for (int i = 0; i < invoiceData.getProducts().size(); i++) {
            InvoiceData.Product product = invoiceData.getProducts().get(i);
            Detalle detalle = new Detalle(i + 1, product.getNmbItem(),
                    product.getQtyItem(), product.getPrcItem(),
                    product.getMontoItem());
            detalleList.add(detalle);
        }

        // 创建referencia
        List<Referencia> referencias = new ArrayList<>();
        if (invoiceData.getIsReferences() !=null && !invoiceData.getIsReferences().isEmpty()){
            for (int i = 0; i < invoiceData.getIsReferences().size(); i++) {
                InvoiceData.Reference reference = invoiceData.getIsReferences().get(i);
                Referencia referencia = new Referencia(i + 1, reference.getType(),
                        reference.getBillId(), reference.getInvoiceDate(),
                        reference.getReasonType(), reference.getResson());
                referencias.add(referencia);
            }
        }

        Documento documento;
        if (referencias.isEmpty()) {
            documento = new Documento(encabezado, detalleList, invoiceData.getTmstFirma(), invoiceData.getDocumentId());
        } else {
            documento = new Documento(encabezado, detalleList, referencias, invoiceData.getTmstFirma(), invoiceData.getDocumentId());
        }
        return documento;
    }

    /**
     * 创建Encabezado对象
     */
    private Encabezado createEncabezado(InvoiceData invoiceData) {
        // 创建IdDoc
        IdDoc idDoc = new IdDoc(invoiceData.getTipoDTE(), invoiceData.getFolio(),
                invoiceData.getFchEmis(), invoiceData.getIndServicio());

        // 创建Emisor
        Emisor emisor = new Emisor(invoiceData.getRutEmisor(), invoiceData.getRznSocEmisor(),
                invoiceData.getGiroEmisor(), invoiceData.getDirOrigen(),
                invoiceData.getCmnaOrigen(), invoiceData.getCiudadOrigen());

        // 创建Receptor
        Receptor receptor = new Receptor(invoiceData.getRutReceptor(), invoiceData.getRznSocReceptor(),
                invoiceData.getDirRecep(), invoiceData.getCmnaRecep(),
                invoiceData.getCiudadRecep());

        // 创建Totales
        Totales totales = new Totales(invoiceData.getMntNeto(), invoiceData.getIva(), invoiceData.getMntAdic(),
                invoiceData.getMntTotal());

        return new Encabezado(idDoc, emisor, receptor, totales);
    }

    /**
     * 生成当前时间戳
     */
    public String generateCurrentTimestamp() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 生成当前日期
     */
    public String generateCurrentDate() {
        return LocalDateTime.now().format(DATE_FORMATTER);
    }


    /**
     * 对指定元素进行 XMLDSIG 签名 (SII 适配)
     * @param doc       DOM 文档
     * @param tagName   要签名的元素标签 (Documento / SetDTE)
     * @param privateKey 私钥
     * @param cert      X509证书
     */
    private static void signXmlForSii(Document doc, String tagName,
                                      PrivateKey privateKey, X509Certificate cert) throws Exception {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // 1. 获取目标元素
        Element target = (Element) doc.getElementsByTagName(tagName).item(0);
        if (target == null) {
            throw new IllegalArgumentException("未找到目标元素: " + tagName);
        }

        // 2. 确保 ID 属性被标记为 XmlID（用于XML签名验证）
        // 注意：在SII的XML中，ID属性通常在元素的默认命名空间中（不在命名空间URI中）
        // 即使元素本身有命名空间，ID属性通常也不在命名空间中
        String idValue = target.getAttribute("ID");
        if (idValue == null || idValue.isEmpty()) {
            throw new IllegalArgumentException(tagName + " 缺少 ID 属性");
        }

        // 设置ID属性为XmlID（必须，用于Reference URI引用）
        // 使用不带命名空间的版本，因为ID属性通常不在命名空间中
        target.setIdAttribute("ID", true);

        // 3. 内层签名（Documento）不使用 Transform
        // 因为 Signature 节点是 Documento 的平级兄弟节点，使用 Enveloped 会导致摘要计算异常
        List<Transform> transforms = Collections.emptyList();

        Reference ref = fac.newReference(
                "#" + idValue,
                fac.newDigestMethod(DigestMethod.SHA1, null),
                transforms,
                null,
                null
        );

        // 4. 使用 Inclusive C14N + RSA_SHA1
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref)
        );

        // 5. 构建 KeyInfo (必须包含证书)
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(cert.getPublicKey());
        X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kv, x509Data));

        // 6. 签名 (放在目标元素后面)
        DOMSignContext signContext = new DOMSignContext(privateKey, target.getParentNode());
        signContext.setDefaultNamespacePrefix(""); // 去掉 ds 前缀
        signContext.setNextSibling(target.getNextSibling());
        
        // 强制不换行，保持摘要稳定
        signContext.setProperty("org.jcp.xml.dsig.internal.dom.SignatureValue.wrap", Boolean.FALSE);

        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(signContext);
    }
    /**
     * 对指定元素进行 XMLDSIG 签名 (SII 适配)
     * @param doc       DOM 文档
     * @param tagName   要签名的元素标签 (Documento / SetDTE)
     * @param privateKey 私钥
     * @param cert      X509证书
     */
    private static void signXmlForSiiOut(Document doc, String tagName,
                                         PrivateKey privateKey, X509Certificate cert) throws Exception {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");

        // 1. 获取目标元素
        Element target = (Element) doc.getElementsByTagName(tagName).item(0);
        if (target == null) {
            throw new IllegalArgumentException("未找到目标元素: " + tagName);
        }

        // 2. 确保 ID 属性被标记为 XmlID（用于XML签名验证）
        // 注意：在SII的XML中，ID属性通常在元素的默认命名空间中（不在命名空间URI中）
        // 即使元素本身有命名空间，ID属性通常也不在命名空间中
        String idValue = target.getAttribute("ID");
        if (idValue == null || idValue.isEmpty()) {
            throw new IllegalArgumentException(tagName + " 缺少 ID 属性");
        }

        // 设置ID属性为XmlID（必须，用于Reference URI引用）
        // 使用不带命名空间的版本，因为ID属性通常不在命名空间中
        target.setIdAttribute("ID", true);

        // 3. 外层签名不使用 enveloped transform（签名节点不在 SetDTE 内）
        List<Transform> transforms = Collections.emptyList();

        Reference ref = fac.newReference(
                "#" + idValue,
                fac.newDigestMethod(DigestMethod.SHA1, null),
                transforms,
                null,
                null
        );

        // 4. 使用 Inclusive C14N + RSA_SHA1
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref)
        );

        // 5. 构建 KeyInfo (必须包含证书)
        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(cert.getPublicKey());
        X509Data x509Data = kif.newX509Data(Collections.singletonList(cert));
        KeyInfo ki = kif.newKeyInfo(Arrays.asList(kv, x509Data));

        // 6. 签名直接放在 EnvioBOLETA 下（紧跟 SetDTE）
        DOMSignContext signContext = new DOMSignContext(privateKey, target.getParentNode());
        // 使用默认命名空间生成签名（无 ds: 前缀）
        signContext.setDefaultNamespacePrefix("");
        signContext.setNextSibling(target.getNextSibling());

        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(signContext);
    }

    /**
     * 移除DOM中的空白文本节点
     * 这些空白节点会导致Schema验证失败（"extra data at end of complex element"）
     */
    private static void removeWhitespaceNodes(Document doc) {
        removeWhitespaceNodesRecursive(doc.getDocumentElement());
    }

    /**
     * 递归移除空白文本节点
     */
    private static void removeWhitespaceNodesRecursive(Node node) {
        if (node == null) {
            return;
        }

        NodeList children = node.getChildNodes();
        // 从后往前遍历，避免删除时索引变化的问题
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                // 如果文本节点只包含空白字符，则删除
                if (text != null && text.trim().isEmpty()) {
                    node.removeChild(child);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                // 递归处理子元素
                removeWhitespaceNodesRecursive(child);
            }
        }
    }

    /**
     * 去掉证书、模数和签名值的换行，避免校验失败
     * 包括：Modulus、X509Certificate、SignatureValue中的空白字符
     */
    private static void cleanBase64Text(Document doc) {
        // 清理所有Base64编码的标签内容（去掉所有空白字符，包括换行符）
        String[] tags = {"Modulus", "X509Certificate", "SignatureValue"};
        for (String tag : tags) {
            NodeList nl = doc.getElementsByTagNameNS("*", tag);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n != null && n.getTextContent() != null) {
                    // 清理所有空白字符，包括空格、换行符、回车符等
                    // 这确保Base64值是连续的，没有换行符
                    String cleaned = n.getTextContent().replaceAll("\\s+", "");
                    n.setTextContent(cleaned);
                }
            }
        }
    }

    /**
     * 自动验签（签名1 + 签名2）
     * 仅用于本地诊断，不修改XML内容
     */
    private static void validateSignatures(Document doc) {
        try {
            markAllIdAttributes(doc);
            NodeList signatures = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (signatures.getLength() == 0) {
                System.out.println("未找到任何 Signature 节点，跳过验签。");
                return;
            }
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            for (int i = 0; i < signatures.getLength(); i++) {
                Node sigNode = signatures.item(i);
                DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(), sigNode);
                XMLSignature signature = fac.unmarshalXMLSignature(valContext);
                boolean coreValid = signature.validate(valContext);
                System.out.println("自动验签结果[签名" + (i + 1) + "] = " + (coreValid ? "有效" : "无效"));
                if (!coreValid) {
                    boolean sv = signature.getSignatureValue().validate(valContext);
                    System.out.println("  SignatureValue 验证: " + (sv ? "有效" : "无效"));
                    @SuppressWarnings("unchecked")
                    List<Reference> refs = signature.getSignedInfo().getReferences();
                    for (int j = 0; j < refs.size(); j++) {
                        Reference ref = refs.get(j);
                        boolean refValid = ref.validate(valContext);
                        System.out.println("  Reference[" + j + "] URI=" + ref.getURI() + " 验证: " + (refValid ? "有效" : "无效"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("自动验签失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 把所有带 ID 的节点标记为 XML ID，保证验签能定位 Reference URI
     */
    private static void markAllIdAttributes(Document doc) {
        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Node node = all.item(i);
            if (node instanceof Element) {
                Element el = (Element) node;
                if (el.hasAttribute("ID")) {
                    el.setIdAttribute("ID", true);
                }
            }
        }
    }

    /**
     * 从 KeyInfo 中读取公钥（X509 或 KeyValue）
     */
    private static class X509KeySelector extends KeySelector {
        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context)
                throws KeySelectorException {
            if (keyInfo == null) {
                throw new KeySelectorException("KeyInfo 为空");
            }
            for (Object info : keyInfo.getContent()) {
                if (info instanceof X509Data) {
                    X509Data x509Data = (X509Data) info;
                    for (Object data : x509Data.getContent()) {
                        if (data instanceof X509Certificate) {
                            X509Certificate cert = (X509Certificate) data;
                            return () -> cert.getPublicKey();
                        }
                    }
                } else if (info instanceof KeyValue) {
                    try {
                        KeyValue kv = (KeyValue) info;
                        PublicKey publicKey = kv.getPublicKey();
                        return () -> publicKey;
                    } catch (KeyException e) {
                        throw new KeySelectorException(e);
                    }
                }
            }
            throw new KeySelectorException("未找到可用公钥");
        }
    }

    private static void cleanSignatureNodes(Document doc) {
        // 需要清理的标签列表
        String[] tags = {"SignatureValue", "Modulus", "Exponent", "X509Certificate"};

        for (String tag : tags) {
            // 注意：签名可能在默认命名空间或无命名空间，建议都查一下
            NodeList nl = doc.getElementsByTagName(tag); // 获取所有匹配标签

            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n != null) {
                    String content = n.getTextContent();
                    if (content != null) {
                        // 移除所有空白字符：空格、换行(\n)、回车(\r)、制表符(\t)
                        String cleaned = content.replaceAll("\\s+", "");
                        n.setTextContent(cleaned);
                    }
                }
            }
        }
    }

    /**
     * 清理XML字符串中的SignatureValue换行符和XML实体
     * 用于处理Transformer序列化时可能自动添加的换行符
     *
     * @param xml XML字符串
     * @return 清理后的XML字符串
     */
    private static String cleanSignatureValueInXml(String xml) {
        // 使用正则表达式匹配SignatureValue标签内容，清理其中的换行符和XML实体
        // 使用DOTALL模式以匹配跨行的内容
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "(<SignatureValue[^>]*>)(.*?)(</SignatureValue>)",
                java.util.regex.Pattern.DOTALL
        );

        java.util.regex.Matcher matcher = pattern.matcher(xml);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String startTag = matcher.group(1);
            String content = matcher.group(2);
            String endTag = matcher.group(3);

            // 清理内容中的：
            // 1. XML实体编码的换行符（&#13;、&#10;等）- 先处理实体编码
            // 2. 所有空白字符（空格、换行、回车、制表符等）
            String cleaned = content
                    .replace("&#13;", "")      // 移除XML实体编码的回车符
                    .replace("&#10;", "")      // 移除XML实体编码的换行符
                    .replace("&#xD;", "")      // 移除十六进制编码的回车符
                    .replace("&#xA;", "")      // 移除十六进制编码的换行符
                    .replace("\r", "")         // 移除实际的回车符
                    .replace("\n", "")         // 移除实际的换行符
                    .replace("\t", "")         // 移除制表符
                    .replace(" ", "");         // 移除空格（如果有）

            matcher.appendReplacement(result, startTag + cleaned + endTag);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 对长内容进行换行处理，避免单行过长（SII限制4090字符）
     * 仅在特定标签边界添加换行，避开 Documento 内部
     */
    private static void addNewlineNodes(Document doc) {
        String[] tags = {"SetDTE", "DTE", "Signature", "Caratula"};
        for (String tag : tags) {
            NodeList nl = doc.getElementsByTagNameNS("*", tag);
            for (int i = 0; i < nl.getLength(); i++) {
                Node n = nl.item(i);
                if (n.getParentNode() != null) {
                    n.getParentNode().insertBefore(doc.createTextNode("\n"), n);
                }
            }
        }
    }

    private static String wrapLongBase64Lines(String xml) {
        return xml;
    }

    /**
     * 生成 SetDTE ID（使用当前智利当地时间作为日期）
     * @param rut 开票方 RUT（格式：XXXXX-XX，如 "78065438-4"）
     * @return 符合 SII 标准的 SetDTE ID
     * @throws IllegalArgumentException 入参非法时抛出异常
     */
    public static String generateSetDteIdWithChileTime(String rut) {
        // 1. 校验 RUT 合法性
        validateRut(rut);
        // 2. 获取当前智利当地日期（自动适配夏令时/冬令时）
        LocalDate chileCurrentDate = LocalDate.now(CHILE_DEFAULT_ZONE);
        // 3. 格式化日期并拼接 ID
        String formattedDate = chileCurrentDate.format(DATE_ID_FORMATTER);
        return String.format("%s%s_%s", PREFIX, rut, formattedDate);
    }

    private static void validateRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) {
            throw new IllegalArgumentException("RUT 不能为空");
        }
        // 智利 RUT 正则表达式（严格校验格式）
        String rutRegex = "^\\d{7,8}-[0-9Kk]$";
        if (!rut.matches(rutRegex)) {
            throw new IllegalArgumentException("RUT 格式非法，正确格式示例：78065438-4 或 1234567-K");
        }
    }

    /**
     * 生成文件前缀（用于保存XML文件）
     * 格式：Folio_时间戳 或 时间戳
     *
     * @param invoiceData 发票数据
     * @return 文件前缀字符串
     */
    private static String generateFilePrefix(InvoiceData invoiceData) {
        String folio = invoiceData.getFolio() != null ? invoiceData.getFolio() : "unknown";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("invoice_%s_%s", folio, timestamp);
    }

    /**
     * 将DOM文档保存为XML文件
     *
     * @param doc  DOM文档
     * @param fileName 文件名（包含路径）
     */
    private static void saveXmlDocument(Document doc, String fileName) {
        try {
            // 确保output目录存在
            Path outputDir = Paths.get("output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }

            // 构建完整文件路径
            Path filePath = outputDir.resolve(fileName);

            // 【关键】必须使用与最终输出完全一致的序列化方式，确保摘要稳定
            String xmlContent = serializeDocument(doc);

            // 确保XML声明存在
            if (!xmlContent.trim().startsWith("<?xml")) {
                xmlContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" + xmlContent;
            }

            // 保存到文件
            Files.write(filePath, xmlContent.getBytes(StandardCharsets.ISO_8859_1));
            System.out.println("已保存同步XML文件: " + filePath.toAbsolutePath());
        } catch (Exception e) {
            System.err.println("保存XML文件失败 [" + fileName + "]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String serializeDocument(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();

        // 严格关闭缩进
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");

        // 我们手动拼接 Header，所以这里省略
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        // trim() 去掉首尾空白，replace 确保没有多余的空行干扰
        return writer.toString().trim();
    }

    /**
     * 对长节点进行规范折行处理（解决 CHR-00002 Line too long）
     * 仅针对 SignatureValue, Modulus, X509Certificate 进行处理
     */
    /**
     * 仅对指定的 Signature 节点下的 Base64 内容进行折行处理
     */
    private static void formatSignatureNode(Element signatureNode) {
        String[] tags = {"SignatureValue", "Modulus", "X509Certificate"};
        for (String tag : tags) {
            NodeList nl = signatureNode.getElementsByTagName(tag);
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node != null && node.getTextContent() != null) {
                    // 清理 + 折行
                    String cleanText = node.getTextContent().replaceAll("\\s+", "");
                    String formattedText = insertNewlines(cleanText, 76);
                    node.setTextContent(formattedText);
                }
            }
        }
    }

    /**
     * 辅助方法：每隔 length 个字符插入一个换行符
     */
    private static String insertNewlines(String text, int length) {
        if (text == null || text.length() <= length) {
            return text;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i += length) {
            // 如果不是第一行，先加换行符
            if (i > 0) {
                sb.append("\n");
            }
            // 截取片段
            int end = Math.min(i + length, text.length());
            sb.append(text.substring(i, end));
        }
        return sb.toString();
    }

    /**
     * DOM 归一化：将 Document 序列化为字节数组再重新解析。
     * 作用：消除 DOM 在内存构建时与最终文件输出时的结构差异（如命名空间、空白符）。
     * 确保签名时看到的结构就是最终文件的结构。
     */
    private static Document normalizeDocument(Document doc) throws Exception {
        // 1. 使用与最终输出相同的配置进行序列化
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(baos));
        byte[] xmlBytes = baos.toByteArray();

        // 2. 重新解析为 DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true); // 必须开启命名空间感知
        // 忽略空白字符，确保结构紧凑
        dbf.setIgnoringElementContentWhitespace(true);

        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
    }
}
