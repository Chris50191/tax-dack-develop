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

public class SingleCaseSendAndQuery {
    private static final int FOLIO_START = 1000;
    private static final int FOLIO_END = 2000;
    private static final Path FOLIO_COUNTER_PATH = Path.of("temp", "folio-counter.txt");
    private static final Path CAF_PATH = Path.of("caf", "FoliosSII780654383912025991029.xml");

    public static void main(String[] args) throws Exception {
        SiiToolProperties cfg = SiiToolProperties.load(Path.of("sii-tool.properties"));
        if (cfg == null) {
            throw new IllegalStateException("无法加载 sii-tool.properties");
        }

        int folio = nextFolio();
        SetBasicoCaseFactory.CaseSpec spec = pickSingleCase();
        InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(cfg, String.valueOf(folio), spec.caseName, spec.lines);

        if (!Files.exists(CAF_PATH)) {
            throw new IllegalArgumentException("CAF 文件不存在: " + CAF_PATH.toAbsolutePath());
        }
        System.out.println("SingleCaseSendAndQuery: Folio=" + folio + ", Case=" + spec.caseName + ", CAF=" + CAF_PATH.toAbsolutePath());
        byte[] cafBytes = Files.readAllBytes(CAF_PATH);
        if (cafBytes.length == 0) {
            throw new IllegalArgumentException("CAF 文件为空: " + CAF_PATH.toAbsolutePath());
        }

        boolean onlyGenerate = Boolean.parseBoolean(System.getProperty("sii.onlyGenerate", "false"));
        if (onlyGenerate) {
            generateOnly(cfg, invoiceData, cafBytes);
            persistNextFolio(folio + 1);
            return;
        }

        InvoiceSendRequest request = buildRequest(invoiceData, cafBytes);
        ResultadoEnvioPost envioPost = sendSingleInvoice(request);
        if (envioPost == null) {
            throw new IllegalStateException("发送失败：ResultadoEnvioPost 为空");
        }

        if (envioPost.getTrackId() != null) {
            queryWithRetry(envioPost.getRutEmisor(), String.valueOf(envioPost.getTrackId()));
        }

        persistNextFolio(folio + 1);
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

    private static void queryWithRetry(String rutEmisor, String trackId) throws VeriFactuException, InterruptedException {
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
            if (resp != null && resp.getDetalleRepRech() != null && !resp.getDetalleRepRech().isEmpty()) {
                return;
            }
        }
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
}
