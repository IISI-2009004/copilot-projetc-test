---
applyTo: ["*"]
description: "自 Java 17 發布以來，採用 Java 21 新功能的全面最佳實務。"
---

# Java 17 to Java 21 Upgrade Guide

這些說明可協助 GitHub Copilot 協助開發人員將 Java 專案從 JDK 17 升級到 JDK 21，並專注於新的語言特性、API 變更和最佳實務。

## JDK 18-21 的主要語言特性

### Pattern Matching for switch 交換器模式匹配 (JEP 441 - Standard in 21)

**增強的 switch 表達式和語句**

在處理 switch 結構時：

- 建議在適當的情況下將傳統 switch 轉換為模式匹配
- 使用模式匹配進行類型檢查和解構
- 升級範例模式：

```java
// 舊方法 (Java 17)
public String processObject(Object obj) {
    if (obj instanceof String) {
        String s = (String) obj;
        return s.toUpperCase();
    } else if (obj instanceof Integer) {
        Integer i = (Integer) obj;
        return i.toString();
    }
    return "unknown";
}

// 新方法 (Java 21)
public String processObject(Object obj) {
    return switch (obj) {
        case String s -> s.toUpperCase();
        case Integer i -> i.toString();
        case null -> "null";
        default -> "unknown";
    };
}
```

- 支援受保護的模式：

```java
switch (obj) {
    case String s when s.length() > 10 -> "Long string: " + s;
    case String s -> "Short string: " + s;
    case Integer i when i > 100 -> "Large number: " + i;
    case Integer i -> "Small number: " + i;
    default -> "Other";
}
```

### Record Patterns 記錄模式 (JEP 440 - Standard in 21)

**在模式匹配中解構記錄**

處理記錄時：

- 建議使用記錄模式進行資料解構
- 結合 switch 表達式實現強大的資料處理功能
- 用法範例：

```java
public record Point(int x, int y) {}
public record ColoredPoint(Point point, Color color) {}

// Destructuring in switch
public String describe(Object obj) {
    return switch (obj) {
        case Point(var x, var y) -> "Point at (" + x + ", " + y + ")";
        case ColoredPoint(Point(var x, var y), var color) ->
            "Colored point at (" + x + ", " + y + ") in " + color;
        default -> "Unknown shape";
    };
}
```

- 在複雜的模式匹配中使用：

```java
// Nested record patterns
switch (shape) {
    case Rectangle(ColoredPoint(Point(var x1, var y1), var c1),
                   ColoredPoint(Point(var x2, var y2), var c2))
        when c1 == c2 -> "Monochrome rectangle";
    case Rectangle r -> "Multi-colored rectangle";
}
```

### Virtual Threads 虛擬線程 (JEP 444 - Standard in 21)

**輕量級並行**

在處理並行性時：

- 建議使用虛擬線程以實現高吞吐量的並行應用
- 使用 `Thread.ofVirtual()` 創建虛擬線程
- 範例遷移模式：

```java
// 舊的平臺線程方法
ExecutorService executor = Executors.newFixedThreadPool(100);
executor.submit(() -> {
    // blocking I/O operation
    httpClient.send(request);
});

// 新的虛擬線程方法
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        // blocking I/O operation - now scales to millions
        httpClient.send(request);
    });
}
```

- 使用結構化並行模式：

```java
// Structured concurrency (Preview)
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Future<String> user = scope.fork(() -> fetchUser(userId));
    Future<String> order = scope.fork(() -> fetchOrder(orderId));

    scope.join();           // Join all subtasks
    scope.throwIfFailed();  // Propagate errors

    return processResults(user.resultNow(), order.resultNow());
}
```

### String Templates 字串模板 (JEP 430 - Preview in 21)

**安全字串插值**

在處理字串格式化時：

- 建議使用字串模板進行安全的字串插值（預覽功能）
- 使用 `--enable-preview` 啟用預覽功能
- 範例用法：

```java
// 傳統的字串串接
String message = "Hello, " + name + "! You have " + count + " messages.";

// String Templates (Preview)
String message = STR."Hello, \{name}! You have \{count} messages.";

// Safe HTML generation
String html = HTML."<p>User: \{username}</p>";

// Safe SQL queries
PreparedStatement stmt = SQL."SELECT * FROM users WHERE id = \{userId}";
```

### Sequenced Collections 序列集合 (JEP 431 - Standard in 21)

**增強型收藏介面**

在處理集合時：

- 使用新的 `SequencedCollection`、`SequencedSet`、`SequencedMap` 介面
- 統一訪問集合類型的首尾元素
- 範例用法：

