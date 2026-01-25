# SingleCaseSendAndQuery（单张生成 + 生产/认证发送 + 查询）

## 入口

- Java 主类：`com.searly.taxcontrol.sii.util.SingleCaseSendAndQuery`

该入口支持：

- **仅生成**：生成 1 张 DTE（默认 CASO-1）并落盘到 `output/`，不调用网络接口。
- **发送 + 查询**：生成 1 张 DTE，调用 SII 发送接口，并自动进行查询（多次重试）。

## 环境切换（test / prod）

环境由 `src/main/resources/config/sii.properties` 控制：

- `sii.environment=test`：认证/测试环境
- `sii.environment=prod`：生产环境

注意：

- 生产/认证环境 **必须使用对应环境的 CAF**。否则会出现：`CAF codigo=516 / IDK no corresponde al ambiente`。
- 生产环境的 Boleta：
  - Carátula 的 `RutReceptor` 必须是 SII（通常 `60803000-K`）
  - Documento 的 `RUTRecep` 才能使用“一般消费者”（例如 `66666666-6`）

## Folio 策略（按环境分离计数器）

为避免认证/测试与生产互相污染，Folio 计数器已按环境分离：

- 生产（prod）：`temp/folio-counter-prod.txt`
  - 默认起始：`1`
  - 默认结束：`5000`
- 认证/测试（test）：`temp/folio-counter-test.txt`
  - 默认起始：`1000`
  - 默认结束：`2000`

你也可以通过系统属性覆盖范围：

- `-Dsii.folioStart=...`
- `-Dsii.folioEnd=...`

## CAF 路径（支持覆盖）

默认 CAF：

- `caf/FoliosSII780654383912025991029.xml`

可通过系统属性覆盖：

- `-Dsii.cafPath=绝对路径或相对路径`

## 运行命令（Windows）

### 1）编译

```powershell
mvn -DskipTests package
```

### 2）仅生成（不发送）

```powershell
cmd /s /c "java -Dsii.onlyGenerate=true -Djava.library.path=chilkat-jdk11-x64 -cp `"target\tax-dack-1.0.3-shaded.jar;chilkat-jdk11-x64\chilkat.jar`" com.searly.taxcontrol.sii.util.SingleCaseSendAndQuery"
```

### 3）发送 + 查询（会发到当前环境 test/prod）

```powershell
cmd /s /c "java -Djava.library.path=chilkat-jdk11-x64 -cp `"target\tax-dack-1.0.3-shaded.jar;chilkat-jdk11-x64\chilkat.jar`" com.searly.taxcontrol.sii.util.SingleCaseSendAndQuery"
```

如需指定 CAF：

```powershell
cmd /s /c "java -Dsii.cafPath=caf\YOUR_PROD_CAF.xml -Djava.library.path=chilkat-jdk11-x64 -cp `"target\tax-dack-1.0.3-shaded.jar;chilkat-jdk11-x64\chilkat.jar`" com.searly.taxcontrol.sii.util.SingleCaseSendAndQuery"
```

如需显式指定 Carátula 与 Documento 的 Receptor：

```powershell
cmd /s /c "java -Dsii.cafPath=caf\YOUR_PROD_CAF.xml -Dsii.caratulaRutReceptor=60803000-K -Dsii.rutReceptor=66666666-6 -Djava.library.path=chilkat-jdk11-x64 -cp `"target\tax-dack-1.0.3-shaded.jar;chilkat-jdk11-x64\chilkat.jar`" com.searly.taxcontrol.sii.util.SingleCaseSendAndQuery"
```

## 输出与证据落盘（发送模式）

发送模式会自动落盘证据到 `output/`，文件名会带上 `trackid`：

- `output/<base>_track_<trackId>_send_request.xml`
- `output/<base>_track_<trackId>_send_response.json`
- `output/<base>_track_<trackId>_query_response.json`

同时仍会保存最终 XML：

- `output/invoice_<folio>_<timestamp>_05_最终XML_发送.xml`

## 已知问题与排查

- **生产环境被拒绝：CAF 环境不匹配**
  - 典型回执：`codigo=516` / `IDK no corresponde al ambiente` / `CAF - IDK enviado es de certificacion`
  - 解决：更换为 **生产 CAF** 后重发。

- **fat-jar 运行报 Invalid signature file digest**
  - 已在 `pom.xml` 的 `maven-shade-plugin` 中过滤 `META-INF/*.SF/*.RSA/*.DSA`。
