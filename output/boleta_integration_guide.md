## SII Boleta 对接整体设计与集成说明

本文档结合官方 XSD/示意图（`output/schema_envio_bol_720/*`、`output/diag_boleta_720/*`）与调试记录，总结当前项目的整体设计、对接流程与常见问题排查。

---

## 1. 目标与范围

目标：完成 Boleta（DTE 39）发送与状态查询的端到端对接，确保 XML 结构与签名符合官方 Schema（`EnvioBOLETA_v11.xsd`）。

范围：
- 生成 Boleta XML（`EnvioBOLETA`）
- Documento 与 EnvioBOLETA 两级签名
- 发送到 SII 接口并获取 `trackId`
- 使用 `trackId` 查询处理状态

---

## 2. 系统结构（当前项目实现）

- 配置：`SiiConfig`
- 发送服务：`SiiApiService`
- XML 生成与签名：`InvoiceGenerator`
- 演示与调试：`Demo`

核心对象：
- `InvoiceData`：业务发票数据
- `EnvioBOLETA / SetDTE / DTE / Documento`：XML 结构模型
- `ResultadoEnvioPost`：发送响应（含 `trackId`）
- `SiiEnvioStatusResponse`：状态查询响应

---

## 3. 配置与环境

配置文件：`src/main/resources/config/sii.properties`

关键项：
- `sii.environment`：`test` 或 `prod`
- `sii.api.url.*`、`sii.boleta.url.*`
- `sii.certificate.path`、`sii.certificate.password`

提示：
- 生产环境一般要求 `EnvioBOLETA_v11.xsd`
- 测试环境可能使用 `v10` 或 `v11`

---

## 4. 总体对接流程

1. 读取配置与证书
2. 从 CAF 读取 RUT 与 Folio 范围
3. 构造 `InvoiceData`
4. 生成 `EnvioBOLETA` XML
5. 内部签名（`Documento`）
6. 外部签名（`SetDTE`），并移动到 `EnvioBOLETA` 根节点
7. 发送 XML 至 SII
8. 获取 `trackId` 并查询状态

---

## 5. XML 结构（官方 Schema 要求）

来自 `EnvioBOLETA_v11.xsd`：

```
<EnvioBOLETA>
  <SetDTE>
    <Caratula>...</Caratula>
    <DTE>...</DTE>
  </SetDTE>
  <ds:Signature/>   <-- 根节点签名（必须）
</EnvioBOLETA>
```

而 `DTE` 的结构是：

```
<DTE>
  <Documento>...</Documento>
  <ds:Signature/>   <-- Documento 签名（必须）
</DTE>
```

注意：
- `Documento` 内部不允许直接出现 `Signature`
- `SetDTE` 内部不允许出现 `Signature`

---

## 6. 签名规则（结合官方 xmldsignature_v10.xsd）

1) Documento 签名：
- `SignedInfo` 中包含 `Reference URI="#DocumentoID"`
- `Transforms` 只能包含一个 `Transform`（`enveloped-signature`）
- `CanonicalizationMethod` 在 `SignedInfo` 中设置

2) EnvioBOLETA 根签名：
- 对 `SetDTE` 签名
- 签名节点必须是 `EnvioBOLETA` 的直接子节点

3) 命名空间：
- 必须使用 `ds:` 前缀
- 根节点声明：`xmlns:ds="http://www.w3.org/2000/09/xmldsig#"`

---

## 7. 关键字段说明（业务含义）

- `RutEmisor`：开票主体
- `RutEnvia`：发送人（证书持有人）
- `RutReceptor`（Caratula）：通常为 SII（`60803000-K`）
- `RUTRecep`（DTE）：客户 RUT（或通用消费者）
- `TmstFirmaEnv` / `TmstFirma`：签名时间
- `Folio`：必须在 CAF 范围内

---

## 8. 发送 Boleta 字段清单 + 业务含义（合并表格）

说明：字段来自 `EnvioBOLETA_v11.xsd`，按层级整理，便于对接与排查。

