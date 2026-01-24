package com.searly.taxcontrol.sii.swingtool;

import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.util.InvoiceGenerator;
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
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class ConsumoFoliosGenerator {

    private static final String NS = "http://www.sii.cl/SiiDte";

    public static String generateAndSign(SiiToolProperties cfg, InvoiceData invoiceData, int secEnvio, KeyStore keyStore, String pfxPassword) throws Exception {
        if (cfg == null) {
            throw new IllegalArgumentException("cfg is null");
        }
        if (invoiceData == null) {
            throw new IllegalArgumentException("invoiceData is null");
        }
        if (keyStore == null) {
            throw new IllegalArgumentException("keyStore is null");
        }
        if (pfxPassword == null) {
            throw new IllegalArgumentException("pfxPassword is null");
        }

        Init.init();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document doc = dbf.newDocumentBuilder().newDocument();

        Element root = doc.createElementNS(NS, "ConsumoFolios");
        root.setAttribute("version", "1.0");
        root.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:ds", Constants.SignatureSpecNS);
        doc.appendChild(root);

        String rutEmisor = cfg.rutEmisor;
        if (rutEmisor == null || rutEmisor.trim().isEmpty()) {
            rutEmisor = invoiceData.getRutEmisor();
        }
        String dateId = ZonedDateTime.now(ZoneId.of("America/Santiago")).format(DateTimeFormatter.ofPattern("yyyyMMdd"));
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

        String todayIso = LocalDate.now(ZoneId.of("America/Santiago")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        appendText(doc, caratula, "FchInicio", todayIso);
        appendText(doc, caratula, "FchFinal", todayIso);
        appendText(doc, caratula, "SecEnvio", String.valueOf(secEnvio));

        String tmst = ZonedDateTime.now(ZoneId.of("America/Santiago")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        appendText(doc, caratula, "TmstFirmaEnv", tmst);

        Element resumen = doc.createElementNS(NS, "Resumen");
        docCf.appendChild(resumen);

        appendText(doc, resumen, "TipoDocumento", String.valueOf(invoiceData.getTipoDTE() == null ? 39 : invoiceData.getTipoDTE()));

        BigDecimal mntNeto = invoiceData.getMntNeto();
        BigDecimal mntExe = invoiceData.getMntExe();
        BigDecimal mntIva = invoiceData.getIva();
        BigDecimal mntTotal = invoiceData.getMntTotal();

        if (mntNeto != null && mntNeto.compareTo(BigDecimal.ZERO) != 0) {
            appendText(doc, resumen, "MntNeto", stripScale(mntNeto));
        }
        if (mntIva != null && mntIva.compareTo(BigDecimal.ZERO) != 0) {
            appendText(doc, resumen, "MntIva", stripScale(mntIva));
            appendText(doc, resumen, "TasaIVA", "19");
        }
        if (mntExe != null && mntExe.compareTo(BigDecimal.ZERO) != 0) {
            appendText(doc, resumen, "MntExento", stripScale(mntExe));
        }

        if (mntTotal == null) {
            throw new IllegalArgumentException("invoiceData.mntTotal is null");
        }
        appendText(doc, resumen, "MntTotal", stripScale(mntTotal));

        appendText(doc, resumen, "FoliosEmitidos", "1");
        appendText(doc, resumen, "FoliosAnulados", "0");
        appendText(doc, resumen, "FoliosUtilizados", "1");

        Element rango = doc.createElementNS(NS, "RangoUtilizados");
        resumen.appendChild(rango);
        appendText(doc, rango, "Inicial", invoiceData.getFolio());
        appendText(doc, rango, "Final", invoiceData.getFolio());

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

        String xml = toXml(doc);

        if (cfg.aliasDocumento != null && !cfg.aliasDocumento.trim().isEmpty()) {
            X509Certificate certDoc = (X509Certificate) keyStore.getCertificate(cfg.aliasDocumento.trim());
            String rut = InvoiceGenerator.extractRutFromCertificate(certDoc);
            if (rut != null && cfg.rutEmisor != null && !rut.equalsIgnoreCase(cfg.rutEmisor.trim())) {
                // no-op: 仅用于调试时可观察签名证书与 RutEmisor 是否一致
            }
        }

        return xml;
    }

    private static String toXml(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");

        StringWriter sw = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(sw));
        String xml = sw.toString();
        xml = xml.replace("\r\n", "\n").replace("\r", "");
        return xml;
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
