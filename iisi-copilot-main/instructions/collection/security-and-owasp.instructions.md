---
applyTo: '**'
description: '基於 OWASP Top 10 2025 的全面安全編碼標準，包含 55 多個反模式、檢測正規表示式、針對現代 Web 和後端框架的框架特定修復以及 AI/LLM 安全指南。'
---

# Security Standards 安全標準

全面的網頁應用程式開發安全規則。每種反模式都包含嚴重性分類、檢測方法、OWASP 2025 參考以及修正程式碼範例。

**Severity levels:**

- **CRITICAL** — 可被利用的漏洞。必須在合併前修復。
- **IMPORTANT** — 重大風險。應在同一個迭代中修復。
- **SUGGESTION** — 防禦深度改進。計劃在未來的迭代中實施。

---

## OWASP Top 10 — 2025 Quick Reference ,OWASP 前 10 名 — 2025 快速參考

| # | Category | Key Mitigation |
|---|----------|----------------|
| A01 | Broken Access Control | 每個端點都啟用身份驗證中間件、基於角色的存取控制 (RBAC) 和所有權檢查 |
| A02 | Security Misconfiguration | 安全標頭、在生產環境中禁用調試、無預設憑證 |
| A03 | Software Supply Chain Failures *(NEW)* | `npm audit`、鎖定檔完整性、SBOM、SLSA 來源 |
| A04 | Cryptographic Failures | 密碼使用 Argon2id/bcrypt、全程 TLS、代碼中不存放秘密 |
| A05 | Injection | 參數化查詢、輸入驗證、禁止使用用戶輸入的原始 HTML |
| A06 | Insecure Design | 威脅建模、安全設計模式、濫用案例測試 |
| A07 | Authentication Failures | 登入速率限制、安全的會話管理、多因素認證 (MFA) |
| A08 | Software or Data Integrity Failures | CDN 腳本使用 SRI、簽名工件、禁止不安全的反序列化 |
| A09 | Security Logging and Alerting Failures | 記錄安全事件、日誌中不包含 PII、關聯 ID、主動警報 |
| A10 | Mishandling of Exceptional Conditions *(NEW)* | 處理所有錯誤、在生產環境中不顯示堆疊追蹤、失敗安全 |

---

## Injection Anti-Patterns (I1-I8)  注入反模式 (I1-I8)

### I1: SQL Injection via String Concatenation  SQL 注入通過字符串連接

- **Severity**: CRITICAL
- **Detection**: `\$\{.*\}.*(?:SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)`
- **OWASP**: A05

```typescript
// BAD
const unsafeResult = await db.query(`SELECT * FROM users WHERE id = ${userId}`);

// GOOD — parameterized query
const safeResult = await db.query('SELECT * FROM users WHERE id = $1', [userId]);
```

### I2: NoSQL Injection (MongoDB Operator Injection)  NoSQL 注入 (MongoDB 操作符注入)

- **Severity**: CRITICAL
- **Detection**: `\{\s*\$(?:gt|gte|lt|lte|ne|in|nin|regex|where|exists)`
- **OWASP**: A05

```typescript
// BAD — attacker sends { "password": { "$gt": "" } }
const user = await User.findOne({ username: req.body.username, password: req.body.password });

// GOOD — validate and cast input types
const username = String(req.body.username);
const password = String(req.body.password);
const user = await User.findOne({ username });
const valid = user && await verifyPassword(user.passwordHash, password);
```

### I3: Command Injection (exec with User Input) 命令注入（使用用户输入的 exec）

- **Severity**: CRITICAL
- **Detection**: `(?:exec|execSync|execFile|execFileSync)\s*\(.*(?:req\.|params\.|query\.|body\.)`
- **OWASP**: A05

```typescript
// BAD — shell interpolation, sync call blocks the event loop
import { execFileSync } from 'node:child_process';
const unsafeOutput = execFileSync('sh', ['-c', `ls -la ${req.query.dir}`]);

// GOOD — async execFile, arguments array, no shell, bounded time/output
import { execFile } from 'node:child_process';
import { promisify } from 'node:util';
const pExecFile = promisify(execFile);

const dir = String(req.query.dir ?? '');
if (!dir || dir.startsWith('-')) throw new Error('Invalid directory');
const { stdout: safeOutput } = await pExecFile('ls', ['-la', '--', dir], {
  timeout: 5_000,      // fail fast on hung processes
  maxBuffer: 1 << 20,  // 1 MiB cap to prevent memory exhaustion
});

// BEST — allowlist validation on top of the async, bounded call above
const allowedDirs = ['/data', '/public'];
if (!allowedDirs.includes(dir)) throw new Error('Invalid directory');
```

Prefer async `execFile`/`spawn` over `execFileSync` in server handlers: the sync variant blocks Node's event loop and can amplify DoS impact. Always pass a `timeout` and `maxBuffer` to bound execution.

### I4: XSS via Unsanitized HTML Rendering ,XSS 通过未消毒的 HTML 渲染

