# 证书RUT不匹配问题分析和解决方案

## 🔴 问题确认

**错误代码：** 505  
**错误描述：** "Firma DTE Incorrecta (Rechaza DTE)"

**根本原因：** 证书RUT与发票rutEmisor不匹配

---

## 📊 当前配置分析

### 证书信息

从生成的XML中提取的证书信息：

```xml
<X509Certificate>...</X509Certificate>
```

**证书Subject（解码后）：**
- `CN=DAQI WU WU1`
- `SERIALNUMBER=24529296-1`
- `EMAILADDRESS=PANSONGQIONG@GMAIL.COM`

**证书RUT：** `24529296-1`

### 发票信息

从响应中提取的发票信息：
```json
{
  "rut_emisor": "78065438-4",
  "rut_envia": "24529296-1"
}
```

- **rutEmisor（发票发行方）：** `78065438-4`
- **rutEnvia（发票发送方）：** `24529296-1`

### 匹配情况

| RUT类型 | 值 | 证书RUT | 匹配状态 |
|---------|-----|---------|----------|
| **rutEmisor** | `78065438-4` | `24529296-1` | ❌ **不匹配** |
| **rutEnvia** | `24529296-1` | `24529296-1` | ✅ 匹配 |

---

## 🔍 SII的RUT匹配要求

### 要求1：Token认证RUT

**代码位置：** `SiiApiService.java` 第108-138行

```java
// 获取Token时使用的RUT必须与发票中的rutEmisor一致
String invoiceRutEmisor = request.getInvoiceData().getRutEmisor();
String rutForAuth = rutParts[0]; // 用于认证的RUT（不含验证位）
String token = AuthUtils.getToken(apiBaseUrl, rutForAuth, ...);
```

✅ **当前状态：** 正确  
- 使用 `rutEmisor`（`78065438`）获取Token

---

### 要求2：签名证书RUT（关键问题）

**SII规范要求：**

根据SII的规范，**用于签名DTE的证书RUT必须与rutEmisor匹配**。

**例外情况：**
- 如果使用代理签名（cesión），证书RUT可以与rutEnvia匹配
- 但必须**在SII系统中注册代理签名关系**

**当前情况：**
- 证书RUT：`24529296-1`
- rutEmisor：`78065438-4` ❌ **不匹配**
- rutEnvia：`24529296-1` ✅ 匹配

**问题分析：**
1. 如果这是**直接签名**（非代理），证书RUT必须与rutEmisor匹配
2. 如果这是**代理签名**，需要在SII系统中配置代理关系

---

## 🛠️ 解决方案

### 方案1：使用rutEmisor对应的证书（推荐）

**适用于：** 直接签名场景

**操作步骤：**

1. **获取rutEmisor对应的证书**
   - RUT：`78065438-4`
   - 确保该证书在有效期内
   - 确保该证书在SII系统中已注册

2. **修改代码使用rutEmisor的证书**
   
   检查您的证书加载逻辑，确保使用rutEmisor对应的证书：
   
   ```java
   // 在SiiApiService.java中
   // 确保加载的证书是rutEmisor（78065438-4）对应的证书
   KeyStore keyStore = CertificateManager.loadPKCS12Certificate(
       certificatePath,  // 使用rutEmisor的证书路径
       certificatePassword
   );
   ```

3. **重新生成XML并测试**

---

### 方案2：配置代理签名（如果适用）

**适用于：** 代理签名场景（rutEnvia与rutEmisor不同）

**前提条件：**
- 必须在SII系统中注册代理签名关系
- rutEnvia（`24529296-1`）必须有权限代表rutEmisor（`78065438-4`）签名

**操作步骤：**

1. **登录SII系统**
   - 使用rutEmisor（`78065438-4`）的账户登录

2. **注册代理签名关系**
   - 在SII系统中注册rutEnvia（`24529296-1`）为代理签名人
   - 确保代理关系已激活

3. **验证代理关系**
   - 确认rutEnvia的证书（`24529296-1`）在SII系统中已授权为代理签名证书

