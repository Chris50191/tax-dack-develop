package com.searly.taxcontrol.sii.util;

import org.apache.xml.security.c14n.Canonicalizer;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.transforms.TransformSpi;
import org.apache.xml.security.transforms.TransformationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 * Chilkat 与 Santuario 的差异点之一：
 * 在本项目生成的 EnvioBOLETA 中，Documento 内层签名的 Reference Transform 声明为 C14N 1.0（inclusive），
 * 但 DigestValue 实际更符合 Exclusive C14N 的结果。
 *
 * 该 Transform 通过“按引用节点类型分流”的方式复现 Chilkat 行为：
 * - 当引用节点为 <Documento> 时，使用 Exclusive C14N（omit comments）计算输出
 * - 其他情况（例如 <SetDTE>），使用标准 C14N 1.0（omit comments）
 *
 * 仅用于实验/对照，不会影响生产逻辑（除非显式注册）。
 */
public class TransformC14NChilkatHybrid extends TransformSpi {

    public static final String URI = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";

    @Override
    protected String engineGetURI() {
        return URI;
    }

    @Override
    protected XMLSignatureInput enginePerformTransform(
            XMLSignatureInput input,
            OutputStream os,
            Element transformElement,
            String baseURI,
            boolean secureValidation
    ) throws TransformationException {
        try {
            String referenceId = extractSameDocReferenceId(transformElement);
            Node target = resolveSameDocReferenceTarget(transformElement);
            if (target == null) {
                target = input == null ? null : input.getSubNode();
            }
            if (target == null) {
                return input;
            }

            String algo = pickAlgoByTarget(target, referenceId);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Canonicalizer.getInstance(algo).canonicalizeSubtree(target, baos);
            byte[] out = baos.toByteArray();

            if (os != null) {
                os.write(out);
            }

            XMLSignatureInput result = new XMLSignatureInput(out);
            result.setSecureValidation(secureValidation);
            return result;
        } catch (Exception e) {
            throw new TransformationException(e);
        }
    }

    private static String pickAlgoByTarget(Node target, String referenceId) {
        // 兜底：签名生成阶段有时难以从 target 可靠判断 Documento；优先按 Reference URI 的 ID 规则判定。
        // 本项目 Documento@ID 规则：BoletaElectronica_SET_<CASE>_<FOLIO>
        if (referenceId != null && !referenceId.isEmpty()) {
            if (referenceId.startsWith("BoletaElectronica_SET_")) {
                return Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
            }
        }
        if (target instanceof Element) {
            String ln = localNameOrNodeName((Element) target);
            if ("Documento".equals(ln)) {
                return Canonicalizer.ALGO_ID_C14N_EXCL_OMIT_COMMENTS;
            }
        }
        return Canonicalizer.ALGO_ID_C14N_OMIT_COMMENTS;
    }

    private static String extractSameDocReferenceId(Element transformElement) {
        try {
            if (transformElement == null) {
                return null;
            }
            Node p = transformElement.getParentNode();
            while (p != null && p.getNodeType() == Node.ELEMENT_NODE) {
                Element pe = (Element) p;
                if ("Reference".equals(localNameOrNodeName(pe))) {
                    String uri = pe.getAttribute("URI");
                    if (uri != null && uri.startsWith("#") && uri.length() > 1) {
                        return uri.substring(1);
                    }
                    return null;
                }
                p = p.getParentNode();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Node resolveSameDocReferenceTarget(Element transformElement) {
        try {
            if (transformElement == null) {
                return null;
            }

            Node p = transformElement.getParentNode();
            while (p != null && p.getNodeType() == Node.ELEMENT_NODE) {
                Element pe = (Element) p;
                if ("Reference".equals(localNameOrNodeName(pe))) {
                    String uri = pe.getAttribute("URI");
                    if (uri != null && uri.startsWith("#") && uri.length() > 1) {
                        String id = uri.substring(1);
                        return findById(pe.getOwnerDocument().getDocumentElement(), id);
                    }
                    return null;
                }
                p = p.getParentNode();
            }
        } catch (Exception ignored) {
        }
        return null;
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

    private static Node findById(Element root, String id) {
        if (root == null || id == null || id.isEmpty()) {
            return null;
        }
        if (id.equals(root.getAttribute("ID"))) {
            return root;
        }
        NodeList all = root.getElementsByTagName("*");
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
}
