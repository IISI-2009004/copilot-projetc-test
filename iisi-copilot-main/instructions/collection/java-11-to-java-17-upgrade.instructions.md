---
applyTo: ["*"]
description: "自 Java 11 發布以來，採用 Java 17 新特性的全面最佳實務。"
---

# Java 11 to Java 17 Upgrade Guide

## Project Context

本指南提供了全面的 GitHub Copilot 說明，用於將 Java 專案從 JDK 11 升級到 JDK 17，涵蓋了主要語言特性、API 變更以及基於這些版本之間整合的 47 個 JEP 的遷移模式。

## 語言特性與 API 變更

### JEP 395: Records 記錄 (Java 16)

**遷移模式**: 將資料類別轉換為記錄

```java
// 舊版：傳統資料類別
public class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name() { return name; }
    public int age() { return age; }

    @Override
    public boolean equals(Object obj) { /* boilerplate */ }
    @Override
    public int hashCode() { /* boilerplate */ }
    @Override
    public String toString() { /* boilerplate */ }
}

// 新版：Record (Java 16+)
public record Person(String name, int age) {
    // 用於驗證的緊湊型構造函數
    public Person {
        if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
    }

    // 可以添加自定義方法
    public boolean isAdult() {
        return age >= 18;
    }
}
```

### JEP 409: Sealed Classes 密封類別 (Java 17)

**遷移模式**: 使用密封類別實作受限繼承

```java
// 新版：密封類別層次結構
public sealed class Shape
    permits Circle, Rectangle, Triangle {

    public abstract double area();
}

public final class Circle extends Shape {
    private final double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

public final class Rectangle extends Shape {
    private final double width, height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double area() {
        return width * height;
    }
}

public non-sealed class Triangle extends Shape {
    // 非密封狀態允許進一步繼承
    private final double base, height;

    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }

    @Override
    public double area() {
        return 0.5 * base * height;
    }
}
```

### JEP 394: Pattern Matching for instanceof (instanceof 的模式匹配模式) (Java 16)

**遷移模式**: 簡化 instanceof 檢查

```java
// 舊版：傳統 instanceof 與類型轉換
public String processObject(Object obj) {
    if (obj instanceof String) {
        String str = (String) obj;
        return str.toUpperCase();
    } else if (obj instanceof Integer) {
        Integer num = (Integer) obj;
        return "Number: " + num;
    } else if (obj instanceof List<?>) {
        List<?> list = (List<?>) obj;
        return "List with " + list.size() + " elements";
    }
    return "Unknown type";
}

// 新版：實例的模式匹配 (Java 16+)
public String processObject(Object obj) {
    if (obj instanceof String str) {
        return str.toUpperCase();
    } else if (obj instanceof Integer num) {
        return "Number: " + num;
    } else if (obj instanceof List<?> list) {
        return "List with " + list.size() + " elements";
    }
    return "Unknown type";
}

// 與密封類別搭配效果極佳
public String describeShape(Shape shape) {
    if (shape instanceof Circle circle) {
        return "Circle with radius " + circle.radius();
    } else if (shape instanceof Rectangle rect) {
        return "Rectangle " + rect.width() + "x" + rect.height();
    } else if (shape instanceof Triangle triangle) {
        return "Triangle with base " + triangle.base();
    }
    return "Unknown shape";
}
```

### JEP 361: Switch Expressions (Switch 表達式) (Java 14)

**遷移模式**: 將 switch 語句轉換為表達式

```java
// 舊版：傳統 switch 語句
public String getDayType(DayOfWeek day) {
    String result;
    switch (day) {
        case MONDAY:
        case TUESDAY:
        case WEDNESDAY:
        case THURSDAY:
        case FRIDAY:
            result = "Workday";
            break;
        case SATURDAY:
        case SUNDAY:
            result = "Weekend";
            break;
        default:
            throw new IllegalArgumentException("Unknown day: " + day);
    }
    return result;
}

// 新版：Switch 表達式 (Java 14+)
public String getDayType(DayOfWeek day) {
    return switch (day) {
        case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Workday";
        case SATURDAY, SUNDAY -> "Weekend";
    };
}

// 使用 yield 處理複雜邏輯
public int calculateScore(Grade grade) {
    return switch (grade) {
        case A -> 100;
        case B -> 85;
        case C -> 70;
        case D -> {
            System.out.println("Consider improvement");
            yield 55;
        }
        case F -> {
            System.out.println("Needs retake");
            yield 0;
        }
    };
}
```

