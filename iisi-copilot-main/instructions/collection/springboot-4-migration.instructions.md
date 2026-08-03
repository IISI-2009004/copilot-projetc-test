---
description: "將 Spring Boot 應用程式從 3.x 遷移到 4.0 的全面指南，重點介紹 Gradle Kotlin DSL 和版本目錄"
applyTo: "**/*.java, **/*.kt, **/build.gradle.kts, **/build.gradle, **/settings.gradle.kts, **/gradle/libs.versions.toml, **/*.properties, **/*.yml, **/*.yaml"
---

# Spring Boot 3.x 到 4.0 的遷移指南

## 專案背景

本指南提供了全面的 GitHub Copilot 指導，用於將 Spring Boot 專案從 3.x 版本升級到 4.0，重點介紹 Gradle Kotlin DSL、版本目錄（`libs.versions.toml`）以及 Kotlin 特定的考量。
**Spring Boot 4.0 的主要架構變更：**

- 模組化依賴結構，具有專注且更小的模組
- 需要 Spring Framework 7.x
- Jakarta EE 11（Servlet 6.1 基線）
- Jackson 3.x 遷移（包命名空間變更）
- 需要 Kotlin 2.2+
- 配置屬性全面重組

## 系統需求

### 最低版本

- **Java**: 17+ (建議使用最新 LTS: Java 21 或 25)
- **Kotlin**: 2.2.0 或更高版本
- **Spring Framework**: 7.x (由 Spring Boot 4.0 管理)
- **Jakarta EE**: 11 (Servlet 6.1 基線)
- **GraalVM** (用於原生映像): 25+
- **Gradle**: 8.5+ (用於 Kotlin DSL 和版本目錄支持)
- **Gradle CycloneDX Plugin**: 3.0.0+

### 驗證相容性

```bash
# 檢查當前版本
./gradlew --version
./gradlew dependencies --configuration runtimeClasspath
```

## 遷移前步驟

### 1. 升級到最新的 Spring Boot 3.5.x

在遷移到 4.0 之前，先升級到最新的 3.5.x 版本：

```kotlin
// libs.versions.toml
[versions]
springBoot = "3.5.6" # 最新的 3.x 版本，遷移到 4.0 之前使用
```

### 2. 清理已棄用的 API

移除所有 Spring Boot 3.x 中已棄用的 API 使用。這些在 4.0 中將成為編譯錯誤：

```bash
# 構建並檢查警告
./gradlew clean build --warning-mode all
```

### 3. 檢查依賴變更

將您的依賴與以下版本進行比較：