| 层级路径 | 必填 | 业务含义 |
| --- | --- | --- |
| `EnvioBOLETA.version` | 是 | 固定版本号 `1.0`。 |
| `EnvioBOLETA.SetDTE` | 是 | 本次发送包的主体。 |
| `EnvioBOLETA.ds:Signature` | 是 | 发送包根级签名。 |
| `SetDTE.ID` | 是 | 发送包唯一标识（签名引用）。 |
| `SetDTE.Caratula` | 是 | 发送封面摘要。 |
| `SetDTE.DTE` | 是 | Boleta 明细（1~500 条）。 |
| `Caratula.version` | 是 | 固定 `1.0`。 |
| `Caratula.RutEmisor` | 是 | 开票主体 RUT。 |
| `Caratula.RutEnvia` | 是 | 实际发送者 RUT（证书持有人）。 |
| `Caratula.RutReceptor` | 是 | 接收方 RUT（一般为 SII `60803000-K`）。 |
| `Caratula.FchResol` | 是 | SII 授权决议日期。 |
| `Caratula.NroResol` | 是 | SII 授权决议编号。 |
| `Caratula.TmstFirmaEnv` | 是 | 发送包签名时间。 |
| `Caratula.SubTotDTE` | 是 | 按类型汇总数量。 |
| `Caratula.SubTotDTE.TpoDTE` | 是 | DTE 类型（39/41）。 |
| `Caratula.SubTotDTE.NroDTE` | 是 | 本次发送数量。 |
| `DTE.Documento` | 是 | 单张 Boleta 数据。 |
| `DTE.ds:Signature` | 是 | Documento 的签名节点。 |
| `Documento.ID` | 是 | 单张 Boleta 的唯一 ID（签名引用）。 |
| `Documento.Encabezado` | 是 | 票头与总计。 |
| `Documento.Detalle` | 否 | 明细行（0~1000）。 |
| `Documento.SubTotInfo` | 否 | 信息型小计（0~20）。 |
| `Documento.DscRcgGlobal` | 否 | 全局折扣/加成（0~20）。 |
| `Documento.Referencia` | 否 | 关联单据（0~40）。 |
| `Documento.TED` | 是 | 电子印章（Timbre）。 |
| `Documento.TmstFirma` | 是 | Documento 签名时间。 |
| `Encabezado.IdDoc` | 是 | DTE 识别信息。 |
| `IdDoc.TipoDTE` | 是 | DTE 类型（39/41）。 |
| `IdDoc.Folio` | 是 | 发票号（CAF 范围内）。 |
| `IdDoc.FchEmis` | 是 | 开票日期。 |
| `IdDoc.IndServicio` | 是 | 交易类型。 |
| `IdDoc.IndMntNeto` | 否 | 明细金额是否按净额。 |
| `IdDoc.PeriodoDesde` | 否 | 周期服务开始日期。 |
| `IdDoc.PeriodoHasta` | 否 | 周期服务结束日期。 |
| `IdDoc.FchVenc` | 否 | 付款到期日。 |
| `Encabezado.Emisor` | 是 | 开票方信息。 |
| `Emisor.RUTEmisor` | 是 | 开票方 RUT。 |
| `Emisor.RznSocEmisor` | 否 | 开票方名称。 |
| `Emisor.GiroEmisor` | 否 | 经营活动（行业）。 |
| `Emisor.CdgSIISucur` | 否 | SII 分支机构编码。 |
| `Emisor.DirOrigen` | 否 | 开票地址。 |
| `Emisor.CmnaOrigen` | 否 | 开票所在区。 |
| `Emisor.CiudadOrigen` | 否 | 开票城市。 |
| `Encabezado.Receptor` | 是 | 购买方信息。 |
| `Receptor.RUTRecep` | 是 | 客户 RUT。 |
| `Receptor.CdgIntRecep` | 否 | 客户内部编码。 |
| `Receptor.RznSocRecep` | 否 | 客户名称。 |
| `Receptor.Contacto` | 否 | 客户联系方式。 |
| `Receptor.DirRecep` | 否 | 收货/服务地址。 |
| `Receptor.CmnaRecep` | 否 | 收货区。 |
| `Receptor.CiudadRecep` | 否 | 收货城市。 |
| `Receptor.DirPostal` | 否 | 邮寄地址。 |
| `Receptor.CmnaPostal` | 否 | 邮寄区。 |
| `Receptor.CiudadPostal` | 否 | 邮寄城市。 |
| `Encabezado.Totales` | 是 | 金额汇总。 |
| `Totales.MntNeto` | 否 | 净额（不含税）。 |
| `Totales.MntExe` | 否 | 免税金额。 |
| `Totales.IVA` | 否 | 增值税金额。 |
| `Totales.MntTotal` | 是 | 总额。 |
| `Totales.MontoNF` | 否 | 非可开票金额。 |
| `Totales.TotalPeriodo` | 否 | 周期合计。 |
| `Totales.SaldoAnterior` | 否 | 上期余额。 |
| `Totales.VlrPagar` | 否 | 本期应付。 |
| `Detalle.NroLinDet` | 是 | 明细行号。 |
| `Detalle.CdgItem` | 否 | 商品/服务编码（0~5）。 |
| `Detalle.CdgItem.TpoCodigo` | 是 | 编码类型。 |
| `Detalle.CdgItem.VlrCodigo` | 是 | 编码值。 |
| `Detalle.IndExe` | 否 | 明细免税/不可开票标识。 |
| `Detalle.ItemEspectaculo` | 否 | 演出票项类型。 |
| `Detalle.RUTMandante` | 否 | 委托方 RUT。 |
| `Detalle.NmbItem` | 是 | 商品/服务名称。 |
| `Detalle.InfoTicket` | 否 | 票务信息。 |
| `Detalle.InfoTicket.FolioTicket` | 是 | 票号。 |
| `Detalle.InfoTicket.FchGenera` | 是 | 票生成时间。 |
| `Detalle.InfoTicket.NmbEvento` | 是 | 事件名称。 |
| `Detalle.InfoTicket.TpoTicket` | 是 | 票种。 |
| `Detalle.InfoTicket.CdgEvento` | 是 | 事件编码。 |
| `Detalle.InfoTicket.FchEvento` | 是 | 事件时间。 |
| `Detalle.InfoTicket.LugarEvento` | 是 | 事件地点。 |
| `Detalle.InfoTicket.UbicEvento` | 是 | 事件区域。 |
| `Detalle.InfoTicket.FilaUbicEvento` | 否 | 座位排。 |
| `Detalle.InfoTicket.AsntoUbicEvento` | 否 | 座位号。 |
| `Detalle.DscItem` | 否 | 明细描述。 |
| `Detalle.QtyItem` | 否 | 数量。 |
| `Detalle.UnmdItem` | 否 | 单位。 |
| `Detalle.PrcItem` | 否 | 单价。 |
| `Detalle.DescuentoPct` | 否 | 折扣百分比。 |
| `Detalle.DescuentoMonto` | 否 | 折扣金额。 |
| `Detalle.RecargoPct` | 否 | 加成百分比。 |
| `Detalle.RecargoMonto` | 否 | 加成金额。 |
| `Detalle.MontoItem` | 是 | 明细行金额。 |
| `SubTotInfo.NroSTI` | 是 | 小计编号。 |
| `SubTotInfo.GlosaSTI` | 否 | 小计名称。 |
| `SubTotInfo.OrdenSTI` | 否 | 打印顺序。 |
| `SubTotInfo.SubTotNetoSTI` | 否 | 小计净额。 |
| `SubTotInfo.SubTotIVASTI` | 否 | 小计 IVA。 |
| `SubTotInfo.SubTotAdicSTI` | 否 | 小计附加税。 |
| `SubTotInfo.SubTotExeSTI` | 否 | 小计免税额。 |
| `SubTotInfo.ValSubtotSTI` | 否 | 小计金额。 |
| `SubTotInfo.LineasDeta` | 否 | 小计涉及的明细行号。 |
| `DscRcgGlobal.NroLinDR` | 是 | 折扣/加成行号。 |
| `DscRcgGlobal.TpoMov` | 是 | 动作类型（D/R）。 |
| `DscRcgGlobal.GlosaDR` | 否 | 描述。 |
| `DscRcgGlobal.TpoValor` | 是 | 单位（%/$）。 |
| `DscRcgGlobal.ValorDR` | 是 | 折扣/加成值。 |
| `DscRcgGlobal.IndExeDR` | 否 | 是否影响免税项。 |
| `Referencia.NroLinRef` | 是 | 参考行号。 |
| `Referencia.TpoDocRef` | 否 | 参考单据类型。 |
| `Referencia.FolioRef` | 否 | 参考单据号。 |
| `Referencia.CodRef` | 否 | 参考原因代码。 |
| `Referencia.RazonRef` | 否 | 参考原因描述。 |
| `Referencia.CodVndor` | 否 | 业务员编码。 |
| `Referencia.CodCaja` | 否 | 收银机编码。 |
| `TED.version` | 是 | 固定 `1.0`。 |
| `TED.DD` | 是 | TED 核心数据。 |
| `TED.DD.RE` | 是 | 开票方 RUT。 |
| `TED.DD.TD` | 是 | DTE 类型。 |
| `TED.DD.F` | 是 | Folio。 |
| `TED.DD.FE` | 是 | 开票日期。 |
| `TED.DD.RR` | 是 | 客户 RUT。 |
| `TED.DD.RSR` | 是 | 客户名称。 |
| `TED.DD.MNT` | 是 | 总额。 |
| `TED.DD.IT1` | 是 | 第一条明细描述。 |
| `TED.DD.CAF` | 是 | CAF 数据。 |
| `TED.DD.CAF.DA` | 是 | CAF 授权信息。 |
| `TED.DD.CAF.DA.RE` | 是 | CAF 授权的 Emisor RUT。 |
| `TED.DD.CAF.DA.RS` | 是 | CAF 授权的 Emisor 名称。 |
| `TED.DD.CAF.DA.TD` | 是 | CAF 授权的 DTE 类型。 |
| `TED.DD.CAF.DA.RNG.D` | 是 | CAF Folio 起始。 |
| `TED.DD.CAF.DA.RNG.H` | 是 | CAF Folio 结束。 |
| `TED.DD.CAF.DA.FA` | 是 | CAF 授权日期。 |
| `TED.DD.CAF.DA.RSAPK` 或 `TED.DD.CAF.DA.DSAPK` | 是 | CAF 公钥（RSA/DSA 二选一）。 |
| `TED.DD.CAF.DA.IDK` | 是 | 密钥标识。 |
| `TED.DD.CAF.FRMA` | 是 | SII 对 DA 的签名。 |
| `TED.TSTED` | 是 | TED 生成时间。 |
| `TED.FRMT` | 是 | TED 签名值。 |

