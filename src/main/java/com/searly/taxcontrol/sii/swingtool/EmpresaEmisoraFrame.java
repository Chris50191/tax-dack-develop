package com.searly.taxcontrol.sii.swingtool;

import com.searly.taxcontrol.sii.util.CertificateManager;
import com.searly.taxcontrol.sii.util.InvoiceGenerator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class EmpresaEmisoraFrame extends JFrame {

    private static final Path CONFIG_PATH = Path.of("sii-tool.properties");

    private final JTextField rutEmisorField = new JTextField();
    private final JTextField rutEnviaField = new JTextField();
    private final JTextField razonSocialField = new JTextField();
    private final JTextField giroField = new JTextField();
    private final JTextField direccionField = new JTextField();
    private final JTextField comunaField = new JTextField();
    private final JTextField ciudadField = new JTextField();
    private final JTextField fchResolField = new JTextField();
    private final JTextField nroResolField = new JTextField();

    private final JTextField certPathField = new JTextField();
    private final JPasswordField certPasswordField = new JPasswordField();

    private final JComboBox<String> aliasDocumentoCombo = new JComboBox<>();
    private final JComboBox<String> aliasSetDteCombo = new JComboBox<>();

    private final JTextArea certInfoArea = new JTextArea();

    private final DefaultTableModel activityModel = new DefaultTableModel(new Object[]{"CODIGO", "DESCRIPCION"}, 4) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return true;
        }
    };

    private KeyStore loadedKeyStore;
    private final SiiToolProperties model;
    private List<String> loadedAliases = new ArrayList<>();

    public EmpresaEmisoraFrame() {
        super("Empresa Emisora");
        this.model = SiiToolProperties.load(CONFIG_PATH);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(980, 680));
        setLocationRelativeTo(null);

        JPanel main = new JPanel(new BorderLayout(12, 12));
        main.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        int row = 0;
        row = addRow(form, gbc, row, "Rut Emisor", rutEmisorField);
        row = addRow(form, gbc, row, "Rut Envia", rutEnviaField);
        row = addRow(form, gbc, row, "Razón Social", razonSocialField);
        row = addRow(form, gbc, row, "Giro", giroField);
        row = addRow(form, gbc, row, "Dirección", direccionField);
        row = addRow(form, gbc, row, "Comuna", comunaField);
        row = addRow(form, gbc, row, "Ciudad", ciudadField);
        row = addRow(form, gbc, row, "Fecha Resolución (dd-MM-yyyy)", fchResolField);
        row = addRow(form, gbc, row, "Resolución (NroResol)", nroResolField);

        JPanel certPanel = new JPanel(new GridBagLayout());
        certPanel.setBorder(BorderFactory.createTitledBorder("Certificado (PFX/P12)"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;

        int crow = 0;

        JButton browse = new JButton("Seleccionar...");
        browse.addActionListener(e -> onSelectCertificate());

        crow = addRowWithButton(certPanel, c, crow, "Ruta", certPathField, browse);

        JButton loadCert = new JButton("Cargar Certificado");
        loadCert.addActionListener(e -> onLoadCertificate());

        crow = addRowWithButton(certPanel, c, crow, "Password", certPasswordField, loadCert);

        crow = addRow(certPanel, c, crow, "Alias Documento (RutEmisor)", aliasDocumentoCombo);
        crow = addRow(certPanel, c, crow, "Alias SetDTE (RutEnvia)", aliasSetDteCombo);

        aliasDocumentoCombo.addActionListener(e -> updateCertInfo());
        aliasSetDteCombo.addActionListener(e -> updateCertInfo());

        certInfoArea.setEditable(false);
        certInfoArea.setLineWrap(true);
        certInfoArea.setWrapStyleWord(true);

        JScrollPane certInfoScroll = new JScrollPane(certInfoArea);
        certInfoScroll.setPreferredSize(new Dimension(420, 160));

        JPanel actPanel = new JPanel(new BorderLayout());
        actPanel.setBorder(BorderFactory.createTitledBorder("Actividades Económicas (máximo 4)"));
        JTable actTable = new JTable(activityModel);
        actTable.setRowHeight(22);
        actPanel.add(new JScrollPane(actTable), BorderLayout.CENTER);

        JPanel left = new JPanel(new BorderLayout(12, 12));
        left.add(form, BorderLayout.NORTH);
        left.add(actPanel, BorderLayout.CENTER);

        JPanel right = new JPanel(new BorderLayout(12, 12));
        right.add(certPanel, BorderLayout.NORTH);

        JPanel certInfoWrap = new JPanel(new BorderLayout());
        certInfoWrap.setBorder(BorderFactory.createTitledBorder("Detalle Certificado"));
        certInfoWrap.add(certInfoScroll, BorderLayout.CENTER);
        right.add(certInfoWrap, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridBagLayout());
        GridBagConstraints b = new GridBagConstraints();
        b.insets = new Insets(6, 6, 6, 6);
        b.fill = GridBagConstraints.HORIZONTAL;

        JButton save = new JButton("Guardar");
        save.addActionListener(e -> onSave());

        JButton openSetBasico = new JButton("Certificación Set Básico");
        openSetBasico.addActionListener(e -> onOpenSetBasico());

        JButton exit = new JButton("Salir");
        exit.addActionListener(e -> dispose());

        b.gridx = 0;
        b.gridy = 0;
        b.weightx = 1;
        buttons.add(save, b);

        b.gridx = 1;
        b.weightx = 1;
        buttons.add(openSetBasico, b);

        b.gridx = 2;
        b.weightx = 1;
        buttons.add(exit, b);

        main.add(left, BorderLayout.CENTER);
        main.add(right, BorderLayout.EAST);
        main.add(buttons, BorderLayout.SOUTH);

        setContentPane(main);

        applyModelToUi();
    }

    private void onOpenSetBasico() {
        onSave();
        CertificacionSetBasicoFrame f = new CertificacionSetBasicoFrame(SiiToolProperties.load(CONFIG_PATH));
        f.setVisible(true);
    }

    private void applyModelToUi() {
        rutEmisorField.setText(nullToEmpty(model.rutEmisor));
        rutEnviaField.setText(nullToEmpty(model.rutEnvia));
        razonSocialField.setText(nullToEmpty(model.razonSocial));
        giroField.setText(nullToEmpty(model.giro));
        direccionField.setText(nullToEmpty(model.direccion));
        comunaField.setText(nullToEmpty(model.comuna));
        ciudadField.setText(nullToEmpty(model.ciudad));
        fchResolField.setText(nullToEmpty(model.fchResol));
        nroResolField.setText(nullToEmpty(model.nroResol));

        certPathField.setText(nullToEmpty(model.certificatePath));
        certPasswordField.setText(nullToEmpty(model.certificatePassword));

        for (int i = 0; i < 4; i++) {
            String code = "";
            String desc = "";
            if (model.activities != null && model.activities.size() > i) {
                SiiToolProperties.EconomicActivity a = model.activities.get(i);
                code = a == null ? "" : nullToEmpty(a.code);
                desc = a == null ? "" : nullToEmpty(a.description);
            }
            activityModel.setValueAt(code, i, 0);
            activityModel.setValueAt(desc, i, 1);
        }

        if (model.certificatePath != null && !model.certificatePath.trim().isEmpty()
                && model.certificatePassword != null && !model.certificatePassword.isEmpty()) {
            SwingUtilities.invokeLater(this::onLoadCertificateSilent);
        }
    }

    private void onSelectCertificate() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("PKCS#12 (*.pfx, *.p12)", "pfx", "p12"));
        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION && chooser.getSelectedFile() != null) {
            certPathField.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }

    private void onLoadCertificateSilent() {
        try {
            onLoadCertificateInternal(false);
        } catch (Exception ignored) {
        }
    }

    private void onLoadCertificate() {
        try {
            onLoadCertificateInternal(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onLoadCertificateInternal(boolean showErrorDialog) {
        String path = certPathField.getText();
        String pwd = new String(certPasswordField.getPassword());

        if (path == null || path.trim().isEmpty()) {
            if (showErrorDialog) {
                JOptionPane.showMessageDialog(this, "请选择证书文件", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }
        if (pwd == null || pwd.trim().isEmpty()) {
            if (showErrorDialog) {
                JOptionPane.showMessageDialog(this, "请输入证书密码", "Error", JOptionPane.ERROR_MESSAGE);
            }
            return;
        }

        this.loadedKeyStore = CertificateManager.loadPKCS12Certificate(path.trim(), pwd);

        List<String> aliases = new ArrayList<>();
        try {
            Enumeration<String> e = loadedKeyStore.aliases();
            while (e.hasMoreElements()) {
                aliases.add(e.nextElement());
            }
        } catch (Exception ex) {
            throw new RuntimeException("读取证书别名失败: " + ex.getMessage(), ex);
        }

        this.loadedAliases = aliases;

        aliasDocumentoCombo.removeAllItems();
        aliasSetDteCombo.removeAllItems();
        for (String a : aliases) {
            aliasDocumentoCombo.addItem(a);
            aliasSetDteCombo.addItem(a);
        }

        if (model.aliasDocumento != null && !model.aliasDocumento.trim().isEmpty()) {
            aliasDocumentoCombo.setSelectedItem(model.aliasDocumento);
        }
        if (model.aliasSetDte != null && !model.aliasSetDte.trim().isEmpty()) {
            aliasSetDteCombo.setSelectedItem(model.aliasSetDte);
        }

        autoSelectAliasesByRut();

        updateCertInfo();
    }

    private void autoSelectAliasesByRut() {
        if (loadedKeyStore == null || loadedAliases == null || loadedAliases.isEmpty()) {
            return;
        }

        String rutEmisor = normalizeRut(rutEmisorField.getText());
        String rutEnvia = normalizeRut(rutEnviaField.getText());

        String aliasForEmisor = null;
        String aliasForEnvia = null;

        if (rutEmisor != null && !rutEmisor.isEmpty()) {
            aliasForEmisor = findAliasByRut(rutEmisor);
        }
        if (rutEnvia != null && !rutEnvia.isEmpty()) {
            aliasForEnvia = findAliasByRut(rutEnvia);
        }

        if (aliasForEmisor != null) {
            aliasDocumentoCombo.setSelectedItem(aliasForEmisor);
        } else if (aliasForEnvia != null) {
            aliasDocumentoCombo.setSelectedItem(aliasForEnvia);
        }
        if (aliasForEnvia != null) {
            aliasSetDteCombo.setSelectedItem(aliasForEnvia);
        }
    }

    private String findAliasByRut(String rut) {
        if (loadedKeyStore == null || loadedAliases == null || loadedAliases.isEmpty()) {
            return null;
        }
        String target = normalizeRut(rut);
        if (target == null || target.isEmpty()) {
            return null;
        }

        for (String a : loadedAliases) {
            try {
                X509Certificate cert = (X509Certificate) loadedKeyStore.getCertificate(a);
                if (cert == null) {
                    continue;
                }
                String certRut = normalizeRut(InvoiceGenerator.extractRutFromCertificate(cert));
                if (target.equalsIgnoreCase(certRut)) {
                    return a;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private void validateAliasRutOrThrow(String alias, String expectedRut, String aliasLabel) {
        if (alias == null || alias.trim().isEmpty()) {
            throw new IllegalStateException(aliasLabel + " 未选择");
        }

        KeyStore ks = loadedKeyStore;
        if (ks == null) {
            String path = safe(certPathField.getText());
            String pwd = new String(certPasswordField.getPassword());
            if (path.trim().isEmpty() || pwd.trim().isEmpty()) {
                throw new IllegalStateException("请先选择证书路径并输入密码");
            }
            ks = CertificateManager.loadPKCS12Certificate(path.trim(), pwd);
        }

        try {
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
            if (cert == null) {
                throw new IllegalStateException(aliasLabel + " 找不到证书: " + alias);
            }

            String exp = normalizeRut(expectedRut);
            String certRut = normalizeRut(InvoiceGenerator.extractRutFromCertificate(cert));
            if (exp != null && !exp.isEmpty() && certRut != null && !certRut.isEmpty() && !exp.equalsIgnoreCase(certRut)) {
                int choice = JOptionPane.showConfirmDialog(
                        this,
                        aliasLabel + "\n证书RUT=" + certRut + "\n期望RUT=" + exp + "\n\n根据业务规则可使用法人/授权人证书为企业签名。仍要继续保存吗？",
                        "Confirm",
                        JOptionPane.YES_NO_OPTION
                );
                if (choice != JOptionPane.YES_OPTION) {
                    throw new IllegalStateException("已取消");
                }
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(aliasLabel + " 校验证书失败: " + e.getMessage());
        }
    }

    private void updateCertInfo() {
        if (loadedKeyStore == null) {
            certInfoArea.setText("");
            return;
        }

        String alias = (String) aliasDocumentoCombo.getSelectedItem();
        if (alias == null || alias.trim().isEmpty()) {
            certInfoArea.setText("");
            return;
        }

        try {
            X509Certificate cert = (X509Certificate) loadedKeyStore.getCertificate(alias);
            if (cert == null) {
                certInfoArea.setText("未找到证书: " + alias);
                return;
            }

            String rut = InvoiceGenerator.extractRutFromCertificate(cert);

            StringBuilder sb = new StringBuilder();
            sb.append("Alias: ").append(alias).append("\n");
            sb.append("RUT: ").append(rut == null ? "" : rut).append("\n");
            sb.append("Subject: ").append(cert.getSubjectX500Principal().getName()).append("\n");
            sb.append("Issuer: ").append(cert.getIssuerX500Principal().getName()).append("\n");
            sb.append("Serial: ").append(cert.getSerialNumber()).append("\n");
            sb.append("NotBefore: ").append(cert.getNotBefore()).append("\n");
            sb.append("NotAfter: ").append(cert.getNotAfter()).append("\n");

            certInfoArea.setText(sb.toString());
        } catch (Exception e) {
            certInfoArea.setText("读取证书详情失败: " + e.getMessage());
        }
    }

    private void onSave() {
        try {
            model.rutEmisor = rutEmisorField.getText();
            model.rutEnvia = rutEnviaField.getText();
            model.razonSocial = razonSocialField.getText();
            model.giro = giroField.getText();
            model.direccion = direccionField.getText();
            model.comuna = comunaField.getText();
            model.ciudad = ciudadField.getText();
            model.fchResol = fchResolField.getText();
            model.nroResol = nroResolField.getText();

            model.certificatePath = certPathField.getText();
            model.certificatePassword = new String(certPasswordField.getPassword());
            model.aliasDocumento = (String) aliasDocumentoCombo.getSelectedItem();
            model.aliasSetDte = (String) aliasSetDteCombo.getSelectedItem();

            validateAliasRutOrThrow(model.aliasDocumento, model.rutEmisor, "Alias Documento (RutEmisor)");
            validateAliasRutOrThrow(model.aliasSetDte, model.rutEnvia, "Alias SetDTE (RutEnvia)");

            List<SiiToolProperties.EconomicActivity> acts = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String code = val(activityModel.getValueAt(i, 0));
                String desc = val(activityModel.getValueAt(i, 1));
                if (!code.isEmpty() || !desc.isEmpty()) {
                    acts.add(new SiiToolProperties.EconomicActivity(code, desc));
                }
            }
            model.activities = acts;

            model.save(CONFIG_PATH);
            JOptionPane.showMessageDialog(this, "Guardado: " + CONFIG_PATH.toAbsolutePath(), "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static String normalizeRut(String rut) {
        if (rut == null) {
            return "";
        }
        String v = rut.trim().replace(".", "").toUpperCase();
        return v;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static int addRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        return row + 1;
    }

    private static int addRowWithButton(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component field, JButton btn) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText, SwingConstants.RIGHT);
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(field, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        panel.add(btn, gbc);

        return row + 1;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String val(Object v) {
        return v == null ? "" : String.valueOf(v).trim();
    }
}