- [Spring Boot 3.5.x 依賴版本](https://docs.spring.io/spring-boot/3.5/appendix/dependency-versions/coordinates.html)
- [Spring Boot 4.0.x 依賴版本](https://docs.spring.io/spring-boot/4.0/appendix/dependency-versions/coordinates.html)

## 模組重構與啟動器變更

### 關鍵：模組化架構

Spring Boot 4.0 引入了 **更小、更專注的模組**，取代了大型單體 jar。這需要在大多數專案中更新依賴。

**對於庫作者的重要提示：** 由於模組化工作和包重組，**強烈不建議在同一個 artifact 中同時支持 Spring Boot 3 和 Spring Boot 4**。庫作者應該為每個主要版本發布單獨的 artifact，以避免運行時衝突並確保乾淨的依賴管理。

### 遷移策略：選擇一種方法

#### 選項 1：技術專用啟動器（建議用於生產環境）

大多數 Spring Boot 涵蓋的技術現在都有 **專用的測試啟動器伴侶**。這提供了細粒度的控制。

**完整啟動器參考：** 有關所有可用啟動器（核心、Web、數據庫、Spring Data、消息、安保、模板、Production-Ready 等）及其測試伴侶的完整表格，請參閱 [官方 Spring Boot 4.0 遷移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide#starters)。

**libs.versions.toml:**

```toml
[versions]
springBoot = "4.0.0"

[libraries]
# Core starters with dedicated test modules
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-webmvc", version.ref = "springBoot" }
spring-boot-starter-webmvc-test = { module = "org.springframework.boot:spring-boot-starter-webmvc-test", version.ref = "springBoot" }

spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa", version.ref = "springBoot" }
spring-boot-starter-data-jpa-test = { module = "org.springframework.boot:spring-boot-starter-data-jpa-test", version.ref = "springBoot" }

spring-boot-starter-security = { module = "org.springframework.boot:spring-boot-starter-security", version.ref = "springBoot" }
spring-boot-starter-security-test = { module = "org.springframework.boot:spring-boot-starter-security-test", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.spring.boot.starter.webmvc)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.security)

    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.boot.starter.security.test)
}
```

#### 選項 2：經典啟動器（快速遷移，已棄用）

快速遷移時，可以使用 **經典啟動器**，它們捆綁了所有自動配置（類似於 Spring Boot 3.x）：

**libs.versions.toml:**

```toml
[libraries]
spring-boot-starter-classic = { module = "org.springframework.boot:spring-boot-starter-classic", version.ref = "springBoot" }
spring-boot-starter-test-classic = { module = "org.springframework.boot:spring-boot-starter-test-classic", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.spring.boot.starter.classic)
    testImplementation(libs.spring.boot.starter.test.classic)
}
```

**Warning**: Classic starters are **deprecated** and will be removed in future releases. Plan migration to technology-specific starters.

#### 選項 3：直接模組依賴（進階）

對於顯式控制傳遞依賴：

**libs.versions.toml:**

```toml
[libraries]
spring-boot-webmvc = { module = "org.springframework.boot:spring-boot-webmvc", version.ref = "springBoot" }
spring-boot-webmvc-test = { module = "org.springframework.boot:spring-boot-webmvc-test", version.ref = "springBoot" }
```

### 重命名的啟動器（破壞性變更）

更新 `libs.versions.toml` 中的這些啟動器名稱：

| Spring Boot 3.x                                   | Spring Boot 4.0                                            | Notes                                         |
| ------------------------------------------------- | ---------------------------------------------------------- | --------------------------------------------- |
| `spring-boot-starter-web`                         | `spring-boot-starter-webmvc`                               | 明確命名                                      |
| `spring-boot-starter-web-services`                | `spring-boot-starter-webservices`                          | 移除連字號                                    |
| `spring-boot-starter-aop`                         | `spring-boot-starter-aspectj`                              | 僅在使用 `org.aspectj.lang.annotation` 時需要 |
| `spring-boot-starter-oauth2-authorization-server` | `spring-boot-starter-security-oauth2-authorization-server` | 安全命名空間                                  |
| `spring-boot-starter-oauth2-client`               | `spring-boot-starter-security-oauth2-client`               | 安全命名空間                                  |
| `spring-boot-starter-oauth2-resource-server`      | `spring-boot-starter-security-oauth2-resource-server`      | 安全命名空間                                  |

**遷移範例 (libs.versions.toml):**

```toml
[libraries]
# Old (Spring Boot 3.x)
# spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web", version.ref = "springBoot" }
# spring-boot-starter-oauth2-client = { module = "org.springframework.boot:spring-boot-starter-oauth2-client", version.ref = "springBoot" }

# New (Spring Boot 4.0)
spring-boot-starter-webmvc = { module = "org.springframework.boot:spring-boot-starter-webmvc", version.ref = "springBoot" }
spring-boot-starter-security-oauth2-client = { module = "org.springframework.boot:spring-boot-starter-security-oauth2-client", version.ref = "springBoot" }
```

### AspectJ 入門指南

只有當您確實使用 AspectJ 註解時才包含 spring-boot-starter-aspectj ：

```kotlin
// Only needed if code uses org.aspectj.lang.annotation package
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before

@Aspect
class MyAspect {
    @Before("execution(* com.example..*(..))")
    fun beforeAdvice() { }
}
```

如果不使用 AspectJ，請移除該依賴。

## 已移除的功能及替代方案

### 嵌入式伺服器

#### Undertow 已移除

**Undertow 已完全移除** - 不兼容 Servlet 6.1 基線。

**遷移方案:**

- 使用 **Tomcat**（默認）或 **Jetty**
- 不要將 Spring Boot 4.0 應用部署到非 Servlet 6.1 容器

**libs.versions.toml:**

```toml
[libraries]
# Remove Undertow
# spring-boot-starter-undertow = { module = "org.springframework.boot:spring-boot-starter-undertow", version.ref = "springBoot" }

# Use Tomcat (default) or Jetty
spring-boot-starter-jetty = { module = "org.springframework.boot:spring-boot-starter-jetty", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.spring.boot.starter.webmvc) {
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation(libs.spring.boot.starter.jetty) // Alternative to Tomcat
}
```

### 會話管理

#### Spring Session Hazelcast 和 MongoDB 已移除

**由各自的團隊維護**，不再由 Spring Boot 管理依賴。

**遷移方案 (libs.versions.toml):**

```toml
[versions]
hazelcast-spring-session = "3.x.x" # Check Hazelcast documentation
mongodb-spring-session = "4.x.x"   # Check MongoDB documentation

[libraries]
# Explicit versions required
spring-session-hazelcast = { module = "com.hazelcast:spring-session-hazelcast", version.ref = "hazelcast-spring-session" }
spring-session-mongodb = { module = "org.springframework.session:spring-session-data-mongodb", version.ref = "mongodb-spring-session" }
```

### 反應式消息傳遞

#### Pulsar 反應式已移除

Spring Pulsar 已放棄 Reactor 支持 - 反應式 Pulsar 客戶端已移除。

**遷移方案:**

- 使用命令式 Pulsar 客戶端
- 或遷移到其他反應式消息傳遞（Kafka、RabbitMQ）

### 測試

#### Spock 框架已移除

**Spock 尚不支持 Groovy 5**（Spring Boot 4.0 所需）。

**遷移方案:**

- 使用 JUnit 5 與 Kotlin
- 或等待 Spock 支持 Groovy 5

### Build Features

#### 可執行 Jar 啟動腳本已移除

嵌入式啟動腳本用於「完全可執行」的 jar 已移除（僅限 Unix，使用有限）。

**build.gradle.kts (移除):**

```kotlin
// 移除此配置
tasks.bootJar {
    launchScript() // 不再支持
}
```

**替代方案:**

- 直接使用 `java -jar app.jar`
- 使用 Gradle Application 插件來創建本地啟動器
- 使用 systemd 服務文件

#### Classic Uber-Jar Loader 已移除

經典的 uber-jar 加載器已被移除。請從構建中移除任何加載器實現配置。

**Maven (pom.xml) - 移除:**

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <loaderImplementation>CLASSIC</loaderImplementation> <!-- 移除此配置 -->
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Gradle (build.gradle.kts) - 移除:**

```kotlin
tasks.bootJar {
    loaderImplementation = org.springframework.boot.loader.tools.LoaderImplementation.CLASSIC // 移除此配置
}
```

## Jackson 3 Migration

### 重大變更：包命名空間

Jackson 3 changes **group ID and package names**:

| Component | Old (Jackson 2)           | New (Jackson 3)                               |
| --------- | ------------------------- | --------------------------------------------- |
| Group ID  | `com.fasterxml.jackson`   | `tools.jackson`                               |
| Packages  | `com.fasterxml.jackson.*` | `tools.jackson.*`                             |
| Exception | `jackson-annotations`     | Still uses `com.fasterxml.jackson.core` group |

**libs.versions.toml:**

```toml
[versions]
jackson = "3.0.1" # Managed by Spring Boot 4.0

[libraries]
# Jackson 3 uses new group ID
jackson-databind = { module = "tools.jackson.core:jackson-databind", version.ref = "jackson" }
jackson-module-kotlin = { module = "tools.jackson.module:jackson-module-kotlin", version.ref = "jackson" }

# Exception: annotations still use old group
jackson-annotations = { module = "com.fasterxml.jackson.core:jackson-annotations", version.ref = "jackson" }
```

### 類別和註解重命名

更新導入和註解：

| Spring Boot 3.x                         | Spring Boot 4.0               |
| --------------------------------------- | ----------------------------- |
| `Jackson2ObjectMapperBuilderCustomizer` | `JsonMapperBuilderCustomizer` |
| `JsonObjectSerializer`                  | `ObjectValueSerializer`       |
| `JsonValueDeserializer`                 | `ObjectValueDeserializer`     |
| `@JsonComponent`                        | `@JacksonComponent`           |
| `@JsonMixin`                            | `@JacksonMixin`               |

**Migration Example:**

```kotlin
// Old (Spring Boot 3.x)
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.boot.jackson.JsonComponent

@JsonComponent
class CustomSerializer : JsonSerializer<MyType>() { }

@Configuration
class JacksonConfig {
    @Bean
    fun customizer(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.simpleDateFormat("yyyy-MM-dd")
        }
    }
}

// New (Spring Boot 4.0)
import tools.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.jackson.JsonMapperBuilderCustomizer
import org.springframework.boot.jackson.JacksonComponent

@JacksonComponent
class CustomSerializer : JsonSerializer<MyType>() { }

@Configuration
class JacksonConfig {
    @Bean
    fun customizer(): JsonMapperBuilderCustomizer {
        return JsonMapperBuilderCustomizer { builder ->
            builder.simpleDateFormat("yyyy-MM-dd")
        }
    }
}
```

### 配置屬性更改

**application.yml migration:**

```yaml
# Old (Spring Boot 3.x)
spring:
  jackson:
    read:
      enums-using-to-string: true
    write:
      dates-as-timestamps: false

# New (Spring Boot 4.0)
spring:
  jackson:
    json:
      read:
        enums-using-to-string: true
      write:
        dates-as-timestamps: false
```

### Jackson 2 兼容模塊（臨時）

為了實現逐步遷移，請使用臨時相容模組 （已棄用，即將移除）：

**libs.versions.toml:**

```toml
[libraries]
spring-boot-jackson2 = { module = "org.springframework.boot:spring-boot-jackson2", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.spring.boot.jackson2)
}
```

**application.yml:**

```yaml
spring:
  jackson:
    use-jackson2-defaults: true # Use Jackson 2 behavior
```

**使用相容模組時，`spring.jackson2.*` 命名空間下的屬性**

**計劃遷移離開此模組** - 它將在未來版本中被移除。

## 核心框架變更

### 空值註解：JSpecify

Spring Boot 4.0 adds **JSpecify nullability annotations** throughout the codebase.

**影響:**

- Kotlin 空值安全性可能會標記新的警告/錯誤
- Null checkers (SpotBugs, NullAway) 可能會報告新的問題
- **RestClient 方法如 `body()` 現在明確標記為可為空** - 始終檢查 null 或使用 `Objects.requireNonNull()`

**Kotlin 遷移示例:**

```kotlin
// 可能需要明確的可空類型
fun processUser(id: String?): User? {
    return userRepository.findById(id) // 現在可能明確為可空
}

// RestClient body() can return null
val body: String? = restClient.get()
    .uri("https://api.example.com/data")
    .retrieve()
    .body(String::class.java) // 可為空 - 適當處理

if (body != null) {
    println(body.length)
}
```

**Actuator endpoint parameters:**

- 不能使用 javax.annotations.NonNull 或 org.springframework.lang.Nullable
- 使用 `org.jspecify.annotations.Nullable` 代替

**libs.versions.toml:**

```toml
[libraries]
jspecify = { module = "org.jspecify:jspecify", version = "1.0.0" }
```

### Package Relocations 包裹搬遷

#### BootstrapRegistry

**Old import:**

```kotlin
import org.springframework.boot.BootstrapRegistry
```

**New import:**

```kotlin
import org.springframework.boot.bootstrap.BootstrapRegistry
```

#### EnvironmentPostProcessor

**Old import:**

```kotlin
import org.springframework.boot.env.EnvironmentPostProcessor
```

**New import:**

```kotlin
import org.springframework.boot.EnvironmentPostProcessor
```

**Update `META-INF/spring.factories`:**

```properties
# Old
org.springframework.boot.env.EnvironmentPostProcessor=com.example.MyPostProcessor

# New
org.springframework.boot.EnvironmentPostProcessor=com.example.MyPostProcessor
```

**注意：** 已棄用的表單仍暫時可用，但將被移除。

#### Entity Scan

**Old import:**

```kotlin
import org.springframework.boot.autoconfigure.domain.EntityScan
```

**New import:**

```kotlin
import org.springframework.boot.persistence.autoconfigure.EntityScan
```

### Logging Changes

#### Logback Default Charset

日誌文件現在默認為 **UTF-8**（與 Log4j2 統一）：

**logback-spring.xml (顯式配置):**

```xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>app.log</file>
        <encoder>
            <charset>UTF-8</charset> <!-- 現在默認 -->
            <pattern>%d{yyyy-MM-dd HH:mm:ss} - %msg%n</pattern>
        </encoder>
    </appender>
</configuration>
```

**控制台日誌記錄：** 如果可用，使用 `Console#charset()`（Java 17+），否則回退到 UTF-8。這提供了更好的平台兼容性，同時保持一致的編碼。

### DevTools 變更

#### Live Reload 默認禁用

**application.yml:**

```yaml
spring:
  devtools:
    livereload:
      enabled: true # 必須在 4.0 中明確啟用
```

**libs.versions.toml:**

```toml
[libraries]
spring-boot-devtools = { module = "org.springframework.boot:spring-boot-devtools", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    developmentOnly(libs.spring.boot.devtools)
}
```

### PropertyMapper API 行為變更

**重大變更：** 當來源為 `null` 時，預設不再調用適配器/謂詞方法。

**遷移模式：**

```kotlin
// 舊行為 (Spring Boot 3.x)
map.from(source::method).to(destination::method)
// 如果來源返回 null，則調用 destination.method(null)

// 新行為 (Spring Boot 4.0)
map.from(source::method).to(destination::method)
// 如果來源返回 null，則跳過調用

// 明確的 null 處理 (新)
map.from(source::method).always().to(destination::method)
// 無論是否為 null，始終調用 destination.method(value)
```

**已移除方法：** `alwaysApplyingNotNull()` - 使用 `always()` 代替。

**遷移範例：** 查看 [Spring Boot commit 239f384ac0](https://github.com/spring-projects/spring-boot/commit/239f384ac0893d151b89f204886874c6adb00001) 了解 Spring Boot 自身如何適應新 API。

## 依賴和構建變更

### Gradle插件更新

**build.gradle.kts:**

```kotlin
plugins {
    kotlin("jvm") version "2.2.0" // Minimum 2.2.0
    kotlin("plugin.spring") version "2.2.0"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.cyclonedx.bom") version "3.0.0" // Minimum 3.0.0
}
```

### Gradle 中的可選依賴項

可選依賴項 **默認不再包含在 uber jar 中**。

**build.gradle.kts (顯式包含可選依賴項):**

```kotlin
tasks.bootJar {
    includeOptional = true // If needed
}
```

### Spring 重試 → Spring Framework Core 重試

Spring Boot 4.0 移除了對 Spring Retry 的依賴管理（產品組合正在遷移到 Spring Framework 7.0 core retry）。

**遷移選項 1：使用 Spring Framework Core Retry（推薦）**

```kotlin
// 使用內建的 Spring Framework 重試
import org.springframework.core.retry.RetryTemplate
import org.springframework.core.retry.support.RetryTemplateBuilder

@Configuration
class RetryConfig {
    @Bean
    fun retryTemplate(): RetryTemplate {
        return RetryTemplateBuilder()
            .maxAttempts(3)
            .fixedBackoff(1000)
            .build()
    }
}
```

**遷移選項 2：明確指定 Spring Retry 版本（臨時）**

**libs.versions.toml:**

```toml
[versions]
spring-retry = "2.0.5" # 需要明確指定版本

[libraries]
spring-retry = { module = "org.springframework.retry:spring-retry", version.ref = "spring-retry" }
```

**計劃遷移到 Spring Framework 核心重試。**

### Spring 授權伺服器

現在是 Spring Security 的一部分 - 明確的版本管理已移除。

**libs.versions.toml (before - Spring Boot 3.x):**

```toml
[versions]
spring-authorization-server = "1.3.0" # No longer works

[libraries]
spring-security-oauth2-authorization-server = { module = "org.springframework.security:spring-security-oauth2-authorization-server", version.ref = "spring-authorization-server" }
```

**遷移 (Spring Boot 4.0):**

```toml
[versions]
spring-security = "7.0.0" # 使用 Spring Security 版本代替

[libraries]
# Managed by spring-security.version property, not separate
spring-security-oauth2-authorization-server = { module = "org.springframework.security:spring-security-oauth2-authorization-server", version.ref = "spring-security" }
```

或依賴 Spring Boot 依賴管理（建議）：

```kotlin
dependencies {
    implementation("org.springframework.security:spring-security-oauth2-authorization-server")
    // Version managed by Spring Boot 4.0
}
```

### Elasticsearch 客戶端變更

#### 低階客戶端替換

**已棄用的低階 `RestClient` → 新的 `Rest5Client`:**

**注意:** 高階客戶端（`ElasticsearchClient` 和 Spring Data 的 `ReactiveElasticsearchClient`）**保持不變**，並已在內部更新以使用新的低階客戶端。

**Imports:**

```kotlin
// 舊 (Spring Boot 3.x)
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer

// 新 (Spring Boot 4.0)
import co.elastic.clients.transport.rest_client.Rest5Client
import co.elastic.clients.transport.rest_client.Rest5ClientBuilder
import org.springframework.boot.autoconfigure.elasticsearch.Rest5ClientBuilderCustomizer
```

**Configuration:**

```kotlin
@Configuration
class ElasticsearchConfig {

    // 舊
    // @Bean
    // fun restClientCustomizer(): RestClientBuilderCustomizer {
    //     return RestClientBuilderCustomizer { builder ->
    //         builder.setRequestConfigCallback { config ->
    //             config.setConnectTimeout(5000)
    //         }
    //     }
    // }

    // 新
    @Bean
    fun rest5ClientCustomizer(): Rest5ClientBuilderCustomizer {
        return Rest5ClientBuilderCustomizer { builder ->
            builder.setRequestConfigCallback { config ->
                config.setConnectTimeout(5000)
            }
        }
    }
}
```

**依賴整合:**

Sniffer 現在包含在 `co.elastic.clients:elasticsearch-java` 模組中。

**libs.versions.toml:**

```toml
[libraries]
# 移除這些 - 不再管理
# elasticsearch-rest-client = { module = "org.elasticsearch.client:elasticsearch-rest-client", version = "..." }
# elasticsearch-rest-client-sniffer = { module = "org.elasticsearch.client:elasticsearch-rest-client-sniffer", version = "..." }

# 使用單一依賴 (包含 sniffer)
elasticsearch-java = { module = "co.elastic.clients:elasticsearch-java", version = "8.x.x" }
```

### Hibernate 相依性變更

**libs.versions.toml:**

```toml
[libraries]
# Renamed module (hibernate-jpamodelgen replaced by hibernate-processor)
hibernate-processor = { module = "org.hibernate.orm:hibernate-processor", version.ref = "hibernate" }

# These artifacts are NO LONGER PUBLISHED by Hibernate:
# hibernate-proxool - discontinued by Hibernate project
# hibernate-vibur - discontinued by Hibernate project
# Remove any dependencies on these modules
```

**注意：** `hibernate-jpamodelgen` 工件仍然存在，但已被棄用。請改用 `hibernate-processor` 。

## 配置屬性更改

### MongoDB 屬性重組

**重大重組:** 非 Spring Data 屬性已移至 `spring.mongodb.*`：
**application.yml migration:**

```yaml
# 舊 (Spring Boot 3.x)
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/mydb
      database: mydb
      host: localhost
      port: 27017
      username: user
      password: pass
      authentication-database: admin
      replica-set-name: rs0
      additional-hosts:
        - host1:27017
        - host2:27017
      ssl:
        enabled: true
        bundle: my-bundle
      representation:
        uuid: STANDARD

management:
  health:
    mongo:
      enabled: true
  metrics:
    mongo:
      command:
        enabled: true
      connectionpool:
        enabled: true

# 新 (Spring Boot 4.0)
spring:
  mongodb:
    uri: mongodb://localhost:27017/mydb
    database: mydb
    host: localhost
    port: 27017
    username: user
    password: pass
    authentication-database: admin
    replica-set-name: rs0
    additional-hosts:
      - host1:27017
      - host2:27017
    ssl:
      enabled: true
      bundle: my-bundle
    representation:
      uuid: STANDARD # Explicit configuration now required

  data:
    mongodb:
      # Spring Data-specific properties remain here
      auto-index-creation: true
      field-naming-strategy: org.springframework.data.mapping.model.SnakeCaseFieldNamingStrategy
      gridfs:
        bucket: fs
        database: gridfs-db
      repositories:
        type: auto
      representation:
        big-decimal: DECIMAL128 # Explicit configuration now required

management:
  health:
    mongodb: # Renamed from "mongo"
      enabled: true
  metrics:
    mongodb: # Renamed from "mongo"
      command:
        enabled: true
      connectionpool:
        enabled: true
```

**主要變更:**

- **UUID 表示法**: **必填** - 沒有提供預設值，必須明確配置 `spring.mongodb.representation.uuid` (例如，`STANDARD`、`JAVA_LEGACY`、`PYTHON_LEGACY`、`C_SHARP_LEGACY`)
- **BigDecimal 表示法**: **必填** - 沒有提供預設值，必須明確配置 `spring.data.mongodb.representation.big-decimal` (例如，`DECIMAL128`、`STRING`)
- **管理屬性**: `mongo` → `mongodb`
- **未配置這些屬性將導致在持久化 UUID 或 BigDecimal 值時發生運行時錯誤**

### Spring Session 屬性重命名

**application.yml 遷移:**

```yaml
# 舊 (Spring Boot 3.x)
spring:
  session:
    redis:
      namespace: myapp:session
      flush-mode: on-save
    mongodb:
      collection-name: sessions

# 新 (Spring Boot 4.0)
spring:
  session:
    data:
      redis:
        namespace: myapp:session
        flush-mode: on-save
      mongodb:
        collection-name: sessions
```

### Persistence Module Property Change 持久化模組屬性更改

**application.yml 遷移:**

```yaml
# 舊 (Spring Boot 3.x)
spring:
  dao:
    exceptiontranslation:
      enabled: true

# 新 (Spring Boot 4.0)
spring:
  persistence:
    exceptiontranslation:
      enabled: true
```

## Web 框架變更

### 靜態資源位置

`PathRequest#toStaticResources()` 現在默認包括 `/fonts/**`。

**安全性配置（如有需要，可排除字體）：**

```kotlin
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.boot.autoconfigure.security.StaticResourceLocation

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            authorizeHttpRequests {
                // Exclude fonts if needed
                authorize(PathRequest.toStaticResources()
                    .atCommonLocations()
                    .excluding(StaticResourceLocation.FONTS), permitAll)
                authorize(anyRequest, authenticated)
            }
        }
        return http.build()
    }
}
```

### HttpMessageConverters 棄用

`HttpMessageConverters` 因框架改進（合併的客戶端/服務器轉換器）而被棄用。

**遷移:**

```kotlin
// 舊 (Spring Boot 3.x)
import org.springframework.boot.autoconfigure.http.HttpMessageConverters
import org.springframework.context.annotation.Bean

@Configuration
class WebConfig {
    @Bean
    fun customConverters(): HttpMessageConverters {
        return HttpMessageConverters(MyCustomConverter())
    }
}

// 新 (Spring Boot 4.0)
import org.springframework.boot.autoconfigure.http.client.ClientHttpMessageConvertersCustomizer
import org.springframework.boot.autoconfigure.http.server.ServerHttpMessageConvertersCustomizer

@Configuration
class WebConfig {

    // Separate client and server converters
    @Bean
    fun clientConvertersCustomizer(): ClientHttpMessageConvertersCustomizer {
        return ClientHttpMessageConvertersCustomizer { converters ->
            converters.add(MyCustomClientConverter())
        }
    }

    @Bean
    fun serverConvertersCustomizer(): ServerHttpMessageConvertersCustomizer {
        return ServerHttpMessageConvertersCustomizer { converters ->
            converters.add(MyCustomServerConverter())
        }
    }
}
```

### Jersey 和 Jackson 3 不兼容

**Jersey 4.0 限制:** Spring Boot 4.0 支持 Jersey 4.0，但 **尚不支持 Jackson 3**。

**解決方案:** 使用 `spring-boot-jackson2` 兼容模組 **可以替代或與** `spring-boot-jackson` 一起使用：

**libs.versions.toml:**

```toml
[libraries]
spring-boot-starter-jersey = { module = "org.springframework.boot:spring-boot-starter-jersey", version.ref = "springBoot" }
spring-boot-jackson2 = { module = "org.springframework.boot:spring-boot-jackson2", version.ref = "springBoot" }
# Optional: Keep Jackson 3 for non-Jersey parts of application
spring-boot-jackson = { module = "org.springframework.boot:spring-boot-jackson", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    implementation(libs.spring.boot.starter.jersey)
    implementation(libs.spring.boot.jackson2) // Required for Jersey JSON processing
    // Optional: Use Jackson 3 elsewhere in application
    // implementation(libs.spring.boot.jackson)
}
```

**注意：** 如果您的應用程序僅使用 Jersey，您可以完全用 Jackson 2 兼容模組替換 Jackson 3。

## 訊息傳遞框架變更

### Kafka Streams 自訂工具替代方案

**Deprecated `StreamBuilderFactoryBeanCustomizer` → `StreamsBuilderFactoryBeanConfigurer`:**

```kotlin
// 舊 (Spring Boot 3.x)
import org.springframework.boot.autoconfigure.kafka.StreamsBuilderFactoryBeanCustomizer

@Configuration
class KafkaStreamsConfig {
    @Bean
    fun streamsCustomizer(): StreamBuilderFactoryBeanCustomizer {
        return StreamBuilderFactoryBeanCustomizer { factoryBean ->
            factoryBean.setKafkaStreamsCustomizer { streams ->
                // Custom config
            }
        }
    }
}

// 新 (Spring Boot 4.0)
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer

@Configuration
class KafkaStreamsConfig {
    @Bean
    fun streamsConfigurer(): StreamsBuilderFactoryBeanConfigurer {
        return StreamsBuilderFactoryBeanConfigurer { factoryBean ->
            factoryBean.setKafkaStreamsCustomizer { streams ->
                // Custom config
            }
        }
    }
}
```

**注意：** 新的配置器實現了 `Ordered`，默認值為 `0`。

### Kafka 重試屬性變更

**application.yml 遷移:**

```yaml
# 舊 (Spring Boot 3.x)
spring:
  kafka:
    retry:
      topic:
        backoff:
          random: true

# 新 (Spring Boot 4.0)
spring:
  kafka:
    retry:
      topic:
        backoff:
          jitter: 0.5 # More flexible than boolean
```

### RabbitMQ 重試自訂拆分

**Spring AMQP 從 Spring Retry 移動到 Spring Framework 核心重試**，並拆分了自訂器：

```kotlin
// 舊 (Spring Boot 3.x)
import org.springframework.boot.autoconfigure.amqp.RabbitRetryTemplateCustomizer

@Configuration
class RabbitConfig {
    @Bean
    fun retryCustomizer(): RabbitRetryTemplateCustomizer {
        return RabbitRetryTemplateCustomizer { template ->
            // Applies to both RabbitTemplate and listeners
        }
    }
}

// 新 (Spring Boot 4.0)
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateRetrySettingsCustomizer
import org.springframework.boot.autoconfigure.amqp.RabbitListenerRetrySettingsCustomizer

@Configuration
class RabbitConfig {

    // For RabbitTemplate operations
    @Bean
    fun templateRetryCustomizer(): RabbitTemplateRetrySettingsCustomizer {
        return RabbitTemplateRetrySettingsCustomizer { settings ->
            settings.maxAttempts = 5
        }
    }

    // For message listeners
    @Bean
    fun listenerRetryCustomizer(): RabbitListenerRetrySettingsCustomizer {
        return RabbitListenerRetrySettingsCustomizer { settings ->
            settings.maxAttempts = 3
        }
    }
}
```

## 測試框架變更

### Mockito 整合移除

`MockitoTestExecutionListener` 已移除（在 3.4 中已棄用）。

**遷移到 MockitoExtension:**

````kotlin
// 舊 (Spring Boot 3.x)
import org.springframework.boot.test.context.SpringBootTest
import org.mockito.Mock
import org.mockito.Captor

@SpringBootTest
class MyServiceTest {
    @Mock
    private lateinit var repository: MyRepository

    @Captor
    private lateinit var captor: ArgumentCaptor<String>
}

```kotlin
// 新 (Spring Boot 4.0)
import org.springframework.boot.test.context.SpringBootTest
import org.mockito.Mock
import org.mockito.Captor
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith

@SpringBootTest
@ExtendWith(MockitoExtension::class) // Explicit extension required
class MyServiceTest {
    @Mock
    private lateinit var repository: MyRepository

    @Captor
    private lateinit var captor: ArgumentCaptor<String>
}
````

### @SpringBootTest 變更

`@SpringBootTest` 不再自動提供 **MockMVC**、**WebTestClient** 或 **TestRestTemplate**。

#### MockMVC 配置

```kotlin
// 舊 (Spring Boot 3.x)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc // Available automatically
}

// 新 (Spring Boot 4.0)
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.HtmlUnit

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // Explicit annotation required
class ControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc
}

// HtmlUnit configuration moved to annotation attribute
@AutoConfigureMockMvc(
    htmlUnit = HtmlUnit(webClient = false, webDriver = false)
)
```

#### WebTestClient 配置

```kotlin
// 舊 (Spring Boot 3.x)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebFluxTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient // Available automatically
}

// 新 (Spring Boot 4.0)
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient // Explicit annotation required
class WebFluxTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient
}
```

#### TestRestTemplate → RestTestClient (Recommended)

**Spring Boot 4.0 introduces `RestTestClient`** as modern replacement for `TestRestTemplate`.

```kotlin
// 舊方法 (仍可使用註解)
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureTestRestTemplate
import org.springframework.boot.test.web.client.TestRestTemplate

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate // 在 4.0 中需要
class RestApiTest {
    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate
}

