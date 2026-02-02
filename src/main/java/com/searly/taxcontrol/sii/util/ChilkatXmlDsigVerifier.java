package com.searly.taxcontrol.sii.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.xml.security.Init;
import org.apache.xml.security.keys.KeyInfo;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.utils.Constants;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ChilkatXmlDsigVerifier {

    public static void main(String[] args) throws Exception {
        String pathStr = args != null && args.length > 0 ? args[0] : null;
        if (pathStr == null || pathStr.trim().isEmpty()) {
            System.err.println("请传入要验签的XML文件路径，例如: temp\\SET_CASO-1_F1_EnvioBOLETA.xml");
            System.exit(2);
            return;
        }

        Path path = Path.of(pathStr);
        if (!Files.exists(path)) {
            System.err.println("文件不存在: " + path.toAbsolutePath());
            System.exit(3);
            return;
        }

        String xml = Files.readString(path, StandardCharsets.ISO_8859_1);
        boolean ok = verifyAll(xml);
        if (!ok) {
            String stripped = stripXsiSchemaLocationForChilkat(xml);
            if (!stripped.equals(xml)) {
                System.out.println("--- Retry after stripping xmlns:xsi/xsi:schemaLocation ---");
                ok = verifyAll(stripped);
            }
        }

        System.out.println("FINAL_RESULT=" + (ok ? "OK" : "FAIL"));
        if (!ok) {
            System.exit(10);
        }
    }

    private static boolean verifyAll(String xml) {
        try {
            if (xml == null || xml.trim().isEmpty()) {
                System.out.println("Santuario验签：XML为空");
                return false;
            }

            Init.init();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(new java.io.ByteArrayInputStream(xml.getBytes(StandardCharsets.ISO_8859_1)));

            registerAllIds(doc);

            NodeList sigNodes = doc.getElementsByTagNameNS(Constants.SignatureSpecNS, "Signature");
            if (sigNodes == null) {
                return false;
            }

            int numSignatures = sigNodes.getLength();
            System.out.println("NumSignatures=" + numSignatures);
            if (numSignatures == 0) {
                return false;
            }

            boolean allOk = true;
            for (int i = 0; i < numSignatures; i++) {
                Node n = sigNodes.item(i);
                if (!(n instanceof Element)) {
                    continue;
                }
                Element sigEl = (Element) n;
                XMLSignature sig = new XMLSignature(sigEl, "");

                PublicKey pk = null;
                KeyInfo ki = sig.getKeyInfo();
                if (ki != null) {
                    try {
                        pk = ki.getPublicKey();
                    } catch (Exception ignored) {
                        pk = null;
                    }

                    if (pk == null) {
                        try {
                            X509Certificate cert = ki.getX509Certificate();
                            if (cert != null) {
                                pk = cert.getPublicKey();
                            }
                        } catch (Exception ignored) {
                            pk = null;
                        }
                    }
                }

                boolean sigOk = false;
                try {
                    if (pk != null) {
                        sigOk = sig.checkSignatureValue(pk);
                    }
                } catch (Exception ignored) {
                    sigOk = false;
                }

                System.out.println("Signature " + (i + 1) + " VerifySignature(true)=" + sigOk);
                if (!sigOk) {
                    allOk = false;
                }
            }

            return allOk;
        } catch (Exception e) {
            System.out.println("Santuario验签异常: " + e.getMessage());
            return false;
        }
    }

    private static void registerAllIds(Document doc) {
        if (doc == null) {
            return;
        }
        NodeList all = doc.getElementsByTagName("*");
        if (all == null) {
            return;
        }
        for (int i = 0; i < all.getLength(); i++) {
            Node n = all.item(i);
            if (n instanceof Element) {
                Element e = (Element) n;
                try {
                    if (e.hasAttribute("ID")) {
                        e.setIdAttribute("ID", true);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private static String stripXsiSchemaLocationForChilkat(String xml) {
        if (xml == null || xml.isEmpty()) return xml;

        // Chilkat 的 "Preprocessing for www.sii.cl" 在存在 xmlns:xsi/xsi:schemaLocation 时
        // 可能导致 Reference digest 计算与本地生成不一致。这里仅用于本地验签诊断，发送给 SII 的 XML 不应移除。
        String out = xml;
        out = out.replaceAll("\\s+xmlns:xsi=\"http://www\\.w3\\.org/2001/XMLSchema-instance\"", "");
        out = out.replaceAll("\\s+xsi:schemaLocation=\"http://www\\.sii\\.cl/SiiDte\\s+EnvioBOLETA_v11\\.xsd\"", "");
        return out;
    }
}
