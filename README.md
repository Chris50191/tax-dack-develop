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