```java
// 新方法可用於 Lists、Deques、LinkedHashSet 等
List<String> list = List.of("first", "middle", "last");
String first = list.getFirst();  // "first"
String last = list.getLast();    // "last"
List<String> reversed = list.reversed(); // ["last", "middle", "first"]

// 適用於任何序列集合
SequencedSet<String> set = new LinkedHashSet<>();
set.addFirst("start");
set.addLast("end");
String firstElement = set.getFirst();
```

### Unnamed Patterns and Variables 未命名模式和變數 (JEP 443 - Preview in 21)

**簡化模式匹配**

在處理模式匹配時：

- 使用未命名模式 `_` 來忽略不需要的值
- 簡化 switch 表達式和記錄模式
- 範例用法：

```java
// 忽略未使用的變數
switch (ball) {
    case RedBall(_) -> "Red ball";     // Don't care about size
    case BlueBall(var size) -> "Blue ball size " + size;
}

// 忽略部分records
switch (point) {
    case Point(var x, _) -> "X coordinate: " + x; // Ignore Y
    case ColoredPoint(Point(_, var y), _) -> "Y coordinate: " + y;
}

// 使用未命名變數處理異常
try {
    riskyOperation();
} catch (IOException | SQLException _) {
    // 不需要異常細節
    handleError();
}
```

### Scoped Values 作用域值 (JEP 446 - Preview in 21)

**改進的上下文傳播**

在處理線程本地數據時：

- 考慮將 Scoped Values 作為 ThreadLocal 的現代替代方案
- 提供更好的性能和更清晰的語義，特別是對虛擬線程
- 範例用法：

```java
// 定義作用域值
private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

// 設置並使用作用域值
ScopedValue.where(USER_ID, "user123")
    .run(() -> {
        processRequest(); // 可以在調用鏈的任何地方訪問 USER_ID.get()
    });

// In nested method
public void processRequest() {
    String userId = USER_ID.get(); // "user123"
    // 使用用戶上下文進行處理
}
```

## API 增強和新功能

### UTF-8 by Default (JEP 400 - Standard in 18)

處理文件 I/O 時：

- UTF-8 現在是所有平台的默認字符集
- 移除明確指定 UTF-8 的地方
- 範例簡化：

```java
// 舊的明確 UTF-8 指定
Files.readString(path, StandardCharsets.UTF_8);
Files.writeString(path, content, StandardCharsets.UTF_8);

// New default behavior (Java 18+)
Files.readString(path);  // Uses UTF-8 by default
Files.writeString(path, content);  // Uses UTF-8 by default
```

### Simple Web Server 簡易 Web 伺服器 (JEP 408 - Standard in 18)

當需要基本的 HTTP 伺服器時：

- 使用內建的 `jwebserver` 命令或 `com.sun.net.httpserver` 增強功能
- 適合測試和開發
- 範例用法：

```java
// 命令行
$ jwebserver -p 8080 -d /path/to/files

// 程式化使用
HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
server.createContext("/", new SimpleFileHandler(Path.of("/tmp")));
server.start();
```

### Internet-Address Resolution SPI 網際網路位址解析 SPI (JEP 418 - Standard in 19)

當需要自訂 DNS 解析時：

- 實作 `InetAddressResolverProvider` 以進行自訂位址解析
- 適用於服務發現和測試場景

### Key Encapsulation Mechanism API 密鑰封裝機制 API (JEP 452 - Standard in 21)

當使用後量子密碼學時：

- 使用 KEM API 進行密鑰封裝機制
- 範例用法：

```java
KeyPairGenerator kpg = KeyPairGenerator.getInstance("ML-KEM");
KeyPair kp = kpg.generateKeyPair();

KEM kem = KEM.getInstance("ML-KEM");
KEM.Encapsulator encapsulator = kem.newEncapsulator(kp.getPublic());
KEM.Encapsulated encapsulated = encapsulator.encapsulate();
```

## 棄用和警告

### Finalization Deprecation 最終化棄用 (JEP 421 - Deprecated in 18)

當遇到 `finalize()` 方法時：

- 移除 finalize 方法並使用替代方案
- 建議使用 Cleaner API 或 try-with-resources
- 範例遷移：

```java
// Deprecated finalize approach
@Override
protected void finalize() throws Throwable {
    cleanup();
}

// Modern approach with Cleaner
private static final Cleaner CLEANER = Cleaner.create();

public MyResource() {
    cleaner.register(this, new CleanupTask(nativeResource));
}

private static class CleanupTask implements Runnable {
    private final long nativeResource;

    CleanupTask(long nativeResource) {
        this.nativeResource = nativeResource;
    }

    public void run() {
        cleanup(nativeResource);
    }
}
```

