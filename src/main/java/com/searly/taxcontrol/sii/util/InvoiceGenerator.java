package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.model.common.*;
import com.chilkatsoft.CkByteData;
import com.chilkatsoft.CkCert;
import com.chilkatsoft.CkGlobal;
import com.chilkatsoft.CkPrivateKey;
import com.chilkatsoft.CkStringBuilder;
import com.chilkatsoft.CkXmlDSigGen;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyException;
import java.security.KeyStore;
import java.security.MessageDigest;
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
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateFactory;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.util.encoders.Hex;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.apache.xml.security.Init;
import org.apache.xml.security.utils.Constants;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.x509.AccessDescription;
import org.bouncycastle.asn1.x509.AuthorityInformationAccess;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509ObjectIdentifiers;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * 发票XML生成器 (JDK 11 Optimized)
 * 修复：SII 505 (签名错误) & CHR-00002 (行过长)
 * 策略：统一序列化出口 + 精准清洗 (\r) + 原生签名折行
 */
public class InvoiceGenerator {

    private static final String PREFIX = "BOLETA_ENVIO_";
    private static final DateTimeFormatter DATE_ID_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final ZoneId CHILE_DEFAULT_ZONE = ZoneId.of("America/Santiago");

    private static volatile Path LAST_SAVED_XML_PATH;

    private static volatile boolean CHILKAT_LOADED;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public String generateInvoiceXML(InvoiceData invoiceData, KeyStore ks, String pfxPassword, InputStream cafFile) throws Exception {
        return generateInvoiceXML(invoiceData, ks, pfxPassword, cafFile, null, null);
    }

    private static void ensureChilkatLoaded() {
        if (CHILKAT_LOADED) return;
        synchronized (InvoiceGenerator.class) {
            if (CHILKAT_LOADED) return;
            try {
                System.loadLibrary("chilkat");
            } catch (UnsatisfiedLinkError e) {
                String dllPathProp = System.getProperty("chilkat.dllPath", "");
                Path dllPath;
                if (dllPathProp != null && !dllPathProp.trim().isEmpty()) {
                    dllPath = Path.of(dllPathProp.trim());
                } else {
                    dllPath = Path.of("chilkat-jdk11-x64", "chilkat.dll");
                }
                System.load(dllPath.toAbsolutePath().toString());
            }

            CkGlobal glob = new CkGlobal();
            String unlock = System.getProperty("chilkat.unlock", "Start my 30-day Trial");
            boolean okUnlock = glob.UnlockBundle(unlock);
            if (!okUnlock) {
                throw new IllegalStateException("Chilkat UnlockBundle 失败: " + glob.lastErrorText());
            }
            int status = glob.get_UnlockStatus();
            if (status != 1) {
                throw new IllegalStateException("Chilkat 解锁状态异常 UnlockStatus=" + status);
            }
            CHILKAT_LOADED = true;
        }
    }

    private static void debugDigestMismatchForReference(Document doc, Element sigEl, String uri) {
        try {
            if (doc == null || sigEl == null || uri == null) return;
            if (!uri.startsWith("#")) return;
            String id = uri.substring(1);
            if (id.trim().isEmpty()) return;

            String expected = "";
            try {
                NodeList dvs = sigEl.getElementsByTagNameNS(Constants.SignatureSpecNS, "DigestValue");
                if (dvs != null && dvs.getLength() > 0) {
                    expected = dvs.item(0).getTextContent();
                    if (expected != null) expected = expected.trim();
                }
            } catch (Exception ignored) {
                expected = "";
            }

            Element target = doc.getElementById(id);
            if (target == null) {
                NodeList all = doc.getElementsByTagName("*");
                for (int i = 0; i < all.getLength(); i++) {
                    Node n = all.item(i);
                    if (n instanceof Element) {
                        Element e = (Element) n;
                        if (e.hasAttribute("ID") && id.equals(e.getAttribute("ID"))) {
                            target = e;
                            break;
                        }
                    }
                }
            }
            if (target == null) {
                System.out.println("Santuario验签: Digest调试: 未找到 URI 对应的 ID=" + id);
                return;
            }

            org.apache.xml.security.c14n.Canonicalizer canon = org.apache.xml.security.c14n.Canonicalizer
                    .getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            canon.canonicalizeSubtree(target, baos);
            byte[] c14n = baos.toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] dig = md.digest(c14n);
            String actual = java.util.Base64.getEncoder().encodeToString(dig);

            String actualC14NIsoBytes = "";
            try {
                String c14nStr = new String(c14n, StandardCharsets.UTF_8);
                byte[] isoBytes = c14nStr.getBytes(StandardCharsets.ISO_8859_1);
                actualC14NIsoBytes = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(isoBytes));
            } catch (Exception ignoredEnc) {
            }

