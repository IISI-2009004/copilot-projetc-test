---
applyTo: ["*"]
description: "自 Java 21 發布以來，採用 Java 25 新功能的全面最佳實務。"
---

# Java 21 to Java 25 升級指南

這些指導說明幫助 GitHub Copilot 協助開發人員將 Java 專案從 JDK 21 升級到 JDK 25，重點關注新的語言特性、API 變更和最佳實踐。

## JDK 22-25 中的語言特性和 API 變更

### Pattern Matching Enhancements 模式匹配增強功能 (JEP 455/488 - Preview in 23)

**Pattern、instanceof 和 switch 中的原始類型**

使用模式匹配時：

- 建議在 switch 表達式和 instanceof 檢查中使用原始類型模式
- 傳統 switch 的升級範例：

```java
// 舊方法 (Java 21)
switch (x.getStatus()) {
    case 0 -> "okay";
    case 1 -> "warning";
    case 2 -> "error";
    default -> "unknown status: " + x.getStatus();
}

// 新方法 (Java 25 Preview)
switch (x.getStatus()) {
    case 0 -> "okay";
    case 1 -> "warning";
    case 2 -> "error";
    case int i -> "unknown status: " + i;
}
```

- 使用 `--enable-preview` 標誌啟用預覽功能
- 建議在更複雜的條件下使用 guard patterns：

```java
switch (x.getYearlyFlights()) {
    case 0 -> ...;
    case int i when i >= 100 -> issueGoldCard();
    case int i -> ... // handle 1-99 range
}
```

### Class-File API 類別檔案 API (JEP 466/484 - Second Preview in 23, Standard in 25)

**用標準 API 取代 ASM**

當檢測到位元碼操作或類別檔案處理時：

- 建議從 ASM 函式庫遷移到標準 Class-File API
- 使用 `java.lang.classfile` 套件取代 `org.objectweb.asm`
- 範例遷移模式：

```java
// 舊版 ASM 方法
ClassReader reader = new ClassReader(classBytes);
ClassWriter writer = new ClassWriter(reader, 0);
// ... ASM 操作

// 新版 Class-File API 方法
ClassModel classModel = ClassFile.of().parse(classBytes);
byte[] newBytes = ClassFile.of().transform(classModel,
    ClassTransform.transformingMethods(methodTransform));
```

### Markdown Documentation Comments (Markdown 文件註釋)(JEP 467 - Standard in 23)

**JavaDoc 現代化**

當處理 JavaDoc 註解時：

- 建議將 HTML-heavy 的 JavaDoc 轉換為 Markdown 語法
- 使用 `///` 來撰寫 Markdown 文件註釋
- 範例轉換：

```java
// 舊版 HTML JavaDoc
/**
 * Returns the <b>absolute</b> value of an {@code int} value.
 * <p>
 * If the argument is not negative, return the argument.
 * If the argument is negative, return the negation of the argument.
 *
 * @param a the argument whose absolute value is to be determined
 * @return the absolute value of the argument
 */

// 新版 Markdown JavaDoc
/// Returns the **absolute** value of an `int` value.
///
/// If the argument is not negative, return the argument.
/// If the argument is negative, return the negation of the argument.
///
/// @param a the argument whose absolute value is to be determined
/// @return the absolute value of the argument
```

### Derived Record Creation 建立衍生記錄 (JEP 468 - Preview in 23)

**記錄增強**

當處理記錄時：

- 建議使用 `with` 表達式來建立衍生記錄
- 啟用預覽功能以使用衍生記錄建立
- 範例模式：

```java
// 舊版手動複製記錄
public record Person(String name, int age, String email) {
    public Person withAge(int newAge) {
        return new Person(name, newAge, email);
    }
}

// 使用衍生記錄建立 (預覽)
Person updated = person with { age = 30; };
```

### Stream Gatherers 串流收集器 (JEP 473/485 - Second Preview in 23, Standard in 25)

**增強的流處理**

當處理複雜的流操作時：

- 建議使用 `Stream.gather()` 進行自訂的中間操作
- 導入 `java.util.stream.Gatherers` 以使用內建的 gatherers
- 範例用法：

```java
// 自訂視窗操作
List<List<String>> windows = stream
    .gather(Gatherers.windowSliding(3))
    .toList();

// 帶狀態的自訂過濾
List<Integer> filtered = numbers.stream()
    .gather(Gatherers.fold(0, (state, element) -> {
        // Custom stateful logic
        return state + element > threshold ? element : null;
    }))
    .filter(Objects::nonNull)
    .toList();
```

## 遷移警告和棄用

### sun.misc.Unsafe 記憶體存取方法 (JEP 471 - 23 已棄用)

當檢測到 `sun.misc.Unsafe` 使用時：

- 警告已棄用的記憶體存取方法
- 建議遷移到標準替代方案：