// 新建議方法
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureRestTestClient
import org.springframework.boot.resttestclient.RestTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient // New annotation
class RestApiTest {
    @Autowired
    private lateinit var restTestClient: RestTestClient

    @Test
    fun testEndpoint() {
        val response = restTestClient.get()
            .uri("/api/users")
            .retrieve()
            .toEntity<List<User>>()

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }
}
```

**TestRestTemplate 套件變更 (如果仍在使用):**

**重要:** 如果繼續使用 `TestRestTemplate`，您必須：

1. 添加 `spring-boot-resttestclient` 測試依賴
2. **更新套件導入** (類已移至新套件)

**libs.versions.toml:**

```toml
[libraries]
spring-boot-resttestclient = { module = "org.springframework.boot:spring-boot-resttestclient", version.ref = "springBoot" }
```

**build.gradle.kts:**

```kotlin
dependencies {
    testImplementation(libs.spring.boot.resttestclient)
}
```

**更新套件導入 (必須):**

```kotlin
// 舊套件導入 - 會導致編譯失敗
// import org.springframework.boot.test.web.client.TestRestTemplate

// 新套件導入 - Spring Boot 4.0 中必須
import org.springframework.boot.resttestclient.TestRestTemplate
import org.springframework.boot.resttestclient.TestRestTemplate
```

### @PropertyMapping 註解遷移

```kotlin
// 舊 (Spring Boot 3.x)
import org.springframework.boot.test.autoconfigure.properties.PropertyMapping
import org.springframework.boot.test.autoconfigure.properties.Skip