### JEP 406: Pattern Matching for switch (switch 語句的模式匹配) (Preview in Java 17)

**遷移模式**: 增強型模式切換（預覽功能）

```java
// 需要啟用預覽標誌
public String formatValue(Object obj) {
    return switch (obj) {
        case String s -> "String: " + s;
        case Integer i -> "Integer: " + i;
        case null -> "null value";
        case default -> "Unknown: " + obj.getClass().getSimpleName();
    };
}

// 附戒備模式
public String categorizeNumber(Object obj) {
    return switch (obj) {
        case Integer i when i < 0 -> "Negative integer";
        case Integer i when i == 0 -> "Zero";
        case Integer i when i > 0 -> "Positive integer";
        case Double d when d.isNaN() -> "Not a number";
        case Number n -> "Other number: " + n;
        case null -> "null";
        case default -> "Not a number";
    };
}
```

### JEP 378: Text Blocks 文字區塊 (Java 15)

**遷移模式**: 使用文字區塊處理多行字串

```java
// 舊版：字串連接
String html = "<html>\n" +
              "  <body>\n" +
              "    <h1>Hello World</h1>\n" +
              "    <p>Welcome to Java 17!</p>\n" +
              "  </body>\n" +
              "</html>";

String sql = "SELECT p.id, p.name, p.email, " +
             "       a.street, a.city, a.state " +
             "FROM person p " +
             "JOIN address a ON p.address_id = a.id " +
             "WHERE p.active = true " +
             "ORDER BY p.name";

// 新版：文字區塊 (Java 15+)
String html = """
              <html>
                <body>
                  <h1>Hello World</h1>
                  <p>Welcome to Java 17!</p>
                </body>
              </html>
              """;

String sql = """
             SELECT p.id, p.name, p.email,
                    a.street, a.city, a.state
             FROM person p
             JOIN address a ON p.address_id = a.id
             WHERE p.active = true
             ORDER BY p.name
             """;

// 使用字串插值方法
String json = """
              {
                "name": "%s",
                "age": %d,
                "city": "%s"
              }
              """.formatted(name, age, city);
```

### JEP 358: Helpful NullPointerExceptions 有用的空指標異常 (Java 14)

**遷移模式**: 更好的 NPE 偵錯（Java 17 中預設為啟用）

```java
// 舊版 NPE 訊息: "Exception in thread 'main' java.lang.NullPointerException"
// 新版 NPE 訊息顯示確切的 null 來源:
// "Cannot invoke 'String.length()' because the return value of 'Person.getName()' is null"

public class PersonProcessor {
    public void processPersons(List<Person> persons) {
        // 這將顯示確切是哪個 person.getName() 返回了 null
        persons.stream()
            .mapToInt(person -> person.getName().length())  // Clear NPE if getName() returns null
            .sum();
    }

    // 更完善的錯誤訊息有助於處理複雜的表達式。
    public void complexExample(Map<String, List<Person>> groups) {
        // NPE 將顯示確切是哪個部分為 null
        int totalNameLength = groups.get("admins")
                                  .get(0)
                                  .getName()
                                  .length();
    }
}
```

### JEP 371: Hidden Classes 隱性類別 (Java 15)

**遷移模式**: 用於框架和代理程式生成

```java
// 用於創建動態代理的框架
public class DynamicProxyExample {
    public static <T> T createProxy(Class<T> interfaceClass, InvocationHandler handler) {
        // 隱藏類別為動態產生的類別提供了更好的封裝性。
        MethodHandles.Lookup lookup = MethodHandles.lookup();

        // 框架代碼將使用隱藏類別以獲得更好的隔離性
        // 這通常由框架處理，而不是應用程式代碼
        return interfaceClass.cast(
            Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                handler
            )
        );
    }
}
```

