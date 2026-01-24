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
