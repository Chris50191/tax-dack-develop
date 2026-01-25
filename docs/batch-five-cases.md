# 单个 XML 内包含 5 个 CASE（CASO-1..CASO-5）

本项目支持一次生成 **1 个 EnvioBOLETA XML**，其中包含 **5 张 DTE**（对应 SetBasico 的 CASO-1..CASO-5）。

## 入口

- Java 主类：`com.searly.taxcontrol.sii.util.FiveCasesGenerateOnly`

该入口会：
- 从 `temp/folio-counter.txt` 读取当前起始 Folio（默认 1000）
- 生成 5 张 DTE（CASO-1..CASO-5），使用连续 Folio
- 在同一个 `EnvioBOLETA/SetDTE` 内写入 5 个 `<DTE>`
- 对每个 `Documento` 插入 TED，并按最终口径（template 紧凑串 + ISO-8859-1）计算 `FRMT`
- 使用 Chilkat 进行 **每张 Documento 内层签名** + **SetDTE 外层签名**
- 输出单个最终 XML 到 `output/`
- 将 `temp/folio-counter.txt` 前移 5（避免重复）

## 运行命令（Windows / PowerShell）

```powershell
$cp = (Get-Content -Raw cp.txt).Trim()
$classpath = "target\\classes;$cp"
java -cp $classpath com.searly.taxcontrol.sii.util.FiveCasesGenerateOnly
```

生成结果会打印：
- `已生成批量最终XML(5 cases in 1 EnvioBOLETA): <path>`

## 注意

- 该入口是 **仅生成**，不会调用网络接口发送。
- 如需发送，可在 SII 门户手工上传该单个 XML，或后续再新增“批量发送”入口。

## Swing 工具入口（可视化）

在 Swing 工具 `Empresa Emisora` 窗口中点击：`Certificación Set Básico`。

界面右下角新增按钮：`Generar 5 Casos + RCOF`，该按钮会：

- 使用界面中的 `CAF` 文件与 `Folio Inicial`
- 生成 5 张 DTE（CASO-1..CASO-5），Folio 从 `Folio Inicial` 开始连续递增
- 输出 1 个 EnvioBOLETA（包含 5 张 DTE）到 `output/`（并尝试拷贝到你选的 `Salida` 目录）
- 生成 1 个匹配的 RCOF 到你选的 `Salida` 目录
  - 文件名：`RVD_RCOF_<FchEmis>_SEC<SecEnvio>_FROM_<minFolio>_<maxFolio>.xml`
  - `SecEnvio` 使用 `sii-tool.properties` 中的 `rvd.secEnvio`，生成成功后会自动 `+1` 并保存回配置

提示：批量 5-case 当前仅支持 `Solo Generar`，发送请手工 Upload 生成的 EnvioBOLETA 与 RCOF。
