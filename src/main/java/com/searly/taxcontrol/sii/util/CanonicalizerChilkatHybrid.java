package com.searly.taxcontrol.sii.util;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.c14n.CanonicalizerSpi;
import org.apache.xml.security.c14n.CanonicalizationException;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315ExclOmitComments;
import org.apache.xml.security.c14n.implementations.Canonicalizer20010315OmitComments;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.OutputStream;
import java.util.Set;

/**
 * 仅用于对照实验：复现 Chilkat 在 EnvioBOLETA 内层 Documento 签名时对 SignedInfo 的非标准规范化行为。
 *
 * 现象：Documento 内层签名的 SignedInfo/Reference 实际符合 xml-exc-c14n (omit comments)，
 * 但 SignedInfo/CanonicalizationMethod 与 Reference/Transform 却声明为 C14N 1.0 (omit comments)。
 *
 * 该 Canonicalizer 将 "REC-xml-c14n-20010315 (omit comments)" 做成 hybrid：
 * - 若 canonicalize 的节点是 SignedInfo，且其 Signature 位于 <DTE> 下（Documento 内层签名），则使用 Exclusive C14N
 * - 其他情况使用标准 Inclusive C14N
 *
 * 只应在实验 CLI 进程内通过反射覆盖 Canonicalizer 注册表启用。
 */
public class CanonicalizerChilkatHybrid extends CanonicalizerSpi {

    public static final String URI = Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;

    private final Canonicalizer20010315OmitComments inclusive = new Canonicalizer20010315OmitComments();
    private final Canonicalizer20010315ExclOmitComments exclusive = new Canonicalizer20010315ExclOmitComments();

    @Override
    public String engineGetURI() {
        return URI;
    }

    @Override
    public void engineCanonicalizeXPathNodeSet(Set<Node> xpathNodeSet, OutputStream writer) throws CanonicalizationException {
        boolean useExcl = shouldUseExclusiveForNodeSet(xpathNodeSet);
        boolean debug = Boolean.parseBoolean(System.getProperty("hybrid.debug", "false"));
        if (debug) {
            System.out.println("[hybrid] CanonicalizerChilkatHybrid: method=XPathNodeSet useExclusive=" + useExcl + " nodeCount=" + (xpathNodeSet == null ? -1 : xpathNodeSet.size()));
        }
        if (useExcl) {
            exclusive.engineCanonicalizeXPathNodeSet(xpathNodeSet, writer);
        } else {
            inclusive.engineCanonicalizeXPathNodeSet(xpathNodeSet, writer);
        }
    }

    @Override
    public void engineCanonicalizeXPathNodeSet(Set<Node> xpathNodeSet, String inclusiveNamespaces, OutputStream writer)
            throws CanonicalizationException {
        boolean useExcl = shouldUseExclusiveForNodeSet(xpathNodeSet);
        boolean debug = Boolean.parseBoolean(System.getProperty("hybrid.debug", "false"));
        if (debug) {
            System.out.println("[hybrid] CanonicalizerChilkatHybrid: method=XPathNodeSet(ns) useExclusive=" + useExcl + " nodeCount=" + (xpathNodeSet == null ? -1 : xpathNodeSet.size()) + " inclusiveNS=" + (inclusiveNamespaces == null ? "" : inclusiveNamespaces));
        }
        if (useExcl) {
            exclusive.engineCanonicalizeXPathNodeSet(xpathNodeSet, inclusiveNamespaces, writer);
        } else {
            inclusive.engineCanonicalizeXPathNodeSet(xpathNodeSet, inclusiveNamespaces, writer);
        }
    }

    @Override
    public void engineCanonicalizeSubTree(Node rootNode, OutputStream writer) throws CanonicalizationException {
        select(rootNode).engineCanonicalizeSubTree(rootNode, writer);
    }

    @Override
    public void engineCanonicalizeSubTree(Node rootNode, String inclusiveNamespaces, OutputStream writer) throws CanonicalizationException {
        select(rootNode).engineCanonicalizeSubTree(rootNode, inclusiveNamespaces, writer);
    }

    @Override
    public void engineCanonicalizeSubTree(Node rootNode, String inclusiveNamespaces, boolean propagateDefaultNamespace, OutputStream writer)
            throws CanonicalizationException {
        select(rootNode).engineCanonicalizeSubTree(rootNode, inclusiveNamespaces, propagateDefaultNamespace, writer);
    }

    private CanonicalizerSpi select(Node node) {
        if (!(node instanceof Element)) {
            return inclusive;
        }
        Element el = (Element) node;

        // Documento 内层签名：Chilkat 的非标准行为要求 Documento 的 digest 实际命中 exc-c14n。
        // Santuario 的签名生成阶段可能直接使用 Canonicalizer 对引用节点做 canonicalize，
        // 因此这里也需要对 Documento 子树做分流。
        if ("Documento".equals(localNameOrNodeName(el))) {
            return exclusive;
        }

        if (!"SignedInfo".equals(localNameOrNodeName(el))) {
            return inclusive;
        }
        Node p = el.getParentNode();
        if (!(p instanceof Element)) {
            return inclusive;
        }
        Element sig = (Element) p;
        if (!"Signature".equals(localNameOrNodeName(sig))) {
            return inclusive;
        }
        Node gp = sig.getParentNode();
        if (!(gp instanceof Element)) {
            return inclusive;
        }
        Element parent = (Element) gp;
        // Documento 内层签名：<Signature> 的父节点是 <DTE>
        if ("DTE".equals(localNameOrNodeName(parent))) {
            return exclusive;
        }
        return inclusive;
    }

    private static String localNameOrNodeName(Element el) {
        if (el == null) {
            return null;
        }
        String ln = el.getLocalName();
        if (ln != null && !ln.isEmpty()) {
            return ln;
        }
        String nn = el.getNodeName();
        if (nn == null) {
            return null;
        }
        int idx = nn.indexOf(':');
        return idx >= 0 ? nn.substring(idx + 1) : nn;
    }

    private static boolean shouldUseExclusiveForNodeSet(Set<Node> xpathNodeSet) {
        if (xpathNodeSet == null || xpathNodeSet.isEmpty()) {
            return false;
        }
        try {
            for (Node n : xpathNodeSet) {
                Element e = (n instanceof Element) ? (Element) n : null;
                Node cur = e != null ? e : n;
                int hop = 0;
                while (cur != null && hop < 12) {
                    if (cur instanceof Element) {
                        Element ce = (Element) cur;
                        String ln = localNameOrNodeName(ce);
                        if ("Documento".equals(ln)) {
                            return true;
                        }
                        String id = ce.getAttribute("ID");
                        if (id != null && id.startsWith("BoletaElectronica_SET_")) {
                            return true;
                        }
                    }
                    cur = cur.getParentNode();
                    hop++;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }
}
