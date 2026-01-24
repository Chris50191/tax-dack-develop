package com.searly.taxcontrol.sii.model.common;

import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 发票数据类
 * 包含生成发票XML所需的所有数据
 */
public class InvoiceData {
    
    // 基本信息
    private Integer tipoDTE;           // 发票类型 (39: 电子发票)
    private String folio;              // 发票号码
    private String fchEmis;            // 发票日期
    private Integer indServicio;       // 服务类型指示器
    private String documentId;         // 文档ID
    
    // 发送方信息
    private String rutEmisor;          // 发送方RUT
    private String rutEnvia;          // 发送方法人RUT
    private String rznSocEmisor;       // 发送方公司名称
    private String giroEmisor;         // 发送方业务类型
    private String dirOrigen;          // 发送方地址
    private String cmnaOrigen;         // 发送方社区
    private String ciudadOrigen;       // 发送方城市
    
    // 接收方信息
    private String rutReceptor;        // 接收方RUT
    private String rznSocReceptor;     // 接收方公司名称
    private String dirRecep;           // 接收方地址
    private String cmnaRecep;          // 接收方社区
    private String ciudadRecep;        // 接收方城市
    
    // 金额信息
    private BigDecimal mntNeto;           // 净额
    private BigDecimal mntExe;            // 免税金额
    private BigDecimal iva;               // IVA税额
    private BigDecimal mntAdic;           // 附加税额
    private BigDecimal mntTotal;          // 总金额
    
    // 商品信息
    private List<Product> products;

    // 来源信息
    private List<Reference> References;

    // 授权信息
    private String fchResol;           // 授权日期
    private Integer nroResol;          // 授权号码
    
    // 时间戳
    private String tmstFirmaEnv;       // 发送签名时间戳
    private String tmstFirma;          // 文档签名时间戳
    
    // 构造函数
    public InvoiceData() {}