### JEP 334: JVM Constants API (JVM 常數 API ) (Java 12)

**遷移模式**: 用於編譯時常數

```java
import java.lang.constant.*;

// 用於進階的元程式設計和工具
public class ConstantExample {
    // 使用動態常數來計算值
    public static final DynamicConstantDesc<String> COMPUTED_CONSTANT =
        DynamicConstantDesc.of(
            ConstantDescs.BSM_INVOKE,
            "computeValue",
            ConstantDescs.CD_String
        );

    // 主要由編譯器和框架開發人員使用
    public static String computeValue() {
        return "Computed at runtime, cached as constant";
    }
}
```

### JEP 415: Context-Specific Deserialization Filters 上下文特定的反序列化過濾器(Java 17)

**遷移模式**: 增強物件反序列化的安全性

```java
import java.io.*;

public class SecureDeserialization {
    // 設置反序列化過濾器以增強安全性
    public static void setupSerializationFilters() {
        // 全局過濾器
        ObjectInputFilter globalFilter = ObjectInputFilter.Config.createFilter(
            "java.base/*;java.util.*;!*"
        );
        ObjectInputFilter.Config.setSerialFilter(globalFilter);
    }

    public <T> T deserializeSecurely(byte[] data, Class<T> expectedType) throws IOException, ClassNotFoundException {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            // 上下文特定的過濾器
            ObjectInputFilter contextFilter = ObjectInputFilter.Config.createFilter(
                expectedType.getName() + ";java.lang.*;!*"
            );
            ois.setObjectInputFilter(contextFilter);

            return expectedType.cast(ois.readObject());
        }
    }
}
```

### JEP 356: Enhanced Pseudo-Random Number Generators 增強型偽隨機數生成器 (Java 17)

**遷移模式**: 使用新的隨機生成器介面

```java
import java.util.random.*;

// 舊版：有限的 Random 類別
Random oldRandom = new Random();
int oldValue = oldRandom.nextInt(100);

// 新版：增強型隨機生成器（Java 17+）
RandomGenerator generator = RandomGeneratorFactory
    .of("Xoshiro256PlusPlus")
    .create(System.nanoTime());

RandomGenerator.SplittableGenerator splittableGenerator =
    RandomGeneratorFactory.of("L64X128MixRandom").create();

// Better for parallel processing
splittableGenerator.splits(4)
    .parallel()
    .mapToInt(rng -> rng.nextInt(1000))
    .forEach(System.out::println);

// Streamable random values
generator.ints(10, 1, 101)
    .forEach(System.out::println);
```

## I/O 和網路改進

### JEP 380: Unix-Domain Socket Channels (Unix 域套接字通道) (Java 16)

**遷移模式**: 使用 Unix 域套接字進行本機進程間通訊

```java
import java.net.UnixDomainSocketAddress;
import java.nio.channels.*;

// 舊版：使用 TCP 套接字進行本地通訊
// ServerSocketChannel server = ServerSocketChannel.open();
// server.bind(new InetSocketAddress("localhost", 8080));

// 新版：使用 Unix 域套接字（Java 16+）
public class UnixSocketExample {
    public void createUnixDomainServer() throws IOException {
        Path socketPath = Path.of("/tmp/my-app.socket");
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);

        try (ServerSocketChannel server = ServerSocketChannel.open(StandardProtocolFamily.UNIX)) {
            server.bind(address);

            while (true) {
                try (SocketChannel client = server.accept()) {
                    // 處理客戶端連接
                    handleClient(client);
                }
            }
        }
    }

    public void connectToUnixSocket() throws IOException {
        Path socketPath = Path.of("/tmp/my-app.socket");
        UnixDomainSocketAddress address = UnixDomainSocketAddress.of(socketPath);

        try (SocketChannel client = SocketChannel.open(address)) {
            // 與伺服器通訊
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            client.read(buffer);
        }
    }

    private void handleClient(SocketChannel client) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int bytesRead = client.read(buffer);
        // 處理客戶端資料
    }
}
```