---

## 9. 枚举值含义（表格）

| 字段 | 枚举值 | 含义 |
| --- | --- | --- |
| `TipoDTE` | `39` | Boleta Electrónica |
| `TipoDTE` | `41` | Boleta Exenta Electrónica |
| `IndServicio` | `1` | Boleta de Servicios Periódicos |
| `IndServicio` | `2` | Boleta de Servicios Periódicos Domiciliarios |
| `IndServicio` | `3` | Boleta de Ventas y Servicio |
| `IndServicio` | `4` | Boleta de Espectáculo emitida por cuenta de terceros |
| `IndMntNeto` | `2` | 明细行金额为“净额”（不含税） |
| `IndExe` | `1` | 明细为免税/非应税 |
| `IndExe` | `2` | 明细不可开票 |
| `IndExe` | `6` | 官方 XSD 未明确说明（按 SII 指引处理） |
| `ItemEspectaculo` | `01` | TICKET |
| `ItemEspectaculo` | `02` | VALOR SERVICIO |
| `TpoMov` | `D` | Descuento（折扣） |
| `TpoMov` | `R` | Recargo（加成） |
| `TpoValor` | `%` | 按百分比 |
| `TpoValor` | `$` | 按金额（比索） |
| `FRMT.algoritmo` | `SHA1withRSA` | RSA 签名算法 |
| `FRMT.algoritmo` | `SHA1withDSA` | DSA 签名算法 |