    public List<Product> getProducts() {
        return this.products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public List<Reference> getIsReferences() {
        return this.References;
    }

    public void setIsReferences(List<Reference> references) {
        this.References = references;
    }

    public BigDecimal getMntAdic() {
        return this.mntAdic;
    }

    public void setMntAdic(BigDecimal mntAdic) {
        this.mntAdic = mntAdic;
    }

    public static class Product {
        private String nmbItem;            // 商品名称
        private BigDecimal qtyItem;           // 商品数量
        private String unmdItem;           // 单位
        private BigDecimal prcItem;           // 商品单价
        private BigDecimal montoItem;         // 商品金额
        private Integer indExe;            // 免税/不可开票标识
        private BigDecimal realAmountWithoutTax;         // 不含税金额
        private BigDecimal taxAmount;         // 增值税额
        private BigDecimal addTaxAmount;         // 附加税额
        private BigDecimal rate;         // 增值税率
        private BigDecimal addRate;         // 附加税率

        public Product() {
        }

        public Product(String nmbItem, BigDecimal qtyItem, BigDecimal prcItem, BigDecimal montoItem) {
            this.nmbItem = nmbItem;
            this.qtyItem = qtyItem;
            this.prcItem = prcItem;
            this.montoItem = montoItem;
        }

        public Product(String nmbItem, BigDecimal qtyItem, BigDecimal prcItem, BigDecimal montoItem, BigDecimal realAmountWithoutTax, BigDecimal taxAmount, BigDecimal addTaxAmount, BigDecimal rate, BigDecimal addRate) {
            this.nmbItem = nmbItem;
            this.qtyItem = qtyItem;
            this.prcItem = prcItem;
            this.montoItem = montoItem;
            this.realAmountWithoutTax = realAmountWithoutTax;
            this.taxAmount = taxAmount;
            this.addTaxAmount = addTaxAmount;
            this.rate = rate;
            this.addRate = addRate;
        }

        public Product(String nmbItem, BigDecimal montoItem, BigDecimal realAmountWithoutTax, BigDecimal taxAmount, BigDecimal addTaxAmount, BigDecimal rate, BigDecimal addRate) {
            this.nmbItem = nmbItem;
            this.montoItem = montoItem;
            this.realAmountWithoutTax = realAmountWithoutTax;
            this.taxAmount = taxAmount;
            this.addTaxAmount = addTaxAmount;
            this.rate = rate;
            this.addRate = addRate;
        }


        public String getNmbItem() {
            return this.nmbItem;
        }

        public void setNmbItem(String nmbItem) {
            this.nmbItem = nmbItem;
        }

        public BigDecimal getQtyItem() {
            return this.qtyItem;
        }

        public void setQtyItem(BigDecimal qtyItem) {
            this.qtyItem = qtyItem;
        }

        public String getUnmdItem() {
            return this.unmdItem;
        }

        public void setUnmdItem(String unmdItem) {
            this.unmdItem = unmdItem;
        }

        public BigDecimal getPrcItem() {
            return this.prcItem;
        }

        public void setPrcItem(BigDecimal prcItem) {
            this.prcItem = prcItem;
        }

        public BigDecimal getMontoItem() {
            return this.montoItem;
        }

        public void setMontoItem(BigDecimal montoItem) {
            this.montoItem = montoItem;
        }

        public Integer getIndExe() {
            return this.indExe;
        }

        public void setIndExe(Integer indExe) {
            this.indExe = indExe;
        }

        public BigDecimal getRealAmountWithoutTax() {
            return this.realAmountWithoutTax;
        }

        public void setRealAmountWithoutTax(BigDecimal realAmountWithoutTax) {
            this.realAmountWithoutTax = realAmountWithoutTax;
        }

        public BigDecimal getTaxAmount() {
            return this.taxAmount;
        }

        public void setTaxAmount(BigDecimal taxAmount) {
            this.taxAmount = taxAmount;
        }

        public BigDecimal getAddTaxAmount() {
            return this.addTaxAmount;
        }

        public void setAddTaxAmount(BigDecimal addTaxAmount) {
            this.addTaxAmount = addTaxAmount;
        }

        public BigDecimal getRate() {
            return this.rate;
        }

        public void setRate(BigDecimal rate) {
            this.rate = rate;
        }

        public BigDecimal getAddRate() {
            return this.addRate;
        }

        public void setAddRate(BigDecimal addRate) {
            this.addRate = addRate;
        }
    }

    public static class Reference {
        private String type;                // 来源发票类型
        private String billId;            // 来源发票单号
        private String invoiceDate;           // 来源发票日期
        private String reasonType;           // 引用原因类型，1-作废 2-更正原始单据的文字信息 3-更正金额 4-一般引用（仅作关联，不影响金额）
        private String resson;         // 引用原因

        public Reference() {
        }

        public Reference(String type, String billId, String invoiceDate, String reasonType, String resson) {
            this.type = type;
            this.billId = billId;
            this.invoiceDate = invoiceDate;
            this.reasonType = reasonType;
            this.resson = resson;
        }

        public String getType() {
            return this.type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getBillId() {
            return this.billId;
        }

        public void setBillId(String billId) {
            this.billId = billId;
        }

        public String getInvoiceDate() {
            return this.invoiceDate;
        }

        public void setInvoiceDate(String invoiceDate) {
            this.invoiceDate = invoiceDate;
        }

        public String getResson() {
            return this.resson;
        }

        public void setResson(String resson) {
            this.resson = resson;
        }

        public String getReasonType() {
            return this.reasonType;
        }

        public void setReasonType(String reasonType) {
            this.reasonType = reasonType;
        }
    }

    public void addProduct(String nmbItem, BigDecimal qtyItem, BigDecimal prcItem, BigDecimal montoItem){
        if (CollectionUtils.isEmpty(products)){
            this.products = new ArrayList<>();
        }
         this.products.add(new Product(nmbItem, qtyItem, prcItem, montoItem));
    }

    public void addProduct(String nmbItem, BigDecimal qtyItem, BigDecimal prcItem, BigDecimal montoItem, BigDecimal realAmount, BigDecimal taxAmount, BigDecimal addTaxAmount, BigDecimal rate, BigDecimal addRate){
        if (CollectionUtils.isEmpty(products)){
            this.products = new ArrayList<>();
        }
         this.products.add(new Product(nmbItem, qtyItem, prcItem, montoItem, realAmount, taxAmount, addTaxAmount, rate, addRate));
    }

    public void addProduct(String nmbItem, BigDecimal montoItem, BigDecimal realAmount, BigDecimal taxAmount, BigDecimal addTaxAmount, BigDecimal rate, BigDecimal addRate){
        if (CollectionUtils.isEmpty(products)){
            this.products = new ArrayList<>();
        }
         this.products.add(new Product(nmbItem, montoItem, realAmount, taxAmount, addTaxAmount, rate, addRate));
    }

    public void setTotal(BigDecimal mntNeto, BigDecimal iva){
        this.mntNeto = mntNeto;
        this.iva = iva;
        this.mntTotal = mntNeto.add(iva);
    }


    // Getter和Setter方法
    public Integer getTipoDTE() {
        return tipoDTE;
    }
    
    public void setTipoDTE(Integer tipoDTE) {
        this.tipoDTE = tipoDTE;
    }
    
    public String getFolio() {
        return folio;
    }
    
    public void setFolio(String folio) {
        this.folio = folio;
    }
    
    public String getFchEmis() {
        return fchEmis;
    }
    
    public void setFchEmis(String fchEmis) {
        this.fchEmis = fchEmis;
    }
    
    public Integer getIndServicio() {
        return indServicio;
    }
    
    public void setIndServicio(Integer indServicio) {
        this.indServicio = indServicio;
    }
    
    public String getDocumentId() {
        return documentId;
    }
    
    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
    
    public String getRutEmisor() {
        return rutEmisor;
    }
    
    public void setRutEmisor(String rutEmisor) {
        this.rutEmisor = rutEmisor;
    }
    
    public String getRznSocEmisor() {
        return rznSocEmisor;
    }
    
    public void setRznSocEmisor(String rznSocEmisor) {
        this.rznSocEmisor = rznSocEmisor;
    }
    
    public String getGiroEmisor() {
        return giroEmisor;
    }
    
    public void setGiroEmisor(String giroEmisor) {
        this.giroEmisor = giroEmisor;
    }
    
    public String getDirOrigen() {
        return dirOrigen;
    }
    
    public void setDirOrigen(String dirOrigen) {
        this.dirOrigen = dirOrigen;
    }
    
    public String getCmnaOrigen() {
        return cmnaOrigen;
    }
    
    public void setCmnaOrigen(String cmnaOrigen) {
        this.cmnaOrigen = cmnaOrigen;
    }
    
    public String getCiudadOrigen() {
        return ciudadOrigen;
    }
    
    public void setCiudadOrigen(String ciudadOrigen) {
        this.ciudadOrigen = ciudadOrigen;
    }
    
    public String getRutReceptor() {
        return rutReceptor;
    }
    
    public void setRutReceptor(String rutReceptor) {
        this.rutReceptor = rutReceptor;
    }
    
    public String getRznSocReceptor() {
        return rznSocReceptor;
    }
    
    public void setRznSocReceptor(String rznSocReceptor) {
        this.rznSocReceptor = rznSocReceptor;
    }
    
    public String getDirRecep() {
        return dirRecep;
    }
    
    public void setDirRecep(String dirRecep) {
        this.dirRecep = dirRecep;
    }
    
    public String getCmnaRecep() {
        return cmnaRecep;
    }
    
    public void setCmnaRecep(String cmnaRecep) {
        this.cmnaRecep = cmnaRecep;
    }
    
    public String getCiudadRecep() {
        return ciudadRecep;
    }
    
    public void setCiudadRecep(String ciudadRecep) {
        this.ciudadRecep = ciudadRecep;
    }

    public BigDecimal getMntNeto() {
        return this.mntNeto;
    }

    public void setMntNeto(BigDecimal mntNeto) {
        this.mntNeto = mntNeto;
    }

    public BigDecimal getMntExe() {
        return this.mntExe;
    }

    public void setMntExe(BigDecimal mntExe) {
        this.mntExe = mntExe;
    }

    public BigDecimal getIva() {
        return this.iva;
    }

    public void setIva(BigDecimal iva) {
        this.iva = iva;
    }

    public BigDecimal getMntTotal() {
        return this.mntTotal;
    }

    public void setMntTotal(BigDecimal mntTotal) {
        this.mntTotal = mntTotal;
    }

    public String getFchResol() {
        return fchResol;
    }
    
    public void setFchResol(String fchResol) {
        this.fchResol = fchResol;
    }
    
    public Integer getNroResol() {
        return nroResol;
    }
    
    public void setNroResol(Integer nroResol) {
        this.nroResol = nroResol;
    }
    
    public String getTmstFirmaEnv() {
        return tmstFirmaEnv;
    }
    
    public void setTmstFirmaEnv(String tmstFirmaEnv) {
        this.tmstFirmaEnv = tmstFirmaEnv;
    }
    
    public String getTmstFirma() {
        return tmstFirma;
    }
    
    public void setTmstFirma(String tmstFirma) {
        this.tmstFirma = tmstFirma;
    }

    public String getRutEnvia() {
        return this.rutEnvia;
    }

    public void setRutEnvia(String rutEnvia) {
        this.rutEnvia = rutEnvia;
    }


}
