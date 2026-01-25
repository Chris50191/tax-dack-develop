package com.searly.taxcontrol.sii.swingtool;

import com.searly.taxcontrol.sii.model.common.InvoiceData;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConsumoFoliosGenerator {

    private static final String NS = "http://www.sii.cl/SiiDte";
    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    public static String generateAndSign(SiiToolProperties cfg, InvoiceData invoiceData, int secEnvio, KeyStore keyStore, String pfxPassword) throws Exception {
        if (invoiceData == null) {
            throw new IllegalArgumentException("invoiceData is null");
        }
        return generateAndSign(cfg, Collections.singletonList(invoiceData), secEnvio, keyStore, pfxPassword);
    }

    public static String generateAndSign(SiiToolProperties cfg, List<InvoiceData> invoiceDataList, int secEnvio, KeyStore keyStore, String pfxPassword) throws Exception {
        if (cfg == null) {
            throw new IllegalArgumentException("cfg is null");
        }
        if (invoiceDataList == null || invoiceDataList.isEmpty()) {
            throw new IllegalArgumentException("invoiceDataList is empty");
        }
        if (keyStore == null) {
            throw new IllegalArgumentException("keyStore is null");
        }
        if (pfxPassword == null) {
            throw new IllegalArgumentException("pfxPassword is null");
        }
        for (InvoiceData inv : invoiceDataList) {
            if (inv == null) {
                throw new IllegalArgumentException("invoiceDataList contains null");
            }
            if (inv.getMntTotal() == null) {
                throw new IllegalArgumentException("invoiceData.mntTotal is null");
            }
        }

        Init.init();

        InvoiceData first = invoiceDataList.get(0);
        Integer tipo = first.getTipoDTE() == null ? 39 : first.getTipoDTE();
        String fchEmis = first.getFchEmis();
        for (InvoiceData inv : invoiceDataList) {
            Integer t = inv.getTipoDTE() == null ? 39 : inv.getTipoDTE();
            if (!tipo.equals(t)) {
                throw new IllegalArgumentException("COF 仅支持同一 TipoDocumento（TipoDTE）");
            }
            if (fchEmis != null && inv.getFchEmis() != null && !fchEmis.equals(inv.getFchEmis())) {
                throw new IllegalArgumentException("COF 仅支持同一 FchEmis（同一天）");
            }
        }

        List<Integer> folios = invoiceDataList.stream()
                .map(InvoiceData::getFolio)
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .map(Integer::parseInt)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        if (folios.isEmpty()) {
            throw new IllegalArgumentException("folio 无效");
        }
        List<IntRange> ranges = buildRanges(folios);

        BigDecimal sumNeto = BigDecimal.ZERO;
        BigDecimal sumExe = BigDecimal.ZERO;
        BigDecimal sumIva = BigDecimal.ZERO;
        BigDecimal sumTotal = BigDecimal.ZERO;
        for (InvoiceData inv : invoiceDataList) {
            if (inv.getMntNeto() != null) sumNeto = sumNeto.add(inv.getMntNeto());
            if (inv.getMntExe() != null) sumExe = sumExe.add(inv.getMntExe());
            if (inv.getIva() != null) sumIva = sumIva.add(inv.getIva());
            sumTotal = sumTotal.add(inv.getMntTotal());
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        Element root = doc.createElementNS(NS, "ConsumoFolios");
        root.setAttribute("version", "1.0");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xsi", XSI_NS);
        root.setAttributeNS(XSI_NS, "xsi:schemaLocation", "http://www.sii.cl/SiiDte ConsumoFolio_v10.xsd");
        doc.appendChild(root);

        String rutEmisor = cfg.rutEmisor;
        if (rutEmisor == null || rutEmisor.trim().isEmpty()) {
            rutEmisor = first.getRutEmisor();
        }
        String dateId;
        if (fchEmis != null && fchEmis.trim().matches("\\d{4}-\\d{2}-\\d{2}")) {
            dateId = fchEmis.trim().replace("-", "");
        } else {
            dateId = ZonedDateTime.now(ZoneId.of("America/Santiago")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        }
        String id = "CF_" + safeId(rutEmisor) + "_" + dateId + "_" + secEnvio;

        Element docCf = doc.createElementNS(NS, "DocumentoConsumoFolios");
        docCf.setAttribute("ID", id);
        docCf.setIdAttribute("ID", true);
        root.appendChild(docCf);

        Element caratula = doc.createElementNS(NS, "Caratula");
        caratula.setAttribute("version", "1.0");
        docCf.appendChild(caratula);

        appendText(doc, caratula, "RutEmisor", cfg.rutEmisor);
        appendText(doc, caratula, "RutEnvia", cfg.rutEnvia);
        appendText(doc, caratula, "FchResol", cfg.getFchResolIso());
        appendText(doc, caratula, "NroResol", safe(cfg.nroResol, "0"));

        String fch = (fchEmis == null || fchEmis.trim().isEmpty())
                ? LocalDate.now(ZoneId.of("America/Santiago")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                : fchEmis.trim();
        appendText(doc, caratula, "FchInicio", fch);
        appendText(doc, caratula, "FchFinal", fch);
        appendText(doc, caratula, "SecEnvio", String.valueOf(secEnvio));

        String tmst = ZonedDateTime.now(ZoneId.of("America/Santiago")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        appendText(doc, caratula, "TmstFirmaEnv", tmst);

        Element resumen = doc.createElementNS(NS, "Resumen");
        docCf.appendChild(resumen);

        appendText(doc, resumen, "TipoDocumento", String.valueOf(tipo));

        if (sumNeto.compareTo(BigDecimal.ZERO) != 0) {
            appendText(doc, resumen, "MntNeto", stripScale(sumNeto));
        }
        if (sumIva.compareTo(BigDecimal.ZERO) != 0) {
            appendText(doc, resumen, "MntIva", stripScale(sumIva));
            appendText(doc, resumen, "TasaIVA", "19");
        }
        if (sumExe.compareTo(BigDecimal.ZERO) != 0) {
            appendText(doc, resumen, "MntExento", stripScale(sumExe));
        }
        appendText(doc, resumen, "MntTotal", stripScale(sumTotal));

        String count = String.valueOf(folios.size());
        appendText(doc, resumen, "FoliosEmitidos", count);
        appendText(doc, resumen, "FoliosAnulados", "0");
        appendText(doc, resumen, "FoliosUtilizados", count);

        for (IntRange r : ranges) {
            Element rango = doc.createElementNS(NS, "RangoUtilizados");
            resumen.appendChild(rango);
            appendText(doc, rango, "Inicial", String.valueOf(r.start));
            appendText(doc, rango, "Final", String.valueOf(r.end));
        }

        String alias = cfg.aliasSetDte != null && !cfg.aliasSetDte.trim().isEmpty() ? cfg.aliasSetDte.trim() : null;
        if (alias == null || alias.trim().isEmpty()) {
            alias = keyStore.aliases().nextElement();
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, pfxPassword.toCharArray());
        X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
        if (privateKey == null || cert == null) {
            throw new IllegalStateException("无法从 KeyStore 读取私钥/证书: alias=" + alias);
        }

        ElementProxy.setDefaultPrefix(Constants.SignatureSpecNS, "");
        XMLSignature sig = new XMLSignature(
                doc,
                "",
                XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                org.apache.xml.security.c14n.Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS
        );
        sig.getElement().setPrefix(null);
        sig.getElement().setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", Constants.SignatureSpecNS);

        root.appendChild(sig.getElement());

        Transforms transforms = new Transforms(doc);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        sig.addDocument("#" + id, transforms, org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);
        sig.addKeyInfo(cert.getPublicKey());
        sig.addKeyInfo(cert);
        sig.sign(privateKey);

        return toXml(doc);
    }

    private static class IntRange {
        final int start;
        final int end;

        private IntRange(int start, int end) {
            this.start = start;
            this.end = end;
        }
    }

    private static List<IntRange> buildRanges(List<Integer> sortedFolios) {
        if (sortedFolios == null || sortedFolios.isEmpty()) {
            return Collections.emptyList();
        }
        int start = sortedFolios.get(0);
        int prev = start;
        java.util.ArrayList<IntRange> out = new java.util.ArrayList<>();
        for (int i = 1; i < sortedFolios.size(); i++) {
            int cur = sortedFolios.get(i);
            if (cur == prev + 1) {
                prev = cur;
                continue;
            }
            out.add(new IntRange(start, prev));
            start = cur;
            prev = cur;
        }
        out.add(new IntRange(start, prev));
        return out;
    }

    private static String toXml(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String xmlBody = sw.toString();
        xmlBody = xmlBody.replace("\r\n", "\n").replace("\r", "");
        xmlBody = xmlBody.trim();
        xmlBody = normalizeConsumoFoliosRoot(xmlBody);

        String xml = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + xmlBody;
        return xml;
    }

    private static String normalizeConsumoFoliosRoot(String xml) {
        if (xml == null || xml.isEmpty()) {
            return xml;
        }

        int start = xml.indexOf("<ConsumoFolios");
        if (start < 0) {
            return xml;
        }
        int end = xml.indexOf(">", start);
        if (end < 0) {
            return xml;
        }

        String tag = xml.substring(start, end + 1);
        String xmlns = extractAttr(tag, "xmlns=\"", "\"");
        String xmlnsXsi = extractAttr(tag, "xmlns:xsi=\"", "\"");
        String schemaLoc = extractAttr(tag, "xsi:schemaLocation=\"", "\"");
        String version = extractAttr(tag, "version=\"", "\"");

        StringBuilder rebuilt = new StringBuilder();
        rebuilt.append("<ConsumoFolios");
        rebuilt.append(" xmlns=\"").append(xmlns == null ? NS : xmlns).append("\"");
        if (xmlnsXsi != null) {
            rebuilt.append(" xmlns:xsi=\"").append(xmlnsXsi).append("\"");
        }
        if (schemaLoc != null) {
            rebuilt.append(" xsi:schemaLocation=\"").append(schemaLoc).append("\"");
        }
        if (version != null) {
            rebuilt.append(" version=\"").append(version).append("\"");
        }
        rebuilt.append(">");

        return xml.substring(0, start) + rebuilt + xml.substring(end + 1);
    }

    private static String extractAttr(String tag, String prefix, String endToken) {
        if (tag == null || prefix == null || endToken == null) {
            return null;
        }
        int i = tag.indexOf(prefix);
        if (i < 0) {
            return null;
        }
        int start = i + prefix.length();
        int end = tag.indexOf(endToken, start);
        if (end < 0) {
            return null;
        }
        return tag.substring(start, end);
    }

    private static void appendText(Document doc, Element parent, String tag, String value) {
        Element el = doc.createElementNS(NS, tag);
        el.setTextContent(value == null ? "" : value);
        parent.appendChild(el);
    }

    private static String stripScale(BigDecimal v) {
        return v == null ? "" : v.stripTrailingZeros().toPlainString();
    }

    private static String safe(String s, String def) {
        if (s == null) {
            return def;
        }
        String t = s.trim();
        return t.isEmpty() ? def : t;
    }

    private static String safeId(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.replace("-", "");
    }
}