### JEP 352: Non-Volatile Mapped Byte Buffers (非揮發性映射字節緩衝區) (Java 14)

**遷移模式**: 用於持久記憶體操作

```java
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

public class PersistentMemoryExample {
    public void usePersistentMemory() throws IOException {
        Path nvmFile = Path.of("/mnt/pmem/data.bin");

        try (FileChannel channel = FileChannel.open(nvmFile,
                StandardOpenOption.READ,
                StandardOpenOption.WRITE,
                StandardOpenOption.CREATE)) {

            // Map 為持久記憶體
            MappedByteBuffer buffer = channel.map(
                FileChannel.MapMode.READ_WRITE, 0, 1024,
                ExtendedMapMode.READ_WRITE_SYNC
            );

            // 寫入在崩潰後仍然持久的資料
            buffer.putLong(0, System.currentTimeMillis());
            buffer.putInt(8, 12345);

            // 強制寫入持久存儲
            buffer.force();
        }
    }
}
```

## 建置系統配置

### Maven 配置

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <maven.compiler.release>17</maven.compiler.release>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <release>17</release>
                <!-- Enable preview features if using JEP 406 -->
                <compilerArgs>
                    <arg>--enable-preview</arg>
                </compilerArgs>
            </configuration>
        </plugin>

        <!-- For running tests with preview features -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.0.0</version>
            <configuration>
                <argLine>--enable-preview</argLine>
            </configuration>
        </plugin>
    </plugins>
</build>
```

### Gradle 配置

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile> {
    options.release.set(17)
    // Enable preview features if needed
    options.compilerArgs.addAll(listOf("--enable-preview"))
}

tasks.withType<Test> {
    useJUnitPlatform()
    // Enable preview features for tests
    jvmArgs("--enable-preview")
}
```

## 棄用和移除

### JEP 411: Deprecate the Security Manager for Removal 棄用安全管理器以進行移除

**遷移模式**：移除安全管理器相依性

```java
// 舊版：使用安全管理器
SecurityManager sm = System.getSecurityManager();
if (sm != null) {
    sm.checkPermission(new RuntimePermission("shutdownHooks"));
}

// 新增：替代安全方法
// 使用應用程式層級的安全性、容器或進程隔離
// 大多數應用程式不需要安全管理器功能
```

### JEP 398: Deprecate the Applet API for Removal

**遷移模式**：從 Applet 遷移到現代 Web 技術

```java
// 舊版：Java Applet（已棄用）
public class MyApplet extends Applet {
    @Override
    public void start() {
        // Applet code
    }
}

// 新增：現代替代方案
// 1. 轉換為獨立的 Java 應用程式
public class MyApplication extends JFrame {
    public MyApplication() {
        setTitle("My Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Application code
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new MyApplication().setVisible(true);
        });
    }
}

// 2. 使用 Java Web Start 替代方案 (jlink)
// 3. 轉換為使用現代框架的 Web 應用程式
```

### JEP 372: Remove the Nashorn JavaScript Engine 移除 Nashorn JavaScript 引擎 (Java 15)

**遷移模式**：使用替代的 JavaScript 引擎

```java
// 舊版：Nashorn（已在 Java 17 移除）
// ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

// 新增：替代方案
// 1. 使用 GraalVM JavaScript 引擎
ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal.js");

// 2. 使用外部 JavaScript 執行
ProcessBuilder pb = new ProcessBuilder("node", "script.js");
Process process = pb.start();

// 3. 使用基於網頁的方法或嵌入式瀏覽器
```

## JVM 和性能改進

### JEP 377: ZGC - 可擴展的低延遲垃圾回收器 (Java 15)

**遷移模式**: 為低延遲應用啟用 ZGC

```bash
# 啟用 ZGC
-XX:+UseZGC
-XX:+UnlockExperimentalVMOptions  # Java 17 中不需要


# Monitor ZGC performance
-XX:+LogVMOutput
-XX:LogFile=gc.log
```

