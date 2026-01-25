package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.swingtool.ConsumoFoliosGenerator;
import com.searly.taxcontrol.sii.swingtool.SetBasicoCaseFactory;
import com.searly.taxcontrol.sii.swingtool.SiiToolProperties;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class FiveCasesGenerateOnly {
    private static final int FOLIO_START = 1000;
    private static final int FOLIO_END = 2000;
    private static final Path FOLIO_COUNTER_PATH = Path.of("temp", "folio-counter.txt");
    private static final Path CAF_PATH = Path.of("caf", "FoliosSII780654383912025991029.xml");

    public static void main(String[] args) throws Exception {
        SiiToolProperties cfg = SiiToolProperties.load(Path.of("sii-tool.properties"));
        if (cfg == null) {
            throw new IllegalStateException("无法加载 sii-tool.properties");
        }

        if (!Files.exists(CAF_PATH)) {
            throw new IllegalArgumentException("CAF 文件不存在: " + CAF_PATH.toAbsolutePath());
        }
        byte[] cafBytes = Files.readAllBytes(CAF_PATH);
        if (cafBytes.length == 0) {
            throw new IllegalArgumentException("CAF 文件为空: " + CAF_PATH.toAbsolutePath());
        }

        List<SetBasicoCaseFactory.CaseSpec> cases = SetBasicoCaseFactory.getCases();
        if (cases == null || cases.isEmpty()) {
            throw new IllegalStateException("未找到 SetBasico 的案例列表");
        }

        int folio = nextFolio();
        if (folio + cases.size() - 1 > FOLIO_END) {
            throw new IllegalStateException("Folio 超出范围: " + folio + " + " + cases.size() + " - 1 (" + FOLIO_START + "-" + FOLIO_END + ")");
        }

        List<InvoiceData> invoiceDataList = new ArrayList<>();
        int current = folio;
        for (SetBasicoCaseFactory.CaseSpec spec : cases) {
            InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(cfg, String.valueOf(current), spec.caseName, spec.lines);
            invoiceDataList.add(invoiceData);
            current++;
        }

        if (cfg.certificatePath == null || cfg.certificatePath.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.path 为空，请先在 sii-tool.properties 设置 cert.path");
        }
        if (cfg.certificatePassword == null || cfg.certificatePassword.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.password 为空，请先在 sii-tool.properties 设置 cert.password");
        }

        KeyStore ks = CertificateManager.loadPKCS12Certificate(cfg.certificatePath, cfg.certificatePassword);

        InvoiceGenerator generator = new InvoiceGenerator();
        generator.generateInvoiceXML(
                invoiceDataList,
                ks,
                cfg.certificatePassword,
                new ByteArrayInputStream(cafBytes),
                cfg.aliasDocumento,
                cfg.aliasSetDte
        );

        Integer forcedSecEnvio = null;
        try {
            String v = System.getProperty("sii.cofSecEnvio");
            if (v != null && !v.trim().isEmpty()) {
                forcedSecEnvio = Integer.parseInt(v.trim());
            }
        } catch (Exception ignored) {
            forcedSecEnvio = null;
        }
        int secEnvio = (forcedSecEnvio != null && forcedSecEnvio > 0)
                ? forcedSecEnvio
                : resolveSecEnvioForDate(invoiceDataList.get(0).getFchEmis());

        String rcofXml = ConsumoFoliosGenerator.generateAndSign(cfg, invoiceDataList, secEnvio, ks, cfg.certificatePassword);
        Path outRcof = Path.of("output").resolve("RVD_RCOF_" + invoiceDataList.get(0).getFchEmis() + "_SEC" + secEnvio + ".xml");
        if (!Files.exists(outRcof.getParent())) {
            Files.createDirectories(outRcof.getParent());
        }
        Files.writeString(outRcof, rcofXml, StandardCharsets.ISO_8859_1);
        System.out.println("RVD/RCOF 生成(匹配 5-case): " + outRcof.toAbsolutePath());

        cfg.rvdSecEnvio = String.valueOf(secEnvio + 1);
        try {
            cfg.save(Path.of("sii-tool.properties"));
        } catch (Exception ignored) {
        }

        persistNextFolio(folio + cases.size());

        Path out = InvoiceGenerator.getLastSavedXmlPath();
        if (out != null) {
            System.out.println("已生成批量最终XML(5 cases in 1 EnvioBOLETA): " + out.toAbsolutePath());
        }
    }

    private static int nextFolio() throws Exception {
        if (!Files.exists(FOLIO_COUNTER_PATH)) {
            return FOLIO_START;
        }
        String raw = Files.readString(FOLIO_COUNTER_PATH, StandardCharsets.UTF_8).trim();
        int value = raw.isEmpty() ? FOLIO_START : Integer.parseInt(raw);
        if (value < FOLIO_START || value > FOLIO_END) {
            throw new IllegalStateException("Folio 超出范围: " + value + " (" + FOLIO_START + "-" + FOLIO_END + ")");
        }
        return value;
    }

    private static void persistNextFolio(int next) throws Exception {
        if (next < FOLIO_START || next > FOLIO_END + 1) {
            throw new IllegalStateException("Folio 超出范围: " + next + " (" + FOLIO_START + "-" + FOLIO_END + ")");
        }
        if (!Files.exists(FOLIO_COUNTER_PATH.getParent())) {
            Files.createDirectories(FOLIO_COUNTER_PATH.getParent());
        }
        Files.writeString(FOLIO_COUNTER_PATH, String.valueOf(next), StandardCharsets.UTF_8);
    }

    private static int resolveSecEnvioForDate(String fchEmis) throws Exception {
        if (fchEmis == null || fchEmis.trim().isEmpty()) {
            return 1;
        }
        String date = fchEmis.trim();
        Path outDir = Path.of("output");
        if (!Files.exists(outDir)) {
            return 1;
        }

        Pattern p = Pattern.compile("^RVD_RCOF_" + Pattern.quote(date) + "_SEC(\\d+)\\.xml$");
        int max = 0;
        try (Stream<Path> stream = Files.list(outDir)) {
            for (Path file : (Iterable<Path>) stream::iterator) {
                String name = file.getFileName().toString();
                Matcher m = p.matcher(name);
                if (!m.matches()) {
                    continue;
                }
                try {
                    int v = Integer.parseInt(m.group(1));
                    if (v > max) {
                        max = v;
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return max <= 0 ? 1 : (max + 1);
    }
}