            String actualC14NWithComments = "";
            String actualExclC14N = "";
            String actualExclC14NWithComments = "";
            String actualC14N11 = "";
            String actualC14N11WithComments = "";
            try {
                org.apache.xml.security.c14n.Canonicalizer canon2 = org.apache.xml.security.c14n.Canonicalizer
                        .getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS);
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                canon2.canonicalizeSubtree(target, baos2);
                actualC14NWithComments = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baos2.toByteArray()));
            } catch (Exception ignored2) {
            }
            try {
                org.apache.xml.security.c14n.Canonicalizer canon11 = org.apache.xml.security.c14n.Canonicalizer
                        .getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N11_OMIT_COMMENTS);
                ByteArrayOutputStream baos11 = new ByteArrayOutputStream();
                canon11.canonicalizeSubtree(target, baos11);
                actualC14N11 = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baos11.toByteArray()));
            } catch (Exception ignored11) {
            }
            try {
                org.apache.xml.security.c14n.Canonicalizer canon11c = org.apache.xml.security.c14n.Canonicalizer
                        .getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N11_WITH_COMMENTS);
                ByteArrayOutputStream baos11c = new ByteArrayOutputStream();
                canon11c.canonicalizeSubtree(target, baos11c);
                actualC14N11WithComments = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baos11c.toByteArray()));
            } catch (Exception ignored11c) {
            }
            try {
                org.apache.xml.security.c14n.Canonicalizer canon3 = org.apache.xml.security.c14n.Canonicalizer
                        .getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS);
                ByteArrayOutputStream baos3 = new ByteArrayOutputStream();
                canon3.canonicalizeSubtree(target, baos3);
                actualExclC14N = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baos3.toByteArray()));
            } catch (Exception ignored3) {
            }
            try {
                org.apache.xml.security.c14n.Canonicalizer canon4 = org.apache.xml.security.c14n.Canonicalizer
                        .getInstance(org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS);
                ByteArrayOutputStream baos4 = new ByteArrayOutputStream();
                canon4.canonicalizeSubtree(target, baos4);
                actualExclC14NWithComments = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baos4.toByteArray()));
            } catch (Exception ignored4) {
            }

            System.out.println("Santuario验签: Digest调试: URI=" + uri);
            System.out.println("Santuario验签: Digest调试: DigestValue(XML)=" + expected);
            System.out.println("Santuario验签: Digest调试: DigestValue(C14N/SHA1)=" + actual);
            if (actualC14NIsoBytes != null && !actualC14NIsoBytes.isEmpty()) {
                System.out.println("Santuario验签: Digest调试: DigestValue(C14N_ISO8859_1bytes/SHA1)=" + actualC14NIsoBytes);
            }
            if (actualC14NWithComments != null && !actualC14NWithComments.isEmpty()) {
                System.out.println("Santuario验签: Digest调试: DigestValue(C14N_with_comments/SHA1)=" + actualC14NWithComments);
            }
            if (actualExclC14N != null && !actualExclC14N.isEmpty()) {
                System.out.println("Santuario验签: Digest调试: DigestValue(EXCL_C14N/SHA1)=" + actualExclC14N);
            }
            if (actualExclC14NWithComments != null && !actualExclC14NWithComments.isEmpty()) {
                System.out.println("Santuario验签: Digest调试: DigestValue(EXCL_C14N_with_comments/SHA1)=" + actualExclC14NWithComments);
            }
            if (actualC14N11 != null && !actualC14N11.isEmpty()) {
                System.out.println("Santuario验签: Digest调试: DigestValue(C14N11/SHA1)=" + actualC14N11);
            }
            if (actualC14N11WithComments != null && !actualC14N11WithComments.isEmpty()) {
                System.out.println("Santuario验签: Digest调试: DigestValue(C14N11_with_comments/SHA1)=" + actualC14N11WithComments);
            }

            try {
                Node parent = target.getParentNode();
                if (parent instanceof Element) {
                    ByteArrayOutputStream baosP = new ByteArrayOutputStream();
                    canon.canonicalizeSubtree(parent, baosP);
                    String parentDigest = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baosP.toByteArray()));
                    System.out.println("Santuario验签: Digest调试: DigestValue(parent_C14N/SHA1)=" + parentDigest);
                }
            } catch (Exception ignoredP) {
            }

            try {
                Element root = doc.getDocumentElement();
                if (root != null) {
                    ByteArrayOutputStream baosR = new ByteArrayOutputStream();
                    canon.canonicalizeSubtree(root, baosR);
                    String rootDigest = java.util.Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baosR.toByteArray()));
                    System.out.println("Santuario验签: Digest调试: DigestValue(root_C14N/SHA1)=" + rootDigest);
                }
            } catch (Exception ignoredR) {
            }
        } catch (Exception ignored) {
        }
    }

    private static String signXmlWithChilkat(String xml, String sigLocation, int sigLocationMod, String sameDocRefId,
                                             X509Certificate cert, PrivateKey privateKey, String behaviors) throws Exception {
        ensureChilkatLoaded();

        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("Chilkat 签名：XML 为空");
        }

        // XML 1.0 解析会将 \r\n/\r 归一化为 \n；若签名计算未统一行尾，可能导致 Digest/验签不一致。
        // 因此在交给 Chilkat 前先做换行归一化。
        xml = xml.replace("\r\n", "\n").replace("\r", "");
        if (sigLocation == null) sigLocation = "";
        if (sameDocRefId == null || sameDocRefId.trim().isEmpty()) {
            throw new IllegalArgumentException("Chilkat 签名：sameDocRefId 为空");
        }
        if (cert == null || privateKey == null) {
            throw new IllegalArgumentException("Chilkat 签名：证书或私钥为空");
        }

        CkXmlDSigGen gen = new CkXmlDSigGen();
        gen.put_VerboseLogging(true);
        gen.put_SigLocation(sigLocation);
        gen.put_SigLocationMod(sigLocationMod);

        gen.put_SigNamespacePrefix(System.getProperty("chilkat.sigPrefix", ""));
        gen.put_SigNamespaceUri("http://www.w3.org/2000/09/xmldsig#");
        String signedInfoCanonAlg = System.getProperty("chilkat.signedInfoCanon", "C14N");
        gen.put_SignedInfoCanonAlg(signedInfoCanonAlg);
        gen.put_SignedInfoDigestMethod("sha1");

        boolean wantSetRefIdAttr = Boolean.parseBoolean(System.getProperty("chilkat.trySetRefIdAttr", "true"));
        if (wantSetRefIdAttr) {
            // Chilkat 文档：SetRefIdAttr(String uri_or_id, String value)
            // 用于指定某个 Reference 所使用的 ID 属性名（默认是 "Id"，而 SII 使用 "ID"）。
            String idAttrName = System.getProperty("chilkat.refIdAttrName", "ID");
            boolean setOk = false;
            try {
                boolean ok1 = gen.SetRefIdAttr(sameDocRefId, idAttrName);
                if (ok1) {
                    System.out.println("Chilkat SetRefIdAttr 成功: (" + sameDocRefId + "," + idAttrName + ")");
                    setOk = true;
                }
            } catch (Exception ignored) {
            }
            if (!setOk) {
                try {
                    boolean ok2 = gen.SetRefIdAttr("#" + sameDocRefId, idAttrName);
                    if (ok2) {
                        System.out.println("Chilkat SetRefIdAttr 成功: (#" + sameDocRefId + "," + idAttrName + ")");
                        setOk = true;
                    }
                } catch (Exception ignored) {
                }
            }
            if (!setOk) {
                System.out.println("Chilkat SetRefIdAttr 未成功设置(忽略继续): " + gen.lastErrorText());
            }
        }

        String refCanonAlg = System.getProperty("chilkat.refCanon", "C14N");
        boolean okRef = gen.AddSameDocRef(sameDocRefId, "sha1", refCanonAlg, "", "");
        if (!okRef) {
            throw new IllegalStateException("Chilkat AddSameDocRef failed: " + gen.lastErrorText());
        }

        CkPrivateKey ckPriv = new CkPrivateKey();
        CkByteData pkBytes = new CkByteData();
        pkBytes.appendByteArray(privateKey.getEncoded());
        boolean okPk = ckPriv.LoadPkcs8(pkBytes);
        if (!okPk) {
            throw new IllegalStateException("Chilkat LoadPkcs8 failed: " + ckPriv.lastErrorText());
        }

        CkCert ckCert = new CkCert();
        CkByteData certBytes = new CkByteData();
        certBytes.appendByteArray(cert.getEncoded());
        boolean okCert = ckCert.LoadFromBinary(certBytes);
        if (!okCert) {
            throw new IllegalStateException("Chilkat LoadFromBinary(cert) failed: " + ckCert.lastErrorText());
        }
        ckCert.SetPrivateKey(ckPriv);
        gen.SetPrivateKey(ckPriv);

        boolean okSetCert = gen.SetX509Cert(ckCert, true);
        if (!okSetCert) {
            throw new IllegalStateException("Chilkat SetX509Cert failed: " + gen.lastErrorText());
        }

        gen.put_KeyInfoType("Custom");
        gen.put_CustomKeyInfoXml(buildKeyInfoXml(cert));
        gen.put_Behaviors(behaviors == null ? "" : behaviors);

        CkStringBuilder sb = new CkStringBuilder();
        sb.Append(xml);
        boolean okSig = gen.CreateXmlDSigSb(sb);
        if (!okSig) {
            throw new IllegalStateException("Chilkat CreateXmlDSigSb failed: " + gen.lastErrorText());
        }

        boolean debug = Boolean.parseBoolean(System.getProperty("chilkat.debug", "false"));
        if (debug) {
            System.out.println("Chilkat debug(lastErrorText):\n" + gen.lastErrorText());
        }

        return sb.getAsString();
    }

    private static String buildKeyInfoXml(X509Certificate cert) throws Exception {
        PublicKey pk = cert.getPublicKey();
        if (!(pk instanceof java.security.interfaces.RSAPublicKey)) {
            throw new IllegalArgumentException("仅支持 RSA 证书进行 KeyValue 输出");
        }
        java.security.interfaces.RSAPublicKey rsa = (java.security.interfaces.RSAPublicKey) pk;
        String modulus = java.util.Base64.getEncoder().encodeToString(toUnsigned(rsa.getModulus()));
        String exponent = java.util.Base64.getEncoder().encodeToString(toUnsigned(rsa.getPublicExponent()));

        String x509 = java.util.Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(cert.getEncoded());

        return "<KeyValue><RSAKeyValue><Modulus>" + modulus + "</Modulus><Exponent>" + exponent + "</Exponent></RSAKeyValue></KeyValue>"
                + "<X509Data><X509Certificate>" + x509 + "</X509Certificate></X509Data>";
    }

    private static byte[] toUnsigned(BigInteger bi) {
        if (bi == null) return new byte[0];
        byte[] b = bi.toByteArray();
        if (b.length > 1 && b[0] == 0) {
            return Arrays.copyOfRange(b, 1, b.length);
        }
        return b;
    }

    private static String saveXmlStringRaw(String xmlContent, String fileName) throws Exception {
        if (xmlContent == null) {
            throw new IllegalArgumentException("xmlContent is null");
        }

        Path outputDir = Path.of("output");
        if (!Files.exists(outputDir)) {
            Files.createDirectories(outputDir);
        }
        Path filePath = outputDir.resolve(fileName);

        String out = xmlContent;
        if (!out.startsWith("<?xml")) {
            out = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" + out;
        }

        Files.writeString(filePath, out, StandardCharsets.ISO_8859_1);
        LAST_SAVED_XML_PATH = filePath;
        System.out.println("已保存XML: " + filePath.toAbsolutePath());
        return out;
    }

    private static String windowsPathToWslPath(String windowsPath) {
        if (windowsPath == null || windowsPath.trim().isEmpty()) {
            return windowsPath;
        }
        String p = windowsPath.trim();
        p = p.replace("\\", "/");
        if (p.length() >= 2 && p.charAt(1) == ':') {
            char drive = Character.toLowerCase(p.charAt(0));
            p = "/mnt/" + drive + p.substring(2);
        }
        return p;
    }

    private static String toPemPkcs8PrivateKey(PrivateKey privateKey) {
        if (privateKey == null) {
            throw new IllegalArgumentException("privateKey is null");
        }
        byte[] der = privateKey.getEncoded();
        if (der == null || der.length == 0) {
            throw new IllegalStateException("privateKey.getEncoded() 为空，无法导出 PKCS8");
        }
        String b64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(der);
        return "-----BEGIN PRIVATE KEY-----\n" + b64 + "\n-----END PRIVATE KEY-----\n";
    }

    private static String toPemCertificate(X509Certificate cert) throws Exception {
        if (cert == null) {
            throw new IllegalArgumentException("cert is null");
        }
        String b64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(cert.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + b64 + "\n-----END CERTIFICATE-----\n";
    }

    private static String readTextAutoCharset(Path filePath) throws IOException {
        // xmlsec1 常见输出为 UTF-8；但我们最终需要 ISO-8859-1 保存。
        // 这里先按 UTF-8 读取，若失败再按 ISO-8859-1 兜底。
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            return Files.readString(filePath, StandardCharsets.ISO_8859_1);
        }
    }

    private static void runXmlSecSignViaWsl(String distro, Path inputXml, Path outputXml, Path privKeyPem, Path certPem,
                                            String idAttrNodeName, String signatureXPath) throws Exception {
        if (distro == null || distro.trim().isEmpty()) {
            distro = "Ubuntu";
        }
        String inWsl = windowsPathToWslPath(inputXml.toAbsolutePath().toString());
        String outWsl = windowsPathToWslPath(outputXml.toAbsolutePath().toString());
        String keyWsl = windowsPathToWslPath(privKeyPem.toAbsolutePath().toString());
        String crtWsl = windowsPathToWslPath(certPem.toAbsolutePath().toString());

        // 关键：通过 --id-attr:ID 声明 SII 的 ID 属性名，避免 xmlsec 只识别默认 "id"
        // idAttrNodeName 需要带命名空间，如 "http://www.sii.cl/SiiDte:Documento"
        ProcessBuilder pb = new ProcessBuilder(
                "wsl", "-d", distro, "-e", "xmlsec1",
                "--sign",
                "--enabled-reference-uris", "same-doc",
                "--id-attr:ID", idAttrNodeName,
                "--node-xpath", signatureXPath,
                "--output", outWsl,
                "--pkcs8-pem", keyWsl + "," + crtWsl,
                inWsl
        );
        pb.redirectErrorStream(true);
        Process p = pb.start();
        String out;
        try (InputStream is = p.getInputStream()) {
            out = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        int code = p.waitFor();
        if (code != 0) {
            throw new IllegalStateException("xmlsec1 签名失败(exit=" + code + ")\n" + out);
        }
    }

    private static Element createSiiSignatureTemplate(Document doc, String referenceId, X509Certificate cert) throws Exception {
        if (doc == null) throw new IllegalArgumentException("doc is null");
        if (referenceId == null || referenceId.trim().isEmpty()) throw new IllegalArgumentException("referenceId is empty");
        if (cert == null) throw new IllegalArgumentException("cert is null");

        final String ds = Constants.SignatureSpecNS;

        Element sig = doc.createElementNS(ds, "Signature");
        sig.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", ds);

        Element signedInfo = doc.createElementNS(ds, "SignedInfo");
        Element canonMethod = doc.createElementNS(ds, "CanonicalizationMethod");
        canonMethod.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        Element sigMethod = doc.createElementNS(ds, "SignatureMethod");
        sigMethod.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#rsa-sha1");

        Element ref = doc.createElementNS(ds, "Reference");
        ref.setAttribute("URI", "#" + referenceId);

        Element transforms = doc.createElementNS(ds, "Transforms");
        Element transform = doc.createElementNS(ds, "Transform");
        transform.setAttribute("Algorithm", "http://www.w3.org/TR/2001/REC-xml-c14n-20010315");
        transforms.appendChild(transform);

        Element digestMethod = doc.createElementNS(ds, "DigestMethod");
        digestMethod.setAttribute("Algorithm", "http://www.w3.org/2000/09/xmldsig#sha1");
        Element digestValue = doc.createElementNS(ds, "DigestValue");

        ref.appendChild(transforms);
        ref.appendChild(digestMethod);
        ref.appendChild(digestValue);

        signedInfo.appendChild(canonMethod);
        signedInfo.appendChild(sigMethod);
        signedInfo.appendChild(ref);

        Element signatureValue = doc.createElementNS(ds, "SignatureValue");

        // KeyInfo 顺序：KeyValue -> X509Data
        PublicKey pk = cert.getPublicKey();
        if (!(pk instanceof java.security.interfaces.RSAPublicKey)) {
            throw new IllegalArgumentException("xmlsec 签名模板：仅支持 RSA 证书");
        }
        java.security.interfaces.RSAPublicKey rsa = (java.security.interfaces.RSAPublicKey) pk;
        String modulus = java.util.Base64.getEncoder().encodeToString(toUnsigned(rsa.getModulus()));
        String exponent = java.util.Base64.getEncoder().encodeToString(toUnsigned(rsa.getPublicExponent()));

        Element keyInfo = doc.createElementNS(ds, "KeyInfo");

        Element keyValue = doc.createElementNS(ds, "KeyValue");
        Element rsaKeyValue = doc.createElementNS(ds, "RSAKeyValue");
        Element modEl = doc.createElementNS(ds, "Modulus");
        modEl.setTextContent(modulus);
        Element expEl = doc.createElementNS(ds, "Exponent");
        expEl.setTextContent(exponent);
        rsaKeyValue.appendChild(modEl);
        rsaKeyValue.appendChild(expEl);
        keyValue.appendChild(rsaKeyValue);

        Element x509Data = doc.createElementNS(ds, "X509Data");
        Element x509Cert = doc.createElementNS(ds, "X509Certificate");
        String x509b64 = java.util.Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.US_ASCII))
                .encodeToString(cert.getEncoded());
        x509Cert.setTextContent(x509b64);
        x509Data.appendChild(x509Cert);

        keyInfo.appendChild(keyValue);
        keyInfo.appendChild(x509Data);

        sig.appendChild(signedInfo);
        sig.appendChild(signatureValue);
        sig.appendChild(keyInfo);
        return sig;
    }

    public static Path getLastSavedXmlPath() {
        return LAST_SAVED_XML_PATH;
    }

    public String generateInvoiceXML(InvoiceData invoiceData, KeyStore ks, String pfxPassword, InputStream cafFile, String aliasDocumento, String aliasSetDte) throws Exception {
        if (invoiceData.getRutEnvia() == null || invoiceData.getRutEnvia().trim().isEmpty()) {
            throw new IllegalArgumentException("发票数据中的 rutEnvia 不能为空");
        }
        if (invoiceData.getRutEmisor() == null || invoiceData.getRutEmisor().trim().isEmpty()) {
            throw new IllegalArgumentException("发票数据中的 rutEmisor 不能为空");
        }

        // 1. 创建并编组基础对象
        EnvioBOLETA envioBOLETA = createEnvioBOLETA(invoiceData);
        JAXBContext context = JAXBContext.newInstance(EnvioBOLETA.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, false);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "ISO-8859-1");

        StringWriter writer = new StringWriter();
        marshaller.marshal(envioBOLETA, writer);

        // 初始清洗：移除 JAXB 可能产生的 CRLF
        String xmlContent = writer.toString().replace("\r\n", "\n").replace("\n", "").trim();

        // 2. 转为 DOM
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder()
                .parse(new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.ISO_8859_1)));


        String filePrefix = generateFilePrefix(invoiceData);

        // 3. 读取 CAF 字节并插入 TED
        byte[] cafBytes = cafFile.readAllBytes();
        if (cafBytes.length == 0) {
            throw new IllegalArgumentException("CAF 文件内容为空，无法生成 TED");
        }
        TedGenerator.insertTed(doc, new ByteArrayInputStream(cafBytes));
        // 本地验证 TED FRMT（使用 CAF 公钥）
        TedGenerator.verifyTedSignature(doc, new ByteArrayInputStream(cafBytes));

        // 4. 【关键步骤】归一化 (Normalize)
        // 目的：在签名之前，将 DOM 强制转换为最终物理形态（无 \r，结构紧凑）。
        // 签名算法计算 Hash 时，将基于这个纯净版本。
        doc = normalizeDocument(doc);
        normalizeCarriageReturnsInTextNodes(doc);
        stripWhitespaceTextNodes(doc);

        // 归一化后 DD 的物理形态可能发生变化，需重算 TED/FRMT 以确保最终发送形态下验签通过
        TedGenerator.recomputeTedFrmt(doc, new ByteArrayInputStream(cafBytes));
        TedGenerator.verifyTedSignature(doc, new ByteArrayInputStream(cafBytes));
        // 重算 TED/FRMT 过程中可能引入 CRLF；再次统一为 \n，确保后续签名的 digest 与解析后的 DOM 一致。
        normalizeCarriageReturnsInTextNodes(doc);

        String aliasEmisor;
        if (aliasDocumento != null && !aliasDocumento.trim().isEmpty()) {
            aliasEmisor = aliasDocumento.trim();
        } else {
            aliasEmisor = selectSigningAlias(ks, invoiceData.getRutEmisor());
        }

        String aliasEnvia;
        if (aliasSetDte != null && !aliasSetDte.trim().isEmpty()) {
            aliasEnvia = aliasSetDte.trim();
        } else {
            aliasEnvia = selectSigningAlias(ks, invoiceData.getRutEnvia());
        }

        PrivateKey privateKeyEmisor = (PrivateKey) ks.getKey(aliasEmisor, pfxPassword.toCharArray());
        X509Certificate certEmisor = (X509Certificate) ks.getCertificate(aliasEmisor);

        PrivateKey privateKeyEnvia = (PrivateKey) ks.getKey(aliasEnvia, pfxPassword.toCharArray());
        X509Certificate certEnvia = (X509Certificate) ks.getCertificate(aliasEnvia);

        String certRutEmisor = extractRutFromCertificate(certEmisor);
        String certRutEnvia = extractRutFromCertificate(certEnvia);

        System.out.println("签名证书(Documento)选择: alias=" + aliasEmisor + ", certRut=" + certRutEmisor + ", rutEmisor=" + invoiceData.getRutEmisor());
        if (certEmisor != null) {
            System.out.println("签名证书(Documento)SubjectDN: " + certEmisor.getSubjectX500Principal().getName());
            System.out.println("签名证书(Documento)IssuerDN: " + certEmisor.getIssuerX500Principal().getName());
            System.out.println("签名证书(Documento)Serial: " + certEmisor.getSerialNumber());
        }

        System.out.println("签名证书(SetDTE)选择: alias=" + aliasEnvia + ", certRut=" + certRutEnvia + ", rutEnvia=" + invoiceData.getRutEnvia());
        if (certEnvia != null) {
            System.out.println("签名证书(SetDTE)SubjectDN: " + certEnvia.getSubjectX500Principal().getName());
            System.out.println("签名证书(SetDTE)IssuerDN: " + certEnvia.getIssuerX500Principal().getName());
            System.out.println("签名证书(SetDTE)Serial: " + certEnvia.getSerialNumber());
        }

        java.security.cert.Certificate[] chainArrEmisor = null;
        try {
            chainArrEmisor = ks.getCertificateChain(aliasEmisor);
        } catch (Exception ignored) {
            chainArrEmisor = null;
        }
        List<X509Certificate> certChainEmisor = new ArrayList<>();
        if (chainArrEmisor != null) {
            for (java.security.cert.Certificate c : chainArrEmisor) {
                if (c instanceof X509Certificate) {
                    certChainEmisor.add((X509Certificate) c);
                }
            }
        }
        if (certChainEmisor.isEmpty() && certEmisor != null) {
            certChainEmisor.add(certEmisor);
        }

        java.security.cert.Certificate[] chainArrEnvia = null;
        try {
            chainArrEnvia = ks.getCertificateChain(aliasEnvia);
        } catch (Exception ignored) {
            chainArrEnvia = null;
        }
        List<X509Certificate> certChainEnvia = new ArrayList<>();
        if (chainArrEnvia != null) {
            for (java.security.cert.Certificate c : chainArrEnvia) {
                if (c instanceof X509Certificate) {
                    certChainEnvia.add((X509Certificate) c);
                }
            }
        }
        if (certChainEnvia.isEmpty() && certEnvia != null) {
            certChainEnvia.add(certEnvia);
        }

        // 若 PFX 中缺失中间证书链（常见：证书链长度=1），则尝试从 AIA(caIssuers) 自动补齐。
        if (certChainEmisor.size() == 1 && certChainEmisor.get(0) != null) {
            try {
                List<X509Certificate> extended = extendCertChainFromAia(certChainEmisor);
                if (extended != null && extended.size() > certChainEmisor.size()) {
                    certChainEmisor = extended;
                    System.out.println("已从 AIA 补齐证书链(Documento)，链长度=" + certChainEmisor.size());
                } else {
                    System.out.println("AIA 补链未获得额外证书(Documento)，仍使用链长度=" + certChainEmisor.size());
                }
            } catch (Exception e) {
                System.out.println("AIA 补链失败(Documento)（忽略，继续用原链）: " + e.getMessage());
            }
        }

        if (certChainEnvia.size() == 1 && certChainEnvia.get(0) != null) {
            try {
                List<X509Certificate> extended = extendCertChainFromAia(certChainEnvia);
                if (extended != null && extended.size() > certChainEnvia.size()) {
                    certChainEnvia = extended;
                    System.out.println("已从 AIA 补齐证书链(SetDTE)，链长度=" + certChainEnvia.size());
                } else {
                    System.out.println("AIA 补链未获得额外证书(SetDTE)，仍使用链长度=" + certChainEnvia.size());
                }
            } catch (Exception e) {
                System.out.println("AIA 补链失败(SetDTE)（忽略，继续用原链）: " + e.getMessage());
            }
        }

        if (certRutEmisor != null && !certRutEmisor.trim().isEmpty()) {
            String emisorRut = invoiceData.getRutEmisor();
            boolean matchEmisor = emisorRut != null && !emisorRut.trim().isEmpty() && certRutEmisor.equalsIgnoreCase(emisorRut.trim());
            if (!matchEmisor) {
                System.out.println("警告：Documento 签名证书RUT与 RutEmisor 不一致: certRut=" + certRutEmisor + ", rutEmisor=" + emisorRut);
                boolean allowMismatch = Boolean.parseBoolean(System.getProperty("sii.allowSignerRutMismatch", "false"));
                if (!allowMismatch) {
                    throw new IllegalStateException("Documento 签名证书RUT(" + certRutEmisor + ") 与 RutEmisor(" + emisorRut + ") 不一致。SII 通常会以‘Error en Firma’拒收。" +
                            "请为 Documento 提供 RUT=" + emisorRut + " 的证书（或在 SII 认证环境完成授权/代表关系）。" +
                            "如需临时放行对照实验，可加 -Dsii.allowSignerRutMismatch=true");
                }
            }
        }

        if (certRutEnvia != null && !certRutEnvia.trim().isEmpty()) {
            String enviaRut = invoiceData.getRutEnvia();
            boolean matchEnvia = enviaRut != null && !enviaRut.trim().isEmpty() && certRutEnvia.equalsIgnoreCase(enviaRut.trim());
            if (!matchEnvia) {
                System.out.println("警告：SetDTE 签名证书RUT与 RutEnvia 不一致: certRut=" + certRutEnvia + ", rutEnvia=" + enviaRut);
            }
        }

        String signer = System.getProperty("sii.signer", "");
        if (signer != null && signer.trim().equalsIgnoreCase("xmlsec")) {
            String distro = System.getProperty("wsl.distro", "Ubuntu");

            Element docEl = (Element) doc.getElementsByTagName("Documento").item(0);
            Element setEl = (Element) doc.getElementsByTagName("SetDTE").item(0);
            if (docEl == null || setEl == null) {
                throw new IllegalStateException("xmlsec 签名：未找到 Documento/SetDTE 元素");
            }
            String docId = docEl.getAttribute("ID");
            String setId = setEl.getAttribute("ID");
            if (docId == null || docId.trim().isEmpty() || setId == null || setId.trim().isEmpty()) {
                throw new IllegalStateException("xmlsec 签名：Documento/SetDTE 缺少 ID 属性");
            }

            // 插入符合 SII 结构的 Signature 模板（让 xmlsec1 填充 DigestValue/SignatureValue）
            Element dteEl = (Element) doc.getElementsByTagName("DTE").item(0);
            if (dteEl == null) {
                throw new IllegalStateException("xmlsec 签名：未找到 DTE 元素");
            }

            Element innerSig = createSiiSignatureTemplate(doc, docId, certEmisor);
            Node nextDocSibling = docEl.getNextSibling();
            if (nextDocSibling != null) {
                dteEl.insertBefore(innerSig, nextDocSibling);
            } else {
                dteEl.appendChild(innerSig);
            }

            Element envioEl = (Element) doc.getElementsByTagName("EnvioBOLETA").item(0);
            if (envioEl == null) {
                throw new IllegalStateException("xmlsec 签名：未找到 EnvioBOLETA 元素");
            }
            Element outerSig = createSiiSignatureTemplate(doc, setId, certEnvia);
            Node nextSetSibling = setEl.getNextSibling();
            if (nextSetSibling != null) {
                envioEl.insertBefore(outerSig, nextSetSibling);
            } else {
                envioEl.appendChild(outerSig);
            }

            // 写入临时文件并调用 WSL xmlsec1
            Path outputDir = Path.of("output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            Path tmpDir = outputDir.resolve("xmlsec_tmp");
            if (!Files.exists(tmpDir)) {
                Files.createDirectories(tmpDir);
            }

            Path keyPem = tmpDir.resolve(filePrefix + "_key.pem");
            Path certPem = tmpDir.resolve(filePrefix + "_cert.pem");
            Files.writeString(keyPem, toPemPkcs8PrivateKey(privateKeyEmisor), StandardCharsets.US_ASCII,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(certPem, toPemCertificate(certEmisor), StandardCharsets.US_ASCII,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            String baseXml = toCleanString(doc);
            Path stage0 = tmpDir.resolve(filePrefix + "_stage0.xml");
            Files.writeString(stage0,
                    "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" + baseXml,
                    StandardCharsets.ISO_8859_1,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            Path stage1 = tmpDir.resolve(filePrefix + "_stage1_inner.xml");
            Path stage2 = tmpDir.resolve(filePrefix + "_stage2_outer.xml");

            String siiNs = "http://www.sii.cl/SiiDte";

            // 签 Documento 内层：选择 DTE 下的 Signature 模板
            runXmlSecSignViaWsl(
                    distro,
                    stage0,
                    stage1,
                    keyPem,
                    certPem,
                    siiNs + ":Documento",
                    "//*[local-name()='DTE']/*[local-name()='Signature' and namespace-uri()='http://www.w3.org/2000/09/xmldsig#'][1]"
            );

            // 外层使用 Envia 证书
            Path keyPem2 = tmpDir.resolve(filePrefix + "_key2.pem");
            Path certPem2 = tmpDir.resolve(filePrefix + "_cert2.pem");
            Files.writeString(keyPem2, toPemPkcs8PrivateKey(privateKeyEnvia), StandardCharsets.US_ASCII,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.writeString(certPem2, toPemCertificate(certEnvia), StandardCharsets.US_ASCII,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            runXmlSecSignViaWsl(
                    distro,
                    stage1,
                    stage2,
                    keyPem2,
                    certPem2,
                    siiNs + ":SetDTE",
                    "/*[local-name()='EnvioBOLETA' and namespace-uri()='http://www.sii.cl/SiiDte']/*[local-name()='Signature' and namespace-uri()='http://www.w3.org/2000/09/xmldsig#'][1]"
            );

            String signedXml = readTextAutoCharset(stage2);
            signedXml = signedXml.replace("\r\n", "\n").replace("\r", "");
            String finalXml = saveXmlStringRaw(signedXml, filePrefix + "_05_最终XML_发送.xml");
            validateSignaturesAfterSerialize(finalXml);
            validateSignaturesWithSantuario(finalXml);
            return finalXml;
        }
        if (signer != null && signer.trim().equalsIgnoreCase("chilkat")) {
            Element docEl = (Element) doc.getElementsByTagName("Documento").item(0);
            Element setEl = (Element) doc.getElementsByTagName("SetDTE").item(0);
            if (docEl == null || setEl == null) {
                throw new IllegalStateException("Chilkat 签名：未找到 Documento/SetDTE 元素");
            }

            // 避免 Chilkat 在计算 Digest 时对祖先默认命名空间(in-scope xmlns)处理差异导致摘要不一致。
            // 显式把默认命名空间写到被签名的元素上，确保 canonicalize 输出稳定。
            String siiNs = "http://www.sii.cl/SiiDte";
            try {
                if (!docEl.hasAttribute("xmlns") && (docEl.getNamespaceURI() == null || siiNs.equals(docEl.getNamespaceURI()))) {
                    docEl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", siiNs);
                }
                if (!setEl.hasAttribute("xmlns") && (setEl.getNamespaceURI() == null || siiNs.equals(setEl.getNamespaceURI()))) {
                    setEl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", siiNs);
                }
            } catch (Exception ignored) {
            }

            String unsignedXml = toCleanString(doc);

            String docId = docEl.getAttribute("ID");
            String setId = setEl.getAttribute("ID");
            if (docId == null || docId.trim().isEmpty() || setId == null || setId.trim().isEmpty()) {
                throw new IllegalStateException("Chilkat 签名：Documento/SetDTE 缺少 ID 属性");
            }

            String xmlAfterDocSig = signXmlWithChilkat(
                    unsignedXml,
                    "EnvioBOLETA|SetDTE|DTE",
                    0,
                    docId,
                    certEmisor,
                    privateKeyEmisor,
                    System.getProperty("chilkat.behaviors.inner", "IndentedSignature")
            );

            System.out.println("Chilkat 内层签名完成，开始中间验签(仅用于调试对照)");
            validateSignaturesAfterSerialize(xmlAfterDocSig);
            validateSignaturesWithSantuario(xmlAfterDocSig);

            String xmlAfterSetSig = signXmlWithChilkat(
                    xmlAfterDocSig,
                    "EnvioBOLETA",
                    0,
                    setId,
                    certEnvia,
                    privateKeyEnvia,
                    System.getProperty("chilkat.behaviors.outer", "SignExistingSignatures")
            );

            String finalXml = saveXmlStringRaw(xmlAfterSetSig, filePrefix + "_05_最终XML_发送.xml");
            validateSignaturesAfterSerialize(finalXml);
            validateSignaturesWithSantuario(finalXml);
            return finalXml;
        }

        // ======== 内部签名 (Documento) ========
        signXmlForSiiSantuario(doc, "Documento", privateKeyEmisor, certChainEmisor);

        // 固定内层签名的最终序列化形态，避免外层签名摘要与最终输出不一致
        // 注意：signXmlForSii 开启了 SignatureValue.wrap。
        // 这会在 DOM 中插入换行符。这没问题，只要我们后续保存时保留这些 \n，且不引入 \r。
        // 关键：Java 生成的折行可能包含 \r\n，而 XML 解析器会把 \r\n 归一化成 \n；
        // 若不在外层签名前统一为 \n，外层 Reference 的 digest 会与 Chilkat/在线工具计算不一致。
        normalizeCarriageReturnsInTextNodes(doc);

        // ======== 外部签名 (SetDTE) ========
        signXmlForSiiOutSantuario(doc, "SetDTE", privateKeyEnvia, certChainEnvia);

        // 自动验签 (本地诊断)
        validateSignatures(doc);

        // 5. 保存并返回最终 XML
        // 【核心】：使用与 normalizeDocument 完全相同的逻辑 (toCleanString) 生成最终字符串。
        // 这样保证了：内存中被签名的字节流 == 最终保存到磁盘的字节流。
        // 这里的 toCleanString 会保留签名库生成的 \n (解决行长问题)，但移除 \r (解决 Hash 问题)。
        String finalXml = saveXmlDocument(doc, filePrefix + "_05_最终XML_发送.xml");
        validateSignaturesAfterSerialize(finalXml);
        validateSignaturesWithSantuario(finalXml);
        return finalXml;
    }

    private static List<X509Certificate> extendCertChainFromAia(List<X509Certificate> existing) throws Exception {
        if (existing == null || existing.isEmpty() || existing.get(0) == null) {
            return existing;
        }

        List<X509Certificate> result = new ArrayList<>(existing);
        X509Certificate current = existing.get(existing.size() - 1);

        // 最多向上追 3 级，避免循环或异常链
        for (int depth = 0; depth < 3; depth++) {
            if (current == null) break;

            // 已经是自签根
            if (current.getSubjectX500Principal().equals(current.getIssuerX500Principal())) {
                break;
            }

            X509Certificate issuer = downloadIssuerFromAia(current);
            if (issuer == null) {
                break;
            }

            boolean already = false;
            for (X509Certificate c : result) {
                if (c != null && c.getSerialNumber().equals(issuer.getSerialNumber())
                        && c.getSubjectX500Principal().equals(issuer.getSubjectX500Principal())) {
                    already = true;
                    break;
                }
            }
            if (already) {
                break;
            }

            result.add(issuer);
            current = issuer;
        }

        return result;
    }

    private static X509Certificate downloadIssuerFromAia(X509Certificate cert) throws Exception {
        if (cert == null) return null;

        byte[] ext = cert.getExtensionValue("1.3.6.1.5.5.7.1.1");
        if (ext == null || ext.length == 0) return null;

        AuthorityInformationAccess aia;
        try (ASN1InputStream a1 = new ASN1InputStream(ext)) {
            ASN1OctetString oct = (ASN1OctetString) a1.readObject();
            try (ASN1InputStream a2 = new ASN1InputStream(oct.getOctets())) {
                aia = AuthorityInformationAccess.getInstance(a2.readObject());
            }
        }

        if (aia == null) return null;

        AccessDescription[] ads = aia.getAccessDescriptions();
        if (ads == null) return null;

        for (AccessDescription ad : ads) {
            if (ad == null) continue;
            if (!X509ObjectIdentifiers.id_ad_caIssuers.equals(ad.getAccessMethod())) {
                continue;
            }
            GeneralName loc = ad.getAccessLocation();
            if (loc == null || loc.getTagNo() != GeneralName.uniformResourceIdentifier) {
                continue;
            }
            String uri = "";
            try {
                java.lang.reflect.Method getUri = loc.getClass().getMethod("getURI");
                Object u = getUri.invoke(loc);
                uri = u == null ? "" : String.valueOf(u);
            } catch (Exception ignored) {
                uri = "";
            }
            System.out.println("AIA caIssuers URI: " + uri);
            X509Certificate downloaded = downloadX509FromUrl(uri.trim());
            if (downloaded != null) {
                return downloaded;
            }
        }

        return null;
    }

    private static X509Certificate downloadX509FromUrl(String url) throws Exception {
        if (url == null || url.trim().isEmpty()) return null;

        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(8000);
        conn.setReadTimeout(8000);
        conn.setRequestMethod("GET");

        try (InputStream is = conn.getInputStream()) {
            byte[] data = is.readAllBytes();
            if (data == null || data.length == 0) return null;
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(data));
        }
    }

    private static void validateSignaturesAfterSerialize(String xml) {
        try {
            if (xml == null || xml.trim().isEmpty()) {
                System.err.println("序列化后的XML为空，跳过二次验签");
                return;
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document parsed = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1)));
            System.out.println("二次验签(序列化->解析)开始");
            validateSignatures(parsed);
        } catch (Exception e) {
            System.err.println("二次验签异常: " + e.getMessage());
        }
    }

    private static String selectSigningAlias(KeyStore ks, String preferredRut) throws Exception {
        String fallback = null;
        java.util.Enumeration<String> aliases = ks.aliases();
        while (aliases.hasMoreElements()) {
            String a = aliases.nextElement();
            if (fallback == null) {
                fallback = a;
            }
            java.security.cert.Certificate c = ks.getCertificate(a);
            if (c instanceof X509Certificate) {
                String rut = extractRutFromCertificate((X509Certificate) c);
                if (preferredRut != null && !preferredRut.trim().isEmpty() && rut != null && rut.equalsIgnoreCase(preferredRut.trim())) {
                    return a;
                }
            }
        }
        if (fallback == null) {
            throw new KeyException("KeyStore 中没有可用别名");
        }
        if (preferredRut != null && !preferredRut.trim().isEmpty()) {
            System.out.println("警告：未在KeyStore中找到匹配 preferredRut=" + preferredRut + " 的证书别名，将使用fallback alias=" + fallback + "。若SII持续505，请提供包含该RUT的.pfx/.p12证书。");
        }
        return fallback;
    }

    public static String extractRutFromCertificate(X509Certificate cert) {
        try {
            if (cert == null) {
                return null;
            }

            String dn = cert.getSubjectX500Principal().getName();
            LdapName ldapName = new LdapName(dn);

            String raw = null;
            for (Rdn rdn : ldapName.getRdns()) {
                String type = rdn.getType();
                if (type == null) continue;
                if ("SERIALNUMBER".equalsIgnoreCase(type) || "2.5.4.5".equals(type)) {
                    Object v = rdn.getValue();
                    if (v != null) {
                        if (v instanceof byte[]) {
                            try (ASN1InputStream asn1 = new ASN1InputStream((byte[]) v)) {
                                ASN1Primitive p = asn1.readObject();
                                if (p instanceof ASN1String) {
                                    raw = ((ASN1String) p).getString();
                                } else {
                                    raw = String.valueOf(v);
                                }
                            }
                        } else {
                            raw = String.valueOf(v);
                        }
                        break;
                    }
                }
            }

            String candidate = null;
            if (raw != null) {
                candidate = raw.trim();
            }

            // DN 里可能出现 hex DER 形式（例如 2.5.4.5=#130a323435...），需要解码后再提取
            if (candidate != null && candidate.startsWith("#")) {
                try {
                    byte[] der = Hex.decode(candidate.substring(1));
                    try (ASN1InputStream asn1 = new ASN1InputStream(der)) {
                        ASN1Primitive p = asn1.readObject();
                        if (p instanceof ASN1String) {
                            candidate = ((ASN1String) p).getString();
                        }
                    }
                } catch (Exception ignored) {
                    // 忽略解码失败，继续走后续兜底
                }
            }
            if (candidate != null && !candidate.isEmpty()) {
                java.util.regex.Matcher rut = java.util.regex.Pattern
                        .compile("(\\d{7,8}-[0-9Kk])")
                        .matcher(candidate);
                if (rut.find()) {
                    return rut.group(1).toUpperCase();
                }
            }

            // 某些证书不在 SERIALNUMBER 字段放 RUT，直接从整个 DN 中兜底提取
            java.util.regex.Matcher rut2 = java.util.regex.Pattern
                    .compile("(\\d{7,8}-[0-9Kk])")
                    .matcher(dn);
            if (rut2.find()) {
                return rut2.group(1).toUpperCase();
            }

            // DN 可能只包含 #hex 形式（无明文），逐段解码后再找 RUT
            java.util.regex.Matcher hexMatcher = java.util.regex.Pattern
                    .compile("#([0-9A-Fa-f]{4,})")
                    .matcher(dn);
            while (hexMatcher.find()) {
                String hex = hexMatcher.group(1);
                try {
                    byte[] der = Hex.decode(hex);
                    try (ASN1InputStream asn1 = new ASN1InputStream(der)) {
                        ASN1Primitive p = asn1.readObject();
                        if (p instanceof ASN1String) {
                            String s = ((ASN1String) p).getString();
                            java.util.regex.Matcher rut3 = java.util.regex.Pattern
                                    .compile("(\\d{7,8}-[0-9Kk])")
                                    .matcher(s);
                            if (rut3.find()) {
                                return rut3.group(1).toUpperCase();
                            }
                        }
                    }
                } catch (Exception ignored) {
                    // continue
                }
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static void validateSignaturesWithSantuario(String xml) {
        try {
            if (xml == null || xml.trim().isEmpty()) {
                System.err.println("Santuario验签：XML为空，跳过");
                return;
            }

            Init.init();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1)));

            // Santuario 在解析后的 DOM 中不会自动识别 ID 类型属性，需手动注册，否则 Reference(#ID) 无法解析
            registerSantuarioIdAttributes(doc);

            NodeList sigs = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
            if (sigs.getLength() == 0) {
                System.out.println("Santuario验签：未找到 Signature 节点");
                return;
            }

            System.out.println("Santuario验签(序列化->解析)开始");
            for (int i = 0; i < sigs.getLength(); i++) {
                try {
                    Node n = sigs.item(i);
                    if (!(n instanceof Element)) {
                        continue;
                    }
                    Element sigEl = (Element) n;
                    org.apache.xml.security.signature.XMLSignature sig = new org.apache.xml.security.signature.XMLSignature(sigEl, "");
                    org.apache.xml.security.keys.KeyInfo ki = sig.getKeyInfo();

                    java.security.PublicKey pk = null;
                    java.security.cert.X509Certificate x509 = null;
                    if (ki != null) {
                        try {
                            pk = ki.getPublicKey();
                        } catch (Exception ignored) {
                            pk = null;
                        }
                        try {
                            x509 = ki.getX509Certificate();
                        } catch (Exception ignored) {
                            x509 = null;
                        }
                    }

                    boolean sigValueOk;
                    if (pk != null) {
                        sigValueOk = sig.checkSignatureValue(pk);
                    } else if (x509 != null) {
                        sigValueOk = sig.checkSignatureValue(x509);
                    } else {
                        System.out.println("Santuario验签[" + i + "]: KeyInfo中未能解析到公钥/证书");
                        sigValueOk = false;
                    }
                    System.out.println("Santuario验签[" + i + "]: SignatureValue=" + (sigValueOk ? "有效" : "无效"));

                    try {
                        Object signedInfo = sig.getSignedInfo();
                        java.lang.reflect.Method getLength = signedInfo.getClass().getMethod("getLength");
                        int refLen = (Integer) getLength.invoke(signedInfo);
                        java.lang.reflect.Method item = signedInfo.getClass().getMethod("item", int.class);
                        for (int r = 0; r < refLen; r++) {
                            Object ref = item.invoke(signedInfo, r);
                            String uri = "";
                            try {
                                java.lang.reflect.Method getUri = ref.getClass().getMethod("getURI");
                                Object u = getUri.invoke(ref);
                                uri = u == null ? "" : String.valueOf(u);
                            } catch (Exception ignored) {
                                uri = "";
                            }
                            boolean refOk;
                            try {
                                java.lang.reflect.Method verify = ref.getClass().getMethod("verify");
                                Object ok = verify.invoke(ref);
                                refOk = ok instanceof Boolean && (Boolean) ok;
                            } catch (Exception e) {
                                refOk = false;
                            }
                            System.out.println("Santuario验签[" + i + "]: Reference[" + r + "] URI=" + uri + " => " + (refOk ? "有效" : "无效"));

                            if (!refOk) {
                                try {
                                    debugDigestMismatchForReference(doc, sigEl, uri);
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Santuario验签[" + i + "]: 无法枚举Reference: " + e.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Santuario验签[" + i + "]: 异常: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Santuario验签异常: " + e.getMessage());
        }
    }

    private static void registerSantuarioIdAttributes(Document doc) {
        try {
            Class<?> idResolverClz = Class.forName("org.apache.xml.security.utils.IdResolver");
            java.lang.reflect.Method registerByElementAndString = null;
            java.lang.reflect.Method registerByElementAndAttr = null;
            try {
                registerByElementAndString = idResolverClz.getMethod("registerElementById", Element.class, String.class);
            } catch (Exception ignored) {
                registerByElementAndString = null;
            }
            try {
                registerByElementAndAttr = idResolverClz.getMethod("registerElementById", Element.class, org.w3c.dom.Attr.class);
            } catch (Exception ignored) {
                registerByElementAndAttr = null;
            }

            NodeList all = doc.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                Node n = all.item(i);
                if (!(n instanceof Element)) continue;
                Element el = (Element) n;
                if (!el.hasAttribute("ID")) continue;
                String id = el.getAttribute("ID");
                if (id == null || id.isEmpty()) continue;
                try {
                    if (registerByElementAndString != null) {
                        registerByElementAndString.invoke(null, el, id);
                    } else if (registerByElementAndAttr != null) {
                        org.w3c.dom.Attr attr = el.getAttributeNode("ID");
                        if (attr != null) {
                            registerByElementAndAttr.invoke(null, el, attr);
                        }
                    }
                } catch (Exception ignored) {
                    // 忽略单个元素注册失败
                }
            }
        } catch (Exception e) {
            System.out.println("Santuario验签：ID注册失败: " + e.getMessage());
        }
    }

    /**
     * 【核心统一序列化方法】
     * 作用：将 Document 转为 ISO-8859-1 字符串，并执行严格清洗。
     * 被 normalizeDocument 和 saveXmlDocument 同时调用，确保一致性。
     */
    private static String toCleanString(Document doc) throws Exception {
        DOMImplementationLS impl = (DOMImplementationLS) doc.getImplementation().getFeature("LS", "3.0");
        LSSerializer serializer = impl.createLSSerializer();
        try {
            serializer.getDomConfig().setParameter("format-pretty-print", Boolean.FALSE);
        } catch (Exception ignored) {
        }
        try {
            serializer.getDomConfig().setParameter("xml-declaration", Boolean.FALSE);
        } catch (Exception ignored) {
        }

        LSOutput out = impl.createLSOutput();
        out.setEncoding("ISO-8859-1");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        out.setByteStream(baos);
        serializer.write(doc, out);
        String xmlContent = baos.toString(StandardCharsets.ISO_8859_1);

        String cleaned = xmlContent.replace("&#13;", "");
        return cleaned.trim();
    }

    /**
     * DOM 归一化：序列化清洗后再解析回 DOM
     */
    private static Document normalizeDocument(Document doc) throws Exception {
        String cleanXml = toCleanString(doc);
        byte[] xmlBytes = cleanXml.getBytes(StandardCharsets.ISO_8859_1);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setIgnoringElementContentWhitespace(true);

        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(xmlBytes));
    }

    /**
     * 保存 XML 文件 (JDK 11)
     */
    private static String saveXmlDocument(Document doc, String fileName) {
        try {
            Path outputDir = Path.of("output");
            if (!Files.exists(outputDir)) {
                Files.createDirectories(outputDir);
            }
            Path filePath = outputDir.resolve(fileName);

            // 复用核心清洗逻辑
            String xmlContent = toCleanString(doc);

            // 拼接标准 Header (Standalone="no")
            if (!xmlContent.startsWith("<?xml")) {
                xmlContent = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>\n" + xmlContent;
            }

            Files.writeString(filePath, xmlContent, StandardCharsets.ISO_8859_1);

            LAST_SAVED_XML_PATH = filePath;

            System.out.println("已保存XML: " + filePath.toAbsolutePath());
            return xmlContent;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // --- 签名方法 (启用原生折行) ---

    private static void signXmlForSii(Document doc, String tagName, PrivateKey privateKey, List<X509Certificate> certChain) throws Exception {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        Element target = (Element) doc.getElementsByTagName(tagName).item(0);
        if (target == null) throw new IllegalArgumentException("未找到目标元素: " + tagName);

        ensureSiiDteDefaultNamespaceDeclared(target);

        String idValue = target.getAttribute("ID");
        if (idValue == null || idValue.isEmpty()) throw new IllegalArgumentException(tagName + " 缺少 ID 属性");

        target.setIdAttribute("ID", true);

        Reference ref = fac.newReference("#" + idValue, fac.newDigestMethod(DigestMethod.SHA1, null), null, null, null);
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref)
        );

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(certChain.get(0).getPublicKey());
        X509Data x509Data = kif.newX509Data(Collections.singletonList(certChain.get(0)));
        KeyInfo ki = kif.newKeyInfo(java.util.Arrays.asList(kv, x509Data));

        DOMSignContext signContext = new DOMSignContext(privateKey, target.getParentNode());
        signContext.setDefaultNamespacePrefix(""); 
        signContext.setNextSibling(target.getNextSibling());

        // 【关键设置】启用 Wrap。让 Java 自动生成带换行的 Base64。
        // 这些换行符 (\r\n 或 \n) 会成为 DOM 的一部分。
        // 后续 toCleanString 会把 \r 去掉，只留 \n，既保留了折行结构，又保证了 Hash 一致。
        signContext.setProperty("org.jcp.xml.dsig.internal.dom.SignatureValue.wrap", Boolean.TRUE);

        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(signContext);
    }

    private static void signXmlForSiiSantuario(Document doc, String tagName, PrivateKey privateKey, List<X509Certificate> certChain) throws Exception {
        Init.init();

        Element target = (Element) doc.getElementsByTagName(tagName).item(0);
        if (target == null) throw new IllegalArgumentException("未找到目标元素: " + tagName);

        ensureSiiDteDefaultNamespaceDeclared(target);

        String idValue = target.getAttribute("ID");
        if (idValue == null || idValue.isEmpty()) throw new IllegalArgumentException(tagName + " 缺少 ID 属性");
        target.setIdAttribute("ID", true);
        registerSantuarioIdAttributes(doc);

        org.apache.xml.security.utils.ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "");

        org.apache.xml.security.signature.XMLSignature sig = new org.apache.xml.security.signature.XMLSignature(
                doc,
                "",
                org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS
        );

        sig.getElement().setPrefix(null);
        sig.getElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", Constants.SignatureSpecNS);

        // 插入到目标元素后（与 SII 示例一致：Signature 作为 Documento 的兄弟节点）
        Node parent = target.getParentNode();
        Node next = target.getNextSibling();
        if (next != null) {
            parent.insertBefore(sig.getElement(), next);
        } else {
            parent.appendChild(sig.getElement());
        }

        sig.addDocument("#" + idValue, null, org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);

        // KeyInfo: 按 SII Schema 顺序输出 KeyValue -> X509Data（仅放 leaf 证书，避免 Schema: LSX-00204）
        if (certChain != null && !certChain.isEmpty() && certChain.get(0) != null) {
            sig.addKeyInfo(certChain.get(0).getPublicKey());
            sig.addKeyInfo(certChain.get(0));
        }

        sig.sign(privateKey);
    }

    private static void signXmlForSiiOutSantuario(Document doc, String tagName, PrivateKey privateKey, List<X509Certificate> certChain) throws Exception {
        Init.init();

        Element target = (Element) doc.getElementsByTagName(tagName).item(0);
        if (target == null) throw new IllegalArgumentException("未找到目标元素: " + tagName);

        ensureSiiDteDefaultNamespaceDeclared(target);

        String idValue = target.getAttribute("ID");
        if (idValue == null || idValue.isEmpty()) throw new IllegalArgumentException(tagName + " 缺少 ID 属性");
        target.setIdAttribute("ID", true);
        registerSantuarioIdAttributes(doc);

        org.apache.xml.security.utils.ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "");

        org.apache.xml.security.signature.XMLSignature sig = new org.apache.xml.security.signature.XMLSignature(
                doc,
                "",
                org.apache.xml.security.signature.XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS
        );

        sig.getElement().setPrefix(null);
        sig.getElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", Constants.SignatureSpecNS);

        Node parent = target.getParentNode();
        Node next = target.getNextSibling();
        if (next != null) {
            parent.insertBefore(sig.getElement(), next);
        } else {
            parent.appendChild(sig.getElement());
        }

        sig.addDocument("#" + idValue, null, org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);

        // KeyInfo: 按 SII Schema 顺序输出 KeyValue -> X509Data（仅放 leaf 证书，避免 Schema: LSX-00204）
        if (certChain != null && !certChain.isEmpty() && certChain.get(0) != null) {
            sig.addKeyInfo(certChain.get(0).getPublicKey());
            sig.addKeyInfo(certChain.get(0));
        }

        sig.sign(privateKey);
    }

    private static void signXmlForSiiOut(Document doc, String tagName, PrivateKey privateKey, List<X509Certificate> certChain) throws Exception {
        XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
        Element target = (Element) doc.getElementsByTagName(tagName).item(0);
        if (target == null) throw new IllegalArgumentException("未找到目标元素: " + tagName);

        ensureSiiDteDefaultNamespaceDeclared(target);

        String idValue = target.getAttribute("ID");
        if (idValue == null || idValue.isEmpty()) throw new IllegalArgumentException(tagName + " 缺少 ID 属性");
        target.setIdAttribute("ID", true);

        Reference ref = fac.newReference("#" + idValue, fac.newDigestMethod(DigestMethod.SHA1, null), null, null, null);
        SignedInfo si = fac.newSignedInfo(
                fac.newCanonicalizationMethod(CanonicalizationMethod.INCLUSIVE, (C14NMethodParameterSpec) null),
                fac.newSignatureMethod(javax.xml.crypto.dsig.SignatureMethod.RSA_SHA1, null),
                Collections.singletonList(ref)
        );

        KeyInfoFactory kif = fac.getKeyInfoFactory();
        KeyValue kv = kif.newKeyValue(certChain.get(0).getPublicKey());
        X509Data x509Data = kif.newX509Data(Collections.singletonList(certChain.get(0)));
        KeyInfo ki = kif.newKeyInfo(java.util.Arrays.asList(kv, x509Data));

        DOMSignContext signContext = new DOMSignContext(privateKey, target.getParentNode());
        signContext.setDefaultNamespacePrefix("");
        signContext.setNextSibling(target.getNextSibling());

        // 外层签名同样启用 Wrap
        signContext.setProperty("org.jcp.xml.dsig.internal.dom.SignatureValue.wrap", Boolean.TRUE);

        XMLSignature signature = fac.newXMLSignature(si, ki);
        signature.sign(signContext);
    }

    // --- 对象创建与辅助方法 (保持不变) ---

    public EnvioBOLETA createEnvioBOLETA(InvoiceData invoiceData) {
        SetDTE setDTE = createSetDTE(invoiceData);
        return new EnvioBOLETA(setDTE);
    }

    private SetDTE createSetDTE(InvoiceData invoiceData) {
        Caratula caratula = createCaratula(invoiceData);
        List<DTE> dteList = new ArrayList<>();
        DTE dte = createDTE(invoiceData);
        dteList.add(dte);
        return new SetDTE(caratula, dteList, generateSetDteIdWithChileTime(invoiceData.getRutEmisor()));
    }

    private Caratula createCaratula(InvoiceData invoiceData) {
        String rutEmisor = invoiceData.getRutEmisor();
        String rutEnvia = invoiceData.getRutEnvia();
        if (rutEnvia == null || rutEnvia.trim().isEmpty()) throw new IllegalArgumentException("rutEnvia 不能为空");
        if (rutEmisor == null || rutEmisor.trim().isEmpty()) throw new IllegalArgumentException("rutEmisor 不能为空");
        List<SubTotDTE> subTotDTEList = new ArrayList<>();
        subTotDTEList.add(new SubTotDTE(invoiceData.getTipoDTE(), 1));
        return new Caratula(rutEmisor, rutEnvia, invoiceData.getRutReceptor(), invoiceData.getFchResol(),
                invoiceData.getNroResol(), invoiceData.getTmstFirmaEnv(), subTotDTEList);
    }

    private DTE createDTE(InvoiceData invoiceData) {
        return new DTE(createDocumento(invoiceData));
    }

    private Documento createDocumento(InvoiceData invoiceData) {
        Encabezado encabezado = createEncabezado(invoiceData);
        List<Detalle> detalleList = new ArrayList<>();
        for (int i = 0; i < invoiceData.getProducts().size(); i++) {
            InvoiceData.Product product = invoiceData.getProducts().get(i);
            Detalle detalle = new Detalle(i + 1, product.getNmbItem(), product.getQtyItem(), product.getPrcItem(), product.getMontoItem());
            if (product.getIndExe() != null) {
                detalle.setIndExe(product.getIndExe());
            }
            if (product.getUnmdItem() != null && !product.getUnmdItem().trim().isEmpty()) {
                detalle.setUnmdItem(product.getUnmdItem());
            }
            detalleList.add(detalle);
        }
        List<Referencia> referencias = new ArrayList<>();
        if (invoiceData.getIsReferences() != null) {
            for (int i = 0; i < invoiceData.getIsReferences().size(); i++) {
                InvoiceData.Reference ref = invoiceData.getIsReferences().get(i);
                referencias.add(new Referencia(i + 1, ref.getType(), ref.getBillId(), ref.getInvoiceDate(), ref.getReasonType(), ref.getResson()));
            }
        }
        if (referencias.isEmpty()) {
            return new Documento(encabezado, detalleList, invoiceData.getTmstFirma(), invoiceData.getDocumentId());
        } else {
            return new Documento(encabezado, detalleList, referencias, invoiceData.getTmstFirma(), invoiceData.getDocumentId());
        }
    }

    private Encabezado createEncabezado(InvoiceData invoiceData) {
        IdDoc idDoc = new IdDoc(invoiceData.getTipoDTE(), invoiceData.getFolio(), invoiceData.getFchEmis(), invoiceData.getIndServicio());
        Emisor emisor = new Emisor(invoiceData.getRutEmisor(), invoiceData.getRznSocEmisor(), invoiceData.getGiroEmisor(), invoiceData.getDirOrigen(), invoiceData.getCmnaOrigen(), invoiceData.getCiudadOrigen());
        Receptor receptor = new Receptor(invoiceData.getRutReceptor(), invoiceData.getRznSocReceptor(), invoiceData.getDirRecep(), invoiceData.getCmnaRecep(), invoiceData.getCiudadRecep());
        Totales totales = new Totales(invoiceData.getMntNeto(), invoiceData.getMntExe(), invoiceData.getIva(), invoiceData.getMntAdic(), invoiceData.getMntTotal());
        return new Encabezado(idDoc, emisor, receptor, totales);
    }

    public String generateCurrentTimestamp() { return LocalDateTime.now().format(DATETIME_FORMATTER); }
    public String generateCurrentDate() { return LocalDateTime.now().format(DATE_FORMATTER); }

    public static String generateSetDteIdWithChileTime(String rut) {
        validateRut(rut);
        LocalDate chileCurrentDate = LocalDate.now(CHILE_DEFAULT_ZONE);
        String formattedDate = chileCurrentDate.format(DATE_ID_FORMATTER);
        return String.format("%s%s_%s", PREFIX, rut, formattedDate);
    }

    private static void validateRut(String rut) {
        if (rut == null || rut.trim().isEmpty()) throw new IllegalArgumentException("RUT 不能为空");
        String rutRegex = "^\\d{7,8}-[0-9Kk]$";
        if (!rut.matches(rutRegex)) throw new IllegalArgumentException("RUT 格式非法，正确格式示例：78065438-4 或 1234567-K");
    }

    private static String generateFilePrefix(InvoiceData invoiceData) {
        String folio = invoiceData.getFolio() != null ? invoiceData.getFolio() : "unknown";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        return String.format("invoice_%s_%s", folio, timestamp);
    }

    private void validateAgainstBoletaSchema(String xml) {
        try {
            Path schemaPath = Path.of("output", "schema_envio_bol_720", "EnvioBOLETA_v11.xsd");
            if (!Files.exists(schemaPath)) return;
            String schemaContent = Files.readString(schemaPath, StandardCharsets.ISO_8859_1);
            schemaContent = schemaContent.replace("<xs:minInclusive value=\"0.01\"/>", "<xs:minInclusive value=\"0.00\"/>");
            SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            javax.xml.transform.stream.StreamSource schemaSource = new javax.xml.transform.stream.StreamSource(new StringReader(schemaContent));
            schemaSource.setSystemId(schemaPath.toUri().toString());
            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(new javax.xml.transform.stream.StreamSource(new StringReader(xml)));
        } catch (Exception e) {
            System.err.println("本地XSD校验失败: " + e.getMessage());
        }
    }

    private static void validateSignatures(Document doc) {
        try {
            NodeList all = doc.getElementsByTagName("*");
            for (int i=0; i<all.getLength(); i++) {
                Element el = (Element) all.item(i);
                if (el.hasAttribute("ID")) el.setIdAttribute("ID", true);
            }
            NodeList signatures = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
            if (signatures.getLength() == 0) {
                System.out.println("未找到 Signature 节点，跳过验签");
                return;
            }
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            for (int i = 0; i < signatures.getLength(); i++) {
                DOMValidateContext valContext = new DOMValidateContext(new X509KeySelector(), signatures.item(i));
                valContext.setProperty("org.jcp.xml.dsig.secureValidation", Boolean.FALSE);
                XMLSignature signature = fac.unmarshalXMLSignature(valContext);
                boolean coreValid = signature.validate(valContext);
                System.out.println("本地验签[" + i + "]: " + (coreValid ? "有效" : "无效"));

                if (!coreValid) {
                    boolean sv = signature.getSignatureValue().validate(valContext);
                    System.out.println("  SignatureValue: " + (sv ? "有效" : "无效"));
                    List<Reference> refs = signature.getSignedInfo().getReferences();
                    for (int j = 0; j < refs.size(); j++) {
                        Reference ref = refs.get(j);
                        boolean refValid = ref.validate(valContext);
                        System.out.println("  Reference[" + j + "] URI=" + ref.getURI() + ": " + (refValid ? "有效" : "无效"));
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("自动验签异常: " + e.getMessage());
        }
    }

    private static class X509KeySelector extends KeySelector {
        @Override
        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose, AlgorithmMethod method, XMLCryptoContext context) throws KeySelectorException {
            if (keyInfo == null) throw new KeySelectorException("KeyInfo 为空");
            for (Object info : keyInfo.getContent()) {
                if (info instanceof X509Data) {
                    X509Data x509Data = (X509Data) info;
                    for (Object data : x509Data.getContent()) {
                        if (data instanceof X509Certificate) return new SimpleKeySelectorResult(((X509Certificate) data).getPublicKey());
                    }
                } else if (info instanceof KeyValue) {
                    try {
                        return new SimpleKeySelectorResult(((KeyValue) info).getPublicKey());
                    } catch (KeyException e) {
                        throw new KeySelectorException(e);
                    }
                }
            }
            throw new KeySelectorException("未找到可用公钥");
        }
    }

    private static class SimpleKeySelectorResult implements KeySelectorResult {
        private final PublicKey pk;
        SimpleKeySelectorResult(PublicKey pk) { this.pk = pk; }
        @Override public java.security.Key getKey() { return pk; }
    }

    private static void ensureSiiDteDefaultNamespaceDeclared(Element element) {
        if (element == null) return;
        String xmlns = element.getAttribute("xmlns");
        if (xmlns == null || xmlns.isEmpty()) {
            element.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", "http://www.sii.cl/SiiDte");
        }
    }

    private static void normalizeCarriageReturnsInTextNodes(Node node) {
        if (node == null) return;
        short t = node.getNodeType();
        if (t == Node.TEXT_NODE || t == Node.CDATA_SECTION_NODE) {
            String v = node.getNodeValue();
            if (v != null && v.indexOf('\r') >= 0) {
                v = v.replace("\r\n", "\n").replace("\r", "\n");
                node.setNodeValue(v);
            }
            return;
        }
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            normalizeCarriageReturnsInTextNodes(child);
            child = next;
        }
    }

    private static void stripWhitespaceTextNodes(Node node) {
        if (node == null) return;
        Node child = node.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == Node.TEXT_NODE) {
                String v = child.getNodeValue();
                if (v != null && v.trim().isEmpty()) {
                    node.removeChild(child);
                    child = next;
                    continue;
                }
            }
            stripWhitespaceTextNodes(child);
            child = next;
        }
    }
}