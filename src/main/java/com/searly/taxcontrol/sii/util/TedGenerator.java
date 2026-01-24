package com.searly.taxcontrol.sii.util;

import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

public class TedGenerator {

    /**
     * 在 Document 中插入 TED
     *
     * @param doc     已生成的 EnvioBOLETA Document
     * @param cafFile CAF.xml 文件路径
     */
    public static void insertTed(Document doc, InputStream cafFile) throws Exception {
        // 加载 CAF
        Document cafDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(cafFile);

        // === 从 CAF 解析私钥 ===
        // 检查 RSASK 是否存在
        NodeList rsaskNodes = cafDoc.getElementsByTagName("RSASK");
        PrivateKey privateKey;
        if (rsaskNodes.getLength() > 0) {
            String privateKeyPem = rsaskNodes.item(0).getTextContent();
            privateKey = loadPrivateKeyFromPem(privateKeyPem);
        } else {
            // 使用公司证书的私钥（需要从外部传入）
            throw new IllegalArgumentException("CAF文件中缺少RSASK，需要使用公司证书私钥");
        }

        // 获取 Documento
        Element documento = (Element) doc.getElementsByTagName("Documento").item(0);

        String ns = documento.getNamespaceURI();

        Element cafEl = (Element) cafDoc.getElementsByTagName("CAF").item(0);
        if (cafEl == null) {
            throw new IllegalArgumentException("CAF 文件缺少 CAF 节点");
        }
        // CAF 文件本身通常无命名空间，并包含 FRMA（SII 对 DA 的签名）。
        // 这里必须保持 CAF 的“无命名空间”语义，否则 SII 侧可能判 timbre 无效。
        // 将 CAF 作为无命名空间节点导入到 DD 下，序列化时会自动产生 xmlns="" 来取消父级默认命名空间。
        Node cafNode = doc.importNode(cafEl, true);

        // 从 documento 提取字段
        String RE = safeTrim(getText(documento, "RUTEmisor"));
        String TD = safeTrim(getText(documento, "TipoDTE"));
        String F = safeTrim(getText(documento, "Folio"));
        String FE = safeTrim(getText(documento, "FchEmis"));
        String RR = safeTrim(getText(documento, "RUTRecep"));
        String RSR = safeTrim(getText(documento, "RznSocRecep"));
        String MNT = safeTrim(getText(documento, "MntTotal"));
        
        // IT1 应该从第一个 Detalle 节点获取
        NodeList detalleList = documento.getElementsByTagName("Detalle");
        if (detalleList.getLength() == 0) {
            throw new IllegalArgumentException("发票必须至少包含一个商品项");
        }
        Element firstDetalle = (Element) detalleList.item(0);
        String IT1 = normalizeIt1(getText(firstDetalle, "NmbItem"));
        
        String ts = safeTrim(getText(documento, "TmstFirma"));

        Element TED = doc.createElementNS(ns, "TED");
        TED.setAttribute("version", "1.0");

        Element DD = doc.createElementNS(ns, "DD");
        appendText(doc, DD, ns, "RE", RE);
        appendText(doc, DD, ns, "TD", TD);
        appendText(doc, DD, ns, "F", F);
        appendText(doc, DD, ns, "FE", FE);
        appendText(doc, DD, ns, "RR", RR);
        appendText(doc, DD, ns, "RSR", RSR);
        appendText(doc, DD, ns, "MNT", MNT);
        appendText(doc, DD, ns, "IT1", IT1);

        DD.appendChild(cafNode);
        appendText(doc, DD, ns, "TSTED", ts);
        TED.appendChild(DD);

        Element FRMT = doc.createElementNS(ns, "FRMT");
        FRMT.setAttribute("algoritmo", "SHA1withRSA");
        TED.appendChild(FRMT);

        // 先插入到最终树中，确保 C14N 的命名空间上下文与最终发送一致
        Node tmstFirmaNode = documento.getElementsByTagName("TmstFirma").item(0);
        documento.insertBefore(TED, tmstFirmaNode);

        byte[] ddBytes = canonicalizeInclusiveIso88591Bytes(DD);
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(privateKey);
        sig.update(ddBytes);
        String frmtBase64 = Base64.getEncoder().encodeToString(sig.sign());
        FRMT.setTextContent(frmtBase64);
    }