### JEP 379: Shenandoah - A Low-Pause-Time Garbage Collector 低延遲垃圾回收器 (Java 15)

**遷移模式**: 為一致延遲啟用 Shenandoah

```bash
# 啟用 Shenandoah
-XX:+UseShenandoahGC
-XX:+UnlockExperimentalVMOptions  # Java 17 中不需要

# Shenandoah 調優
-XX:ShenandoahGCHeuristics=adaptive
```

### JEP 341: Default CDS Archives 預設 CDS 歸檔 (Java 12) & JEP 350: Dynamic CDS Archives 動態 CDS 歸檔 (Java 13)

**遷移模式**: 改善啟動性能

```bash
# CDS 預設啟用，但可以創建自定義檔案
# 創建自定義 CDS 檔案
java -XX:DumpLoadedClassList=classes.lst -cp myapp.jar com.example.Main
java -Xshare:dump -XX:SharedClassListFile=classes.lst -XX:SharedArchiveFile=myapp.jsa -cp myapp.jar

# 使用自定義 CDS 檔案
java -XX:SharedArchiveFile=myapp.jsa -cp myapp.jar com.example.Main
```

## 測試和遷移策略

### Phase 1: 基礎階段 (Weeks 1-2)

1. **更新建置系統**

   - 修改 Maven/Gradle 配置以支援 Java 17
   - 更新 CI/CD 管道
   - 驗證依賴相容性

2. **處理移除和棄用的功能**
   - 移除 Nashorn JavaScript 引擎的使用
   - 替換已棄用的 Applet API
   - 更新安全管理器的使用

### Phase 2: 語言特質 (Weeks 3-4)

1. **實作 Records**

   - 將資料類別轉換為 records
   - 在緊湊建構子中添加驗證
   - 測試序列化相容性

2. **新增模式匹配**
   - 轉換 instanceof 鏈
   - 實作型別安全的轉型模式

### Phase 3: 進階特性 (Weeks 5-6)

1. **Switch Expressions**

   - 將 switch 語句轉換為表達式
   - 使用新的箭頭語法
   - 實作複雜的 yield 邏輯

2. **Text Blocks**
   - 替換多行字串連接
   - 更新 SQL 和 HTML 生成
   - 使用格式化方法

### Phase 4: 封閉類別 (Weeks 7-8)

1. **設計封閉層次結構**

   - 識別繼承限制
   - 實作封閉類別模式
   - 與模式匹配結合

2. **測試和驗證**
   - 完整的測試覆蓋
   - 性能基準測試
   - 相容性驗證

## 性能考量

### Records 與傳統類別比較

- Records 更節省記憶體
- 創建和相等性檢查更快
- 自動支援序列化
- 適用於資料傳輸物件

### 模式匹配性能

- 消除冗餘的型別檢查
- 減少轉型開銷
- 提供更好的 JVM 優化機會
- 與封閉類別結合使用以確保完整性

### Switch 表達式優化

- 更高效的位元碼生成
- 更好的常數折疊
- 改進的分支預測
- 用於複雜的條件邏輯

## 最佳實踐

1. **使用 Records 作為資料類別**

   - 不可變資料容器
   - API 資料傳輸物件
   - 配置物件

2. **策略性地使用模式匹配**

   - 替換 instanceof 鏈
   - 與封閉類別結合使用
   - 與 switch 表達式結合使用

3. **使用 Text Blocks 處理多行內容**

   - SQL 查詢
   - JSON 模板
   - HTML 內容
   - 配置檔案

4. **設計封閉類別**

   - 領域建模
   - 狀態機
   - 代數資料類型
   - API 演進控制

5. **利用增強的隨機生成器**
   - 平行處理場景
   - 高品質隨機數
   - 統計應用
   - 遊戲和模擬

本綜合指南使 GitHub Copilot 能夠在將 Java 11 專案升級到 Java 17 時提供符合上下文的建議，並專注於語言增強、API 改進和現代 Java 開發實踐。
