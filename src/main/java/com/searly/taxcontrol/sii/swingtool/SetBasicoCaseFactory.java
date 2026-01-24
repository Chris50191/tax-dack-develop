package com.searly.taxcontrol.sii.swingtool;

import com.searly.taxcontrol.sii.model.common.InvoiceData;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SetBasicoCaseFactory {

    public static class CaseSpec {
        public final String caseName;
        public final List<Line> lines;

        public CaseSpec(String caseName, List<Line> lines) {
            this.caseName = caseName;
            this.lines = lines;
        }
    }

    public static class Line {
        public final String name;
        public final int qty;
        public final BigDecimal unitPriceWithIva;
        public final boolean exento;
        public final String unmdItem;

        public Line(String name, int qty, BigDecimal unitPriceWithIva, boolean exento, String unmdItem) {
            this.name = name;
            this.qty = qty;
            this.unitPriceWithIva = unitPriceWithIva;
            this.exento = exento;
            this.unmdItem = unmdItem;
        }
    }

    public static List<CaseSpec> getCases() {
        List<CaseSpec> cases = new ArrayList<>();

        cases.add(new CaseSpec("CASO-1", Arrays.asList(
                new Line("Cambio de aceite", 1, bd(19900), false, null),
                new Line("Alineacion y balanceo", 1, bd(9900), false, null)
        )));

        cases.add(new CaseSpec("CASO-2", Arrays.asList(
                new Line("Papel de regalo", 17, bd(120), false, null)
        )));

        cases.add(new CaseSpec("CASO-3", Arrays.asList(
                new Line("Sandwic", 2, bd(1500), false, null),
                new Line("Bebida", 2, bd(550), false, null)
        )));

        cases.add(new CaseSpec("CASO-4", Arrays.asList(
                new Line("item afecto 1", 8, bd(1590), false, null),
                new Line("item exento 2", 2, bd(1000), true, null)
        )));

        cases.add(new CaseSpec("CASO-5", Arrays.asList(
                new Line("Arroz", 5, bd(700), false, "Kg")
        )));

        return cases;
    }

    public static InvoiceData buildInvoice(SiiToolProperties cfg, String folio, String caseName, List<Line> lines) {
        InvoiceData data = new InvoiceData();

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Santiago"));
        String nowDate = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String overrideFchEmis = System.getProperty("sii.fchEmis");
        if (overrideFchEmis != null) {
            overrideFchEmis = overrideFchEmis.trim();
            if (overrideFchEmis.matches("\\d{4}-\\d{2}-\\d{2}")) {
                nowDate = overrideFchEmis;
            }
        }
        String currentTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        data.setTipoDTE(39);
        data.setFolio(folio);
        data.setFchEmis(nowDate);
        data.setIndServicio(3);
        data.setDocumentId("BoletaElectronica_SET_" + caseName + "_" + folio);

        data.setRutEmisor(cfg.rutEmisor);
        data.setRutEnvia(cfg.rutEnvia);

        data.setRznSocEmisor(cfg.razonSocial);
        data.setGiroEmisor(cfg.giro);
        data.setDirOrigen(cfg.direccion);
        data.setCmnaOrigen(cfg.comuna);
        data.setCiudadOrigen(cfg.ciudad);

        data.setRutReceptor("60803000-K");
        data.setRznSocReceptor("SII");

        Totals totals = computeTotals(lines);
        if (totals.mntNeto != null) {
            data.setMntNeto(totals.mntNeto);
        }
        if (totals.mntExe != null) {
            data.setMntExe(totals.mntExe);
        }
        if (totals.iva != null) {
            data.setIva(totals.iva);
        }
        data.setMntTotal(totals.mntTotal);

        List<InvoiceData.Product> products = new ArrayList<>();
        for (Line l : lines) {
            InvoiceData.Product p = new InvoiceData.Product();
            p.setNmbItem(l.name == null ? null : l.name.trim());
            p.setQtyItem(new BigDecimal(l.qty));

            if (l.unmdItem != null && !l.unmdItem.trim().isEmpty()) {
                p.setUnmdItem(l.unmdItem.trim());
            }

            if (l.exento) {
                p.setIndExe(1);
                p.setPrcItem(l.unitPriceWithIva);
                p.setMontoItem(l.unitPriceWithIva.multiply(new BigDecimal(l.qty)).setScale(0, RoundingMode.UNNECESSARY));
            } else {
                BigDecimal lineGross = l.unitPriceWithIva.multiply(new BigDecimal(l.qty)).setScale(0, RoundingMode.UNNECESSARY);
                p.setPrcItem(l.unitPriceWithIva);
                p.setMontoItem(lineGross);
            }

            products.add(p);
        }
        data.setProducts(products);

        InvoiceData.Reference ref = new InvoiceData.Reference();
        ref.setReasonType("SET");
        ref.setResson(caseName);
        data.setIsReferences(Arrays.asList(ref));

        String fchResolIso = cfg.getFchResolIso();
        if (fchResolIso == null || fchResolIso.trim().isEmpty() || !fchResolIso.matches("\\d{4}-\\d{2}-\\d{2}")) {
            throw new IllegalArgumentException("Caratula 授权日期(FchResol)无效: '" + cfg.fchResol + "'。请在 sii-tool.properties 设置 empresa.fchResol（例如 23-01-2026 或 2026-01-23），并确保能转换为 yyyy-MM-dd。");
        }
        data.setFchResol(fchResolIso);

        Integer nroResol;
        try {
            nroResol = Integer.parseInt(cfg.nroResol == null ? "" : cfg.nroResol.trim());
        } catch (Exception e) {
            nroResol = null;
        }
        if (nroResol == null || nroResol <= 0) {
            System.out.println("警告：empresa.nroResol 为空或<=0，将使用 NroResol=0 生成 XML。提交 SII 大概率返回 CRT-3-19（Fecha/Numero Resolucion Invalido）。");
            nroResol = 0;
        }
        data.setNroResol(nroResol);

        data.setTmstFirmaEnv(currentTime);
        data.setTmstFirma(currentTime);

        return data;
    }

    private static class Totals {
        BigDecimal mntNeto;
        BigDecimal mntExe;
        BigDecimal iva;
        BigDecimal mntTotal;
    }

    private static Totals computeTotals(List<Line> lines) {
        BigDecimal mntNeto = BigDecimal.ZERO;
        BigDecimal mntExe = BigDecimal.ZERO;
        BigDecimal iva = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;

        for (Line l : lines) {
            BigDecimal lineGross = l.unitPriceWithIva.multiply(new BigDecimal(l.qty));
            total = total.add(lineGross);

            if (l.exento) {
                mntExe = mntExe.add(lineGross);
                continue;
            }

            BigDecimal unitNet = calcNetFromGross(l.unitPriceWithIva);
            BigDecimal lineNet = unitNet.multiply(new BigDecimal(l.qty)).setScale(0, RoundingMode.UNNECESSARY);
            BigDecimal lineIva = lineGross.subtract(lineNet);

            mntNeto = mntNeto.add(lineNet);
            iva = iva.add(lineIva);
        }

        Totals t = new Totals();
        t.mntNeto = mntNeto.compareTo(BigDecimal.ZERO) == 0 ? null : mntNeto;
        t.mntExe = mntExe.compareTo(BigDecimal.ZERO) == 0 ? null : mntExe;
        t.iva = iva.compareTo(BigDecimal.ZERO) == 0 ? null : iva;
        t.mntTotal = total.setScale(0, RoundingMode.UNNECESSARY);
        return t;
    }

    private static BigDecimal calcNetFromGross(BigDecimal gross) {
        return gross.divide(new BigDecimal("1.19"), 0, RoundingMode.HALF_UP);
    }

    private static BigDecimal bd(int v) {
        return new BigDecimal(v);
    }
}
