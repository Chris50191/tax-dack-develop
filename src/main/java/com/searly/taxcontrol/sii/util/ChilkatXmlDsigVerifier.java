package com.searly.taxcontrol.sii.util;

import com.chilkatsoft.CkXmlDSig;
import com.chilkatsoft.CkString;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Base64;

public class ChilkatXmlDsigVerifier {

    static {
        try {
            System.loadLibrary("chilkat");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load: " + e);
            System.err.println("请确保 chilkat.dll 可被找到（建议 VM 参数: -Djava.library.path=chilkat-jdk11-x64 ），或将 chilkat.dll 放入 PATH。");
            System.exit(1);
        }
    }

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
        CkXmlDSig dsig = new CkXmlDSig();
        dsig.put_VerboseLogging(true);

        boolean success = dsig.LoadSignature(xml);
        if (!success) {
            System.out.println("LoadSignature failed");
            System.out.println(dsig.lastErrorText());
            return false;
        }

        int numSignatures = dsig.get_NumSignatures();
        System.out.println("NumSignatures=" + numSignatures);

        boolean allOk = true;

        for (int i = 0; i < numSignatures; i++) {
            dsig.put_Selector(i);

            boolean sigOk = dsig.VerifySignature(false);
            System.out.println("Signature " + (i + 1) + " VerifySignature(false)=" + sigOk);
            if (!sigOk) {
                System.out.println(dsig.lastErrorText());
                allOk = false;
            }

            int numRefs = dsig.get_NumReferences();
            System.out.println("  NumReferences=" + numRefs);

            for (int j = 0; j < numRefs; j++) {
                String uri = dsig.referenceUri(j);
                boolean refOk = dsig.VerifyReferenceDigest(j);
                System.out.println("  Reference[" + (j + 1) + "] uri=" + uri + " verified=" + refOk);
                if (!refOk) {
                    allOk = false;
                    System.out.println("    RefFailReason=" + dsig.get_RefFailReason());
                    System.out.println(dsig.lastErrorText());

				String id = uri;
				if (id != null && id.startsWith("#")) {
					id = id.substring(1);
				}
				String c14n = tryCanonicalizeFragment(dsig, xml, id);
				if (c14n != null) {
					String sha1b64 = sha1Base64(c14n.getBytes(StandardCharsets.UTF_8));
					System.out.println("    C14N(SHA1,b64)=" + sha1b64);
					int previewLen = Math.min(300, c14n.length());
					System.out.println("    C14N preview=" + c14n.substring(0, previewLen));
				}
                }
            }

            boolean sigOkWithRefs = dsig.VerifySignature(true);
            System.out.println("Signature " + (i + 1) + " VerifySignature(true)=" + sigOkWithRefs);
            if (!sigOkWithRefs) {
                allOk = false;
                System.out.println(dsig.lastErrorText());
            }
        }

        return allOk;
    }

	private static String tryCanonicalizeFragment(CkXmlDSig dsig, String xml, String idWithoutHash) {
		try {
			CkString out = new CkString();
			String alg = "http://www.w3.org/TR/2001/REC-xml-c14n-20010315";
			boolean ok = dsig.CanonicalizeFragment(xml, idWithoutHash, alg, "", false, out);
			if (ok) {
				return out.getString();
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static String sha1Base64(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			byte[] dig = md.digest(bytes);
			return Base64.getEncoder().encodeToString(dig);
		} catch (Exception e) {
			return "";
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
