# Santuario vs Chilkat 签名不一致：根因、路线与配置

## 1. 背景

本项目当前使用 Chilkat 生成 XMLDSIG（Documento 内层 + SetDTE 外层）。
为了评估后续切换到 Apache Santuario（xmlsec）的可行性，我们对“同一份 XML”在两套实现下的 `DigestValue` 与 `SignatureValue` 做了对照。

对照工具：`com.searly.taxcontrol.sii.util.SantuarioChilkatSignatureLab`

输入样本（均为 Chilkat 已成功签名的 XML）：

- `output/batch_1059_1063_20260125_022124_05_最终XML_发送.xml`（EnvioBOLETA，包含 5 个 DTE）
- `output/RVD_RCOF_2026-01-25_SEC1_FROM_1054_1058_NEW.xml`（RCOF）

## 2. 根本原因（Root Cause）

### 2.1 现象

在 `EnvioBOLETA` 的 **Documento 内层签名**中：

- XML 里声明：
  - `SignedInfo/CanonicalizationMethod` = C14N 1.0（inclusive）
  - `Reference/Transforms/Transform` = C14N 1.0（inclusive）
  - 示例（节选）：

```xml
<CanonicalizationMethod Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/>
...
<Transform Algorithm="http://www.w3.org/TR/2001/REC-xml-c14n-20010315"/>
```

- 但对照实验结果表明：该 `DigestValue` **实际命中的是 Exclusive C14N** 的计算结果：

`http://www.w3.org/2001/10/xml-exc-c14n#`

即：**声明为 inclusive C14N，但真实计算等价于 exclusive C14N**。

### 2.2 影响

- Santuario（以及大多数标准实现）会严格按 XML 声明的 Transform/C14N 执行，因此会得到不同的 canonical bytes，从而导致：
  - `Reference.verify()` / digest 校验失败
  - `SignedInfo.verify()` / `checkSignatureValue()` 失败
  - 重新签名时 `DigestValue` / `SignatureValue` 无法与 Chilkat 完全一致

- 在同一个 `EnvioBOLETA` 中：
  - **SetDTE 外层签名**的 digest/签名更符合标准 inclusive C14N 1.0
  - **RCOF** 的签名也符合标准（digest/验签可按声明验证）

因此，差异点集中在 **EnvioBOLETA 的 Documento 内层签名**。

## 3. 复现方式（对照实验）

### 3.1 仅做诊断（不重签）

```powershell
$cp=(Get-Content -Raw cp.txt).Trim();
$classpath="target\classes;" + $cp;
java -Dlab.skipResign=true -cp $classpath com.searly.taxcontrol.sii.util.SantuarioChilkatSignatureLab "output\batch_1059_1063_20260125_022124_05_最终XML_发送.xml"
```

输出中会出现类似：

- Documento：`DIGEST MATCH via http://www.w3.org/2001/10/xml-exc-c14n#`
- SetDTE：`DIGEST MATCH via http://www.w3.org/TR/2001/REC-xml-c14n-20010315`

## 4. 两条路线

### 4.1 路线 A（标准 Santuario，输出与 Chilkat 不同）

- 优点：更符合 XMLDSIG 规范，Santuario/JSR105 验签可直接通过。
- 缺点：`DigestValue`/`SignatureValue` 不可能与历史 Chilkat 产物逐字节一致。

适用场景：不需要复刻 Chilkat 旧签名值，只要能通过 SII 即可。

### 4.2 路线 B（Santuario 复刻 Chilkat，输出与 Chilkat 一致）

目标：在 **不改变 XML 中 Algorithm 声明**（仍是 C14N 1.0）的前提下，让 Santuario 的实际 canonicalize 行为与 Chilkat 一致：

- Documento 内层：
  - SignedInfo 的 C14N 1.0 **实际按 exc-c14n 执行**
  - Reference 的 C14N 1.0 **实际按 exc-c14n 执行**
- SetDTE 外层：保持按标准 C14N 1.0 执行

这属于“兼容 Chilkat 非标准行为”的实现。

## 5. 推荐配置开关（正式程序可切换）

正式程序当前入口：`com.searly.taxcontrol.sii.util.InvoiceGenerator`

### 5.1 现有开关（已存在）

- `-Dsii.signer=chilkat`（默认）
- `-Dsii.allowNonChilkatSigner=true`（允许切换到非 chilkat，仅建议用于对照/实验）

### 5.2 计划新增 signer（实现路线 B）

- `-Dsii.signer=santuario-chilkat-compat`
  - 启用 Santuario 签名
  - 通过“Hybrid Canonicalizer + Hybrid Transform”在运行期覆盖 xmlsec 对 C14N 1.0 的实际执行，实现与 Chilkat 输出对齐

注意：该模式是为了“复刻 Chilkat 输出”；它属于非标准行为，后续需结合 SII 实际验收结果再决定是否用于生产。
