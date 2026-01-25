package com.searly.taxcontrol.sii.swingtool;

import com.searly.taxcontrol.sii.config.SiiConfig;
import com.searly.taxcontrol.sii.model.common.InvoiceData;
import com.searly.taxcontrol.sii.model.request.InvoiceSendRequest;
import com.searly.taxcontrol.sii.model.response.ResultadoEnvioPost;
import com.searly.taxcontrol.sii.service.SiiApiService;
import com.searly.taxcontrol.sii.util.CertificateManager;
import com.searly.taxcontrol.sii.util.InvoiceGenerator;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CertificacionSetBasicoFrame extends JFrame {

    private final SiiToolProperties config;

    private final JTextField cafPathField = new JTextField(40);
    private final JTextField outputDirField = new JTextField(28);
    private final JTextArea logArea = new JTextArea();

    public CertificacionSetBasicoFrame(SiiToolProperties config) {
        super("Certificación de Set Básico - Boletas Electrónicas");
        this.config = config;

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(12, 12));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel header = new JPanel(new BorderLayout());
        JLabel info = new JLabel("RutEmisor=" + safe(config.rutEmisor) + " | RutEnvia=" + safe(config.rutEnvia) + " | NroResol=" + safe(config.nroResol) + " | FchResol=" + safe(config.fchResol));
        header.add(info, BorderLayout.CENTER);

        JTabbedPane tabs = new JTabbedPane();
        Map<String, SetBasicoCaseFactory.CaseSpec> caseByName = new HashMap<>();
        List<SetBasicoCaseFactory.CaseSpec> caseSpecs = SetBasicoCaseFactory.getCases();
        for (SetBasicoCaseFactory.CaseSpec cs : caseSpecs) {
            caseByName.put(cs.caseName, cs);
        }

        tabs.addTab("CASO-1", buildCasePanel(caseByName.get("CASO-1")));
        tabs.addTab("CASO-2", buildCasePanel(caseByName.get("CASO-2")));
        tabs.addTab("CASO-3", buildCasePanel(caseByName.get("CASO-3")));
        tabs.addTab("CASO-4", buildCasePanel(caseByName.get("CASO-4")));
        tabs.addTab("CASO-5", buildCasePanel(caseByName.get("CASO-5")));

        JPanel options = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        options.setBorder(BorderFactory.createTitledBorder("Opciones"));

        options.add(new JLabel("CAF:"));
        cafPathField.setText(guessDefaultCafPath());
        options.add(cafPathField);
        JButton browseCaf = new JButton("Seleccionar...");
        browseCaf.addActionListener(e -> onSelectCaf());
        options.add(browseCaf);

        options.add(new JLabel("Salida:"));
        outputDirField.setText(safe(config.outputDir).trim().isEmpty() ? "temp" : safe(config.outputDir).trim());
        options.add(outputDirField);
        JButton browseOut = new JButton("Carpeta...");
        browseOut.addActionListener(e -> onSelectOutputDir());
        options.add(browseOut);

        JTextField folioInicial = new JTextField(12);
        options.add(new JLabel("Folio Inicial:"));
        options.add(folioInicial);

        JCheckBox deleteIndividual = new JCheckBox("Eliminar XML Individuales");
        options.add(deleteIndividual);

        JRadioButton genOnly = new JRadioButton("Solo Generar", true);
        JRadioButton genAndSend = new JRadioButton("Generar y Enviar");
        ButtonGroup group = new ButtonGroup();
        group.add(genOnly);
        group.add(genAndSend);
        options.add(genOnly);
        options.add(genAndSend);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JButton generateFiveCases = new JButton("Generar 5 Casos + RCOF");
        generateFiveCases.addActionListener(e -> onGenerateFiveCases(folioInicial, deleteIndividual, genOnly.isSelected(), genAndSend.isSelected()));
        JButton generate = new JButton("Generar Documentos");
        generate.addActionListener(e -> onGenerate(tabs, folioInicial, deleteIndividual, genOnly.isSelected(), genAndSend.isSelected()));

        JButton close = new JButton("Cerrar");
        close.addActionListener(e -> dispose());

        bottom.add(generateFiveCases);
        bottom.add(generate);
        bottom.add(close);

        root.add(header, BorderLayout.NORTH);
        root.add(tabs, BorderLayout.CENTER);
        root.add(options, BorderLayout.SOUTH);

        logArea.setEditable(false);
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setPreferredSize(new Dimension(860, 160));

        JPanel wrapper = new JPanel(new BorderLayout(12, 12));
        wrapper.add(root, BorderLayout.CENTER);
        JPanel south = new JPanel(new BorderLayout(12, 12));
        south.add(logScroll, BorderLayout.CENTER);
        south.add(bottom, BorderLayout.SOUTH);
        wrapper.add(south, BorderLayout.SOUTH);

        setContentPane(wrapper);
    }

    private JPanel buildCasePanel(SetBasicoCaseFactory.CaseSpec spec) {
        DefaultTableModel model = new DefaultTableModel(new Object[]{"Item", "Qty", "Precio(IVA)", "Exento", "Unidad"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        if (spec != null && spec.lines != null) {
            for (SetBasicoCaseFactory.Line l : spec.lines) {
                model.addRow(new Object[]{
                        safe(l.name),
                        l.qty,
                        l.unitPriceWithIva == null ? "" : l.unitPriceWithIva.toPlainString(),
                        l.exento ? "SI" : "NO",
                        safe(l.unmdItem)
                });
            }
        }

        JTable table = new JTable(model);
        table.setRowHeight(24);

        JPanel p = new JPanel(new BorderLayout());
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    private void onSelectCaf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CAF (*.xml)", "xml"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            cafPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onSelectOutputDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            outputDirField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onGenerate(JTabbedPane tabs, JTextField folioInicial, JCheckBox deleteIndividual, boolean genOnly, boolean genAndSend) {
        SwingUtilities.invokeLater(() -> {
            try {
                logArea.setText("");

                if (config == null) {
                    throw new IllegalStateException("config is null");
                }
                if (config.certificatePath == null || config.certificatePath.trim().isEmpty()) {
                    throw new IllegalArgumentException("请先在窗口1选择证书路径并保存");
                }
                if (config.certificatePassword == null || config.certificatePassword.trim().isEmpty()) {
                    throw new IllegalArgumentException("请先在窗口1填写证书密码并保存");
                }

                validateCertificateRutMatches();

                String cafPath = cafPathField.getText();
                if (cafPath == null || cafPath.trim().isEmpty()) {
                    throw new IllegalArgumentException("请选择 CAF 文件");
                }
                byte[] cafBytes = Files.readAllBytes(Path.of(cafPath.trim()));
                if (cafBytes.length == 0) {
                    throw new IllegalArgumentException("CAF 文件为空");
                }

                String outDirStr = safe(outputDirField.getText()).trim();
                if (outDirStr.isEmpty()) {
                    outDirStr = "temp";
                }
                config.outputDir = outDirStr;
                Path outDir = Path.of(outDirStr);
                if (!Files.exists(outDir)) {
                    Files.createDirectories(outDir);
                }

                String folioStr = safe(folioInicial.getText()).trim();
                if (folioStr.isEmpty()) {
                    folioStr = "1";
                }
                try {
                    Integer.parseInt(folioStr);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Folio Inicial 必须是数字");
                }

                String selectedCase = tabs.getTitleAt(tabs.getSelectedIndex());
                SetBasicoCaseFactory.CaseSpec spec = null;
                List<SetBasicoCaseFactory.CaseSpec> all = SetBasicoCaseFactory.getCases();
                for (SetBasicoCaseFactory.CaseSpec c : all) {
                    if (c.caseName.equalsIgnoreCase(selectedCase)) {
                        spec = c;
                        break;
                    }
                }
                if (spec == null) {
                    throw new IllegalStateException("未找到用例: " + selectedCase);
                }

                Set<Path> before = listOutputFiles();

                InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(config, folioStr, spec.caseName, spec.lines);

                if (genAndSend) {
                    ResultadoEnvioPost result = sendInvoice(invoiceData, cafBytes);
                    log("Enviado: TrackID=" + result.getTrackId() + ", Estado=" + result.getEstado());

                    Path rvdFile = generateRvdForInvoice(invoiceData);
                    log("RVD/RCOF generado (manual upload): " + (rvdFile == null ? "" : rvdFile.toAbsolutePath()));
                } else {
                    String xml = generateOnly(invoiceData, cafBytes);
                    Path out = outDir.resolve("SET_" + spec.caseName + "_F" + folioStr + "_EnvioBOLETA.xml");
                    Files.writeString(out, xml, StandardCharsets.ISO_8859_1);
                    log("Generado: " + out.toAbsolutePath());
                }

                Set<Path> after = listOutputFiles();
                if (deleteIndividual != null && deleteIndividual.isSelected()) {
                    int deleted = deleteOutputDiff(before, after);
                    log("Eliminar XML Individuales: deleted=" + deleted);
                }

                JOptionPane.showMessageDialog(this, "OK", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                log("Error: " + ex.getMessage());
            }
        });
    }

    private void onGenerateFiveCases(JTextField folioInicial, JCheckBox deleteIndividual, boolean genOnly, boolean genAndSend) {
        SwingUtilities.invokeLater(() -> {
            try {
                logArea.setText("");

                if (config == null) {
                    throw new IllegalStateException("config is null");
                }
                if (config.certificatePath == null || config.certificatePath.trim().isEmpty()) {
                    throw new IllegalArgumentException("请先在窗口1选择证书路径并保存");
                }
                if (config.certificatePassword == null || config.certificatePassword.trim().isEmpty()) {
                    throw new IllegalArgumentException("请先在窗口1填写证书密码并保存");
                }

                if (genAndSend) {
                    throw new IllegalArgumentException("批量 5-case 当前仅支持生成（Solo Generar）。如需发送请先在 SII 门户手工 Upload 单个 EnvioBOLETA。 ");
                }

                validateCertificateRutMatches();

                String cafPath = cafPathField.getText();
                if (cafPath == null || cafPath.trim().isEmpty()) {
                    throw new IllegalArgumentException("请选择 CAF 文件");
                }
                byte[] cafBytes = Files.readAllBytes(Path.of(cafPath.trim()));
                if (cafBytes.length == 0) {
                    throw new IllegalArgumentException("CAF 文件为空");
                }

                String outDirStr = safe(outputDirField.getText()).trim();
                if (outDirStr.isEmpty()) {
                    outDirStr = "temp";
                }
                config.outputDir = outDirStr;
                Path outDir = Path.of(outDirStr);
                if (!Files.exists(outDir)) {
                    Files.createDirectories(outDir);
                }

                String folioStr = safe(folioInicial.getText()).trim();
                if (folioStr.isEmpty()) {
                    folioStr = "1";
                }

                final int startFolio;
                try {
                    startFolio = Integer.parseInt(folioStr);
                } catch (Exception e) {
                    throw new IllegalArgumentException("Folio Inicial 必须是数字");
                }

                List<SetBasicoCaseFactory.CaseSpec> cases = SetBasicoCaseFactory.getCases();
                if (cases == null || cases.isEmpty()) {
                    throw new IllegalStateException("未找到 SetBasico 的案例列表");
                }

                List<InvoiceData> invoiceDataList = new ArrayList<>();
                int current = startFolio;
                for (SetBasicoCaseFactory.CaseSpec spec : cases) {
                    InvoiceData invoiceData = SetBasicoCaseFactory.buildInvoice(config, String.valueOf(current), spec.caseName, spec.lines);
                    invoiceDataList.add(invoiceData);
                    current++;
                }

                Set<Path> before = listOutputFiles();

                KeyStore ks = CertificateManager.loadPKCS12Certificate(config.certificatePath, config.certificatePassword);
                InvoiceGenerator generator = new InvoiceGenerator();
                generator.generateInvoiceXML(
                        invoiceDataList,
                        ks,
                        config.certificatePassword,
                        new ByteArrayInputStream(cafBytes),
                        config.aliasDocumento,
                        config.aliasSetDte
                );

                Path batchSaved = InvoiceGenerator.getLastSavedXmlPath();
                if (batchSaved != null) {
                    log("EnvioBOLETA(5 casos) generado: " + batchSaved.toAbsolutePath());
                    try {
                        if (batchSaved.getParent() != null && !batchSaved.getParent().toAbsolutePath().normalize().equals(outDir.toAbsolutePath().normalize())) {
                            Path copy = outDir.resolve(batchSaved.getFileName().toString());
                            Files.copy(batchSaved, copy, StandardCopyOption.REPLACE_EXISTING);
                            log("Copia EnvioBOLETA a carpeta de salida: " + copy.toAbsolutePath());
                        }
                    } catch (Exception ignored) {
                    }
                }

                Path rvdFile = generateRvdForInvoices(invoiceDataList);
                log("RVD/RCOF generado (manual upload): " + (rvdFile == null ? "" : rvdFile.toAbsolutePath()));

                Set<Path> after = listOutputFiles();
                if (deleteIndividual != null && deleteIndividual.isSelected()) {
                    int deleted = deleteOutputDiff(before, after);
                    log("Eliminar XML Individuales: deleted=" + deleted);
                }

                JOptionPane.showMessageDialog(this, "OK", "OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                log("Error: " + ex.getMessage());
            }
        });
    }

    private void validateCertificateRutMatches() {
        try {
            KeyStore ks = CertificateManager.loadPKCS12Certificate(config.certificatePath, config.certificatePassword);

            if (config.aliasDocumento == null || config.aliasDocumento.trim().isEmpty()) {
                throw new IllegalArgumentException("未选择 Alias Documento (RutEmisor)");
            }
            if (config.aliasSetDte == null || config.aliasSetDte.trim().isEmpty()) {
                throw new IllegalArgumentException("未选择 Alias SetDTE (RutEnvia)");
            }

            String rutEmisor = normalizeRut(config.rutEmisor);
            String rutEnvia = normalizeRut(config.rutEnvia);

            String rutDoc = extractRutFromAlias(ks, config.aliasDocumento);
            if (!rutEmisor.equalsIgnoreCase(rutDoc)) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Alias Documento (Documento 签名)\n证书RUT=" + rutDoc + "\nRutEmisor=" + rutEmisor
                                + "\n\n提示：在 SII 场景中可使用法人/授权人证书代表企业签名，但该证书必须已在 SII 授权给该企业。\n仍要继续发送吗？",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice != JOptionPane.YES_OPTION) {
                    throw new IllegalArgumentException("已取消");
                }
            }

            String rutSet = extractRutFromAlias(ks, config.aliasSetDte);
            if (!rutEnvia.equalsIgnoreCase(rutSet)) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Alias SetDTE (SetDTE/Envio 签名)\n证书RUT=" + rutSet + "\nRutEnvia=" + rutEnvia
                                + "\n\n提示：可使用法人/授权人证书签名，但需满足 SII 对发送者/授权关系的校验。\n仍要继续发送吗？",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice != JOptionPane.YES_OPTION) {
                    throw new IllegalArgumentException("已取消");
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("证书校验失败: " + e.getMessage());
        }
    }

    private static String extractRutFromAlias(KeyStore ks, String alias) throws Exception {
        java.security.cert.Certificate cert = ks.getCertificate(alias);
        if (!(cert instanceof java.security.cert.X509Certificate)) {
            return "";
        }
        return normalizeRut(InvoiceGenerator.extractRutFromCertificate((java.security.cert.X509Certificate) cert));
    }

    private static String normalizeRut(String rut) {
        if (rut == null) {
            return "";
        }
        return rut.trim().replace(".", "").toUpperCase();
    }

    private ResultadoEnvioPost sendInvoice(InvoiceData invoiceData, byte[] cafBytes) throws Exception {
        InvoiceSendRequest req = new InvoiceSendRequest();

        String[] sender = splitRut(config.rutEnvia);
        String[] company = splitRut(config.rutEmisor);
        req.setRutSender(sender[0]);
        req.setDvSender(sender[1]);
        req.setRutCompany(company[0]);
        req.setDvCompany(company[1]);
        req.setInvoiceData(invoiceData);
        req.setCafFile(new ByteArrayInputStream(cafBytes));

        if (config.aliasDocumento != null && !config.aliasDocumento.trim().isEmpty()) {
            req.setAliasDocumento(config.aliasDocumento.trim());
        }
        if (config.aliasSetDte != null && !config.aliasSetDte.trim().isEmpty()) {
            req.setAliasSetDte(config.aliasSetDte.trim());
        }

        SiiConfig siiConfig = new SiiConfig();
        siiConfig.loadConfig();
        siiConfig.setCertificatePath(config.certificatePath);
        siiConfig.setCertificatePassword(config.certificatePassword);
        SiiApiService service = new SiiApiService(siiConfig);
        return service.registerInvoice(req);
    }

    private Path generateRvdForInvoice(InvoiceData invoiceData) throws Exception {
        KeyStore keyStore = CertificateManager.loadPKCS12Certificate(config.certificatePath, config.certificatePassword);

        int secEnvio = 1;
        try {
            secEnvio = Integer.parseInt(safe(config.rvdSecEnvio).trim());
            if (secEnvio <= 0) {
                secEnvio = 1;
            }
        } catch (Exception ignored) {
            secEnvio = 1;
        }

        String rvdXml = ConsumoFoliosGenerator.generateAndSign(config, invoiceData, secEnvio, keyStore, config.certificatePassword);

        String outDirStr = safe(config.outputDir).trim();
        if (outDirStr.isEmpty()) {
            outDirStr = "temp";
        }
        Path outDir = Path.of(outDirStr);
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }
        Path out = outDir.resolve("RVD_RCOF_" + invoiceData.getFchEmis() + "_SEC" + secEnvio + ".xml");
        Files.writeString(out, rvdXml, StandardCharsets.ISO_8859_1);
        log("RVD/RCOF generado: " + out.toAbsolutePath());

        config.rvdSecEnvio = String.valueOf(secEnvio + 1);
        try {
            Path cfgPath = Path.of("sii-tool.properties");
            config.save(cfgPath);
        } catch (Exception ignored) {
        }
        return out;
    }

    private Path generateRvdForInvoices(List<InvoiceData> invoiceDataList) throws Exception {
        if (invoiceDataList == null || invoiceDataList.isEmpty()) {
            return null;
        }

        KeyStore keyStore = CertificateManager.loadPKCS12Certificate(config.certificatePath, config.certificatePassword);

        int secEnvio = 1;
        try {
            secEnvio = Integer.parseInt(safe(config.rvdSecEnvio).trim());
            if (secEnvio <= 0) {
                secEnvio = 1;
            }
        } catch (Exception ignored) {
            secEnvio = 1;
        }

        String rvdXml = ConsumoFoliosGenerator.generateAndSign(config, invoiceDataList, secEnvio, keyStore, config.certificatePassword);

        String outDirStr = safe(config.outputDir).trim();
        if (outDirStr.isEmpty()) {
            outDirStr = "temp";
        }
        Path outDir = Path.of(outDirStr);
        if (!Files.exists(outDir)) {
            Files.createDirectories(outDir);
        }

        String date = invoiceDataList.get(0).getFchEmis();
        int[] range = minMaxFolio(invoiceDataList);
        Path out = outDir.resolve("RVD_RCOF_" + date + "_SEC" + secEnvio + "_FROM_" + range[0] + "_" + range[1] + ".xml");
        Files.writeString(out, rvdXml, StandardCharsets.ISO_8859_1);
        log("RVD/RCOF generado: " + out.toAbsolutePath());

        config.rvdSecEnvio = String.valueOf(secEnvio + 1);
        try {
            Path cfgPath = Path.of("sii-tool.properties");
            config.save(cfgPath);
        } catch (Exception ignored) {
        }

        return out;
    }

    private static int[] minMaxFolio(List<InvoiceData> invoiceDataList) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (InvoiceData inv : invoiceDataList) {
            if (inv == null || inv.getFolio() == null) {
                continue;
            }
            try {
                int v = Integer.parseInt(inv.getFolio().trim());
                if (v < min) {
                    min = v;
                }
                if (v > max) {
                    max = v;
                }
            } catch (Exception ignored) {
            }
        }
        if (min == Integer.MAX_VALUE || max == Integer.MIN_VALUE) {
            return new int[]{0, 0};
        }
        return new int[]{min, max};
    }

    private String generateOnly(InvoiceData invoiceData, byte[] cafBytes) throws Exception {
        KeyStore keyStore = CertificateManager.loadPKCS12Certificate(config.certificatePath, config.certificatePassword);
        InvoiceGenerator generator = new InvoiceGenerator();
        return generator.generateInvoiceXML(
                invoiceData,
                keyStore,
                config.certificatePassword,
                new ByteArrayInputStream(cafBytes),
                config.aliasDocumento,
                config.aliasSetDte
        );
    }

    private Set<Path> listOutputFiles() {
        try {
            Path outDir = Path.of("output");
            if (!Files.exists(outDir)) {
                return new HashSet<>();
            }
            Set<Path> set = new HashSet<>();
            try (Stream<Path> stream = Files.list(outDir)) {
                stream.filter(p -> Files.isRegularFile(p)).forEach(set::add);
            }
            return set;
        } catch (Exception e) {
            return new HashSet<>();
        }
    }

    private int deleteOutputDiff(Set<Path> before, Set<Path> after) {
        int deleted = 0;
        if (after == null || after.isEmpty()) {
            return 0;
        }
        Set<Path> diff = new HashSet<>(after);
        if (before != null) {
            diff.removeAll(before);
        }
        for (Path p : diff) {
            try {
                String name = p.getFileName().toString();
                if (name.endsWith("_05_最终XML_发送.xml")) {
                    continue;
                }
                Files.deleteIfExists(p);
                deleted++;
            } catch (Exception ignored) {
            }
        }
        return deleted;
    }

    private void log(String msg) {
        logArea.append(msg);
        if (!msg.endsWith("\n")) {
            logArea.append("\n");
        }
    }

    private static String guessDefaultCafPath() {
        try {
            Path cafDir = Path.of("caf");
            if (!Files.exists(cafDir)) {
                return "";
            }
            try (Stream<Path> stream = Files.list(cafDir)) {
                return stream
                        .filter(p -> Files.isRegularFile(p))
                        .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".xml"))
                        .findFirst()
                        .map(p -> p.toAbsolutePath().toString())
                        .orElse("");
            }
        } catch (Exception e) {
            return "";
        }
    }

    private static String[] splitRut(String rut) {
        if (rut == null || !rut.contains("-")) {
            throw new IllegalArgumentException("RUT 格式不正确: " + rut);
        }
        String[] parts = rut.trim().split("-");
        if (parts.length != 2) {
            throw new IllegalArgumentException("RUT 格式不正确: " + rut);
        }
        return parts;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