    private static String normalizeIt1(String s) {
        if (s == null) return "";
        // SII 常见提示：可能存在“商品名称多余空格”导致 timbre 无效。
        // 这里折叠所有空白为单空格，并截断 40 字符。
        String v = s.replaceAll("\\s+", " ").trim();
        if (v.length() > 40) {
            v = v.substring(0, 40);
        }
        return v;
    }

    private static byte[] canonicalizeInclusiveIso88591Bytes(Element el) throws Exception {
        byte[] utf8 = canonicalizeInclusive(el);
        // Apache xmlsec 的 C14N 输出为 UTF-8 bytes。
        // 但 DTE 文档声明为 ISO-8859-1，SII 的 timbre 验证实现可能按文档编码取字节。
        // 为避免字节不一致，这里将 C14N 结果按 UTF-8 解码后，再以 ISO-8859-1 编码为签名输入。
        String s = new String(utf8, java.nio.charset.StandardCharsets.UTF_8);
        return s.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    private static PublicKey buildRsaPublicKey(String modulusB64, String exponentB64) throws Exception {
        if (modulusB64 == null || exponentB64 == null) {
            throw new IllegalArgumentException("CAF 公钥参数为空，无法构建公钥");
        }
        byte[] modBytes = Base64.getDecoder().decode(modulusB64.trim());
        byte[] expBytes = Base64.getDecoder().decode(exponentB64.trim());
        BigInteger modulus = new BigInteger(1, modBytes);
        BigInteger exponent = new BigInteger(1, expBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(new RSAPublicKeySpec(modulus, exponent));
    }

    /**
     * 使用 CAF 公钥验签 TED/FRMT（调试 505 问题）
     */
    public static void verifyTedSignature(Document doc, InputStream cafFile) throws Exception {
        CAFResolve.CafData cafData = CAFResolve.loadCaf(cafFile);
        PublicKey publicKey = buildRsaPublicKey(cafData.modulusB64, cafData.exponentB64);

        Element documento = (Element) doc.getElementsByTagName("Documento").item(0);
        if (documento == null) {
            throw new IllegalStateException("未找到 Documento 节点，无法验签 TED");
        }
        Element ted = (Element) documento.getElementsByTagName("TED").item(0);
        if (ted == null) {
            throw new IllegalStateException("未找到 TED 节点，无法验签 TED");
        }
        Element dd = (Element) ted.getElementsByTagName("DD").item(0);
        if (dd == null) {
            throw new IllegalStateException("TED 中缺少 DD 节点");
        }
        Element frmt = (Element) ted.getElementsByTagName("FRMT").item(0);
        if (frmt == null) {
            throw new IllegalStateException("TED 中缺少 FRMT 节点");
        }

        byte[] ddBytes = canonicalizeInclusiveIso88591Bytes(dd);
        byte[] signature = Base64.getDecoder().decode(frmt.getTextContent().trim());

        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initVerify(publicKey);
        sig.update(ddBytes);
        boolean ok = sig.verify(signature);
        System.out.println("TED FRMT 本地验签: " + (ok ? "通过" : "失败"));
        if (!ok) {
            throw new IllegalStateException("TED FRMT 本地验签失败，发送将被 SII 判 505");
        }
    }

    public static void recomputeTedFrmt(Document doc, InputStream cafFile) throws Exception {
        Document cafDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(cafFile);
        NodeList rsaskNodes = cafDoc.getElementsByTagName("RSASK");
        if (rsaskNodes.getLength() == 0) {
            throw new IllegalArgumentException("CAF文件中缺少RSASK，无法重算FRMT");
        }
        PrivateKey privateKey = loadPrivateKeyFromPem(rsaskNodes.item(0).getTextContent());

        Element documento = (Element) doc.getElementsByTagName("Documento").item(0);
        if (documento == null) {
            throw new IllegalStateException("未找到 Documento 节点，无法重算 TED FRMT");
        }
        Element ted = (Element) documento.getElementsByTagName("TED").item(0);
        if (ted == null) {
            throw new IllegalStateException("未找到 TED 节点，无法重算 TED FRMT");
        }
        Element dd = (Element) ted.getElementsByTagName("DD").item(0);
        if (dd == null) {
            throw new IllegalStateException("TED 中缺少 DD 节点，无法重算 TED FRMT");
        }
        Element frmt = (Element) ted.getElementsByTagName("FRMT").item(0);
        if (frmt == null) {
            throw new IllegalStateException("TED 中缺少 FRMT 节点，无法重算 TED FRMT");
        }

        byte[] ddBytes = canonicalizeInclusiveIso88591Bytes(dd);
        Signature sig = Signature.getInstance("SHA1withRSA");
        sig.initSign(privateKey);
        sig.update(ddBytes);
        String frmtBase64 = Base64.getEncoder().encodeToString(sig.sign());
        frmt.setTextContent(frmtBase64);
    }

    // 从 PEM 私钥解析
    private static PrivateKey loadPrivateKeyFromPem(String pem) throws Exception {
        String cleaned = pem.replace("\r", "").replace("\n", "\n").trim();
        if (cleaned.contains("BEGIN RSA PRIVATE KEY")) {
            // PKCS#1 -> 包一层 PKCS#8
            byte[] pkcs1 = base64Between(pem, "BEGIN RSA PRIVATE KEY", "END RSA PRIVATE KEY");
            byte[] pkcs8 = wrapPkcs1ToPkcs8(pkcs1);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        } else if (cleaned.contains("BEGIN PRIVATE KEY")) {
            byte[] pkcs8 = base64Between(pem, "BEGIN PRIVATE KEY", "END PRIVATE KEY");
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(new PKCS8EncodedKeySpec(pkcs8));
        } else {
            throw new IllegalArgumentException("不识别的私钥 PEM 格式");
        }
    }

    private static byte[] base64Between(String pem, String begin, String end) {
        String s = pem.replace("\r", "");
        int i1 = s.indexOf("-----" + begin + "-----");
        int i2 = s.indexOf("-----" + end + "-----");
        if (i1 < 0 || i2 < 0) throw new IllegalArgumentException("PEM 边界未找到: " + begin + "/" + end);
        String base64 = s.substring(i1 + ("-----" + begin + "-----").length(), i2).replace("\n", "").trim();
        return Base64.getDecoder().decode(base64);
    }

    /**
     * 把 PKCS#1 RSA 私钥包装成 PKCS#8（RSA OID 1.2.840.113549.1.1.1）
     */
    private static byte[] wrapPkcs1ToPkcs8(byte[] pkcs1) {
        // 组装一个最小的 PKCS#8：Sequence(algId, OCTET STRING(pkcs1))
        // ASN.1 粗暴拼装（可用 BouncyCastle 更省心，这里为避免额外依赖）
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            // 覆盖长度时用 DER 简单编码
            // SEQ
            out.write(0x30);
            ByteArrayOutputStream body = new ByteArrayOutputStream();
            // version: INTEGER 0
            body.write(new byte[]{0x02, 0x01, 0x00});
            // algId: SEQ { OID rsaEncryption, NULL }
            ByteArrayOutputStream alg = new ByteArrayOutputStream();
            alg.write(0x30);
            ByteArrayOutputStream algBody = new ByteArrayOutputStream();
            // OID 1.2.840.113549.1.1.1
            algBody.write(new byte[]{0x06, 0x09,
                    0x2A, (byte) 0x86, 0x48, (byte) 0x86, (byte) 0xF7, 0x0D, 0x01, 0x01, 0x01});
            // NULL
            algBody.write(new byte[]{0x05, 0x00});
            writeLen(alg, algBody.size());
            alg.write(algBody.toByteArray());
            body.write(alg.toByteArray());
            // OCTET STRING (pkcs1)
            body.write(0x04);
            writeLen(body, pkcs1.length);
            body.write(pkcs1);
            // 写总长度
            writeLen(out, body.size());
            out.write(body.toByteArray());
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeLen(OutputStream os, int len) throws IOException {
        if (len < 128) {
            os.write(len);
        } else if (len < 256) {
            os.write(0x81);
            os.write(len);
        } else if (len < 65536) {
            os.write(0x82);
            os.write((len >> 8) & 0xFF);
            os.write(len & 0xFF);
        } else {
            throw new IllegalArgumentException("长度过大");
        }
    }

    // 获取标签文本
    private static String getText(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            throw new IllegalArgumentException("缺少必需的TED字段: " + tag);
        }
        return nodes.item(0).getTextContent();
    }

    private static String safeTrim(String v) {
        return v == null ? null : v.trim();
    }

    // 添加子元素
    private static void appendText(Document doc, Element parent, String ns, String tag, String value) {
        Element e = doc.createElementNS(ns, tag);
        e.setTextContent(value);
        parent.appendChild(e);
    }

    // Inclusive C14N
    private static byte[] canonicalizeInclusive(Node node) throws Exception {
        Init.init();
        Canonicalizer canon = Canonicalizer.getInstance(Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        canon.canonicalizeSubtree(node, baos);
        return baos.toByteArray();
    }
}