---

## 11. 接口响应字段整理（发送 + 查询，表格）

### 11.1 发送接口响应（XML/JSON）
对应模型：`ResultadoEnvioPost`、`SiiInvoiceResponse`

| 字段 | 必填 | 业务含义 |
| --- | --- | --- |
| `rut_emisor` | 是 | 发票发送者 RUT。 |
| `rut_envia` | 是 | 实际发送者 RUT。 |
| `trackid` | 是 | 跟踪 ID（用于后续查询）。 |
| `fecha_recepcion` | 是 | SII 接收时间。 |
| `estado` | 是 | 处理状态码。 |
| `file` | 否 | SII 记录的文件标识。 |

常见状态码（来自现有模型/经验）：
- `REC`：已接收待处理
- `EPR`：处理中
- `RFR`：已接收，处理中（查询接口常见；发送接口可能表示格式拒收，需结合返回详情）
- `RCH`：拒收（业务规则失败）
- `ACD`：已接受
- `FIR`：签名拒收
- `PRC`：部分接受
- `DUP`：重复发送

提示：不同接口可能返回不同状态码集合，建议同时参考 `output/estado_boleta.md`。

### 11.2 查询接口响应（JSON）
对应模型：`SiiEnvioStatusResponse`

| 字段 | 必填 | 业务含义 |
| --- | --- | --- |
| `rut_emisor` | 是 | 发票发送者 RUT。 |
| `rut_envia` | 是 | 实际发送者 RUT。 |
| `trackid` | 是 | 跟踪 ID。 |
| `fecha_recepcion` | 是 | 接收时间。 |
| `estado` | 是 | 处理状态码。 |
| `estadistica[]` | 否 | 统计信息列表。 |
| `estadistica[].tipo` | 否 | DTE 类型。 |
| `estadistica[].informados` | 否 | 已报告数量。 |
| `estadistica[].aceptados` | 否 | 已接受数量。 |
| `estadistica[].rechazados` | 否 | 已拒绝数量。 |
| `estadistica[].reparos` | 否 | 待修复数量。 |
| `detalle_rep_rech[]` | 否 | 明细错误/拒绝信息列表。 |
| `detalle_rep_rech[].tipo` | 否 | DTE 类型。 |
| `detalle_rep_rech[].folio` | 否 | 发票号。 |
| `detalle_rep_rech[].estado` | 否 | 单张票状态。 |
| `detalle_rep_rech[].descripcion` | 否 | 总体说明。 |
| `detalle_rep_rech[].error[]` | 否 | 错误清单。 |
| `detalle_rep_rech[].error[].seccion` | 否 | 错误所在区块。 |
| `detalle_rep_rech[].error[].linea` | 否 | 错误所在行。 |
| `detalle_rep_rech[].error[].nivel` | 否 | 严重级别。 |
| `detalle_rep_rech[].error[].codigo` | 否 | 错误码。 |
| `detalle_rep_rech[].error[].descripcion` | 否 | 错误描述。 |
| `detalle_rep_rech[].error[].detalle` | 否 | 错误详情。 |

