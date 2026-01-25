# 从已通过的 EnvioBOLETA 反推生成 RCOF（补传 COF）

当某一批 `EnvioBOLETA` 已在 SII 端处理通过（DTE 已生效），但对应日期的 RCOF/COF 仍未成功接收时，可以使用本入口从已生成的 `EnvioBOLETA` XML 解析 `Folio/FchEmis/Totales`，并生成匹配的 `RVD_RCOF_YYYY-MM-DD_SECx_...xml`。

## 入口

- Java 主类：`com.searly.taxcontrol.sii.util.GenerateRcofFromEnvioBoleta`

## 运行命令（Windows / PowerShell）

```powershell
$cp = (Get-Content -Raw cp.txt).Trim()
$classpath = "target\\classes;$cp"

# 仅从已存在的 EnvioBOLETA XML 生成 RCOF，不会生成/重签 DTE
java -cp $classpath com.searly.taxcontrol.sii.util.GenerateRcofFromEnvioBoleta `
  output\\batch_1054_1058_20260125_021232_05_最终XML_发送.xml 1
```

## 参数

- 第 1 个参数：`EnvioBOLETA` XML 文件路径
- 第 2 个参数（可选）：`SecEnvio`（默认 1）
- 第 3 个参数（可选）：输出文件路径

## 输出

默认输出到 `output/`：

- `RVD_RCOF_<FchEmis>_SEC<SecEnvio>_FROM_<minFolio>_<maxFolio>.xml`