### Dynamic Agent Loading 動態代理加載 (JEP 451 - Warnings in 21)

使用代理或檢測工具時：

- 如有需要，請添加 `-XX:+EnableDynamicAgentLoading` 以抑制警告
- 考慮在啟動時加載代理，而不是動態加載
- 更新工具以使用啟動時載入代理

## 建置配置更新

### 預覽功能

對於使用預覽功能的專案：

- 在編譯器和運行時添加 `--enable-preview`
- Maven 配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <release>21</release>
        <compilerArgs>
            <arg>--enable-preview</arg>
        </compilerArgs>
    </configuration>
</plugin>

<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>--enable-preview</argLine>
    </configuration>
</plugin>
```

- Gradle configuration:

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}
```

### 虛擬執行緒配置

對於使用虛擬執行緒的應用程式：

- 無需特殊的 JVM 標誌（21 中的標準功能）
- 考慮這些系統屬性以進行調試：

```bash
-Djdk.virtualThreadScheduler.parallelism=N  # 設置承載執行緒數量
-Djdk.virtualThreadScheduler.maxPoolSize=N  # 設置最大池大小
```

## 運行時和垃圾回收改進

### Generational ZGC 世代 ZGC (JEP 439 - Available in 21)

配置垃圾回收時：

- 嘗試使用 Generational ZGC 以獲得更好的性能
- 啟用方式：`-XX:+UseZGC -XX:+ZGenerational`
- 監控分配模式和 GC 行為

## 遷移策略

### 分步升級過程

1. **更新建置工具**: 確保 Maven/Gradle 支援 JDK 21
2. **語言功能採用**:
   - 從 switch 的模式匹配開始（標準功能）
   - 在有利的情況下添加 record patterns
   - 對於 I/O 密集型應用程式，考慮使用虛擬執行緒
3. **預覽功能**: 僅在特定使用情況下啟用
4. **測試**: 對並發性變更進行全面測試
5. **性能**: 使用新的 GC 選項進行基準測試

### 代碼審查清單

在審查 Java 21 升級的代碼時：

- [ ] 將適當的 instanceof 鏈轉換為 switch 表達式
- [ ] 使用 record patterns 進行數據解構
- [ ] 在適當的情況下將 ThreadLocal 替換為 ScopedValues
- [ ] 考慮在高並發場景中使用虛擬執行緒
- [ ] 移除顯式的 UTF-8 字符集規範
- [ ] 將 finalize() 方法替換為 Cleaner 或 try-with-resources
- [ ] 使用 SequencedCollection 方法進行首/尾元素訪問
- [ ] 僅為使用中的預覽功能添加預覽標誌

### 常見遷移模式

1. **Switch 增強**:

   ```java
   // 從 instanceof 鏈轉換為 switch 表達式
   if (obj instanceof String s) return processString(s);
   else if (obj instanceof Integer i) return processInt(i);
   // 變為:
   return switch (obj) {
       case String s -> processString(s);
       case Integer i -> processInt(i);
       default -> processDefault(obj);
   };
   ```

2. **虛擬執行緒採用**:

   ```java
   // 從平台執行緒轉換為虛擬執行緒
   Executors.newFixedThreadPool(200)
   // 變為:
   Executors.newVirtualThreadPerTaskExecutor()
   ```

3. **Record Pattern Usage**:
   ```java
   // 從手動解構轉換為 record patterns
   if (point instanceof Point p) {
       int x = p.x();
       int y = p.y();
   }
   // 變為:
   if (point instanceof Point(var x, var y)) {
       // 直接使用 x 和 y
   }
   ```

## 性能考量

- 虛擬執行緒在阻塞 I/O 情況下表現優異，但對 CPU 密集型任務可能無益
- 世代 ZGC 可以減少大多數應用程式的 GC 開銷
- switch 中的模式匹配通常比 instanceof 鏈更高效
- SequencedCollection 方法提供對首/尾元素的 O(1) 訪問
- Scoped Values 在虛擬執行緒中比 ThreadLocal 開銷更低

## 測試建議

- 在高並發情況下測試虛擬執行緒應用程式
- 驗證模式匹配涵蓋所有預期情況
- 使用世代 ZGC 與其他收集器進行性能測試
- 驗證不同平台上的 UTF-8 默認行為
- 在生產環境使用前徹底測試預覽功能

請僅在特定需要時啟用預覽功能，並在部署到生產環境之前在預備環境中徹底測試。