---

## 12. 调试中发现的典型错误

1) `SCH-00001: Invalid Schema Name`
- 多数是 `xsi:schemaLocation` 不符合环境或版本

2) `LSX-00204: extra data at end of complex element`
- 结构多出签名节点或节点顺序不对

3) `LSX-00213: only 0 occurrences of particle "Signature"`
- 缺少 `DTE` 或 `EnvioBOLETA` 的签名节点

4) `cvc-complex-type.2.4.d: ds:Transform`
- `Transforms` 下有两个 `Transform`
- 需只保留 `enveloped-signature`

---

## 13. 本地 XSD 校验（诊断）

生成 XML 会写入：
- `*_05_最终XML_发送.xml`

并使用本地 XSD 进行校验（只作为诊断用途）。

注意：官方 XSD 存在 `PctType` 最小值冲突，本地校验已进行临时修正，仅用于定位结构错误。

---

## 14. 查询状态（trackId）

独立查询方式（运行参数）：

```
java -cp "target/classes;target/dependency/*" com.searly.taxcontrol.sii.demo.Demo 78065438-4 20442022270
```

返回状态码含义见：`output/estado_boleta.md`

---

## 15. 推荐的运行方式

1) 生成并发送：
```
mvn -q -DskipTests package
java -cp "target/classes;target/dependency/*" com.searly.taxcontrol.sii.demo.Demo
```

2) 查询状态：
```
java -cp "target/classes;target/dependency/*" com.searly.taxcontrol.sii.demo.Demo 78065438-4 20442022270
```

---

## 16. 常见实践建议

- 如果没有折扣/加成，不要输出 `DescuentoPct/RecargoPct`
- 结构校验不通过时，优先检查：签名位置与 ds 前缀
- 建议保留 `_05_最终XML_发送.xml` 作为对照

---

## 17. 参考资料（本地）

- 官方 Schema：`output/schema_envio_bol_720/EnvioBOLETA_v11.xsd`
- 签名规范：`output/schema_envio_bol_720/xmldsignature_v10.xsd`
- 结构示意图：`output/diag_boleta_720/*.png`
- 状态码对照：`output/estado_boleta.md`
