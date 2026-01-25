package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.swingtool.SiiToolProperties;
import org.apache.xml.security.Init;
import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315ExclOmitComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315OmitComments;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.signature.Reference;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.apache.xml.security.utils.ElementProxy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class SantuarioChilkatSignatureLab {

    private static final String XMLDSIG_NS = Constants.SignatureSpecNS;

    private static final class SigSnapshot {
        final String owner;
        final String referenceUri;
        final List<String> transformAlgos;
        final String digestMethod;
        final String digestValue;
        final String c14nMethod;
        final String signatureMethod;
        final String signatureValue;
        final String embeddedX509CertB64;

        SigSnapshot(String owner,
                    String referenceUri,
                    List<String> transformAlgos,
                    String digestMethod,
                    String digestValue,
                    String c14nMethod,
                    String signatureMethod,
                    String signatureValue,
                    String embeddedX509CertB64) {
            this.owner = owner;
            this.referenceUri = referenceUri;
            this.transformAlgos = transformAlgos;
            this.digestMethod = digestMethod;
            this.digestValue = digestValue;
            this.c14nMethod = c14nMethod;
            this.signatureMethod = signatureMethod;
            this.signatureValue = signatureValue;
            this.embeddedX509CertB64 = embeddedX509CertB64;
        }
    }

    private static final class Variant {
        final String name;
        final String sigPrefix; // "" means default namespace (no prefix)
        final boolean forceXmlnsAttr;
        final String signedInfoC14nAlgo;
        final String documentoRefTransformOverride;

        Variant(String name, String sigPrefix, boolean forceXmlnsAttr, String signedInfoC14nAlgo, String documentoRefTransformOverride) {
            this.name = name;
            this.sigPrefix = sigPrefix;
            this.forceXmlnsAttr = forceXmlnsAttr;
            this.signedInfoC14nAlgo = signedInfoC14nAlgo;
            this.documentoRefTransformOverride = documentoRefTransformOverride;
        }
    }

    public static void main(String[] args) throws Exception {
        Init.init();

        maybeInstallHybridC14NTransform();

        if (args == null || args.length == 0) {
            System.out.println("用法: java ... com.searly.taxcontrol.sii.util.SantuarioChilkatSignatureLab <xmlPath1> [xmlPath2 ...]");
            System.out.println("示例: java ... com.searly.taxcontrol.sii.util.SantuarioChilkatSignatureLab output\\RVD_RCOF_2026-01-25_SEC1_FROM_1054_1058_NEW.xml output\\batch_1059_1063_20260125_022124_05_最终XML_发送.xml");
            return;
        }

        SiiToolProperties cfg = SiiToolProperties.load(Path.of("sii-tool.properties"));
        if (cfg == null) {
            throw new IllegalStateException("无法加载 sii-tool.properties");
        }
        if (cfg.certificatePath == null || cfg.certificatePath.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.path 为空，请先在 sii-tool.properties 设置 cert.path");
        }
        if (cfg.certificatePassword == null || cfg.certificatePassword.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.password 为空，请先在 sii-tool.properties 设置 cert.password");
        }

        KeyStore ks = CertificateManager.loadPKCS12Certificate(cfg.certificatePath, cfg.certificatePassword);
        if (ks == null) {
            throw new IllegalStateException("KeyStore 加载失败");
        }

        List<Variant> variants = new ArrayList<>();
        variants.add(new Variant(
                "A_inclusiveSI_docUseXmlTransform",
                "",
                true,
                Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS,
                null
        ));
        variants.add(new Variant(
                "B_inclusiveSI_docForceExclusiveTransform",
                "",
                true,
                Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS,
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS
        ));
        variants.add(new Variant(
                "C_exclusiveSI_docForceExclusiveTransform",
                "",
                true,
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS
        ));
        variants.add(new Variant(
                "D_exclusiveSI_docUseXmlTransform",
                "",
                true,
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                null
        ));

        boolean skipResign = Boolean.parseBoolean(System.getProperty("lab.skipResign", "false"));
        int maxResign = -1;
        try {
            maxResign = Integer.parseInt(System.getProperty("lab.maxResign", "-1").trim());
        } catch (Exception ignored) {
            maxResign = -1;
        }

        for (String p : args) {
            if (p == null || p.trim().isEmpty()) {
                continue;
            }
            Path xmlPath = Path.of(p.trim());
            if (!Files.exists(xmlPath)) {
                throw new IllegalArgumentException("XML 不存在: " + xmlPath.toAbsolutePath());
            }

            System.out.println("\n============================");
            System.out.println("Target XML: " + xmlPath.toAbsolutePath());

            Document originalDoc = parseXml(xmlPath);
            registerAllIds(originalDoc);
            List<Element> originalSigEls = findSignatureElements(originalDoc);
            if (originalSigEls.isEmpty()) {
                System.out.println("未找到 Signature 节点。");
                continue;
            }

            List<SigSnapshot> snapshots = snapshotSignatures(originalDoc, originalSigEls);
            System.out.println("签名数量: " + snapshots.size());

            System.out.println("\n[1A] Digest 诊断：尝试不同 C14N 算法计算 Reference Digest 以命中原 DigestValue...");
            diagnoseReferenceDigests(originalDoc, snapshots);

            System.out.println("\n[1] 使用 Santuario 对原始(Chilkat)签名做验签与重算校验...");
            verifyAndRecompute(originalDoc, originalSigEls, ks, cfg.certificatePassword);

            if (skipResign) {
                System.out.println("\n[2] skipResign=true，跳过重签枚举阶段。");
                continue;
            }

            boolean found = false;
            for (Variant v : variants) {
                System.out.println("\n[2] Variant: " + v.name);

                boolean allMatch = true;
                for (int i = 0; i < snapshots.size(); i++) {
                    if (maxResign > 0 && i >= maxResign) {
                        break;
                    }
                    SigSnapshot expect = snapshots.get(i);

                    Document doc = parseXml(xmlPath);
                    registerAllIds(doc);

                    Element oldSig = findSignatureByOwner(doc, expect.owner);
                    if (oldSig == null) {
                        System.out.println("  - [" + i + "] 找不到目标 Signature: owner=" + expect.owner);
                        allMatch = false;
                        continue;
                    }

                    Node parent = oldSig.getParentNode();
                    Node next = oldSig.getNextSibling();
                    parent.removeChild(oldSig);

                    String refId = stripHash(expect.referenceUri);
                    Element refEl = findById(doc, refId);
                    if (refEl == null) {
                        System.out.println("  - [" + i + "] 找不到 Reference 目标节点: URI=" + expect.referenceUri + " owner=" + expect.owner);
                        allMatch = false;
                        continue;
                    }
                    markIdAttribute(refEl);

                    boolean debugRefDigest = Boolean.parseBoolean(System.getProperty("lab.debugRefDigest", "false"));
                    if (debugRefDigest && i == 0) {
                        try {
                            ByteArrayOutputStream baosEx = new ByteArrayOutputStream();
                            new Canonicalizer20010315ExclOmitComments().engineCanonicalizeSubTree(refEl, baosEx);
                            String exB64 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baosEx.toByteArray()));

                            ByteArrayOutputStream baosIn = new ByteArrayOutputStream();
                            new Canonicalizer20010315OmitComments().engineCanonicalizeSubTree(refEl, baosIn);
                            String inB64 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(baosIn.toByteArray()));

                            System.out.println("    [lab] debugRefDigest owner=" + expect.owner + " refId=" + refId);
                            System.out.println("    [lab] debugRefDigest expectedDigest=" + expect.digestValue);
                            System.out.println("    [lab] debugRefDigest exc-c14nDigest=" + exB64);
                            System.out.println("    [lab] debugRefDigest inc-c14nDigest=" + inB64);
                        } catch (Exception e) {
                            System.out.println("    [lab] debugRefDigest error: " + e.getMessage());
                        }
                    }

                    ElementProxy.setDefaultPrefix(XMLDSIG_NS, v.sigPrefix);

                    XMLSignature sig = new XMLSignature(
                            doc,
                            "",
                            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA1,
                            v.signedInfoC14nAlgo
                    );

                    Element sigEl = sig.getElement();
                    if (v.sigPrefix == null || v.sigPrefix.isEmpty()) {
                        sigEl.setPrefix(null);
                        if (v.forceXmlnsAttr) {
                            sigEl.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns", XMLDSIG_NS);
                        }
                    } else {
                        sigEl.setPrefix(v.sigPrefix);
                    }

                    if (next != null) {
                        parent.insertBefore(sigEl, next);
                    } else {
                        parent.appendChild(sigEl);
                    }

                    Transforms transforms = new Transforms(doc);

                    boolean isDocumento = expect.owner != null && expect.owner.startsWith("Documento#");
                    if (isDocumento && v.documentoRefTransformOverride != null && !v.documentoRefTransformOverride.trim().isEmpty()) {
                        transforms.addTransform(v.documentoRefTransformOverride);
                    } else {
                        for (String t : expect.transformAlgos) {
                            transforms.addTransform(t);
                        }
                    }

                    sig.addDocument("#" + refId, transforms, org.apache.xml.security.utils.Constants.ALGO_ID_DIGEST_SHA1);

                    X509Certificate embedded = parseX509FromSnapshot(expect.embeddedX509CertB64);
                    String aliasForSig = findAliasByPublicKey(ks, embedded == null ? null : embedded.getPublicKey());
                    if (aliasForSig == null) {
                        aliasForSig = ks.aliases().nextElement();
                    }
                    PrivateKey pk = (PrivateKey) ks.getKey(aliasForSig, cfg.certificatePassword.toCharArray());
                    X509Certificate certForSig = embedded != null ? embedded : (X509Certificate) ks.getCertificate(aliasForSig);
                    if (pk == null || certForSig == null) {
                        throw new IllegalStateException("无法为签名选择私钥/证书: owner=" + expect.owner + ", alias=" + aliasForSig);
                    }

                    sig.addKeyInfo((PublicKey) certForSig.getPublicKey());
                    sig.addKeyInfo(certForSig);

                    boolean injectWhitespace = Boolean.parseBoolean(System.getProperty("lab.injectChilkatWhitespace", "false"));
                    if (injectWhitespace && isDocumento) {
                        try {
                            injectChilkatWhitespace(sig.getElement());
                        } catch (Exception ignored) {
                        }
                    }

                    boolean traceHybrid = Boolean.parseBoolean(System.getProperty("lab.traceHybrid", "false"));
                    if (traceHybrid) {
                        System.out.println("    [lab] traceHybrid beforeSign variant=" + v.name + " owner=" + expect.owner + " refId=" + refId);
                    }
                    sig.sign(pk);
                    if (traceHybrid) {
                        System.out.println("    [lab] traceHybrid afterSign variant=" + v.name + " owner=" + expect.owner + " refId=" + refId);
                    }

                    boolean debugAfterSign = Boolean.parseBoolean(System.getProperty("lab.debugAfterSign", "false"));
                    if (debugAfterSign && i == 0) {
                        try {
                            if (sig.getSignedInfo() != null && sig.getSignedInfo().getLength() > 0) {
                                Reference rr = sig.getSignedInfo().item(0);
                                byte[] stored = rr.getDigestValue();
                                String storedB64 = stored == null ? "" : Base64.getEncoder().encodeToString(stored);

                                XMLSignatureInput xinBefore = rr.getContentsBeforeTransformation();
                                byte[] bb = xinBefore == null ? null : xinBefore.getBytes();
                                String beforeB64 = "";
                                int beforeLen = bb == null ? -1 : bb.length;
                                if (bb != null) {
                                    beforeB64 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(bb));
                                }

                                XMLSignatureInput xin = rr.getContentsAfterTransformation();
                                byte[] b = xin == null ? null : xin.getBytes();
                                String recomputedB64 = "";
                                int bytesLen = b == null ? -1 : b.length;
                                if (b != null) {
                                    recomputedB64 = Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest(b));
                                }

                                System.out.println("    [lab] debugAfterSign storedDigest=" + storedB64);
                                System.out.println("    [lab] debugAfterSign beforeDigest=" + beforeB64 + " bytesLen=" + beforeLen);
                                System.out.println("    [lab] debugAfterSign recomputedDigest=" + recomputedB64 + " bytesLen=" + bytesLen);
                            }
                        } catch (Exception e) {
                            System.out.println("    [lab] debugAfterSign error: " + e.getMessage());
                        }
                    }

                    SigSnapshot actual = snapshotSignatureSingle(doc, sig.getElement(), expect.owner);

                    boolean digestMatch = normalizeB64(expect.digestValue).equals(normalizeB64(actual.digestValue));
                    boolean sigMatch = normalizeB64(expect.signatureValue).equals(normalizeB64(actual.signatureValue));

                    boolean verifyOk;
                    try {
                        verifyOk = sig.checkSignatureValue(certForSig);
                    } catch (Exception e) {
                        verifyOk = false;
                    }

                    System.out.println("  - [" + i + "] owner=" + expect.owner
                            + " digest=" + (digestMatch ? "OK" : "DIFF")
                            + " signature=" + (sigMatch ? "OK" : "DIFF")
                            + " verify=" + (verifyOk ? "OK" : "FAIL"));

                    if (!digestMatch) {
                        System.out.println("    expectDigest=" + expect.digestValue);
                        System.out.println("    actualDigest=" + actual.digestValue);
                    }
                    if (!sigMatch) {
                        System.out.println("    expectSig=" + shortB64(expect.signatureValue));
                        System.out.println("    actualSig=" + shortB64(actual.signatureValue));
                    }

                    if (!(digestMatch && sigMatch)) {
                        allMatch = false;
                    }
                }

                if (allMatch) {
                    System.out.println("\n>>> SUCCESS: Variant " + v.name + " 可 100% 复现 Chilkat 的 DigestValue 与 SignatureValue");
                    found = true;
                    break;
                }
            }

            if (!found) {
                System.out.println("\n>>> 未找到可完全匹配的 Variant（仍可扩展枚举：CanonicalizationMethod/Transforms/Prefix/KeyInfo 结构）。");
            }
        }
    }

    private static void injectChilkatWhitespace(Element signatureEl) {
        if (signatureEl == null) {
            return;
        }
        Document doc = signatureEl.getOwnerDocument();
        if (doc == null) {
            return;
        }

        Element signedInfoEl = firstChildByLocalName(signatureEl, "SignedInfo");
        if (signedInfoEl == null) {
            return;
        }

        injectNewlinesBetweenElementChildren(doc, signedInfoEl);

        Element refEl = firstChildByLocalName(signedInfoEl, "Reference");
        if (refEl != null) {
            injectNewlinesBetweenElementChildren(doc, refEl);
            Element transformsEl = firstChildByLocalName(refEl, "Transforms");
            if (transformsEl != null) {
                injectNewlinesBetweenElementChildren(doc, transformsEl);
            }
        }
    }

    private static void injectNewlinesBetweenElementChildren(Document doc, Element parent) {
        if (doc == null || parent == null) {
            return;
        }

        Node child = parent.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Node prev = child.getPreviousSibling();
                if (!(prev != null && prev.getNodeType() == Node.TEXT_NODE && "\n".equals(prev.getNodeValue()))) {
                    parent.insertBefore(doc.createTextNode("\n"), child);
                }
            }
            child = next;
        }

        Node last = parent.getLastChild();
        if (last != null && last.getNodeType() == Node.ELEMENT_NODE) {
            parent.appendChild(doc.createTextNode("\n"));
        }
    }

    private static void verifyAndRecompute(Document doc, List<Element> sigEls, KeyStore ks, String pfxPassword) throws Exception {
        for (int i = 0; i < sigEls.size(); i++) {
            Element sigEl = sigEls.get(i);
            SigSnapshot snap = snapshotSignatureSingle(doc, sigEl, ownerOfSignature(sigEl));

            try {
                XMLSignature sig = new XMLSignature(sigEl, "");
                X509Certificate embeddedCert = extractEmbeddedCertificateFromSignature(sigEl);
                X509Certificate verifyCert = embeddedCert;
                if (verifyCert == null) {
                    String anyAlias = ks.aliases().nextElement();
                    verifyCert = (X509Certificate) ks.getCertificate(anyAlias);
                }

                boolean ok = sig.checkSignatureValue(verifyCert);
                System.out.println("  - [" + i + "] verify=" + (ok ? "OK" : "FAIL") + " owner=" + snap.owner);

                try {
                    String siC14nUri = sig.getSignedInfo() == null ? "" : sig.getSignedInfo().getCanonicalizationMethodURI();
                    System.out.println("    signedInfo.c14nUri=" + siC14nUri);
                } catch (Exception ignored) {
                }

                try {
                    boolean siOk = sig.getSignedInfo() != null && sig.getSignedInfo().verify();
                    System.out.println("    signedInfo.verify=" + (siOk ? "OK" : "FAIL"));
                } catch (Exception e) {
                    System.out.println("    signedInfo.verify=ERROR err=" + e.getMessage());
                }

                try {
                    if (sig.getSignedInfo() != null) {
                        int len = sig.getSignedInfo().getLength();
                        for (int r = 0; r < len; r++) {
                            Reference ref = sig.getSignedInfo().item(r);
                            boolean refOk;
                            try {
                                refOk = ref.verify();
                            } catch (Exception ex) {
                                refOk = false;
                            }
                            System.out.println("    ref[" + r + "].uri=" + ref.getURI() + " verify=" + (refOk ? "OK" : "FAIL"));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("    ref.verify=ERROR err=" + e.getMessage());
                }

                if (!ok) {
                    try {
                        if (sig.getSignedInfo() != null && sig.getSignedInfo().getLength() > 0) {
                            Reference ref = sig.getSignedInfo().item(0);
                            String uri = ref.getURI();
                            byte[] storedDigest = ref.getDigestValue();
                            String storedB64 = storedDigest == null ? "" : Base64.getEncoder().encodeToString(storedDigest);

                            XMLSignatureInput in = ref.getContentsAfterTransformation();
                            byte[] bytes = in == null ? null : in.getBytes();
                            String recomputedDigestB64 = "";
                            int len = bytes == null ? -1 : bytes.length;
                            if (bytes != null) {
                                MessageDigest md = MessageDigest.getInstance("SHA-1");
                                recomputedDigestB64 = Base64.getEncoder().encodeToString(md.digest(bytes));
                            }

                            System.out.println("    refUri=" + uri + " storedDigest=" + storedB64 + " recomputedDigest=" + recomputedDigestB64 + " bytesLen=" + len);
                        }
                    } catch (Exception e) {
                        System.out.println("    refDiag=ERROR err=" + e.getMessage());
                    }
                }

                try {
                    Element signedInfoEl = firstChildByLocalName(sigEl, "SignedInfo");
                    Element sigValueEl = firstChildByLocalName(sigEl, "SignatureValue");
                    if (signedInfoEl == null || sigValueEl == null) {
                        System.out.println("    signedInfoOrSignatureValueMissing");
                        continue;
                    }

                    byte[] sigBytes = Base64.getDecoder().decode(normalizeB64(sigValueEl.getTextContent()));

                    String[] algos = new String[]{
                            Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS,
                            Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS
                    };

                    String aliasForSig = findAliasByPublicKey(ks, verifyCert == null ? null : verifyCert.getPublicKey());
                    if (aliasForSig == null) {
                        aliasForSig = ks.aliases().nextElement();
                    }
                    PrivateKey privateKey = (PrivateKey) ks.getKey(aliasForSig, pfxPassword.toCharArray());

                    boolean anyAlgoVerified = false;
                    for (String algo : algos) {
                        try {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            Canonicalizer.getInstance(algo).canonicalizeSubtree(signedInfoEl, baos);
                            byte[] c14nBytes = baos.toByteArray();

                            Signature verifier = Signature.getInstance("SHA1withRSA");
                            verifier.initVerify(verifyCert.getPublicKey());
                            verifier.update(c14nBytes);
                            boolean verified = verifier.verify(sigBytes);

                            if (verified) {
                                anyAlgoVerified = true;
                                System.out.println("    signedInfoVerifyByAlgo=OK algo=" + algo);

                                if (privateKey != null) {
                                    Signature signer = Signature.getInstance("SHA1withRSA");
                                    signer.initSign(privateKey);
                                    signer.update(c14nBytes);
                                    String recomputedB64 = Base64.getEncoder().encodeToString(signer.sign());

                                    boolean same = normalizeB64(recomputedB64).equals(normalizeB64(snap.signatureValue));
                                    System.out.println("    recomputeSignedInfoSignature=" + (same ? "MATCH" : "DIFF") + " algo=" + algo);
                                } else {
                                    System.out.println("    recomputeSignedInfoSignature=SKIP (privateKey null)");
                                }
                                break;
                            }
                        } catch (Exception ignored) {
                        }
                    }

                    if (!anyAlgoVerified) {
                        System.out.println("    signedInfoVerifyByAlgo=FAIL (tested inclusive/exclusive omit-comments)");
                    }
                } catch (Exception e) {
                    System.out.println("    signedInfoVerifyDiag=ERROR err=" + e.getMessage());
                }
            } catch (Exception e) {
                System.out.println("  - [" + i + "] verify=ERROR owner=" + snap.owner + " err=" + e.getMessage());
            }
        }
    }

    private static X509Certificate extractEmbeddedCertificateFromSignature(Element sigEl) {
        try {
            NodeList list = sigEl.getElementsByTagNameNS(XMLDSIG_NS, "X509Certificate");
            if (list == null || list.getLength() == 0) {
                return null;
            }
            Node n = list.item(0);
            if (!(n instanceof Element)) {
                return null;
            }
            String b64 = ((Element) n).getTextContent();
            if (b64 == null || b64.trim().isEmpty()) {
                return null;
            }
            String normalized = normalizeB64(b64);
            byte[] der = Base64.getDecoder().decode(normalized);

            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
        } catch (Exception e) {
            return null;
        }
    }

    private static String findAliasByPublicKey(KeyStore ks, PublicKey pk) {
        if (ks == null || pk == null) {
            return null;
        }
        try {
            for (java.util.Enumeration<String> e = ks.aliases(); e.hasMoreElements(); ) {
                String alias = e.nextElement();
                java.security.cert.Certificate c = ks.getCertificate(alias);
                if (!(c instanceof X509Certificate)) {
                    continue;
                }
                PublicKey cpk = ((X509Certificate) c).getPublicKey();
                if (cpk != null && java.util.Arrays.equals(cpk.getEncoded(), pk.getEncoded())) {
                    return alias;
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String extractEmbeddedX509CertText(Element sigEl) {
        try {
            NodeList list = sigEl.getElementsByTagNameNS(XMLDSIG_NS, "X509Certificate");
            if (list == null || list.getLength() == 0) {
                return "";
            }
            Node n = list.item(0);
            if (!(n instanceof Element)) {
                return "";
            }
            String b64 = ((Element) n).getTextContent();
            return b64 == null ? "" : normalizeB64(b64);
        } catch (Exception e) {
            return "";
        }
    }

    private static X509Certificate parseX509FromSnapshot(String b64) {
        try {
            if (b64 == null || b64.trim().isEmpty()) {
                return null;
            }
            byte[] der = Base64.getDecoder().decode(normalizeB64(b64));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(der));
        } catch (Exception e) {
            return null;
        }
    }

    private static void diagnoseReferenceDigests(Document doc, List<SigSnapshot> snapshots) {
        if (doc == null || snapshots == null || snapshots.isEmpty()) {
            return;
        }

        final String[] algos = new String[]{
                Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS,
                Canonicalizer.ALGO_ID_C14N_WITH_COMMENTS,
                Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS,
                Canonicalizer.ALGO_ID_C14N_EXCL_WITH_COMMENTS
        };

        for (SigSnapshot s : snapshots) {
            String refId = stripHash(s.referenceUri);
            if (refId.isEmpty()) {
                continue;
            }
            Element refEl = findById(doc, refId);
            if (refEl == null) {
                System.out.println("  - owner=" + s.owner + " ref=" + s.referenceUri + " element NOT FOUND");
                continue;
            }
            markIdAttribute(refEl);

            String expected = normalizeB64(s.digestValue);
            boolean hit = false;
            for (String algo : algos) {
                try {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    Canonicalizer.getInstance(algo).canonicalizeSubtree(refEl, baos);
                    byte[] canon = baos.toByteArray();
                    MessageDigest md = MessageDigest.getInstance("SHA-1");
                    String got = Base64.getEncoder().encodeToString(md.digest(canon));
                    if (normalizeB64(got).equals(expected)) {
                        System.out.println("  - owner=" + s.owner + " ref=" + s.referenceUri + " DIGEST MATCH via " + algo);
                        hit = true;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
            if (!hit) {
                System.out.println("  - owner=" + s.owner + " ref=" + s.referenceUri + " DIGEST NO MATCH (tested 4 C14N algos)");
            }
        }
    }

    private static Document parseXml(Path xmlPath) throws Exception {
        byte[] bytes = Files.readAllBytes(xmlPath);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        return dbf.newDocumentBuilder().parse(new ByteArrayInputStream(bytes));
    }

    private static List<Element> findSignatureElements(Document doc) {
        NodeList list = doc.getElementsByTagNameNS(XMLDSIG_NS, "Signature");
        List<Element> out = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            Node n = list.item(i);
            if (n instanceof Element) {
                out.add((Element) n);
            }
        }
        return out;
    }

    private static List<SigSnapshot> snapshotSignatures(Document doc, List<Element> sigEls) {
        List<SigSnapshot> out = new ArrayList<>();
        for (Element sigEl : sigEls) {
            out.add(snapshotSignatureSingle(doc, sigEl, ownerOfSignature(sigEl)));
        }
        return out;
    }

    private static SigSnapshot snapshotSignatureSingle(Document doc, Element sigEl, String owner) {
        Element signedInfo = firstChildByLocalName(sigEl, "SignedInfo");
        if (signedInfo == null) {
            return new SigSnapshot(owner, "", new ArrayList<>(), "", "", "", "", "", "");
        }

        Element canon = firstChildByLocalName(signedInfo, "CanonicalizationMethod");
        Element sigMethod = firstChildByLocalName(signedInfo, "SignatureMethod");
        Element ref = firstChildByLocalName(signedInfo, "Reference");

        String c14nAlgo = canon == null ? "" : safeAttr(canon, "Algorithm");
        String sigAlgo = sigMethod == null ? "" : safeAttr(sigMethod, "Algorithm");

        String uri = ref == null ? "" : safeAttr(ref, "URI");

        List<String> transforms = new ArrayList<>();
        if (ref != null) {
            Element transformsEl = firstChildByLocalName(ref, "Transforms");
            if (transformsEl != null) {
                Node cur = transformsEl.getFirstChild();
                while (cur != null) {
                    if (cur.getNodeType() == Node.ELEMENT_NODE && "Transform".equals(((Element) cur).getLocalName())) {
                        transforms.add(safeAttr((Element) cur, "Algorithm"));
                    }
                    cur = cur.getNextSibling();
                }
            }
        }

        String digestMethod = "";
        String digestValue = "";
        if (ref != null) {
            Element dm = firstChildByLocalName(ref, "DigestMethod");
            Element dv = firstChildByLocalName(ref, "DigestValue");
            digestMethod = dm == null ? "" : safeAttr(dm, "Algorithm");
            digestValue = dv == null ? "" : safeText(dv);
        }

        String sigValue = "";
        Element sv = firstChildByLocalName(sigEl, "SignatureValue");
        if (sv != null) {
            sigValue = safeText(sv);
        }

        String embeddedCert = extractEmbeddedX509CertText(sigEl);

        return new SigSnapshot(owner, uri, transforms, digestMethod, digestValue, c14nAlgo, sigAlgo, sigValue, embeddedCert);
    }

    private static String ownerOfSignature(Element sigEl) {
        if (sigEl == null) {
            return "";
        }
        Node parent = sigEl.getParentNode();
        if (parent instanceof Element) {
            String ln = ((Element) parent).getLocalName();
            if ("DTE".equals(ln)) {
                Element docEl = firstChildByLocalName((Element) parent, "Documento");
                if (docEl != null) {
                    String id = docEl.getAttribute("ID");
                    return "Documento" + (id == null || id.isEmpty() ? "" : "#" + id);
                }
            }
            if ("ConsumoFolios".equals(ln)) {
                Element doc = firstChildByLocalName((Element) parent, "DocumentoConsumoFolios");
                if (doc != null) {
                    String id = doc.getAttribute("ID");
                    return "DocumentoConsumoFolios" + (id == null || id.isEmpty() ? "" : "#" + id);
                }
            }
            if ("EnvioBOLETA".equals(ln)) {
                Element set = firstChildByLocalName((Element) parent, "SetDTE");
                if (set != null) {
                    String id = set.getAttribute("ID");
                    return "SetDTE" + (id == null || id.isEmpty() ? "" : "#" + id);
                }
            }
        }

        // fallback: climb up to known owners
        Node p = parent;
        while (p != null && p.getNodeType() == Node.ELEMENT_NODE) {
            Element pe = (Element) p;
            String ln = pe.getLocalName();
            if ("SetDTE".equals(ln) || "Documento".equals(ln) || "DocumentoConsumoFolios".equals(ln)) {
                String id = pe.getAttribute("ID");
                return ln + (id == null || id.isEmpty() ? "" : "#" + id);
            }
            p = p.getParentNode();
        }
        return "Signature";
    }

    private static Element findSignatureByOwner(Document doc, String owner) {
        if (doc == null || owner == null || owner.trim().isEmpty()) {
            return null;
        }
        List<Element> sigs = findSignatureElements(doc);
        for (Element s : sigs) {
            String o = ownerOfSignature(s);
            if (owner.equals(o)) {
                return s;
            }
        }
        return null;
    }

    private static Element firstChildByLocalName(Element parent, String localName) {
        if (parent == null) {
            return null;
        }
        Node cur = parent.getFirstChild();
        while (cur != null) {
            if (cur.getNodeType() == Node.ELEMENT_NODE) {
                Element e = (Element) cur;
                if (localName.equals(e.getLocalName())) {
                    return e;
                }
            }
            cur = cur.getNextSibling();
        }
        return null;
    }

    private static String safeAttr(Element el, String name) {
        String v = el.getAttribute(name);
        return v == null ? "" : v.trim();
    }

    private static String safeText(Element el) {
        String v = el.getTextContent();
        return v == null ? "" : v.trim();
    }

    private static String stripHash(String uri) {
        if (uri == null) {
            return "";
        }
        String t = uri.trim();
        if (t.startsWith("#")) {
            return t.substring(1);
        }
        return t;
    }

    private static String normalizeB64(String b64) {
        if (b64 == null) {
            return "";
        }
        return b64.replace("\r", "").replace("\n", "").replace("\t", "").replace(" ", "").trim();
    }

    private static String shortB64(String b64) {
        String n = normalizeB64(b64);
        if (n.length() <= 48) {
            return n;
        }
        return n.substring(0, 48) + "...";
    }

    private static Element findById(Document doc, String id) {
        if (doc == null || id == null || id.trim().isEmpty()) {
            return null;
        }

        NodeList all = doc.getElementsByTagName("*");
        for (int i = 0; i < all.getLength(); i++) {
            Node n = all.item(i);
            if (!(n instanceof Element)) {
                continue;
            }
            Element e = (Element) n;
            if (id.equals(e.getAttribute("ID"))) {
                return e;
            }
        }
        return null;
    }

    private static void markIdAttribute(Element el) {
        try {
            if (el != null && el.hasAttribute("ID")) {
                el.setIdAttribute("ID", true);
            }
        } catch (Exception ignored) {
        }
    }

    private static void registerAllIds(Document doc) {
        if (doc == null) {
            return;
        }
        try {
            NodeList all = doc.getElementsByTagName("*");
            for (int i = 0; i < all.getLength(); i++) {
                Node n = all.item(i);
                if (!(n instanceof Element)) {
                    continue;
                }
                Element e = (Element) n;
                if (e.hasAttribute("ID")) {
                    try {
                        e.setIdAttribute("ID", true);
                    } catch (Exception ignored) {
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    private static void maybeInstallHybridC14NTransform() {
        boolean enabled = Boolean.parseBoolean(System.getProperty("lab.useHybridC14N", "false"));
        if (!enabled) {
            return;
        }

        try {
            SantuarioChilkatCompatInstaller.install();
            System.out.println("[lab] HybridC14N 已启用（register）：Documento=>exc-c14n, others=>c14n；SignedInfo under DTE=>exc-c14n");
            return;
        } catch (Exception e) {
            System.out.println("[lab] HybridC14N register 失败，尝试反射注入兜底: " + e.getMessage());
        }

        try {
            Class<?> transformClass = Class.forName("org.apache.xml.security.transforms.Transform");
            java.lang.reflect.Field f = transformClass.getDeclaredField("transformSpiHash");
            f.setAccessible(true);
            Object v = f.get(null);
            if (!(v instanceof Map)) {
                System.out.println("[lab] HybridC14N: transformSpiHash 类型不匹配: " + (v == null ? "null" : v.getClass().getName()));
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) v;
            map.put(TransformC14NChilkatHybrid.URI, new TransformC14NChilkatHybrid());

            System.out.println("[lab] HybridC14N 已启用：URI=" + TransformC14NChilkatHybrid.URI + " (Documento=>exc-c14n, others=>c14n)");
        } catch (Exception e) {
            System.out.println("[lab] HybridC14N 注入失败: " + e.getMessage());
        }

        try {
            Class<?> canonClass = Class.forName("org.apache.xml.security.c14n.Canonicalizer");
            java.lang.reflect.Field f = canonClass.getDeclaredField("canonicalizerHash");
            f.setAccessible(true);
            Object v = f.get(null);
            if (!(v instanceof Map)) {
                System.out.println("[lab] HybridC14N: canonicalizerHash 类型不匹配: " + (v == null ? "null" : v.getClass().getName()));
                return;
            }

            @SuppressWarnings("unchecked")
            Map<String, Class<?>> map = (Map<String, Class<?>>) v;
            map.put(CanonicalizerChilkatHybrid.URI, CanonicalizerChilkatHybrid.class);
            System.out.println("[lab] HybridCanonicalizer 已启用：URI=" + CanonicalizerChilkatHybrid.URI + " (SignedInfo under DTE=>exc-c14n)");
        } catch (Exception e) {
            System.out.println("[lab] HybridCanonicalizer 注入失败: " + e.getMessage());
        }
    }
}
