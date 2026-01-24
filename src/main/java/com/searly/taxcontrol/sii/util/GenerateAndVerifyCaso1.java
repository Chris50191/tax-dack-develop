package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.swingtool.SetBasicoCaseFactory;
import com.searly.taxcontrol.sii.swingtool.SiiToolProperties;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class GenerateAndVerifyCaso1 {

    public static void main(String[] args) throws Exception {
        Path cfgPath = Path.of("sii-tool.properties");
        SiiToolProperties cfg = SiiToolProperties.load(cfgPath);

        System.out.println("Config: empresa.fchResol=" + cfg.fchResol + " => iso=" + cfg.getFchResolIso() + ", empresa.nroResol=" + cfg.nroResol);

        int baseFolio = 5006;
        List<SetBasicoCaseFactory.CaseSpec> cases = SetBasicoCaseFactory.getCases();
        if (cases == null || cases.isEmpty()) {
            throw new IllegalStateException("未找到 SetBasico 的案例列表");
        }

        if (cfg.certificatePath == null || cfg.certificatePath.trim().isEmpty()) {
            throw new IllegalArgumentException("默认配置 cert.path 为空，请先在 Swing 窗口1设置证书路径并保存到 sii-tool.properties");
        }
        if (cfg.certificatePassword == null || cfg.certificatePassword.trim().isEmpty()) {
            throw new IllegalArgumentException("默认配置 cert.password 为空，请先在 Swing 窗口1设置证书密码并保存到 sii-tool.properties");
        }

        KeyStore ks = CertificateManager.loadPKCS12Certificate(cfg.certificatePath, cfg.certificatePassword);

        InvoiceGenerator generator = new InvoiceGenerator();

        for (int i = 0; i < cases.size(); i++) {
            SetBasicoCaseFactory.CaseSpec c = cases.get(i);
            if (c == null || c.caseName == null || c.caseName.trim().isEmpty()) {
                continue;
            }

            String folio = String.valueOf(baseFolio + i);

            Path cafPath = guessDefaultCafPath(folio);
            if (cafPath == null || !Files.exists(cafPath)) {
                throw new IllegalArgumentException("未找到 CAF 文件，请将 CAF 放入项目 caf/ 目录或在 Swing 中选择后生成。 folio=" + folio + ", cafPath=" + cafPath);
            }
            byte[] cafBytes = Files.readAllBytes(cafPath);
            if (cafBytes.length == 0) {
                throw new IllegalArgumentException("CAF 文件为空: " + cafPath.toAbsolutePath());
            }

            System.out.println("CASE=" + c.caseName + ", Folio=" + folio + ", CAF=" + cafPath.toAbsolutePath());

            InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(cfg, folio, c.caseName, c.lines);
            System.out.println("Invoice: FchResol=" + invoiceData.getFchResol() + ", NroResol=" + invoiceData.getNroResol());
            generator.generateInvoiceXML(
                    invoiceData,
                    ks,
                    cfg.certificatePassword,
                    new ByteArrayInputStream(cafBytes),
                    cfg.aliasDocumento,
                    cfg.aliasSetDte
            );

            // InvoiceGenerator 会自动写入 output/*_05_最终XML_发送.xml。
            Path finalOut = InvoiceGenerator.getLastSavedXmlPath();
            if (finalOut == null || !Files.exists(finalOut)) {
                finalOut = findLatestFinalXmlInOutputDir();
            }
            if (finalOut == null || !Files.exists(finalOut)) {
                throw new IllegalStateException("未在 output/ 目录找到 *_05_最终XML_发送.xml，请检查 InvoiceGenerator.saveXmlDocument 输出。");
            }
            System.out.println("Final XML (output): " + finalOut.toAbsolutePath());
            System.out.println("本地验签：请以 InvoiceGenerator 输出的 JSR105 二次验签 + Santuario 验签结果为准。\n");
        }
    }

    private static Path guessDefaultCafPath(String folio) {
        try {
            Path cafDir = Path.of("caf");
            if (!Files.exists(cafDir)) {
                return null;
            }

            int folioNum = Integer.parseInt(folio);
            Path selected = null;

            try (Stream<Path> stream = Files.list(cafDir)) {
                List<Path> files = stream
                        .filter(p -> Files.isRegularFile(p))
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml"))
                        .toList();

                for (Path p : files) {
                    int[] range = tryParseCafRange(p);
                    if (range == null) continue;
                    if (folioNum >= range[0] && folioNum <= range[1]) {
                        selected = p;
                        break;
                    }
                }

                if (selected != null) {
                    return selected;
                }
                return files.isEmpty() ? null : files.get(0);
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static int[] tryParseCafRange(Path cafPath) {
        try {
            String xml = Files.readString(cafPath);
            Integer d = extractIntTag(xml, "D");
            Integer h = extractIntTag(xml, "H");
            if (d == null || h == null) return null;
            return new int[]{d, h};
        } catch (Exception e) {
            return null;
        }
    }

    private static Integer extractIntTag(String xml, String tag) {
        if (xml == null) return null;
        Pattern p = Pattern.compile("<" + Pattern.quote(tag) + ">(\\d+)</" + Pattern.quote(tag) + ">", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(xml);
        if (!m.find()) return null;
        try {
            return Integer.parseInt(m.group(1));
        } catch (Exception e) {
            return null;
        }
    }

    private static Path findLatestFinalXmlInOutputDir() {
        try {
            Path outDir = Path.of("output");
            if (!Files.exists(outDir)) return null;
            try (Stream<Path> stream = Files.list(outDir)) {
                Optional<Path> latest = stream
                        .filter(p -> Files.isRegularFile(p))
                        .filter(p -> {
                            String name = p.getFileName().toString();
                            return name.endsWith("_05_最终XML_发送.xml");
                        })
                        .max(Comparator.comparingLong(p -> {
                            try {
                                return Files.getLastModifiedTime(p).toMillis();
                            } catch (Exception e) {
                                return 0L;
                            }
                        }));
                return latest.orElse(null);
            }
        } catch (Exception e) {
            return null;
        }
    }
}