- **Severity**: CRITICAL
- **Detection**: `(?:v-html|\[innerHTML\]|dangerouslySetInner|bypassSecurityTrust)`
- **OWASP**: A05

  適用於所有前端框架。每個框架都有一個繞過默認 XSS 保護的 API：

- **React**: `dangerouslySetInnerHTML` prop with raw user content
- **Angular**: `[innerHTML]` binding or `bypassSecurityTrustHtml` with unsanitized input
- **Vue**: `v-html` directive with user-controlled content

```typescript
// GOOD — sanitize with DOMPurify before rendering any raw HTML
import DOMPurify from 'dompurify';
const clean = DOMPurify.sanitize(userContent);

// BEST — use text interpolation when HTML is not needed
// React:   {userContent}
// Angular: {{ userContent }}
// Vue:     {{ userContent }}
```

### I5: SSRF via User-Controlled URLs ,SSRF 通过用户控制的 URL

- **Severity**: CRITICAL
- **Detection**: `fetch\((?:req\.|params\.|query\.|body\.|url|href)`
- **OWASP**: A01

```typescript
// BAD
const data = await fetch(req.body.url);

// GOOD — scheme allowlist + hostname allowlist + DNS/IP validation (see TOCTOU note)
import { promises as dns } from 'node:dns';

function isPrivateIP(ip: string): boolean {
  // Normalize IPv4-mapped IPv6 (e.g., ::ffff:127.0.0.1 → 127.0.0.1)
  const normalized = ip.startsWith('::ffff:') ? ip.slice(7) : ip;
  // IPv4 private/reserved/loopback ranges
  if (/^(10\.|172\.(1[6-9]|2\d|3[01])\.|192\.168\.|127\.|0\.|169\.254\.)/.test(normalized)) return true;
  // IPv6 loopback, link-local (fe80::/10), and unique-local
  if (/^(::1|fe[89ab]|fc|fd)/i.test(normalized)) return true;
  return false;
}

const parsed = new URL(req.body.url);
if (parsed.protocol !== 'https:') throw new Error('Only HTTPS allowed');
const allowedHosts = ['api.example.com', 'cdn.example.com'];
if (!allowedHosts.includes(parsed.hostname)) throw new Error('Host not allowed');
// Resolve all A/AAAA records to prevent DNS rebinding via multiple IPs
const resolved = await dns.lookup(parsed.hostname, { all: true });
if (resolved.length === 0 || resolved.some(({ address }) => isPrivateIP(address))) {
  throw new Error('Private or reserved IPs not allowed');
}
// Note: for production, pin the resolved IP in the HTTP client to prevent
// TOCTOU rebinding between this check and fetch(). See undici Agent docs.
const data = await fetch(parsed.toString(), { redirect: 'error' });
```

### I6: Path Traversal in File Operations ,文件操作中的路径遍歷

- **Severity**: CRITICAL
- **Detection**: `(?:readFile|readFileSync|createReadStream|path\.join)\s*\(.*(?:req\.|params\.|query\.|body\.)`
- **OWASP**: A01

```typescript
// BAD
const file = fs.readFileSync(`/data/${req.params.filename}`);

// GOOD — resolve and validate within allowed directory
import path from 'path';
const basePath = '/data';
const filePath = path.resolve(basePath, req.params.filename);
if (!filePath.startsWith(basePath + path.sep)) throw new Error('Path traversal detected');
const file = fs.readFileSync(filePath);
```

### I7: Template Injection ,模板注入

- **Severity**: CRITICAL
- **Detection**: `(?:render|compile|template)\s*\(.*(?:req\.|params\.|query\.|body\.)`
- **OWASP**: A05

```typescript
// BAD — user input as template source
const html = ejs.render(req.body.template, data);

// GOOD — predefined templates, user input only as data
const html = ejs.renderFile('./templates/page.ejs', { content: req.body.content });
```

### I8: XXE Injection (XML External Entity) ,XXE 注入（XML 外部實體）

- **Severity**: CRITICAL
- **Detection**: `(?:parseXml|DOMParser|xml2js|libxmljs).*(?:req\.|body\.|file)`
- **OWASP**: A05

```typescript
// GOOD — disable external entities in XML parser
import { XMLParser } from 'fast-xml-parser';
const parser = new XMLParser({
  allowBooleanAttributes: true,
  processEntities: false,
  htmlEntities: false,
});
const result = parser.parse(req.body.xml);
```

---

## Authentication Anti-Patterns (AU1-AU8) 認證反模式 (AU1-AU8)

### AU1: JWT Algorithm Confusion (alg:none) JWT 算法混淆 (alg:none)

- **Severity**: CRITICAL
- **Detection**: `jwt\.verify\((?![^)]*\balgorithms\b)[^)]*\)`
- **OWASP**: A07

