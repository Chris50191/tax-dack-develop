package com.searly.taxcontrol.sii.swingtool;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SiiToolProperties {

    public static class EconomicActivity {
        public String code;
        public String description;

        public EconomicActivity() {
        }

        public EconomicActivity(String code, String description) {
            this.code = code;
            this.description = description;
        }
    }

    public String rutEmisor;
    public String rutEnvia;
    public String razonSocial;
    public String giro;
    public String direccion;
    public String comuna;
    public String ciudad;

    public String fchResol;
    public String nroResol;

    public String certificatePath;
    public String certificatePassword;
    public String aliasDocumento;
    public String aliasSetDte;

    public String rvdEndpointPath;
    public String rvdSecEnvio;

    public String outputDir;

    public List<EconomicActivity> activities = new ArrayList<>();

    public static SiiToolProperties defaults() {
        SiiToolProperties p = new SiiToolProperties();
        p.rutEmisor = "78065438-4";
        p.rutEnvia = "24529296-1";
        p.razonSocial = "XIAOQI SPA";
        p.giro = "VENTA AL POR MENOR EN COMERCIO NO ESPECIALIZADO";
        p.direccion = "SAN ALFONSO 637";
        p.comuna = "SANTIAGO";
        p.ciudad = "SANTIAGO";
        p.fchResol = "22-01-2026";
        p.nroResol = "0";
        p.certificatePath = "";
        p.certificatePassword = "";
        p.aliasDocumento = "";
        p.aliasSetDte = "";
        p.rvdEndpointPath = "/boleta.electronica.rvd";
        p.rvdSecEnvio = "1";
        p.outputDir = "temp";
        p.activities.add(new EconomicActivity("464100", "VENTA AL POR MAYOR DE PRODUCTOS TEXTILES, PRENDAS DE VESTIR Y CALZADO"));
        p.activities.add(new EconomicActivity("464903", "VENTA AL POR MAYOR DE ARTICULOS DE PERFUMERIA, DE TOCADOR Y COSMETICOS"));
        p.activities.add(new EconomicActivity("464909", "VENTA AL POR MAYOR DE OTROS ENSERES DOMESTICOS N.C.P."));
        p.activities.add(new EconomicActivity("475909", "VENTA AL POR MENOR DE APARATOS ELECTRICOS, TEXTILES PARA EL HOGAR Y OT"));
        return p;
    }

    public static SiiToolProperties load(Path path) {
        SiiToolProperties p = defaults();
        if (path == null || !Files.exists(path)) {
            return p;
        }

        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException ignored) {
            return p;
        }

        p.rutEmisor = props.getProperty("empresa.rutEmisor", p.rutEmisor);
        p.rutEnvia = props.getProperty("empresa.rutEnvia", p.rutEnvia);
        p.razonSocial = props.getProperty("empresa.razonSocial", p.razonSocial);
        p.giro = props.getProperty("empresa.giro", p.giro);
        p.direccion = props.getProperty("empresa.direccion", p.direccion);
        p.comuna = props.getProperty("empresa.comuna", p.comuna);
        p.ciudad = props.getProperty("empresa.ciudad", p.ciudad);

        p.fchResol = props.getProperty("empresa.fchResol", p.fchResol);
        p.nroResol = props.getProperty("empresa.nroResol", p.nroResol);

        p.certificatePath = props.getProperty("cert.path", p.certificatePath);
        p.certificatePassword = props.getProperty("cert.password", p.certificatePassword);
        p.aliasDocumento = props.getProperty("cert.aliasDocumento", p.aliasDocumento);
        p.aliasSetDte = props.getProperty("cert.aliasSetDte", p.aliasSetDte);

        p.rvdEndpointPath = props.getProperty("rvd.endpointPath", p.rvdEndpointPath);
        p.rvdSecEnvio = props.getProperty("rvd.secEnvio", p.rvdSecEnvio);

        p.outputDir = props.getProperty("output.dir", p.outputDir);

        p.activities.clear();
        for (int i = 1; i <= 4; i++) {
            String code = props.getProperty("actividad." + i + ".code", "");
            String desc = props.getProperty("actividad." + i + ".desc", "");
            if ((code != null && !code.trim().isEmpty()) || (desc != null && !desc.trim().isEmpty())) {
                p.activities.add(new EconomicActivity(code, desc));
            }
        }

        return p;
    }

    public void save(Path path) throws IOException {
        if (path == null) {
            throw new IOException("path is null");
        }

        Properties props = new Properties();
        props.setProperty("empresa.rutEmisor", nullToEmpty(rutEmisor));
        props.setProperty("empresa.rutEnvia", nullToEmpty(rutEnvia));
        props.setProperty("empresa.razonSocial", nullToEmpty(razonSocial));
        props.setProperty("empresa.giro", nullToEmpty(giro));
        props.setProperty("empresa.direccion", nullToEmpty(direccion));
        props.setProperty("empresa.comuna", nullToEmpty(comuna));
        props.setProperty("empresa.ciudad", nullToEmpty(ciudad));
        props.setProperty("empresa.fchResol", nullToEmpty(fchResol));
        props.setProperty("empresa.nroResol", nullToEmpty(nroResol));

        props.setProperty("cert.path", nullToEmpty(certificatePath));
        props.setProperty("cert.password", nullToEmpty(certificatePassword));
        props.setProperty("cert.aliasDocumento", nullToEmpty(aliasDocumento));
        props.setProperty("cert.aliasSetDte", nullToEmpty(aliasSetDte));

        props.setProperty("rvd.endpointPath", nullToEmpty(rvdEndpointPath));
        props.setProperty("rvd.secEnvio", nullToEmpty(rvdSecEnvio));

        props.setProperty("output.dir", nullToEmpty(outputDir));

        for (int i = 1; i <= 4; i++) {
            EconomicActivity a = (activities != null && activities.size() >= i) ? activities.get(i - 1) : null;
            props.setProperty("actividad." + i + ".code", a == null ? "" : nullToEmpty(a.code));
            props.setProperty("actividad." + i + ".desc", a == null ? "" : nullToEmpty(a.description));
        }

        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        try (OutputStream out = Files.newOutputStream(path)) {
            props.store(out, "");
        }
    }

    public String getFchResolIso() {
        if (fchResol == null || fchResol.trim().isEmpty()) {
            return "";
        }

        String v = fchResol.trim();
        DateTimeFormatter iso = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter es = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        try {
            return LocalDate.parse(v, iso).format(iso);
        } catch (Exception ignored) {
        }

        try {
            return LocalDate.parse(v, es).format(iso);
        } catch (Exception ignored) {
        }

        return v;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