4. **使用现有代码**
   - 如果代理关系已配置，现有代码应该可以工作
   - 如果仍然报错，可能是代理关系未正确配置

---

### 方案3：统一RUT（如果可能）

**适用于：** 业务允许的情况下

**操作步骤：**

1. **修改发票数据**
   - 将 `rutEnvia` 设置为与 `rutEmisor` 相同（`78065438-4`）
   - 确保发送方和发行方是同一实体

2. **使用rutEmisor对应的证书**
   - 证书RUT：`78065438-4`
   - 签名发票

---

## 🔍 验证步骤

### 步骤1：检查证书RUT

**方法1：从证书文件中提取**

```bash
# 使用OpenSSL提取证书信息
openssl pkcs12 -in keystore.p12 -clcerts -nokeys -out certificate.pem
openssl x509 -in certificate.pem -noout -subject -serial
```

**方法2：从XML中提取（已生成）**

检查生成的XML文件中的 `<X509Certificate>` 节点，解码后查看Subject中的SERIALNUMBER。

**方法3：在Java代码中打印**

```java
X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
String subject = cert.getSubjectDN().getName();
System.out.println("证书Subject: " + subject);

// 提取RUT（通常从SERIALNUMBER或CN中提取）
// 格式：SERIALNUMBER=24529296-1
```

---

### 步骤2：检查证书权限

1. **登录SII系统**
   - 使用证书对应的RUT登录

2. **检查签名权限**
   - 确认该RUT有DTE签名权限
   - 确认证书在有效期内
   - 确认证书未被吊销

---

### 步骤3：检查代理关系（如果适用）

1. **登录SII系统（rutEmisor账户）**
   - RUT：`78065438-4`

2. **检查代理签名人列表**
   - 查看是否已注册 `24529296-1` 为代理签名人
   - 确认代理关系状态为"激活"

---

## 📋 检查清单

请逐项检查：

### 证书配置
- [ ] 证书RUT与rutEmisor匹配（`78065438-4`）
- [ ] 或证书RUT与rutEnvia匹配（`24529296-1`）且代理关系已配置

### 证书有效性
- [ ] 证书在有效期内
- [ ] 证书未被吊销
- [ ] 证书在SII系统中已注册

### 证书权限
- [ ] 证书对应的RUT有DTE签名权限
- [ ] 证书来自SII认可的CA（如Acerta.com）

### SII配置
- [ ] Token使用rutEmisor获取（已确认✅）
- [ ] 代理签名关系已配置（如果使用代理）

---

## 🎯 立即行动

### 优先级1：确认证书配置

1. **检查当前使用的证书RUT**
   ```java
   // 在代码中添加日志
   X509Certificate cert = (X509Certificate) ks.getCertificate(alias);
   System.out.println("证书Subject: " + cert.getSubjectDN().getName());
   ```

2. **确认证书RUT与rutEmisor的关系**
   - 如果不匹配，准备rutEmisor对应的证书

### 优先级2：选择解决方案

- **如果这是直接签名：** 使用方案1（rutEmisor证书）
- **如果这是代理签名：** 使用方案2（配置代理关系）
- **如果可以统一RUT：** 使用方案3（修改rutEnvia）

### 优先级3：测试验证

1. 使用正确的证书重新生成XML
2. 发送到SII测试环境
3. 检查是否还有505错误

---

## 💡 建议

**根据您的当前配置，最可能的问题是：**

1. **证书RUT不匹配rutEmisor**
   - 证书RUT：`24529296-1`
   - rutEmisor：`78065438-4`
   - **不匹配**

2. **SII可能要求证书RUT必须与rutEmisor匹配**
   - 除非配置了代理签名关系

**建议操作：**

1. **立即检查：** 是否有rutEmisor（`78065438-4`）对应的证书
2. **如果有：** 使用该证书替换当前证书，重新测试
3. **如果没有：** 需要：
   - 为rutEmisor申请证书，或
   - 在SII系统中配置代理签名关系

---

**证书RUT不匹配很可能是导致505错误的根本原因！**

请先检查证书配置，确保证书RUT与rutEmisor匹配（或在SII系统中配置了代理关系）。