```typescript
// BAD — accepts any algorithm including "none"
const decoded = jwt.verify(token, secret);

// GOOD — enforce specific algorithm
const decoded = jwt.verify(token, publicKey, { algorithms: ['RS256'] });
```

### AU2: JWT Without Expiration Check JWT 缺少過期檢查

- **Severity**: CRITICAL
- **Detection**: `jwt\.sign\((?![^)]*\b(?:expiresIn|exp)\b)[^)]*\)`
- **OWASP**: A07

```typescript
// BAD — token never expires
const token = jwt.sign({ userId: user.id }, secret);

// GOOD — short-lived token
const token = jwt.sign({ userId: user.id }, secret, { expiresIn: '15m' });
```

### AU3: JWT Stored in localStorage JWT 存儲在 localStorage 中

- **Severity**: IMPORTANT
- **Detection**: `localStorage\.setItem\(.*(?:token|jwt|auth|session)`
- **OWASP**: A07

```typescript
// BAD — accessible via XSS
localStorage.setItem('accessToken', token);

// GOOD — httpOnly cookie set by server
res.cookie('token', token, { httpOnly: true, secure: true, sameSite: 'strict' });
```

### AU4: Plaintext / Fast Hash for Passwords (MD5/SHA-1/SHA-256) 明文 / 快速哈希密碼 (MD5/SHA-1/SHA-256)

- **Severity**: CRITICAL
- **Detection**: `(?:createHash|md5|sha1|sha256)\s*\(.*password`
- **OWASP**: A04

```typescript
// BAD — fast hash, no salt
const sha256Hash = crypto.createHash('sha256').update(password).digest('hex');

// GOOD — Argon2id (OWASP recommended)
import { hash as argon2Hash, argon2id } from 'argon2';
const hashed = await argon2Hash(password, { type: argon2id, memoryCost: 65536, timeCost: 3 });
```

### AU5: Missing Brute-Force Protection on Login 登錄缺少暴力破解保護

- **Severity**: CRITICAL
- **Detection**: `(?:post|router\.post)\s*\(\s*['"]\/(?:login|signin|auth|register|reset)`
- **OWASP**: A07

```typescript
// BAD — no rate limiting
app.post('/api/auth/login', loginHandler);

// GOOD
import rateLimit from 'express-rate-limit';
const authLimiter = rateLimit({ windowMs: 15 * 60 * 1000, max: 5 });
app.post('/api/auth/login', authLimiter, loginHandler);
```

### AU6: Missing Session Regeneration on Login (Session Fixation) 登錄缺少會話再生 (會話固定)

- **Severity**: IMPORTANT
- **Detection**: `(?:session|req\.session)\s*\.\s*(?:userId|user|authenticated)\s*=`
- **OWASP**: A07

```typescript
// GOOD — regenerate session ID on successful login to prevent fixation
req.session.regenerate((err) => {
  if (err) return next(err);
  req.session.userId = user.id;
  req.session.save(next);
});
```

相關：當密碼變更或權限提升時，也應使該使用者的所有其他活動會話失效（例如，透過增加 tokenVersion 列的值並拒絕版本過期的會話，或透過遍歷會話儲存並銷毀與該使用者關聯的條目）。

### AU7: OAuth Without State Parameter OAuth 缺少 state 參數

- **Severity**: CRITICAL
- **Detection**: `authorize\?(?![^\n#]*\bstate=)[^\n#]*`
- **OWASP**: A07

