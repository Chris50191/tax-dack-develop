package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.swingtool.ConsumoFoliosGenerator;
import com.searly.taxcontrol.sii.swingtool.SiiToolProperties;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

public class GenerateRcofFromEnvioBoleta {

    private static final String NS = "http://www.sii.cl/SiiDte";

    public static void main(String[] args) throws Exception {
        if (args == null || args.length < 1 || args[0] == null || args[0].trim().isEmpty()) {
            System.out.println("用法: java ... com.searly.taxcontrol.sii.util.GenerateRcofFromEnvioBoleta <envioBoletaXmlPath> [secEnvio] [outPath]");
            System.out.println("示例: java ... com.searly.taxcontrol.sii.util.GenerateRcofFromEnvioBoleta output\\batch_1054_1058_20260125_021232_05_最终XML_发送.xml 1");
            return;
        }

        Path envioPath = Paths.get(args[0].trim());
        if (!Files.exists(envioPath)) {
            throw new IllegalArgumentException("EnvioBOLETA XML 不存在: " + envioPath.toAbsolutePath());
        }

        int secEnvio = 1;
        if (args.length >= 2 && args[1] != null && !args[1].trim().isEmpty()) {
            secEnvio = Integer.parseInt(args[1].trim());
        }
        if (secEnvio <= 0) {
            throw new IllegalArgumentException("secEnvio 必须 > 0");
        }

        SiiToolProperties cfg = SiiToolProperties.load(Paths.get("sii-tool.properties"));
        if (cfg == null) {
            throw new IllegalStateException("无法加载 sii-tool.properties");
        }
        if (cfg.certificatePath == null || cfg.certificatePath.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.path 为空，请先在 sii-tool.properties 设置 cert.path");
        }
        if (cfg.certificatePassword == null || cfg.certificatePassword.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.password 为空，请先在 sii-tool.properties 设置 cert.password");
        }

        List<InvoiceData> invoiceDataList = parseInvoiceDataFromEnvioBoleta(envioPath);
        if (invoiceDataList.isEmpty()) {
            throw new IllegalStateException("未从 EnvioBOLETA 解析到任何 Documento");
        }

        KeyStore ks = CertificateManager.loadPKCS12Certificate(cfg.certificatePath, cfg.certificatePassword);
        String rcofXml = ConsumoFoliosGenerator.generateAndSign(cfg, invoiceDataList, secEnvio, ks, cfg.certificatePassword);

        Path out;
        if (args.length >= 3 && args[2] != null && !args[2].trim().isEmpty()) {
            out = Paths.get(args[2].trim());
        } else {
            String date = invoiceDataList.get(0).getFchEmis();
            int[] range = minMaxFolio(invoiceDataList);
            out = Paths.get("output").resolve("RVD_RCOF_" + date + "_SEC" + secEnvio + "_FROM_" + range[0] + "_" + range[1] + ".xml");
        }

        if (out.getParent() != null && !Files.exists(out.getParent())) {
            Files.createDirectories(out.getParent());
        }
        Files.write(out, rcofXml.getBytes(StandardCharsets.ISO_8859_1));
        System.out.println("RVD/RCOF 已生成(从 EnvioBOLETA 反推): " + out.toAbsolutePath());
    }

    private static List<InvoiceData> parseInvoiceDataFromEnvioBoleta(Path envioPath) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().parse(envioPath.toFile());

        NodeList documentos = doc.getElementsByTagNameNS(NS, "Documento");
        List<InvoiceData> out = new ArrayList<>();
        for (int i = 0; i < documentos.getLength(); i++) {
            Node n = documentos.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element documento = (Element) n;

            Element encabezado = firstChild(documento, "Encabezado");
            Element idDoc = encabezado == null ? null : firstChild(encabezado, "IdDoc");
            Element totales = encabezado == null ? null : firstChild(encabezado, "Totales");
            if (idDoc == null || totales == null) {
                continue;
            }

            String tipo = childText(idDoc, "TipoDTE");
            String folio = childText(idDoc, "Folio");
            String fchEmis = childText(idDoc, "FchEmis");
            String mntTotal = childText(totales, "MntTotal");
            if (folio == null || folio.trim().isEmpty() || fchEmis == null || fchEmis.trim().isEmpty() || mntTotal == null || mntTotal.trim().isEmpty()) {
                continue;
            }

            InvoiceData inv = new InvoiceData();
            try {
                inv.setTipoDTE(tipo == null || tipo.trim().isEmpty() ? 39 : Integer.parseInt(tipo.trim()));
            } catch (Exception ignored) {
                inv.setTipoDTE(39);
            }
            inv.setFolio(folio.trim());
            inv.setFchEmis(fchEmis.trim());

            inv.setMntNeto(parseBigDecimal(childText(totales, "MntNeto")));
            BigDecimal exe = parseBigDecimal(childText(totales, "MntExe"));
            if (exe == null) {
                exe = parseBigDecimal(childText(totales, "MntExento"));
            }
            inv.setMntExe(exe);
            inv.setIva(parseBigDecimal(childText(totales, "IVA")));
            inv.setMntTotal(parseBigDecimal(mntTotal));

            out.add(inv);
        }
        return out;
    }

    private static int[] minMaxFolio(List<InvoiceData> list) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (InvoiceData inv : list) {
            if (inv == null || inv.getFolio() == null) {
                continue;
            }
            try {
                int v = Integer.parseInt(inv.getFolio().trim());
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            } catch (Exception ignored) {
            }
        }
        if (min == Integer.MAX_VALUE || max == Integer.MIN_VALUE) {
            return new int[]{0, 0};
        }
        return new int[]{min, max};
    }

    private static BigDecimal parseBigDecimal(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return new BigDecimal(t);
    }

    private static Element firstChild(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        Node cur = parent.getFirstChild();
        while (cur != null) {
            if (cur.getNodeType() == Node.ELEMENT_NODE) {
                Element el = (Element) cur;
                if (localName.equals(el.getLocalName()) && NS.equals(el.getNamespaceURI())) {
                    return el;
                }
            }
            cur = cur.getNextSibling();
        }
        return null;
    }

    private static String childText(Element parent, String localName) {
        Element el = firstChild(parent, localName);
        if (el == null) {
            return null;
        }
        String v = el.getTextContent();
        return v == null ? null : v.trim();
    }
}
