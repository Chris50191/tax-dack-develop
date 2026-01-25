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
        // 对 nodeSet 无法可靠判断上下文；默认走标准 inclusive。
        inclusive.engineCanonicalizeXPathNodeSet(xpathNodeSet, writer);
    }

    @Override
    public void engineCanonicalizeXPathNodeSet(Set<Node> xpathNodeSet, String inclusiveNamespaces, OutputStream writer)
            throws CanonicalizationException {
        inclusive.engineCanonicalizeXPathNodeSet(xpathNodeSet, inclusiveNamespaces, writer);
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
}
