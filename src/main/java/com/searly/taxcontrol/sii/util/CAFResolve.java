/**
 * 项目名：	tax-dack
 * 文件名：	CAFResole.java
 * 模块说明：
 * 修改历史：
 * 2025/8/28 - cc - 创建。
 */
package com.searly.taxcontrol.sii.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.PrivateKey;

/**
 * @author cc
 */
public class CAFResolve {

  public static class CafData {
    public Document cafDoc;
    public Element cafElement; // <CAF>
    public String re, rs, fa, idk, frmaB64;
    public int td;
    public long rngD, rngH;
    public String modulusB64, exponentB64;
    public PrivateKey cafPrivateKey; // 可能为 null（若 CAF 不含 RSASK）
  }

  public static CafData loadCaf(InputStream cafXmlFile) throws Exception {
    Document cafDoc = parseXml(cafXmlFile);
    Element cafEl = (Element) cafDoc.getDocumentElement().getElementsByTagName("CAF").item(0);
    if (cafEl == null) throw new IllegalArgumentException("CAF.xml 缺少 <CAF>");

    // 必须的字段
    String re = text(cafEl, "DA/RE");
    String rs = text(cafEl, "DA/RS");
    String td = text(cafEl, "DA/TD");
    String d = text(cafEl, "DA/RNG/D");
    String h = text(cafEl, "DA/RNG/H");
    String fa = text(cafEl, "DA/FA");
    String mB64 = text(cafEl, "DA/RSAPK/M");
    String eB64 = text(cafEl, "DA/RSAPK/E");
    String idk = text(cafEl, "DA/IDK");
    String frmaB64 = text(cafEl, "FRMA"); // 仅存放，SII下发自带

    // 可选：CAF 里自带 RSASK（PEM 的 PKCS#1）
    PrivateKey cafPrivateKey = null;
//    NodeList rsaskList = cafDoc.getDocumentElement().getElementsByTagName("RSASK");
//    if (rsaskList.getLength() > 0) {
//      String pem = rsaskList.item(0).getTextContent();
//      cafPrivateKey = loadPrivateKeyFromPemPossiblyPkcs1(pem);
//    }

    CafData data = new CafData();
    data.cafDoc = cafDoc;
    data.cafElement = cafEl;
    data.re = re;
    data.rs = rs;
    data.td = Integer.parseInt(td);
    data.rngD = Long.parseLong(d);
    data.rngH = Long.parseLong(h);
    data.fa = fa;
    data.modulusB64 = mB64;
    data.exponentB64 = eB64;
    data.idk = idk;
    data.frmaB64 = frmaB64;
    data.cafPrivateKey = cafPrivateKey;
    return data;
  }

  /**
   * 解析 XML 文件为 DOM
   */
  private static Document parseXml(InputStream f) throws Exception {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(false);
    dbf.setIgnoringElementContentWhitespace(true);
    DocumentBuilder db = dbf.newDocumentBuilder();
    Document parse = null;
    try {
      parse = db.parse(f);
    } finally {
      if (f != null) {
        f.close();
      }
    }

    return parse;
  }

  /**
   * XPath-like 简易取文本（无命名空间版）
   */
  private static String text(Element root, String path) {
    String[] parts = path.split("/");
    Node cur = root;
    for (String p : parts) {
      NodeList nl = ((Element) cur).getElementsByTagName(p);
      if (nl.getLength() == 0) return null;
      cur = nl.item(0);
    }
    return cur.getTextContent() == null ? null : cur.getTextContent().trim();
  }
}
