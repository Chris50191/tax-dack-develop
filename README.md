# 外网
```shell
git config remote.origin.url https://gitlab.s-early.com/onlean/tax-dack.git
```

# 内网
```shell
git config remote.origin.url http://192.168.50.200:8082/onlean/tax-dack.git
```

# 单用例自动发送/查询
```shell
# 运行单用例发送与查询（Folio 从 1000 自增，CAF=FoliosSII780654383912025991029.xml）
java -cp "target/classes;@cp.txt" com.searly.taxcontrol.sii.util.SingleCaseSendAndQuery
```

# 单个 XML 内包含 5 个 CASE（仅生成）
```shell
# 生成 1 个 EnvioBOLETA，其中包含 5 张 DTE（CASO-1..CASO-5），并输出到 output/
java -cp "target/classes;@cp.txt" com.searly.taxcontrol.sii.util.FiveCasesGenerateOnly
```

# 从已通过的 EnvioBOLETA 反推生成 RCOF（补传 COF）
```shell
# 从已存在的 EnvioBOLETA XML 解析 Totales/Folio 并生成 RCOF（不重生成 DTE）
# 参数: <envioBoletaXmlPath> [secEnvio]
@REM Windows PowerShell 推荐写法（确保依赖 jar 在 classpath 中）
powershell -NoProfile -Command "$cp=(Get-Content -Raw cp.txt).Trim(); $classpath='target\\classes;' + $cp; java -cp $classpath com.searly.taxcontrol.sii.util.GenerateRcofFromEnvioBoleta 'output\\batch_1054_1058_20260125_021232_05_最终XML_发送.xml' 1"