// 新 (Spring Boot 4.0)
import org.springframework.boot.test.context.PropertyMapping
import org.springframework.boot.test.context.PropertyMapping.Skip
```

## Production-Ready 功能和模組

### 健康、指標和可觀察性模組

Spring Boot 4.0 將生產就緒功能模組化為專注的模組：

**libs.versions.toml:**

```toml
[libraries]
# Health monitoring
spring-boot-health = { module = "org.springframework.boot:spring-boot-health", version.ref = "springBoot" }

# Micrometer metrics
spring-boot-micrometer-metrics = { module = "org.springframework.boot:spring-boot-micrometer-metrics", version.ref = "springBoot" }
spring-boot-micrometer-metrics-test = { module = "org.springframework.boot:spring-boot-micrometer-metrics-test", version.ref = "springBoot" }

# Micrometer observation
spring-boot-micrometer-observation = { module = "org.springframework.boot:spring-boot-micrometer-observation", version.ref = "springBoot" }

# Distributed tracing
spring-boot-micrometer-tracing = { module = "org.springframework.boot:spring-boot-micrometer-tracing", version.ref = "springBoot" }
spring-boot-micrometer-tracing-test = { module = "org.springframework.boot:spring-boot-micrometer-tracing-test", version.ref = "springBoot" }
spring-boot-micrometer-tracing-brave = { module = "org.springframework.boot:spring-boot-micrometer-tracing-brave", version.ref = "springBoot" }
spring-boot-micrometer-tracing-opentelemetry = { module = "org.springframework.boot:spring-boot-micrometer-tracing-opentelemetry", version.ref = "springBoot" }