```typescript
// GOOD — include state parameter for CSRF protection
const state = crypto.randomBytes(32).toString('hex');
session.oauthState = state;
const authUrl = `https://provider.com/authorize?client_id=${clientId}&redirect_uri=${redirectUri}&state=${state}`;
```

### AU8: Missing PKCE for Public OAuth Clients

- **Severity**: IMPORTANT
- **Detection**: `(?:authorization_code|code).*(?!.*code_challenge)`
- **OWASP**: A07

對所有公共客戶端（SPA、行動端）使用 PKCE（代碼交換證明金鑰）和 S256 挑戰方法。

---

## Authorization Anti-Patterns (AZ1-AZ6) 授權反模式 (AZ1-AZ6)

### AZ1: Missing Auth Middleware on New Endpoints 新端點缺少身份驗證中間件

- **Severity**: CRITICAL
- **Detection**: `(?:app|router)\.\w+\s*\(\s*['"]\/api\/(?:admin|users|settings)`
- **OWASP**: A01

```typescript
// BAD
router.delete('/api/users/:id', deleteUser);

// GOOD
router.delete('/api/users/:id', authenticate, authorize('admin'), deleteUser);
```

### AZ2: Client-Side Only Authorization 客戶端僅授權

- **Severity**: CRITICAL
- **Detection**: Component guards without server-side checks
- **OWASP**: A01

Frontend guards are UX only. ALWAYS verify on server.

### AZ3: IDOR (Insecure Direct Object Reference), IDOR（不安全的直接对象引用）

- **Severity**: CRITICAL
- **Detection**: `params\.(?:id|userId|orderId)` without ownership check
- **OWASP**: A01

```typescript
// GOOD — verify ownership
router.get('/api/orders/:orderId', authenticate, async (req, res) => {
  const order = await Order.findById(req.params.orderId);
  if (!order || order.userId !== req.user.id) {
    return res.status(404).json({ error: 'Not found' });
  }
  res.json(order);
});
```

### AZ4: Mass Assignment  大量賦值

- **Severity**: CRITICAL
- **Detection**: `(?:create|update|findOneAndUpdate)\s*\(\s*req\.body\s*\)`
- **OWASP**: A01

```typescript
// BAD
await User.findByIdAndUpdate(id, req.body);

// GOOD — explicitly pick allowed fields
const { name, email, avatar } = req.body;
await User.findByIdAndUpdate(id, { name, email, avatar });
```

### AZ5: Privilege Escalation via Role Parameter 角色參數導致的權限提升

- **Severity**: CRITICAL
- **Detection**: `req\.body\.role|req\.body\.isAdmin|req\.body\.permissions`
- **OWASP**: A01

```typescript
// GOOD — ignore role from input
const { name, email, password } = req.body;
const user = await User.create({ name, email, password, role: 'user' });
```

### AZ6: Missing Re-Authentication for Sensitive Operations 敏感操作缺少重新身份驗證

- **Severity**: IMPORTANT
- **Detection**: `(?:delete|destroy|remove).*(?:account|user|organization)` without re-auth
- **OWASP**: A01

刪除帳戶、更改郵箱或其他敏感操作前，必須先輸入目前密碼。

---

## Secrets Anti-Patterns (S1-S6)  秘密反模式 (S1-S6)

### S1: Hardcoded API Keys / Tokens 硬編碼的 API 金鑰/令牌

- **Severity**: CRITICAL
- **Detection**: `(?:password|secret|api_key|token|apiKey)\s*[:=]\s*['"][A-Za-z0-9+/=]{8,}['"]`
- **OWASP**: A04

```typescript
// BAD
const API_KEY = 'sk_live_abc123def456';

// GOOD
const API_KEY = process.env.API_KEY;
```

### S2: .env Committed to Git

- **Severity**: CRITICAL
- **Detection**: `git ls-files .env` (should return empty)
- **OWASP**: A04

```gitignore
# .gitignore
.env
.env.local
.env.*.local
*.pem
*.key
```

### S3: Server Secrets Exposed to Client 伺服器秘密暴露給客戶端

- **Severity**: CRITICAL
- **Detection**: `NEXT_PUBLIC_.*(?:SECRET|PRIVATE|PASSWORD|KEY(?!.*PUBLIC))`
- **OWASP**: A02

```bash
# BAD
NEXT_PUBLIC_DATABASE_URL=postgresql://...

# GOOD
DATABASE_URL=postgresql://...
NEXT_PUBLIC_API_URL=https://api.example.com
```

Angular：不要將金鑰放在打包到客戶端的 `environment.ts` 檔案中。

### S4: Default Credentials in Config 配置中的預設憑證

- **Severity**: CRITICAL
- **Detection**: `(?:admin|root|default|test).*(?:password|pass|pwd)\s*[:=]\s*['"](?:admin|root|password|1234|test)`
- **OWASP**: A02

使用環境變數並進行驗證（zod schema）。

### S5: Secrets in CI/CD Pipeline Logs CI/CD 管道日誌中的秘密

- **Severity**: IMPORTANT
- **Detection**: `(?:echo|console\.log|print).*(?:\$SECRET|\$TOKEN|\$PASSWORD|process\.env)`
- **OWASP**: A09

在持續整合 (CI) 中使用遮罩密鑰。切勿回顯包含密鑰的環境變數。

### S6: Sensitive Data in Error Responses / Stack Traces 錯誤響應/堆棧跟踪中的敏感數據

- **Severity**: IMPORTANT
- **Detection**: `(?:stack|trace|query|sql).*(?:res\.json|res\.send|c\.JSON)`
- **OWASP**: A10

```typescript
// GOOD — generic error to client, details only in logs
app.use((err, req, res, _next) => {
  logger.error({ err, path: req.path, method: req.method });
  const isDev = process.env.NODE_ENV === 'development';
  res.status(500).json({
    error: 'Internal Server Error',
    ...(isDev && { message: err.message }),
  });
});
```

---

## Headers Anti-Patterns (H1-H8) 標頭反模式 (H1-H8)

### H1: Missing Content-Security-Policy 缺少內容安全策略

- **Severity**: IMPORTANT
- **Detection**: Absence of `Content-Security-Policy` header
- **OWASP**: A02

### H2: CSP with unsafe-inline and unsafe-eval ,CSP 包含 unsafe-inline 和 unsafe-eval

- **Severity**: IMPORTANT
- **Detection**: `Content-Security-Policy.*(?:'unsafe-inline'|'unsafe-eval')`
- **OWASP**: A02

使用基於 nonce 的 CSP： script-src 'self' 'nonce-{SERVER_GENERATED}'

### H3: Missing Strict-Transport-Security 缺少嚴格的運輸安全

- **Severity**: IMPORTANT
- **Detection**: Absence of `Strict-Transport-Security` header
- **OWASP**: A02

Value: `max-age=31536000; includeSubDomains; preload`

### H4: Missing X-Content-Type-Options 缺少 X-Content-Type-Options

- **Severity**: IMPORTANT
- **Detection**: Absence of `X-Content-Type-Options: nosniff`
- **OWASP**: A02

### H5: Missing X-Frame-Options 缺少 X-Frame-Options

- **Severity**: IMPORTANT
- **Detection**: Absence of `X-Frame-Options` header
- **OWASP**: A02

值： DENY 。同時設定 Content-Security-Policy: frame-ancestors 'none' 。

### H6: Permissive Referrer-Policy 寬鬆的 Referrer-Policy

- **Severity**: SUGGESTION
- **Detection**: `Referrer-Policy.*(?:unsafe-url|no-referrer-when-downgrade)`
- **OWASP**: A02

使用： `strict-origin-when-cross-origin`

### H7: Missing Permissions-Policy 缺少 Permissions-Policy

- **Severity**: SUGGESTION
- **Detection**: Absence of `Permissions-Policy` header
- **OWASP**: A02

值： `camera=(), microphone=(), geolocation=(), payment=()`

### H8: CORS Wildcard with Credentials ,CORS 通配符與憑證

- **Severity**: CRITICAL
- **Detection**: `(?:cors|Access-Control-Allow-Origin).*\*`
- **OWASP**: A02

```typescript
// GOOD
app.use(cors({
  origin: ['https://app.example.com', 'https://staging.example.com'],
  credentials: true,
}));
```

---

## Frontend Anti-Patterns (FE1-FE8) 前端反模式 (FE1-FE8)

### FE1: Unsanitized HTML Rendering 未經消毒的 HTML 渲染

- **Severity**: CRITICAL
- **Detection**: `(?:innerHTML|v-html|dangerouslySetInner)` without DOMPurify
- **OWASP**: A05

在渲染使用者可控的 HTML 之前，請務必使用 DOMPurify 進行清潔。參見 I4。

### FE2: Dynamic Code Evaluation with User Input 使用用戶輸入的動態代碼評估

- **Severity**: CRITICAL
- **Detection**: `eval\s*\(`
- **OWASP**: A05

使用結構化數據解析器（如 JSON.parse）代替 eval。

### FE3: postMessage Without Origin Validation ,postMessage 沒有來源驗證

- **Severity**: IMPORTANT
- **Detection**: `addEventListener\s*\(\s*['"]message['"].*(?!.*origin)`
- **OWASP**: A01

```typescript
window.addEventListener('message', (event) => {
  if (event.origin !== 'https://trusted.example.com') return;
  processData(event.data);
});
```

### FE4: Prototype Pollution 原型污染

- **Severity**: IMPORTANT
- **Detection**: `(?:__proto__|constructor\.prototype|Object\.assign)\s*.*(?:req\.|body\.|query\.)`
- **OWASP**: A05

在將使用者輸入的鍵合併到物件之前，對其進行驗證和篩選。

### FE5: Open Redirect 開放重定向

- **Severity**: IMPORTANT
- **Detection**: `(?:window\.location|location\.href|router\.push)\s*=\s*(?:req\.|params\.|query\.)`
- **OWASP**: A01

```typescript
// GOOD — relative paths only
const redirect = new URLSearchParams(window.location.search).get('redirect');
if (redirect?.startsWith('/') && !redirect.startsWith('//')) {
  window.location.href = redirect;
}
```

### FE6: Sensitive Data in localStorage ,本地儲存中的敏感數據

- **Severity**: IMPORTANT
- **Detection**: `localStorage\.setItem\(.*(?:token|session|credit|ssn|password)`
- **OWASP**: A07

Use httpOnly cookies for tokens.

### FE7: Missing CSRF Token 缺少 CSRF 令牌

- **Severity**: IMPORTANT
- **Detection**: POST/PUT/DELETE forms without CSRF token or SameSite cookie
- **OWASP**: A01

使用雙重提交 cookie 或同步令牌。Next.js Server Actions 內建 CSRF 驗證 via Origin header。

### FE8: Client-Only Input Validation 僅客戶端輸入驗證

- **Severity**: IMPORTANT
- **Detection**: Form validation only in frontend
- **OWASP**: A05

始終在伺服器端進行驗證。使用 zod、joi 或 class-validator。

---

## Dependencies Anti-Patterns (D1-D5) 依賴反模式 (D1-D5)

### D1: Known Vulnerable Dependency 已知存在漏洞的依賴

- **Severity**: CRITICAL
- **Detection**: `npm audit --audit-level=high` exits non-zero
- **OWASP**: A03

### D2: Lockfile Out of Sync 鎖定檔不同步

- **Severity**: IMPORTANT
- **Detection**: `npm ci` fails
- **OWASP**: A08

### D3: Typosquatting Risk 域名搶注風險

- **Severity**: IMPORTANT
- **Detection**: Manual review of new dependency names
- **OWASP**: A03

### D4: Postinstall Scripts in New Dependency 新依賴中的 Postinstall 腳本

- **Severity**: IMPORTANT
- **Detection**: `"postinstall"` in new dependency's package.json
- **OWASP**: A03

### D5: Unpinned Versions in Production 未固定版本的生產環境依賴

- **Severity**: SUGGESTION
- **Detection**: `":\s*["']\*["']|":\s*["']latest["']`
- **OWASP**: A03

---

## API Anti-Patterns (AP1-AP6) API 反模式 (AP1-AP6)

### AP1: New Endpoint Without Rate Limiting 新端點缺少速率限制

- **Severity**: IMPORTANT
- **OWASP**: A05

### AP2: GraphQL Without Depth Limiting ,GraphQL 缺少深度限制

- **Severity**: IMPORTANT
- **Detection**: `new ApolloServer` without depth/complexity limits
- **OWASP**: A05

```typescript
import depthLimit from 'graphql-depth-limit';
const server = new ApolloServer({
  schema,
  validationRules: [depthLimit(5)],
  introspection: process.env.NODE_ENV !== 'production',
});
```

### AP3: File Upload Without Validation 無需驗證即可上傳文件

- **Severity**: IMPORTANT
- **Detection**: `multer|formidable|busboy` without type/size checks
- **OWASP**: A05

```typescript
const upload = multer({
  dest: 'uploads/',
  limits: { fileSize: 5 * 1024 * 1024 },
  fileFilter: (req, file, cb) => {
    const allowed = ['image/jpeg', 'image/png', 'image/webp'];
    cb(null, allowed.includes(file.mimetype));
  },
});
```

### AP4: Webhook Without Signature Verification 新端點缺少簽名驗證

- **Severity**: CRITICAL
- **OWASP**: A08

請務必驗證 webhook 簽章（Stripe、GitHub HMAC 等）。

### AP5: API Exposing Internal Info ,API 暴露內部信息

- **Severity**: IMPORTANT
- **Detection**: `(?:stack|trace|query|sql).*(?:res\.json|res\.send)`
- **OWASP**: A10

### AP6: Missing Request Body Size Limit 缺少請求正文大小限制

- **Severity**: IMPORTANT
- **Detection**: `express\.json\(\)` without `limit`
- **OWASP**: A05

```typescript
app.use(express.json({ limit: '100kb' }));
```

---

## AI/LLM Security Anti-Patterns (AI1-AI3) AI/LLM 安全反模式 (AI1-AI3)

### AI1: Prompt Injection via User Input 用戶輸入的提示注入

- **Severity**: CRITICAL
- **Detection**: User input concatenated into LLM prompts without sanitization
- **OWASP**: A05 (Injection)

```typescript
// BAD — user input directly in prompt
const response = await llm.complete(`Summarize this: ${userInput}`);

// GOOD — structured input with system/user message separation
const response = await llm.complete({
  system: "You are a summarization assistant. Only summarize the provided text.",
  user: userInput,
});
```

### AI2: LLM Output Used in SQL/Shell Without Sanitization LLM 輸出在 SQL/Shell 中未經過消毒

- **Severity**: CRITICAL
- **Detection**: LLM response passed to `db.query()`, `exec()`, or template literals without validation
- **OWASP**: A05 (Injection)

N永遠不要將 LLM 的輸出視為安全資料。應將其視為不可信的使用者輸入——對查詢進行參數化，轉義 shell 參數，並在渲染前清理 HTML。

### AI3: Missing Output Validation from LLM Responses LLM 回應缺少輸出驗證

- **Severity**: IMPORTANT
- **Detection**: LLM response rendered or executed without schema validation
- **OWASP**: A08 (Software or Data Integrity Failures)

在應用程式邏輯中使用 LLM 輸出之前，請根據預期模式（Zod、JSON Schema）驗證 LLM 輸出。拒絕不符合預期結構的回應。
---

## Logging Anti-Patterns (L1-L4) 日誌反模式 (L1-L4)

### L1: Security Events Not Logged 安全事件未記錄

- **Severity**: IMPORTANT
- **OWASP**: A09

日誌：身份驗證失敗、存取被拒絕、達到速率限制、輸入驗證失敗、密碼變更。

### L2: Sensitive Data in Logs 日誌中的敏感數據

- **Severity**: CRITICAL
- **Detection**: `(?:log|logger)\.\w+\(.*(?:password|token|secret|ssn|credit)`
- **OWASP**: A09

```typescript
import pino from 'pino';
const logger = pino({ redact: ['req.headers.authorization', 'req.body.password'] });
```

### L3: Missing Trace IDs 缺少跟踪 ID

- **Severity**: SUGGESTION
- **OWASP**: A09

### L4: Log Injection 日誌注入

- **Severity**: IMPORTANT
- **Detection**: `console\.log\(.*\+.*(?:req\.|user\.|body\.)`
- **OWASP**: A09

使用結構化日誌（JSON，自動轉義）而不是字串拼接。
---

## Framework-Specific: React / Next.js (RX1-RX4) 特定於框架：React / Next.js (RX1-RX4)

### RX1: Server Action Without Auth 伺服器操作缺少身份驗證

- **Severity**: CRITICAL
- **Detection**: `'use server'` function without `auth()` or session check
- **OWASP**: A01

```typescript
'use server';
import { auth } from '@/auth';
export async function deleteUser(id: string) {
  const session = await auth();
  if (!session?.user || session.user.role !== 'admin') throw new Error('Unauthorized');
  await db.user.delete({ where: { id } });
}
```

### RX2: process.env Without NEXT_PUBLIC_ in Client , Components 客戶端組件中使用 process.env 而不使用 NEXT_PUBLIC_

- **Severity**: IMPORTANT
- **Detection**: `'use client'` file accessing `process.env` without `NEXT_PUBLIC_`
- **OWASP**: A02

### RX3: RSC Serialization Leaking Data ,RSC 序列化泄漏数据

- **Severity**: IMPORTANT
- **OWASP**: A01

在將 DB 對象傳遞給客戶端組件之前，只選擇所需的字段。

### RX4: middleware.ts Not Protecting API Routes ,middleware.ts 未保護 API 路由

- **Severity**: IMPORTANT
- **Detection**: `config.matcher` not covering `/api/`
- **OWASP**: A01

---

## Framework-Specific: Angular (NG1-NG3) 特定於框架：Angular (NG1-NG3)

### NG1: bypassSecurityTrustHtml with User Input 使用者輸入的 bypassSecurityTrustHtml

- **Severity**: CRITICAL
- **Detection**: `bypassSecurityTrust(?:Html|Script|Style|Url|ResourceUrl)`
- **OWASP**: A05

在調用 bypassSecurityTrust 之前，請使用 DOMPurify 進行消毒。

### NG2: Template Expression Injection 模板表達式注入

- **Severity**: IMPORTANT
- **OWASP**: A05

不要使用 JitCompilerFactory 與使用者控制的模板。

### NG3: HttpInterceptor Not Attaching Auth , HttpInterceptor 未附加身份驗證令牌

- **Severity**: IMPORTANT
- **OWASP**: A07

使用集中式 `HttpInterceptorFn` 來處理身份驗證令牌。

---

## Framework-Specific: Express (EX1-EX4) 特定於框架：Express (EX1-EX4)

### EX1: Missing helmet.js ,Security Headers 缺少 helmet.js 安全標頭

- **Severity**: IMPORTANT
- **OWASP**: A02

```typescript
import helmet from 'helmet';
app.use(helmet());
app.disable('x-powered-by');
```

### EX2: express.json() Without Body Size Limit ,express.json() 沒有設置請求體大小限制

- **Severity**: IMPORTANT
- **OWASP**: A05

```typescript
app.use(express.json({ limit: '100kb' }));
```

### EX3: Cookie Without Secure Flags

- **Severity**: IMPORTANT
- **OWASP**: A07

```typescript
res.cookie('session', value, {
  httpOnly: true, secure: true, sameSite: 'strict', maxAge: 3600000, path: '/',
});
```

### EX4: Error Handler Exposing Stack Trace 錯誤處理程序公開堆疊追蹤

- **Severity**: IMPORTANT
- **OWASP**: A10

僅在開發模式下顯示錯誤詳情。

---

## Framework-Specific: Go (GO1-GO3) 特定於框架：Go (GO1-GO3)

### GO1: math/rand for Security Operations 使用 math/rand 進行安全操作

- **Severity**: CRITICAL
- **Detection**: `math/rand` import in security-related files
- **OWASP**: A04

使用 crypto/rand 取得加密安全的隨機值。

### GO2: TLS InsecureSkipVerify 

- **Severity**: CRITICAL
- **Detection**: `InsecureSkipVerify:\s*true`
- **OWASP**: A04

使用系統 CA 池（默認）代替。

### GO3: String Interpolation in SQL 字符串插值在 SQL 中

- **Severity**: CRITICAL
- **Detection**: `fmt\.Sprintf\s*\(.*(?:SELECT|INSERT|UPDATE|DELETE|FROM|WHERE)`
- **OWASP**: A05

```go
// GOOD — parameterized
db.Where("id = ?", userID).Find(&user)
```

---

## Security Headers Template 安全標頭模板

### helmet.js (Express)

```typescript
import helmet from 'helmet';

app.use(helmet({
  contentSecurityPolicy: {
    directives: {
      defaultSrc: ["'self'"],
      scriptSrc: ["'self'"],
      styleSrc: ["'self'"],
      imgSrc: ["'self'", "data:", "https:"],
      fontSrc: ["'self'"],
      connectSrc: ["'self'"],
      frameAncestors: ["'none'"],
      objectSrc: ["'none'"],
      baseUri: ["'self'"],
      formAction: ["'self'"],
      upgradeInsecureRequests: [],
    },
  },
  hsts: { maxAge: 31536000, includeSubDomains: true, preload: true },
  frameguard: { action: 'deny' },
  referrerPolicy: { policy: 'strict-origin-when-cross-origin' },
  crossOriginOpenerPolicy: { policy: 'same-origin' },
  crossOriginResourcePolicy: { policy: 'same-origin' },
}));
app.disable('x-powered-by');
```

---

## JWT Validation Checklist ,JWT 驗證檢查清單

1. 使用預期演算法驗證簽章－拒絕 `alg: none`
2. 強制演算法：`algorithms: ['RS256']` 或 `['ES256']`
3. 檢查 `exp` — 拒絕過期的令牌
4. 檢查 `iat` — 拒絕發行過久的令牌
5. 檢查 `aud` — 拒絕不針對此服務的令牌
6. 檢查 `iss` — 拒絕來自未知發行者的令牌
7. 將令牌存儲在 httpOnly cookie 中 — 不使用 localStorage
8. 使用短期存取令牌（15 分鐘）+ 刷新令牌輪換
9. 定期輪換簽名密鑰

---

## Secure Cookie Flags 安全 Cookie 標誌

```
Set-Cookie: session=value; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=3600
```

| Flag | Purpose | When to use |
|------|---------|-------------|
| `HttpOnly` | 無法透過 JavaScript 存取（防止 XSS 令牌竊取） | Always |
| `Secure` | 僅通過 HTTPS 發送 | Always |
| `SameSite=Strict` | 僅在同站請求中發送（最強的 CSRF 防護） | Auth/session cookies |
| `SameSite=Lax` | 在頂層導航中發送（中等 CSRF 防護） | 需要跨站頂層導航的 Cookie（例如 OAuth 返回） |
| `Path=/` | 限制 Cookie 範圍 | Always |
| `Max-Age` | 明確的過期時間（優先於 `Expires`） | Always |

---

## Security Checklist 安全檢查清單

### Authentication and Sessions 認證與會話
- [ ] 密碼使用 Argon2id 或 bcrypt 雜湊（成本 >= 12）
- [ ] JWT 使用 RS256/ES256 簽名，驗證時強制演算法
- [ ] 存取令牌過期時間 <= 15 分鐘
- [ ] 刷新令牌：一次性使用，輪換，存儲在 httpOnly cookie 中
- [ ] 登錄、註冊和密碼重置的速率限制
- [ ] 認證後重新生成會話
- [ ] 特權帳戶可用多因素認證（MFA）

### Authorization 授權
- [ ] 每個 API 端點都有授權中間件
- [ ] 所有資源訪問都有所有權檢查（防止 IDOR）
- [ ] 伺服器端授權（前端守衛僅為 UX）
- [ ] 防止大規模分配（明確選擇字段）
- [ ] 敏感操作需要重新認證

### Input and Output 輸入與輸出
- [ ] 所有用戶輸入在伺服器端驗證（zod/joi/class-validator）
- [ ] 所有數據庫操作使用參數化查詢
- [ ] 渲染用戶內容時對 HTML 輸出進行清理（DOMPurify）
- [ ] 錯誤響應在生產環境中不暴露堆棧追蹤

### Secrets 秘密
- [ ] 不在源代碼中硬編碼秘密
- [ ] `.env` 文件在 `.gitignore` 中
- [ ] 伺服器秘密不暴露給客戶端（秘密上不使用 NEXT_PUBLIC_）
- [ ] 啟動時驗證環境變量

### Headers 標頭
- [ ] 配置 Content-Security-Policy（優先使用 nonce-based） 
- [ ] Strict-Transport-Security 與 preload
- [ ] X-Content-Type-Options: nosniff
- [ ] X-Frame-Options: DENY
- [ ] Referrer-Policy: strict-origin-when-cross-origin
- [ ] Permissions-Policy restricting unused APIs
- [ ] CORS restricted to known origins

### Dependencies 依賴
- [ ] `npm audit` （或等效方法）在 CI 中透過過濾已知漏洞
- [ ] Lockfile 提交並使用 `npm ci` 驗證
- [ ] 新依賴審查以防止 typosquatting 和 postinstall 腳本
- [ ] 生產環境中不使用通配符或 "latest" 版本

### Logging 日誌
- [ ] 安全事件日誌（認證失敗、訪問被拒、速率限制）
- [ ] 日誌中不包含敏感數據（密碼、令牌、個人識別信息）
- [ ] 使用結構化日誌並包含關聯 ID
- [ ] 配置異常模式的警報