```java
// 舊版 sun.misc.Unsafe 記憶體存取
Unsafe unsafe = Unsafe.getUnsafe();
unsafe.getInt(object, offset);

// 建議使用 VarHandle API
VarHandle vh = MethodHandles.lookup()
    .findVarHandle(MyClass.class, "fieldName", int.class);
int value = (int) vh.get(object);

// 或者使用 off-heap: Foreign Function & Memory API
MemorySegment segment = MemorySegment.ofArray(new int[10]);
int value = segment.get(ValueLayout.JAVA_INT, offset);
```

### JNI 使用警告 (JEP 472 - 24 中的警告)

當檢測到 JNI 使用時：

- 警告即將對 JNI 使用施加限制
- 建議為使用 JNI 的應用程式添加 `--enable-native-access` 標誌
- 建議在可能的情況下遷移到 Foreign Function & Memory API
- 為本機訪問添加 module-info.java 條目：

```java
module com.example.app {
    requires jdk.unsupported; // for remaining JNI usage
}
```

## 垃圾收集最新進展

### ZGC Generational Mode (ZGC 世代模式) (JEP 474 - Default in 23)

當配置垃圾收集時：

- 預設 ZGC 現在使用世代模式
- 如果明確使用非世代 ZGC，請更新 JVM 標誌：

```bash
# 明確使用非世代模式 (將顯示棄用警告)
-XX:+UseZGC -XX:-ZGenerational

# 預設世代模式
-XX:+UseZGC
```

### G1 Improvements (G1 改進) (JEP 475 - Implemented in 24)

當使用 G1GC 時：

- 不需要更改代碼 - 內部 JVM 優化
- 可能會看到 C2 編譯器的編譯性能提升

## Vector API (JEP 469 - Eighth Incubator in 25)

當處理數值計算時：

- 建議使用 Vector API 進行 SIMD 操作（仍在孵化中）
- 添加 `--add-modules jdk.incubator.vector`
- 範例用法：

```java
import jdk.incubator.vector.*;

// 傳統標量計算
for (int i = 0; i < a.length; i++) {
    c[i] = a[i] + b[i];
}

// 向量化計算
var species = IntVector.SPECIES_PREFERRED;
for (int i = 0; i < a.length; i += species.length()) {
    var va = IntVector.fromArray(species, a, i);
    var vb = IntVector.fromArray(species, b, i);
    var vc = va.add(vb);
    vc.intoArray(c, i);
}
```

## 編譯和建置配置

### 預覽功能

對於使用預覽功能的專案：

- 在編譯器參數中添加 `--enable-preview`
- 在運行時參數中添加 `--enable-preview`
- Maven 配置：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <release>25</release>
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

- Gradle 配置：

```kotlin
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}
```

## 遷移策略

### 分步升級流程

1. **更新建置工具**: 確保 Maven/Gradle 支援 JDK 25
2. **更新依賴項**: 檢查 JDK 25 相容性
3. **處理警告**: 處理 JEPs 471/472 的棄用警告
4. **啟用預覽功能**: 如果使用模式匹配或其他預覽功能
5. **徹底測試**: 特別是對使用 JNI 或 sun.misc.Unsafe 的應用程式
6. **性能測試**: 驗證新的 ZGC 預設行為

### 代碼審查清單

在審查 Java 25 升級的代碼時：

- [ ] 將 ASM 使用替換為 Class-File API
- [ ] 將複雜的 HTML JavaDoc 轉換為 Markdown
- [ ] 在 switch 表達式中使用原始模式（如適用）
- [ ] 將 sun.misc.Unsafe 替換為 VarHandle 或 FFM API
- [ ] 為 JNI 使用添加本機訪問權限
- [ ] 使用 Stream gatherers 處理複雜的流操作
- [ ] 更新建置配置以支援預覽功能

### 測試考量

- 使用 `--enable-preview` 標誌測試預覽功能
- 驗證 JNI 應用程式在本機訪問警告下的運作
- 使用新的 ZGC 世代模式進行性能測試
- 驗證使用 Markdown 註解生成的 JavaDoc

## 常見陷阱

1. **預覽功能依賴**: 不要在庫代碼中使用預覽功能，除非有明確的文檔說明
2. **本機訪問**: 直接或間接使用 JNI 的應用程式可能需要 `--enable-native-access` 配置
3. **Unsafe 遷移**: 不要延遲從 sun.misc.Unsafe 遷移 - 棄用警告表示未來將移除
4. **模式匹配範圍**: 原始模式適用於所有原始類型，而不僅僅是 int
5. **記錄增強**: 派生記錄的創建需要在 Java 23 中啟用預覽標誌

## 性能考量

- ZGC 世代模式可能會提高大多數工作負載的性能
- Class-File API 減少了與 ASM 相關的開銷
- Stream gatherers 為複雜的流操作提供了更好的性能
- G1GC 改進減少了 JIT 編譯開銷

記得在部署 Java 25 升級到生產系統之前，在預備環境中進行徹底測試。