# OpenTelemetry integration
spring-boot-opentelemetry = { module = "org.springframework.boot:spring-boot-opentelemetry", version.ref = "springBoot" }

# Zipkin reporter
spring-boot-zipkin = { module = "org.springframework.boot:spring-boot-zipkin", version.ref = "springBoot" }
```

**build.gradle.kts (example observability stack):**

```kotlin
dependencies {
    // Actuator with metrics and tracing
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.micrometer.observation)
    implementation(libs.spring.boot.micrometer.tracing.opentelemetry)
    implementation(libs.spring.boot.opentelemetry)

    // Test support
    testImplementation(libs.spring.boot.micrometer.metrics.test)
    testImplementation(libs.spring.boot.micrometer.tracing.test)
}
```

**注意:** 大多數使用 starters 的應用程式（例如 `spring-boot-starter-actuator`）不需要直接聲明這些模組。對於細粒度控制，請使用直接模組依賴。

## Actuator 變更

### 健康探針預設啟用

現在預設啟用存活探測和就緒探測。

**application.yml (如果需要禁用):**

```yaml
management:
  endpoint:
    health:
      probes:
        enabled: false # 如果不使用 Kubernetes 探針，請禁用
```

**自動暴露:**

- `/actuator/health/liveness`
- `/actuator/health/readiness`

## 建置配置

### Kotlin 編譯器配置

**build.gradle.kts:**

```kotlin
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.0" // Minimum 2.2.0
    kotlin("plugin.spring") version "2.2.0"
    kotlin("plugin.jpa") version "2.2.0"
    id("org.springframework.boot") version "4.0.0"
    id("io.spring.dependency-management") version "1.1.7"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21) // Or 17, 25
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll(
            "-Xjsr305=strict", // Strict null-safety
            "-Xemit-jvm-type-annotations" // Emit type annotations
        )
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21" // Match Java toolchain
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

