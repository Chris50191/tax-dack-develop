package com.searly.taxcontrol.sii.util;

import com.searly.taxcontrol.sii.config.SiiConfig;
import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.model.response.SiiEnvioStatusResponse;
import com.searly.taxcontrol.sii.service.SiiApiService;
import com.searly.taxcontrol.sii.swingtool.SetBasicoCaseFactory;
import com.searly.taxcontrol.sii.swingtool.SiiToolProperties;
import com.searly.taxcontrol.verifactu.model.VeriFactuException;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SingleCaseSendAndQuery {
    private static final Path DEFAULT_CAF_PATH = Path.of("caf", "FoliosSII780654383912025991029.xml");

    public static void main(String[] args) throws Exception {
        SiiConfig envConfig = new SiiConfig();
        envConfig.loadConfig();
        boolean isProd = !envConfig.getIsTestEnvironment();

        if (isProd) {
            System.setProperty("sii.caratulaRutReceptor", System.getProperty("sii.caratulaRutReceptor", "60803000-K"));
            System.setProperty("sii.rutReceptor", System.getProperty("sii.rutReceptor", "66666666-6"));
            System.setProperty("sii.rznSocReceptor", System.getProperty("sii.rznSocReceptor", "CONSUMIDOR FINAL"));
        }

        Path cafPath = resolveCafPath();
        if (!Files.exists(cafPath)) {
            throw new IllegalArgumentException("CAF 文件不存在: " + cafPath.toAbsolutePath());
        }
        byte[] cafBytes = Files.readAllBytes(cafPath);
        if (cafBytes.length == 0) {
            throw new IllegalArgumentException("CAF 文件为空: " + cafPath.toAbsolutePath());
        }

        int[] cafRange = tryParseCafRange(cafBytes);
        String defaultStart = isProd ? String.valueOf(cafRange[0] > 0 ? cafRange[0] : 1) : "1000";
        String defaultEnd = isProd ? String.valueOf(cafRange[1] > 0 ? cafRange[1] : 5000) : "2000";

        int folioStart = Integer.parseInt(System.getProperty("sii.folioStart", defaultStart));
        int folioEnd = Integer.parseInt(System.getProperty("sii.folioEnd", defaultEnd));
        Path folioCounterPath = Path.of("temp", isProd ? "folio-counter-prod.txt" : "folio-counter-test.txt");

        SiiToolProperties cfg = SiiToolProperties.load(Path.of("sii-tool.properties"));
        if (cfg == null) {
            throw new IllegalStateException("无法加载 sii-tool.properties");
        }

        boolean onlyGenerate = Boolean.parseBoolean(System.getProperty("sii.onlyGenerate", "false"));
        boolean generateAllCases = Boolean.parseBoolean(System.getProperty("sii.generateAllCases", "false"));

        int folio = nextFolio(folioCounterPath, folioStart, folioEnd);

        if (onlyGenerate) {
            if (generateAllCases) {
                List<SetBasicoCaseFactory.CaseSpec> cases = SetBasicoCaseFactory.getCases();
                if (cases == null || cases.isEmpty()) {
                    throw new IllegalStateException("未找到 SetBasico 的案例列表");
                }
                int current = folio;
                for (SetBasicoCaseFactory.CaseSpec spec : cases) {
                    InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(cfg, String.valueOf(current), spec.caseName, spec.lines);
                    System.out.println("SingleCaseSendAndQuery(批量仅生成): Folio=" + current + ", Case=" + spec.caseName + ", CAF=" + cafPath.toAbsolutePath());
                    generateOnly(cfg, invoiceData, cafBytes);
                    current++;
                }
                persistNextFolio(folioCounterPath, folio + cases.size(), folioStart, folioEnd);
                return;
            }

            SetBasicoCaseFactory.CaseSpec spec = pickSingleCase();
            InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(cfg, String.valueOf(folio), spec.caseName, spec.lines);
            System.out.println("SingleCaseSendAndQuery: Folio=" + folio + ", Case=" + spec.caseName + ", CAF=" + cafPath.toAbsolutePath());
            generateOnly(cfg, invoiceData, cafBytes);
            persistNextFolio(folioCounterPath, folio + 1, folioStart, folioEnd);
            return;
        }

        if (generateAllCases) {
            throw new IllegalArgumentException("sii.generateAllCases 仅支持在 sii.onlyGenerate=true 下使用，避免误发 5 张到 SII");
        }

        SetBasicoCaseFactory.CaseSpec spec = pickSingleCase();
        InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(cfg, String.valueOf(folio), spec.caseName, spec.lines);
        System.out.println("SingleCaseSendAndQuery: Folio=" + folio + ", Case=" + spec.caseName + ", CAF=" + cafPath.toAbsolutePath());

        InvoiceSendRequest request = buildRequest(invoiceData, cafBytes);
        ResultadoEnvioPost envioPost = sendSingleInvoice(request);
        if (envioPost == null) {
            throw new IllegalStateException("发送失败：ResultadoEnvioPost 为空");
        }

        persistSendArtifacts(envioPost);

        if (envioPost.getTrackId() != null) {
            queryWithRetry(envioPost.getRutEmisor(), String.valueOf(envioPost.getTrackId()), InvoiceGenerator.getLastSavedXmlPath());
        }

        persistNextFolio(folioCounterPath, folio + 1, folioStart, folioEnd);
    }

    private static Path resolveCafPath() {
        String override = System.getProperty("sii.cafPath");
        if (override == null) {
            return DEFAULT_CAF_PATH;
        }
        override = override.trim();
        if (override.isEmpty()) {
            return DEFAULT_CAF_PATH;
        }
        return Path.of(override);
    }

    private static void generateOnly(SiiToolProperties cfg, InvoiceData invoiceData, byte[] cafBytes) throws Exception {
        if (cfg.certificatePath == null || cfg.certificatePath.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.path 为空，请先在 sii-tool.properties 设置 cert.path");
        }
        if (cfg.certificatePassword == null || cfg.certificatePassword.trim().isEmpty()) {
            throw new IllegalArgumentException("cert.password 为空，请先在 sii-tool.properties 设置 cert.password");
        }

        KeyStore ks = CertificateManager.loadPKCS12Certificate(cfg.certificatePath, cfg.certificatePassword);
        InvoiceGenerator generator = new InvoiceGenerator();
        generator.generateInvoiceXML(
                invoiceData,
                ks,
                cfg.certificatePassword,
                new ByteArrayInputStream(cafBytes),
                cfg.aliasDocumento,
                cfg.aliasSetDte
        );

        Path out = InvoiceGenerator.getLastSavedXmlPath();
        if (out != null) {
            System.out.println("已生成最终XML(仅生成模式): " + out.toAbsolutePath());
        }
    }

    private static SetBasicoCaseFactory.CaseSpec pickSingleCase() {
        List<SetBasicoCaseFactory.CaseSpec> cases = SetBasicoCaseFactory.getCases();
        if (cases == null || cases.isEmpty()) {
            throw new IllegalStateException("未找到 SetBasico 的案例列表");
        }
        return cases.get(0);
    }

    private static InvoiceSendRequest buildRequest(InvoiceData invoiceData, byte[] cafBytes) {
        InvoiceSendRequest request = new InvoiceSendRequest();
        String[] sender = splitRut(invoiceData.getRutEnvia());
        String[] company = splitRut(invoiceData.getRutEmisor());
        request.setRutSender(sender[0]);
        request.setDvSender(sender[1]);
        request.setRutCompany(company[0]);
        request.setDvCompany(company[1]);
        request.setInvoiceData(invoiceData);
        request.setCafFile(new ByteArrayInputStream(cafBytes));
        return request;
    }

    private static ResultadoEnvioPost sendSingleInvoice(InvoiceSendRequest request) throws VeriFactuException {
        SiiConfig config = new SiiConfig();
        config.loadConfig();
        SiiApiService service = new SiiApiService(config);
        return service.registerInvoice(request);
    }

    private static void queryWithRetry(String rutEmisor, String trackId, Path lastSavedXml) throws VeriFactuException, InterruptedException {
        if (rutEmisor == null || trackId == null) {
            return;
        }
        String[] rut = splitRut(rutEmisor);
        SiiConfig config = new SiiConfig();
        config.loadConfig();
        SiiApiService service = new SiiApiService(config);

        Duration[] delays = new Duration[]{Duration.ofSeconds(5), Duration.ofSeconds(15), Duration.ofSeconds(30)};
        for (Duration delay : delays) {
            Thread.sleep(delay.toMillis());
            SiiEnvioStatusResponse resp = service.queryInvoice(rut[0], rut[1], Long.parseLong(trackId));
            persistQueryArtifacts(rutEmisor, trackId, resp, lastSavedXml);
            if (resp != null && resp.getDetalleRepRech() != null && !resp.getDetalleRepRech().isEmpty()) {
                return;
            }
        }
    }

    private static void persistSendArtifacts(ResultadoEnvioPost envioPost) {
        try {
            Path lastSaved = InvoiceGenerator.getLastSavedXmlPath();
            Path base = artifactBase(lastSaved, envioPost.getTrackId() == null ? null : String.valueOf(envioPost.getTrackId()));

            String responseJson = envioPost.getResponseJson();
            if (responseJson != null && !responseJson.trim().isEmpty()) {
                Files.writeString(base.resolveSibling(base.getFileName() + "_send_response.json"), responseJson, StandardCharsets.UTF_8);
            }

            String requestXml = envioPost.getRequestJson();
            if (requestXml != null && !requestXml.trim().isEmpty()) {
                Files.writeString(base.resolveSibling(base.getFileName() + "_send_request.xml"), requestXml, StandardCharsets.ISO_8859_1);
            }
        } catch (Exception e) {
            System.out.println("警告：保存 send 回执失败: " + e.getMessage());
        }
    }

    private static void persistQueryArtifacts(String rutEmisor, String trackId, SiiEnvioStatusResponse resp, Path lastSavedXml) {
        try {
            if (resp == null) {
                return;
            }
            Path base = artifactBase(lastSavedXml, trackId);
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resp);
            Files.writeString(base.resolveSibling(base.getFileName() + "_query_response.json"), json, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("警告：保存 query 回执失败: " + e.getMessage());
        }
    }

    private static Path artifactBase(Path lastSavedXml, String trackId) {
        try {
            if (lastSavedXml != null) {
                String file = lastSavedXml.getFileName().toString();
                int dot = file.lastIndexOf('.');
                String baseName = dot > 0 ? file.substring(0, dot) : file;
                if (trackId != null && !trackId.trim().isEmpty()) {
                    baseName = baseName + "_track_" + trackId.trim();
                }
                return lastSavedXml.resolveSibling(baseName);
            }
        } catch (Exception ignored) {
        }
        Path fallbackDir = Path.of("output");
        String baseName = "send_artifact";
        if (trackId != null && !trackId.trim().isEmpty()) {
            baseName = baseName + "_track_" + trackId.trim();
        }
        return fallbackDir.resolve(baseName);
    }

    private static String[] splitRut(String rut) {
        if (rut == null || !rut.contains("-")) {
            throw new IllegalArgumentException("RUT 格式无效: " + rut);
        }
        String[] parts = rut.split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("RUT 格式无效: " + rut);
        }
        return parts;
    }

    private static int nextFolio(Path counterPath, int folioStart, int folioEnd) throws Exception {
        if (counterPath == null) {
            throw new IllegalArgumentException("counterPath is null");
        }
        if (!Files.exists(counterPath)) {
            return folioStart;
        }
        String raw = Files.readString(counterPath, StandardCharsets.UTF_8).trim();
        int value = raw.isEmpty() ? folioStart : Integer.parseInt(raw);
        if (value < folioStart) {
            return folioStart;
        }
        if (value > folioEnd) {
            throw new IllegalStateException("Folio 超出范围: " + value + " (" + folioStart + "-" + folioEnd + ")");
        }
        return value;
    }

    private static int[] tryParseCafRange(byte[] cafBytes) {
        int[] out = new int[]{-1, -1};
        if (cafBytes == null || cafBytes.length == 0) {
            return out;
        }
        try {
            String s = new String(cafBytes, StandardCharsets.UTF_8);
            Matcher md = Pattern.compile("<D>\\s*(\\d+)\\s*</D>").matcher(s);
            Matcher mh = Pattern.compile("<H>\\s*(\\d+)\\s*</H>").matcher(s);
            if (md.find()) {
                out[0] = Integer.parseInt(md.group(1));
            }
            if (mh.find()) {
                out[1] = Integer.parseInt(mh.group(1));
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private static void persistNextFolio(Path counterPath, int next, int folioStart, int folioEnd) throws Exception {
        if (counterPath == null) {
            throw new IllegalArgumentException("counterPath is null");
        }
        if (next < folioStart || next > folioEnd + 1) {
            throw new IllegalStateException("Folio 超出范围: " + next + " (" + folioStart + "-" + folioEnd + ")");
        }
        if (counterPath.getParent() != null && !Files.exists(counterPath.getParent())) {
            Files.createDirectories(counterPath.getParent());
        }
        Files.writeString(counterPath, String.valueOf(next), StandardCharsets.UTF_8);
    }
}