### Java 預覽功能 (如果使用 Java 25)

**build.gradle.kts:**

```kotlin
tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}
```

## 遷移檢查清單

### 遷移前

- [ ] 升級到最新的 Spring Boot 3.5.x
- [ ] 檢查並修復所有棄用警告
- [ ] 記錄當前依賴版本
- [ ] 執行完整測試套件並驗證綠色構建
- [ ] 查看 [Spring Boot 3.5.x → 4.0 依賴變更](https://docs.spring.io/spring-boot/4.0/appendix/dependency-versions/coordinates.html)

### 核心遷移

- [ ] 更新 `libs.versions.toml` 為 Spring Boot 4.0.0
- [ ] 更新 Kotlin 版本至 2.2.0+
- [ ] 重新命名 starters: `spring-boot-starter-web` → `spring-boot-starter-webmvc` 等
- [ ] 添加技術特定的測試 starters（或暫時使用經典 starters）
- [ ] 移除 Undertow 依賴（如果存在，切換到 Tomcat/Jetty）
- [ ] 移除 `spring-session-hazelcast` / `spring-session-mongodb` 或添加明確版本

### Jackson 3 遷移

- [ ] 更新導入: `com.fasterxml.jackson` → `tools.jackson`
- [ ] 更新異常: `jackson-annotations` 仍使用 `com.fasterxml.jackson.core`
- [ ] 重命名: `@JsonComponent` → `@JacksonComponent`
- [ ] 重命名: `Jackson2ObjectMapperBuilderCustomizer` → `JsonMapperBuilderCustomizer`
- [ ] 更新屬性: `spring.jackson.read.*` → `spring.jackson.json.read.*`
- [ ] 考慮暫時使用 `spring-boot-jackson2` 模組（如果需要）

### 屬性更新

- [ ] MongoDB: `spring.data.mongodb.*` → `spring.mongodb.*` (非 Spring Data 屬性)
- [ ] Session: `spring.session.redis.*` → `spring.session.data.redis.*`
- [ ] Persistence: `spring.dao.exceptiontranslation` → `spring.persistence.exceptiontranslation`
- [ ] Kafka 重試: `backoff.random` → `backoff.jitter`

### 代碼更新

- [ ] 更新包: `BootstrapRegistry` → `org.springframework.boot.bootstrap.BootstrapRegistry`
- [ ] 更新包: `EnvironmentPostProcessor` → `org.springframework.boot.EnvironmentPostProcessor`
- [ ] 更新包: `EntityScan` → `org.springframework.boot.persistence.autoconfigure.EntityScan`
- [ ] 更新: `RestClient` → `Rest5Client` (Elasticsearch)
- [ ] 更新: `StreamBuilderFactoryBeanCustomizer` → `StreamsBuilderFactoryBeanConfigurer` (Kafka)
- [ ] 拆分: `RabbitRetryTemplateCustomizer` → `RabbitTemplateRetrySettingsCustomizer` / `RabbitListenerRetrySettingsCustomizer`
- [ ] 替換: `HttpMessageConverters` → `ClientHttpMessageConvertersCustomizer` / `ServerHttpMessageConvertersCustomizer`
- [ ] 更新: `PropertyMapper` 使用 `.always()` 如果需要處理 null

### 測試更新

- [ ] 為使用 `@Mock` / `@Captor` 的測試添加 `@ExtendWith(MockitoExtension::class)`
- [ ] 為使用 `MockMvc` 的測試添加 `@AutoConfigureMockMvc`
- [ ] 為使用 `WebTestClient` 的測試添加 `@AutoConfigureWebTestClient`
- [ ] 將 `TestRestTemplate` 遷移到 `RestTestClient`（或添加 `@AutoConfigureTestRestTemplate`）
- [ ] 更新: `@PropertyMapping` 導入 → `org.springframework.boot.test.context`

### 構建配置

- [ ] 更新 Gradle 至 8.5+
- [ ] 更新 Gradle CycloneDX 插件至 3.0.0+
- [ ] 檢查 uber jar 中的可選依賴包含
- [ ] 移除 `loaderImplementation = CLASSIC`（如果存在）
- [ ] 移除 `launchScript()` 配置（如果存在）

### 驗證

- [ ] 執行 `./gradlew clean build`
- [ ] 執行完整測試套件
- [ ] 驗證使用 TestContainers 的整合測試
- [ ] 檢查新的 Kotlin 空安全警告
- [ ] 測試 Spring Boot Actuator 端點
- [ ] 驗證健康檢查探針 (`/actuator/health/liveness`, `/actuator/health/readiness`)
- [ ] 使用新默認值進行性能測試

### 遷移後

- [ ] 查看 Spring Boot 4.0 發行說明以了解其他功能
- [ ] 考慮採用新的 Spring Framework 7.0 功能
- [ ] 計劃從經典啟動器遷移（如果使用）
- [ ] 計劃從 `spring-boot-jackson2` 模組遷移（如果使用）
- [ ] 更新 CI/CD 管道以符合 Java 17+ 要求
- [ ] 更新部署清單（Servlet 6.1 容器）

## 常見陷阱

1. **經典啟動器**: 記住這些已被棄用 - 計劃遷移到技術特定的啟動器
2. **Undertow**: 完全移除，無法替代 - 必須使用 Tomcat 或 Jetty
3. **Jackson 3 packages**: 容易忽略 `jackson-annotations` 仍使用舊的 group ID
4. **MongoDB properties**: 許多已移至 `spring.mongodb.*`，但部分仍在 `spring.data.mongodb.*`
5. **Test configuration**: `@SpringBootTest` 不再自動配置 MockMVC/WebTestClient/TestRestTemplate
6. **Kotlin 2.2**: 最低要求 - 舊版本無法使用
7. **Null-safety**: JSpecify 註解可能在 Kotlin 中產生新的警告
8. **PropertyMapper**: 處理 null 的行為變化 - 檢查使用情況
9. **Jersey + Jackson 3**: 不兼容 - 使用 `spring-boot-jackson2` 模組
10. **Health probes**: 現在默認啟用 - 可能影響非 Kubernetes 部署

## 性能考量

- **模組化啟動器**: 使用技術特定的啟動器可以生成更小的 JAR 並加快啟動速度
- **Spring Framework 7**: 核心框架性能提升
- **Jackson 3**: 改進的 JSON 處理性能
- **虛擬線程**: 考慮在 Java 21+ 中啟用 (`spring.threads.virtual.enabled=true`)

## 資源

- [Spring Boot 4.0 遷移指南](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
- [Spring Boot 4.0 發行說明](https://github.com/spring-projects/spring-boot/releases)
- [Spring Framework 7.0 文檔](https://docs.spring.io/spring-framework/reference/)
- [Jackson 3 遷移指南](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md)
- [Kotlin 2.2 發行說明](https://kotlinlang.org/docs/whatsnew22.html)

---
