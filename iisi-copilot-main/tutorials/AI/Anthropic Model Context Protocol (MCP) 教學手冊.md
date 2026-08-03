+++
date = '2026-01-09T18:11:38+08:00'
draft = false
title = 'Anthropic Model Context Protocol (MCP) 教學手冊'
tags = ['教學', 'AI開發']
categories = ['教學']
+++

> **版本**: 1.0  
> **最後更新**: 2026年1月9日  
> **適用於**: Claude Code
> **Created by**: Eric Cheng

# Anthropic Model Context Protocol (MCP) 教學手冊

> **版本資訊**：本手冊基於 MCP 規範版本 2025-11-25  
> **最後更新**：2026 年 1 月  
> **適用對象**：資深軟體開發工程師、系統架構師

## 目錄

- [第一章：MCP 概述與核心概念](#第一章mcp-概述與核心概念)
  - [1.1 什麼是 MCP？](#11-什麼是-mcp)
  - [1.2 為什麼需要 MCP？](#12-為什麼需要-mcp)
  - [1.3 MCP 架構概覽](#13-mcp-架構概覽)
- [第二章：MCP 技術架構深度解析](#第二章mcp-技術架構深度解析)
  - [2.1 分層架構](#21-分層架構)
  - [2.2 資料層協議（Data Layer Protocol）](#22-資料層協議data-layer-protocol)
  - [2.3 MCP 核心原語（Primitives）](#23-mcp-核心原語primitives)
  - [2.4 通知機制（Notifications）](#24-通知機制notifications)
- [第三章：傳輸層深度解析](#第三章傳輸層深度解析)
  - [3.1 STDIO Transport](#31-stdio-transport)
  - [3.2 Streamable HTTP Transport](#32-streamable-http-transport)
- [第四章：實戰開發指南](#第四章實戰開發指南)
  - [4.1 開發環境設置](#41-開發環境設置)
  - [4.2 開發 MCP Server](#42-開發-mcp-server)
  - [4.3 開發 MCP Client](#43-開發-mcp-client)
  - [4.4 整合到 AI 應用](#44-整合到-ai-應用)
- [第五章：完整實戰範例](#第五章完整實戰範例)
  - [5.1 範例一：檔案系統 MCP Server](#51-範例一檔案系統-mcp-server)
  - [5.2 範例二：資料庫查詢 MCP Server](#52-範例二資料庫查詢-mcp-server)
  - [5.3 範例三：API 整合 MCP Server](#53-範例三api-整合-mcp-server)
- [第六章：最佳實踐與設計模式](#第六章最佳實踐與設計模式)
  - [6.1 MCP Server 設計原則](#61-mcp-server-設計原則)
  - [6.2 效能優化](#62-效能優化)
  - [6.3 安全性考量](#63-安全性考量)
  - [6.4 測試策略](#64-測試策略)
- [第七章：進階主題](#第七章進階主題)
  - [7.1 Tasks 實驗性功能](#71-tasks-實驗性功能)
  - [7.2 自訂傳輸層](#72-自訂傳輸層)
  - [7.3 多語言 SDK 比較](#73-多語言-sdk-比較)
  - [7.4 偵錯與監控](#74-偵錯與監控)
- [第八章：疑難排解](#第八章疑難排解)
  - [8.1 常見錯誤與解決方案](#81-常見錯誤與解決方案)
  - [8.2 除錯技巧](#82-除錯技巧)
  - [8.3 錯誤訊息參考](#83-錯誤訊息參考)
- [第九章：實際案例研究](#第九章實際案例研究)
  - [9.1 案例一：企業知識庫 MCP Server](#91-案例一企業知識庫-mcp-server)
  - [9.2 案例二：DevOps 整合 MCP Server](#92-案例二devops-整合-mcp-server)
- [第十章：資源與參考](#第十章資源與參考)
  - [10.1 官方資源](#101-官方資源)
  - [10.2 社群資源](#102-社群資源)
  - [10.3 開發環境建議](#103-開發環境建議)
  - [10.4 版本相容性](#104-版本相容性)
  - [10.5 快速參考](#105-快速參考)
- [附錄：檢查清單（Checklist）](#附錄檢查清單checklist)
  - [A. Server 開發檢查清單](#a-server-開發檢查清單)
  - [B. 部署檢查清單](#b-部署檢查清單)
  - [C. 程式碼審查檢查清單](#c-程式碼審查檢查清單)
  - [D. 故障排除檢查清單](#d-故障排除檢查清單)

---

## 第一章：MCP 概述與核心概念

### 1.1 什麼是 MCP？

#### 1.1.1 MCP 的定義與核心價值

**Model Context Protocol（MCP）** 是由 Anthropic 開發的一個開放標準協議，旨在為 AI 應用程式提供一個統一的方式來連接各種資料來源、工具和服務。

> **核心價值**：MCP 就像是 AI 世界的「USB-C」—— 一個標準化的介面，讓任何 AI 應用程式都能輕鬆連接到任何相容的資料來源或工具。

```
┌─────────────────────────────────────────────────────────────────┐
│                        MCP 核心價值                              │
├─────────────────────────────────────────────────────────────────┤
│  ✓ 標準化：統一的協議規範，降低整合成本                           │
│  ✓ 可重用：一次開發，多處使用                                    │
│  ✓ 安全性：內建安全機制，保護敏感資料                             │
│  ✓ 擴展性：模組化設計，易於擴展功能                               │
└─────────────────────────────────────────────────────────────────┘
```

#### 1.1.2 USB-C 類比解釋

想像在 USB-C 出現之前，每個設備都有自己專屬的連接線：

```mermaid
graph LR
    subgraph "沒有 MCP 之前"
        A1[AI 應用 1] --> |專用 API| D1[資料庫]
        A1 --> |專用連接器| F1[檔案系統]
        A2[AI 應用 2] --> |另一套 API| D1
        A2 --> |另一套連接器| F1
    end
```

```mermaid
graph LR
    subgraph "有 MCP 之後"
        A1[AI 應用 1] --> |MCP| M[MCP Server]
        A2[AI 應用 2] --> |MCP| M
        M --> D1[資料庫]
        M --> F1[檔案系統]
        M --> E1[外部 API]
    end
```

#### 1.1.3 MCP 在 AI 應用生態系統中的定位

| 層級       | 說明                    | 範例                                  |
| ---------- | ----------------------- | ------------------------------------- |
| **應用層** | AI 應用程式（MCP Host） | Claude Desktop、VS Code、自訂 AI 應用 |
| **協議層** | MCP 協議                | 標準化的通訊規範                      |
| **服務層** | MCP Server              | 資料庫連接器、檔案系統服務、API 整合  |
| **資料層** | 底層資源                | 資料庫、檔案、外部服務                |

---

### 1.2 為什麼需要 MCP？

#### 1.2.1 解決的核心問題

**問題一：N × M 整合問題**

沒有 MCP 時，如果有 N 個 AI 應用和 M 個資料來源，需要開發 N × M 個整合。

```
傳統方式：N × M = 大量重複工作
┌─────────┐     ┌─────────┐
│ AI App 1│────→│ 資料源 1 │
│         │────→│ 資料源 2 │
│         │────→│ 資料源 3 │
└─────────┘     └─────────┘
┌─────────┐     ┌─────────┐
│ AI App 2│────→│ 資料源 1 │ （重複開發）
│         │────→│ 資料源 2 │
│         │────→│ 資料源 3 │
└─────────┘     └─────────┘

MCP 方式：N + M = 大幅減少工作量
┌─────────┐          ┌─────────┐
│ AI App 1│──┐   ┌──→│ 資料源 1 │
│         │  │   │   └─────────┘
└─────────┘  │   │   ┌─────────┐
┌─────────┐  ├──MCP──→│ 資料源 2 │
│ AI App 2│  │   │   └─────────┘
│         │──┘   │   ┌─────────┐
└─────────┘      └──→│ 資料源 3 │
                     └─────────┘
```

**問題二：安全性與權限管理分散**

每個整合都需要獨立處理認證授權，MCP 提供統一的安全框架。

**問題三：維護成本高昂**

API 變更時需要更新多處程式碼，MCP 的抽象層降低了這種耦合。

#### 1.2.2 對不同角色的價值

| 角色         | MCP 帶來的價值                                                       |
| ------------ | -------------------------------------------------------------------- |
| **開發者**   | • 減少重複開發工作<br>• 標準化的開發模式<br>• 豐富的現成 Server 可用 |
| **AI 應用**  | • 快速獲取外部資料<br>• 動態發現可用工具<br>• 統一的錯誤處理         |
| **終端用戶** | • 更強大的 AI 能力<br>• 更好的整合體驗<br>• 資料安全有保障           |
| **企業**     | • 降低整合成本<br>• 統一的安全管理<br>• 更好的可維護性               |

#### 1.2.3 實際應用場景範例

```
場景 1：企業知識庫整合
┌────────────────────────────────────────────────────────┐
│ Claude Desktop                                          │
│    ↓ MCP                                               │
│ ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│ │ Confluence   │  │ SharePoint   │  │ 內部 Wiki    │   │
│ │ MCP Server   │  │ MCP Server   │  │ MCP Server   │   │
│ └──────────────┘  └──────────────┘  └──────────────┘   │
└────────────────────────────────────────────────────────┘

場景 2：開發者工具鏈整合
┌────────────────────────────────────────────────────────┐
│ VS Code + Claude                                        │
│    ↓ MCP                                               │
│ ┌──────────────┐  ┌──────────────┐  ┌──────────────┐   │
│ │   GitHub     │  │    Jira      │  │  Database    │   │
│ │ MCP Server   │  │ MCP Server   │  │ MCP Server   │   │
│ └──────────────┘  └──────────────┘  └──────────────┘   │
└────────────────────────────────────────────────────────┘
```

---

### 1.3 MCP 架構概覽

#### 1.3.1 Client-Server 架構說明

MCP 採用經典的 Client-Server 架構，但有其獨特的角色定義：

```mermaid
graph TB
    subgraph "MCP 架構"
        Host[MCP Host<br/>如 Claude Desktop]
        Client[MCP Client<br/>協議客戶端]
        Server[MCP Server<br/>功能提供者]
        Resource[(資源<br/>資料庫/檔案/API)]

        Host --> |包含| Client
        Client <--> |MCP Protocol| Server
        Server --> |存取| Resource
    end
```

#### 1.3.2 MCP Host、Client、Server 的角色與關係

| 角色       | 說明         | 職責                                                                 | 範例                              |
| ---------- | ------------ | -------------------------------------------------------------------- | --------------------------------- |
| **Host**   | 宿主應用程式 | • 提供使用者介面<br>• 管理 Client 生命週期<br>• 處理使用者授權       | Claude Desktop、VS Code、自訂應用 |
| **Client** | 協議客戶端   | • 與 Server 建立連接<br>• 發送請求<br>• 處理回應                     | 通常內嵌於 Host                   |
| **Server** | 功能提供者   | • 暴露 Tools/Resources/Prompts<br>• 處理客戶端請求<br>• 存取底層資源 | 檔案系統 Server、資料庫 Server    |

**關係圖解**：

```
┌─────────────────────────────────────────────────────────────────┐
│                         MCP Host                                 │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │                      MCP Client                          │    │
│  │  • 連接管理                                              │    │
│  │  • 請求/回應處理                                         │    │
│  │  • 能力協商                                              │    │
│  └─────────────────────────────────────────────────────────┘    │
│              ↑                    ↑                    ↑         │
│              │ MCP Protocol       │                    │         │
│              ↓                    ↓                    ↓         │
│     ┌────────────┐      ┌────────────┐      ┌────────────┐      │
│     │ Server A   │      │ Server B   │      │ Server C   │      │
│     │ (檔案系統) │      │ (資料庫)   │      │ (GitHub)   │      │
│     └────────────┘      └────────────┘      └────────────┘      │
└─────────────────────────────────────────────────────────────────┘
```

#### 1.3.3 本地伺服器 vs 遠端伺服器的差異

| 特性         | 本地伺服器（Local Server） | 遠端伺服器（Remote Server） |
| ------------ | -------------------------- | --------------------------- |
| **部署位置** | 與 Host 同一機器           | 獨立部署的伺服器            |
| **傳輸方式** | STDIO（標準輸入/輸出）     | HTTP/SSE                    |
| **啟動方式** | Host 直接啟動進程          | 獨立運行，透過網路連接      |
| **適用場景** | 個人工具、本地資源存取     | 團隊共享、雲端服務          |
| **安全考量** | 繼承本地權限               | 需要認證授權機制            |
| **效能**     | 低延遲、高效能             | 網路延遲、需考慮可用性      |

**選擇建議**：

```
┌─────────────────────────────────────────────────────────────────┐
│                    選擇本地 vs 遠端伺服器                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  選擇本地伺服器當：                                               │
│  ✓ 存取本地檔案系統                                              │
│  ✓ 需要低延遲回應                                                │
│  ✓ 個人使用場景                                                  │
│  ✓ 開發測試階段                                                  │
│                                                                  │
│  選擇遠端伺服器當：                                               │
│  ✓ 多人共享服務                                                  │
│  ✓ 需要集中管理                                                  │
│  ✓ 整合雲端服務                                                  │
│  ✓ 生產環境部署                                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

> **💡 實務建議**：開發階段建議使用本地伺服器進行快速迭代，確認功能後再部署為遠端伺服器供團隊使用。

---

## 第二章：MCP 技術架構深度解析

### 2.1 分層架構

MCP 採用清晰的分層架構設計，將關注點分離：

```mermaid
graph TB
    subgraph "MCP 分層架構"
        App[應用層<br/>AI 應用程式]
        Data[資料層<br/>JSON-RPC 2.0 訊息]
        Transport[傳輸層<br/>STDIO / HTTP]
        Physical[實體層<br/>進程 / 網路]

        App --> Data
        Data --> Transport
        Transport --> Physical
    end
```

#### 2.1.1 資料層（Data Layer）詳解

資料層負責定義訊息格式和協議語義：

| 元素         | 說明         | 範例                      |
| ------------ | ------------ | ------------------------- |
| **訊息格式** | JSON-RPC 2.0 | 請求、回應、通知          |
| **生命週期** | 連接狀態管理 | 初始化、就緒、關閉        |
| **能力協商** | 功能探索     | 支援的方法、版本          |
| **原語定義** | 核心抽象     | Tools、Resources、Prompts |

#### 2.1.2 傳輸層（Transport Layer）詳解

傳輸層負責訊息的實際傳遞：

| 傳輸方式       | 協議                           | 特點                 |
| -------------- | ------------------------------ | -------------------- |
| **STDIO**      | 標準輸入/輸出                  | 本地進程通訊、低延遲 |
| **HTTP + SSE** | HTTP POST + Server-Sent Events | 遠端通訊、支援串流   |

#### 2.1.3 層與層之間的互動關係

```
┌─────────────────────────────────────────────────────────────────┐
│                        資料層 ←→ 傳輸層 互動                      │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1. 序列化                                                       │
│     資料層物件 ──JSON.stringify──→ 傳輸層字串                     │
│                                                                  │
│  2. 傳輸                                                         │
│     傳輸層字串 ──STDIO/HTTP──→ 對端                               │
│                                                                  │
│  3. 反序列化                                                     │
│     傳輸層字串 ──JSON.parse──→ 資料層物件                         │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### 2.2 資料層協議（Data Layer Protocol）

#### 2.2.1 JSON-RPC 2.0 基礎

MCP 使用 JSON-RPC 2.0 作為訊息協議，包含三種訊息類型：

**請求（Request）**：

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "read_file",
    "arguments": {
      "path": "/path/to/file.txt"
    }
  }
}
```

**回應（Response）**：

```json
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "檔案內容..."
      }
    ]
  }
}
```

**通知（Notification）**：

```json
{
  "jsonrpc": "2.0",
  "method": "notifications/tools/list_changed"
}
```

#### 2.2.2 生命週期管理（Lifecycle Management）

MCP 連接的生命週期包含以下階段：

```mermaid
stateDiagram-v2
    [*] --> Initializing: 連接建立
    Initializing --> Initialized: initialize 成功
    Initialized --> Ready: initialized 通知
    Ready --> Ready: 正常操作
    Ready --> Closing: 關閉請求
    Closing --> [*]: 連接關閉

    Initializing --> [*]: 初始化失敗
    Ready --> [*]: 連接錯誤
```

**初始化流程**：

```
Client                              Server
   │                                   │
   │──────── initialize ──────────────→│
   │         {protocolVersion,         │
   │          capabilities,            │
   │          clientInfo}              │
   │                                   │
   │←─────── initialize result ────────│
   │         {protocolVersion,         │
   │          capabilities,            │
   │          serverInfo}              │
   │                                   │
   │──────── initialized ─────────────→│
   │         (notification)            │
   │                                   │
   │         === 就緒狀態 ===           │
   │                                   │
```

**初始化請求範例**：

```json
// Client → Server
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "initialize",
  "params": {
    "protocolVersion": "2025-11-25",
    "capabilities": {
      "roots": {
        "listChanged": true
      },
      "sampling": {}
    },
    "clientInfo": {
      "name": "MyApp",
      "version": "1.0.0"
    }
  }
}

// Server → Client
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "protocolVersion": "2025-11-25",
    "capabilities": {
      "tools": {
        "listChanged": true
      },
      "resources": {
        "subscribe": true,
        "listChanged": true
      },
      "prompts": {
        "listChanged": true
      }
    },
    "serverInfo": {
      "name": "FileSystemServer",
      "version": "1.0.0"
    }
  }
}
```

#### 2.2.3 能力協商機制（Capability Negotiation）

能力協商允許 Client 和 Server 宣告各自支援的功能：

**Client 能力**：

| 能力           | 說明                 |
| -------------- | -------------------- |
| `roots`        | 支援工作區根目錄功能 |
| `sampling`     | 支援 LLM 取樣請求    |
| `experimental` | 實驗性功能支援       |

**Server 能力**：

| 能力           | 說明               |
| -------------- | ------------------ |
| `tools`        | 提供可呼叫的工具   |
| `resources`    | 提供可讀取的資源   |
| `prompts`      | 提供可用的提示模板 |
| `logging`      | 支援日誌功能       |
| `experimental` | 實驗性功能支援     |

---

### 2.3 MCP 核心原語（Primitives）

MCP 定義了多種原語（Primitives），作為 Client 與 Server 之間互動的基本單位：

```mermaid
graph TB
    subgraph "Server 端原語"
        Tools[Tools<br/>可執行函數]
        Resources[Resources<br/>資料來源]
        Prompts[Prompts<br/>模板]
    end

    subgraph "Client 端原語"
        Sampling[Sampling<br/>LLM 請求]
        Elicitation[Elicitation<br/>用戶互動]
        Logging[Logging<br/>日誌]
    end

    subgraph "通用原語"
        Tasks[Tasks<br/>持久化任務]
    end
```

#### 2.3.1 Server 端原語：Tools

**Tools** 是 Server 暴露給 Client 的可執行函數，允許 AI 執行特定操作。

**Tool 定義結構**：

```json
{
  "name": "read_file",
  "description": "讀取指定路徑的檔案內容",
  "inputSchema": {
    "type": "object",
    "properties": {
      "path": {
        "type": "string",
        "description": "檔案的完整路徑"
      }
    },
    "required": ["path"]
  }
}
```

**列出工具（tools/list）**：

```json
// Request
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}

// Response
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "tools": [
      {
        "name": "read_file",
        "description": "讀取檔案內容",
        "inputSchema": { ... }
      },
      {
        "name": "write_file",
        "description": "寫入檔案內容",
        "inputSchema": { ... }
      }
    ]
  }
}
```

**呼叫工具（tools/call）**：

```json
// Request
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "read_file",
    "arguments": {
      "path": "/home/user/document.txt"
    }
  }
}

// Response
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "這是檔案的內容..."
      }
    ],
    "isError": false
  }
}
```

#### 2.3.2 Server 端原語：Resources

**Resources** 代表 Server 可以提供的資料來源，可以是檔案、資料庫記錄、API 回應等。

**Resource 定義結構**：

```json
{
  "uri": "file:///path/to/document.txt",
  "name": "document.txt",
  "description": "專案說明文件",
  "mimeType": "text/plain"
}
```

**列出資源（resources/list）**：

```json
// Request
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "resources/list"
}

// Response
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "resources": [
      {
        "uri": "file:///docs/readme.md",
        "name": "README",
        "mimeType": "text/markdown"
      },
      {
        "uri": "db://users/schema",
        "name": "Users Schema",
        "mimeType": "application/json"
      }
    ]
  }
}
```

**讀取資源（resources/read）**：

```json
// Request
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "resources/read",
  "params": {
    "uri": "file:///docs/readme.md"
  }
}

// Response
{
  "jsonrpc": "2.0",
  "id": 2,
  "result": {
    "contents": [
      {
        "uri": "file:///docs/readme.md",
        "mimeType": "text/markdown",
        "text": "# README\n\n這是專案說明..."
      }
    ]
  }
}
```

#### 2.3.3 Server 端原語：Prompts

**Prompts** 是預定義的可重用模板，可以包含動態參數。

**Prompt 定義結構**：

```json
{
  "name": "code_review",
  "description": "程式碼審查模板",
  "arguments": [
    {
      "name": "language",
      "description": "程式語言",
      "required": true
    },
    {
      "name": "code",
      "description": "要審查的程式碼",
      "required": true
    }
  ]
}
```

**取得提示（prompts/get）**：

```json
// Request
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "prompts/get",
  "params": {
    "name": "code_review",
    "arguments": {
      "language": "Python",
      "code": "def hello(): print('Hello')"
    }
  }
}

// Response
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "description": "Python 程式碼審查",
    "messages": [
      {
        "role": "user",
        "content": {
          "type": "text",
          "text": "請審查以下 Python 程式碼：\n\ndef hello(): print('Hello')\n\n請提供改進建議。"
        }
      }
    ]
  }
}
```

#### 2.3.4 Client 端原語：Sampling

**Sampling** 允許 Server 請求 Client 進行 LLM 完成操作。

```json
// Server → Client Request
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "sampling/createMessage",
  "params": {
    "messages": [
      {
        "role": "user",
        "content": {
          "type": "text",
          "text": "請摘要以下內容..."
        }
      }
    ],
    "maxTokens": 500
  }
}

// Client → Server Response
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "role": "assistant",
    "content": {
      "type": "text",
      "text": "這是摘要內容..."
    },
    "model": "claude-3-sonnet",
    "stopReason": "end_turn"
  }
}
```

#### 2.3.5 Client 端原語：Elicitation

**Elicitation** 允許 Server 請求使用者輸入或確認。

```json
// Server → Client Request
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "elicitation/create",
  "params": {
    "message": "是否確認刪除此檔案？",
    "requestedSchema": {
      "type": "object",
      "properties": {
        "confirm": {
          "type": "boolean",
          "description": "確認刪除"
        }
      },
      "required": ["confirm"]
    }
  }
}
```

#### 2.3.6 Client 端原語：Logging

**Logging** 允許 Server 發送日誌訊息給 Client。

```json
// Server → Client Notification
{
  "jsonrpc": "2.0",
  "method": "notifications/message",
  "params": {
    "level": "info",
    "logger": "FileSystem",
    "data": "檔案讀取成功: /path/to/file.txt"
  }
}
```

日誌級別：

| 級別        | 說明         |
| ----------- | ------------ |
| `debug`     | 除錯資訊     |
| `info`      | 一般資訊     |
| `notice`    | 重要通知     |
| `warning`   | 警告訊息     |
| `error`     | 錯誤訊息     |
| `critical`  | 嚴重錯誤     |
| `alert`     | 需要立即處理 |
| `emergency` | 系統不可用   |

#### 2.3.7 通用原語：Tasks（實驗性）

**Tasks** 是實驗性功能，用於包裝長時間運行的操作。

```json
// 啟動任務
{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/call",
  "params": {
    "name": "long_running_task",
    "arguments": { ... }
  }
}

// 回應包含任務 ID
{
  "jsonrpc": "2.0",
  "id": 1,
  "result": {
    "content": [
      {
        "type": "text",
        "text": "任務已啟動"
      }
    ],
    "taskId": "task-123"
  }
}

// 查詢任務狀態
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tasks/get",
  "params": {
    "taskId": "task-123"
  }
}
```

---

### 2.4 通知機制（Notifications）

MCP 的通知機制允許 Server 和 Client 之間進行非同步的單向訊息傳遞。

#### 2.4.1 即時更新的設計

通知是無需回應的訊息，用於事件通知：

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server

    Note over C,S: 正常操作中
    S->>C: notifications/tools/list_changed
    Note over C: Client 更新工具列表
    C->>S: tools/list
    S->>C: tools/list response
```

#### 2.4.2 主要通知類型

| 通知類型                               | 方向            | 說明              |
| -------------------------------------- | --------------- | ----------------- |
| `notifications/initialized`            | Client → Server | Client 完成初始化 |
| `notifications/progress`               | 雙向            | 進度更新          |
| `notifications/message`                | Server → Client | 日誌訊息          |
| `notifications/tools/list_changed`     | Server → Client | 工具列表變更      |
| `notifications/resources/list_changed` | Server → Client | 資源列表變更      |
| `notifications/resources/updated`      | Server → Client | 資源內容更新      |
| `notifications/prompts/list_changed`   | Server → Client | 提示列表變更      |

#### 2.4.3 進度通知範例

```json
{
  "jsonrpc": "2.0",
  "method": "notifications/progress",
  "params": {
    "progressToken": "operation-123",
    "progress": 50,
    "total": 100,
    "message": "處理中... 50%"
  }
}
```

> **💡 實務建議**：
>
> - 善用通知機制實現即時更新，提升使用者體驗
> - 避免過於頻繁的通知，可能造成效能問題
> - 重要的狀態變更才發送通知

---

## 第三章：傳輸層深度解析

### 3.1 STDIO Transport

#### 3.1.1 適用場景：本地進程通訊

STDIO Transport 透過標準輸入/輸出進行通訊，適用於：

- ✅ 本地工具整合
- ✅ 開發測試環境
- ✅ 單一使用者場景
- ✅ 需要存取本地資源
- ❌ 不適合多使用者共享
- ❌ 不適合遠端存取

```mermaid
graph LR
    Host[MCP Host] -->|stdout| Server[MCP Server]
    Server -->|stdin| Host

    subgraph "同一台機器"
        Host
        Server
    end
```

#### 3.1.2 配置方式與範例

**Claude Desktop 配置（claude_desktop_config.json）**：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "node",
      "args": ["/path/to/filesystem-server/index.js"],
      "env": {
        "NODE_ENV": "production"
      }
    },
    "database": {
      "command": "python",
      "args": ["-m", "database_server"],
      "env": {
        "DB_CONNECTION": "postgresql://localhost/mydb"
      }
    }
  }
}
```

**配置說明**：

| 欄位      | 說明     | 範例                    |
| --------- | -------- | ----------------------- |
| `command` | 執行命令 | `node`、`python`、`npx` |
| `args`    | 命令參數 | 腳本路徑、模組名稱      |
| `env`     | 環境變數 | 資料庫連線字串等        |

#### 3.1.3 效能特性與限制

| 特性       | 說明                    |
| ---------- | ----------------------- |
| **延遲**   | 極低（毫秒級）          |
| **吞吐量** | 受限於 pipe buffer size |
| **穩定性** | 依賴進程管理            |
| **除錯**   | 需要額外日誌機制        |

**限制**：

- 無法跨機器通訊
- 進程重啟會中斷連接
- 除錯較為困難

---

### 3.2 Streamable HTTP Transport

#### 3.2.1 適用場景：遠端伺服器通訊

HTTP Transport 適用於需要遠端存取的場景：

- ✅ 團隊共享服務
- ✅ 雲端部署
- ✅ 需要認證授權
- ✅ 負載均衡
- ❌ 延遲較高
- ❌ 需要網路連接

```mermaid
graph LR
    subgraph "用戶端"
        Client[MCP Client]
    end

    subgraph "伺服器端"
        Server[MCP Server]
        DB[(資料庫)]
        API[外部 API]
    end

    Client -->|HTTP POST| Server
    Server -->|SSE| Client
    Server --> DB
    Server --> API
```

#### 3.2.2 HTTP POST 與 Server-Sent Events

**請求流程**：

1. Client 發送 HTTP POST 請求
2. Server 可以回傳：
   - 直接 JSON 回應
   - SSE 串流（用於長時間操作）

**HTTP POST 範例**：

```http
POST /mcp/v1 HTTP/1.1
Host: mcp-server.example.com
Content-Type: application/json
Authorization: Bearer <token>

{
  "jsonrpc": "2.0",
  "id": 1,
  "method": "tools/list"
}
```

**SSE 回應範例**：

```http
HTTP/1.1 200 OK
Content-Type: text/event-stream

event: message
data: {"jsonrpc":"2.0","id":1,"result":{"tools":[...]}}

event: message
data: {"jsonrpc":"2.0","method":"notifications/progress","params":{...}}
```

#### 3.2.3 認證機制

**Bearer Token**：

```http
POST /mcp/v1 HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

**API Keys**：

```http
POST /mcp/v1 HTTP/1.1
X-API-Key: sk-1234567890abcdef
```

**OAuth 2.0 流程**：

```mermaid
sequenceDiagram
    participant U as 使用者
    participant C as MCP Client
    participant A as 授權伺服器
    participant S as MCP Server

    U->>C: 請求連接 MCP Server
    C->>A: 重導向到授權頁面
    U->>A: 授權同意
    A->>C: 回傳授權碼
    C->>A: 授權碼換取 Token
    A->>C: 回傳 Access Token
    C->>S: 請求 + Access Token
    S->>C: 回應資料
```

#### 3.2.4 安全性最佳實踐

```
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP Transport 安全清單                        │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ✓ 一律使用 HTTPS                                                │
│  ✓ 實施適當的認證機制                                            │
│  ✓ 設定合理的 Token 過期時間                                     │
│  ✓ 實施速率限制                                                  │
│  ✓ 驗證輸入參數                                                  │
│  ✓ 記錄存取日誌                                                  │
│  ✓ 使用 CORS 限制來源                                            │
│  ✓ 定期輪換憑證                                                  │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

**CORS 配置範例**：

```javascript
// Express.js 範例
app.use(
  cors({
    origin: ["https://claude.ai", "https://your-app.com"],
    methods: ["POST"],
    allowedHeaders: ["Content-Type", "Authorization"],
    credentials: true,
  }),
);
```

> **⚠️ 安全警告**：
>
> - 永遠不要在程式碼中硬編碼認證憑證
> - 使用環境變數或安全的密鑰管理服務
> - 定期審計存取日誌

---

## 章節小結

本章節涵蓋了 MCP 的基礎概念與技術架構：

### 重點回顧

| 章節       | 關鍵概念                 |
| ---------- | ------------------------ |
| **第一章** | MCP 定義、價值、架構角色 |
| **第二章** | 分層架構、JSON-RPC、原語 |
| **第三章** | STDIO vs HTTP Transport  |

### 下一步

接下來的章節將進入實戰開發：

- 第四章：開發環境設置與實作
- 第五章：完整範例專案
- 第六章：最佳實踐

---

## 第四章：實戰開發指南

### 4.1 開發環境設置

#### 4.1.1 SDK 選擇與安裝

MCP 提供多種語言的官方 SDK：

| SDK                | 語言         | 套件名稱                    | 成熟度 |
| ------------------ | ------------ | --------------------------- | ------ |
| **Python SDK**     | Python 3.10+ | `mcp`                       | 穩定   |
| **TypeScript SDK** | Node.js 18+  | `@modelcontextprotocol/sdk` | 穩定   |
| **Java SDK**       | Java 17+     | 社群維護                    | 開發中 |

**Python SDK 安裝**：

```bash
# 使用 pip 安裝
pip install mcp

# 或使用 uv（推薦）
uv add mcp

# 安裝開發工具
pip install mcp[cli]
```

**TypeScript SDK 安裝**：

```bash
# 使用 npm
npm install @modelcontextprotocol/sdk

# 或使用 pnpm
pnpm add @modelcontextprotocol/sdk

# 安裝 CLI 工具
npm install -g @modelcontextprotocol/inspector
```

#### 4.1.2 開發工具介紹（MCP Inspector）

**MCP Inspector** 是官方提供的除錯工具，可用於測試 MCP Server：

```bash
# 啟動 Inspector
npx @modelcontextprotocol/inspector

# 連接到本地 Server
npx @modelcontextprotocol/inspector node /path/to/server.js
```

**Inspector 功能**：

```
┌─────────────────────────────────────────────────────────────────┐
│                    MCP Inspector 功能                            │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  📋 工具列表    檢視 Server 提供的所有工具                        │
│  📁 資源瀏覽    瀏覽可用資源並讀取內容                            │
│  💬 提示測試    測試 Prompts 並查看結果                          │
│  🔧 互動測試    手動呼叫工具並檢視回應                            │
│  📊 日誌監控    即時查看 Server 日誌                             │
│  🔍 協議檢查    檢視原始 JSON-RPC 訊息                           │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

#### 4.1.3 專案結構建議

**Python 專案結構**：

```
my-mcp-server/
├── pyproject.toml          # 專案配置
├── README.md               # 說明文件
├── src/
│   └── my_mcp_server/
│       ├── __init__.py
│       ├── __main__.py     # 入口點
│       ├── server.py       # Server 主程式
│       ├── tools/          # 工具定義
│       │   ├── __init__.py
│       │   ├── file_tools.py
│       │   └── db_tools.py
│       ├── resources/      # 資源處理
│       │   ├── __init__.py
│       │   └── handlers.py
│       └── utils/          # 工具函數
│           ├── __init__.py
│           └── helpers.py
├── tests/                  # 測試檔案
│   ├── test_tools.py
│   └── test_resources.py
└── config/                 # 配置檔案
    └── settings.yaml
```

**TypeScript 專案結構**：

```
my-mcp-server/
├── package.json
├── tsconfig.json
├── README.md
├── src/
│   ├── index.ts           # 入口點
│   ├── server.ts          # Server 主程式
│   ├── tools/
│   │   ├── index.ts
│   │   ├── fileTools.ts
│   │   └── dbTools.ts
│   ├── resources/
│   │   ├── index.ts
│   │   └── handlers.ts
│   └── utils/
│       ├── index.ts
│       └── helpers.ts
├── tests/
│   ├── tools.test.ts
│   └── resources.test.ts
└── dist/                  # 編譯輸出
```

---

### 4.2 開發 MCP Server

#### 4.2.1 基礎範例：最簡單的 MCP Server

**Python 版本**：

```python
#!/usr/bin/env python3
"""
最簡單的 MCP Server 範例
"""

from mcp.server import Server
from mcp.server.stdio import stdio_server
from mcp.types import Tool, TextContent

# 建立 Server 實例
server = Server("simple-server")

# 定義工具列表
@server.list_tools()
async def list_tools() -> list[Tool]:
    """回傳可用工具列表"""
    return [
        Tool(
            name="hello",
            description="回傳問候訊息",
            inputSchema={
                "type": "object",
                "properties": {
                    "name": {
                        "type": "string",
                        "description": "要問候的名字"
                    }
                },
                "required": ["name"]
            }
        )
    ]

# 實作工具處理
@server.call_tool()
async def call_tool(name: str, arguments: dict) -> list[TextContent]:
    """處理工具呼叫"""
    if name == "hello":
        user_name = arguments.get("name", "World")
        return [TextContent(type="text", text=f"Hello, {user_name}!")]

    raise ValueError(f"Unknown tool: {name}")

# 主程式入口
async def main():
    """啟動 Server"""
    async with stdio_server() as (read_stream, write_stream):
        await server.run(
            read_stream,
            write_stream,
            server.create_initialization_options()
        )

if __name__ == "__main__":
    import asyncio
    asyncio.run(main())
```

**TypeScript 版本**：

```typescript
#!/usr/bin/env node
/**
 * 最簡單的 MCP Server 範例
 */

import { Server } from "@modelcontextprotocol/sdk/server/index.js";
import { StdioServerTransport } from "@modelcontextprotocol/sdk/server/stdio.js";
import {
  CallToolRequestSchema,
  ListToolsRequestSchema,
} from "@modelcontextprotocol/sdk/types.js";

// 建立 Server 實例
const server = new Server(
  {
    name: "simple-server",
    version: "1.0.0",
  },
  {
    capabilities: {
      tools: {},
    },
  },
);

// 處理工具列表請求
server.setRequestHandler(ListToolsRequestSchema, async () => {
  return {
    tools: [
      {
        name: "hello",
        description: "回傳問候訊息",
        inputSchema: {
          type: "object",
          properties: {
            name: {
              type: "string",
              description: "要問候的名字",
            },
          },
          required: ["name"],
        },
      },
    ],
  };
});

// 處理工具呼叫請求
server.setRequestHandler(CallToolRequestSchema, async (request) => {
  const { name, arguments: args } = request.params;

  if (name === "hello") {
    const userName = (args as { name: string }).name || "World";
    return {
      content: [
        {
          type: "text",
          text: `Hello, ${userName}!`,
        },
      ],
    };
  }

  throw new Error(`Unknown tool: ${name}`);
});

// 啟動 Server
async function main() {
  const transport = new StdioServerTransport();
  await server.connect(transport);
  console.error("Simple MCP Server running on stdio");
}

main().catch(console.error);
```

#### 4.2.2 註冊與初始化流程

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server

    Note over S: Server 啟動
    S->>S: 載入設定
    S->>S: 註冊 Handlers

    Note over C: Client 連接
    C->>S: initialize
    S->>S: 驗證版本
    S->>S: 設定能力
    S->>C: initialize response
    C->>S: initialized (notification)

    Note over C,S: 就緒，可以處理請求
```

**初始化選項配置**：

```java
// Java - 自訂初始化選項
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.spec.ServerCapabilities;

public class MyMcpServer {
    public static void main(String[] args) {
        // 設定 Server 能力
        ServerCapabilities capabilities = ServerCapabilities.builder()
            .tools(ServerCapabilities.ToolCapabilities.builder()
                .listChanged(true)
                .build())
            .resources(ServerCapabilities.ResourceCapabilities.builder()
                .listChanged(true)
                .subscribe(true)
                .build())
            .build();

        // 建立 Server 配置
        McpServerOptions options = McpServerOptions.builder()
            .serverName("my-server")
            .serverVersion("1.0.0")
            .capabilities(capabilities)
            .build();

        McpServer server = new McpServer(options);
    }
}
```

#### 4.2.3 進階範例：實作 Tools（資料庫查詢）

```java
/**
 * 資料庫查詢工具範例
 * DatabaseMcpServer.java
 */
package com.example.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class DatabaseMcpServer {

    private final McpServer server;
    private final HikariDataSource dataSource;

    public DatabaseMcpServer() {
        // 初始化資料庫連接池
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:postgresql://localhost:5432/mydb");
        config.setUsername("user");
        config.setPassword("password");
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(20);
        this.dataSource = new HikariDataSource(config);

        // 建立 MCP Server
        McpServerOptions options = McpServerOptions.builder()
            .serverName("database-server")
            .serverVersion("1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(ServerCapabilities.ToolCapabilities.builder()
                    .listChanged(true)
                    .build())
                .build())
            .build();

        this.server = new McpServer(options);

        // 註冊工具
        registerTools();
    }

    private void registerTools() {
        // 註冊查詢工具
        server.addTool(Tool.builder()
            .name("query_database")
            .description("執行 SQL 查詢（僅支援 SELECT）")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "query", Map.of(
                        "type", "string",
                        "description", "SQL SELECT 查詢語句"
                    ),
                    "limit", Map.of(
                        "type", "integer",
                        "description", "結果筆數限制",
                        "default", 100
                    )
                ),
                "required", List.of("query")
            ))
            .build());

        // 註冊列表資料表工具
        server.addTool(Tool.builder()
            .name("list_tables")
            .description("列出資料庫中的所有資料表")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of()
            ))
            .build());

        // 註冊描述資料表工具
        server.addTool(Tool.builder()
            .name("describe_table")
            .description("取得資料表的 schema 資訊")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "table_name", Map.of(
                        "type", "string",
                        "description", "資料表名稱"
                    )
                ),
                "required", List.of("table_name")
            ))
            .build());

        // 設定工具處理器
        server.setToolHandler(this::handleToolCall);
    }

    private CompletableFuture<CallToolResult> handleToolCall(CallToolRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            String toolName = request.getParams().getName();
            Map<String, Object> arguments = request.getParams().getArguments();

            try {
                return switch (toolName) {
                    case "query_database" -> handleQuery(arguments);
                    case "list_tables" -> handleListTables();
                    case "describe_table" -> handleDescribeTable(arguments);
                    default -> CallToolResult.error("Unknown tool: " + toolName);
                };
            } catch (Exception e) {
                return CallToolResult.error("錯誤：" + e.getMessage());
            }
        });
    }

    private CallToolResult handleQuery(Map<String, Object> arguments) throws SQLException {
        String query = (String) arguments.get("query");
        int limit = (int) arguments.getOrDefault("limit", 100);

        // 安全檢查：僅允許 SELECT
        if (!query.trim().toUpperCase().startsWith("SELECT")) {
            return CallToolResult.text("錯誤：僅支援 SELECT 查詢");
        }

        // 加入 LIMIT 限制
        if (!query.toUpperCase().contains("LIMIT")) {
            query = query + " LIMIT " + limit;
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 建立表頭
            StringBuilder result = new StringBuilder();
            result.append("| ");
            for (int i = 1; i <= columnCount; i++) {
                result.append(metaData.getColumnName(i)).append(" | ");
            }
            result.append("\n| ");
            for (int i = 1; i <= columnCount; i++) {
                result.append("--- | ");
            }
            result.append("\n");

            // 建立資料列
            int rowCount = 0;
            while (rs.next()) {
                result.append("| ");
                for (int i = 1; i <= columnCount; i++) {
                    result.append(rs.getString(i)).append(" | ");
                }
                result.append("\n");
                rowCount++;
            }

            if (rowCount == 0) {
                return CallToolResult.text("查詢無結果");
            }

            return CallToolResult.text(result.toString());
        }
    }

    private CallToolResult handleListTables() throws SQLException {
        String query = """
            SELECT table_name
            FROM information_schema.tables
            WHERE table_schema = 'public'
            ORDER BY table_name
            """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            List<String> tables = new ArrayList<>();
            while (rs.next()) {
                tables.add(rs.getString("table_name"));
            }

            StringBuilder result = new StringBuilder("資料表列表：\n");
            for (String table : tables) {
                result.append("- ").append(table).append("\n");
            }

            return CallToolResult.text(result.toString());
        }
    }

    private CallToolResult handleDescribeTable(Map<String, Object> arguments) throws SQLException {
        String tableName = (String) arguments.get("table_name");

        String query = """
            SELECT column_name, data_type, is_nullable, column_default
            FROM information_schema.columns
            WHERE table_name = ?
            ORDER BY ordinal_position
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tableName);

            try (ResultSet rs = stmt.executeQuery()) {
                StringBuilder result = new StringBuilder();
                result.append("資料表 ").append(tableName).append(" 結構：\n\n");
                result.append("| 欄位名稱 | 資料型別 | 可為空 | 預設值 |\n");
                result.append("| --- | --- | --- | --- |\n");

                boolean hasRows = false;
                while (rs.next()) {
                    hasRows = true;
                    result.append("| ")
                        .append(rs.getString("column_name")).append(" | ")
                        .append(rs.getString("data_type")).append(" | ")
                        .append(rs.getString("is_nullable")).append(" | ")
                        .append(rs.getString("column_default") != null ?
                                rs.getString("column_default") : "-")
                        .append(" |\n");
                }

                if (!hasRows) {
                    return CallToolResult.text("資料表 " + tableName + " 不存在");
                }

                return CallToolResult.text(result.toString());
            }
        }
    }

    public void start() throws Exception {
        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
        System.err.println("Database MCP Server running on stdio");
    }

    public void shutdown() {
        dataSource.close();
    }

    public static void main(String[] args) throws Exception {
        DatabaseMcpServer server = new DatabaseMcpServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::shutdown));
        server.start();
    }
}
```

#### 4.2.4 進階範例：實作 Resources（檔案系統）

```java
/**
 * 檔案系統資源範例
 * FilesystemMcpServer.java
 */
package com.example.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class FilesystemMcpServer {

    private final McpServer server;
    private final Path allowedRoot;

    public FilesystemMcpServer(String rootPath) {
        this.allowedRoot = Paths.get(rootPath).toAbsolutePath().normalize();

        // 建立 MCP Server
        McpServerOptions options = McpServerOptions.builder()
            .serverName("filesystem-server")
            .serverVersion("1.0.0")
            .capabilities(ServerCapabilities.builder()
                .resources(ServerCapabilities.ResourceCapabilities.builder()
                    .listChanged(true)
                    .subscribe(true)
                    .build())
                .build())
            .build();

        this.server = new McpServer(options);

        // 註冊資源處理器
        registerHandlers();
    }

    /**
     * 檢查路徑是否在允許範圍內
     */
    private boolean isSafePath(Path path) {
        try {
            Path resolved = path.toAbsolutePath().normalize();
            return resolved.startsWith(allowedRoot);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 猜測 MIME 類型
     */
    private String guessMimeType(Path path) {
        try {
            String contentType = Files.probeContentType(path);
            return contentType != null ? contentType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    private void registerHandlers() {
        // 註冊資源列表處理器
        server.setResourceListHandler(request -> CompletableFuture.supplyAsync(() -> {
            List<Resource> resources = new ArrayList<>();

            try {
                Files.walkFileTree(allowedRoot, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        Path relativePath = allowedRoot.relativize(file);
                        String mimeType = guessMimeType(file);

                        resources.add(Resource.builder()
                            .uri("file:///" + relativePath.toString().replace("\\", "/"))
                            .name(file.getFileName().toString())
                            .description("檔案：" + relativePath)
                            .mimeType(mimeType)
                            .build());

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                System.err.println("掃描目錄錯誤：" + e.getMessage());
            }

            return ListResourcesResult.builder()
                .resources(resources)
                .build();
        }));

        // 註冊資源讀取處理器
        server.setResourceReadHandler(request -> CompletableFuture.supplyAsync(() -> {
            String uri = request.getParams().getUri();

            // 解析 URI
            String relativePath;
            if (uri.startsWith("file:///")) {
                relativePath = uri.substring(8);
            } else {
                throw new IllegalArgumentException("不支援的 URI 格式：" + uri);
            }

            Path filePath = allowedRoot.resolve(relativePath);

            // 安全檢查
            if (!isSafePath(filePath)) {
                throw new SecurityException("存取被拒絕：路徑不在允許範圍內");
            }

            if (!Files.exists(filePath)) {
                throw new IllegalArgumentException("檔案不存在：" + relativePath);
            }

            try {
                String mimeType = guessMimeType(filePath);

                if (mimeType.startsWith("text/") || mimeType.equals("application/json")) {
                    // 文字檔案
                    String content = Files.readString(filePath);
                    return ReadResourceResult.builder()
                        .contents(List.of(
                            ResourceContent.text(uri, mimeType, content)
                        ))
                        .build();
                } else {
                    // 二進位檔案
                    byte[] bytes = Files.readAllBytes(filePath);
                    String base64Content = Base64.getEncoder().encodeToString(bytes);
                    return ReadResourceResult.builder()
                        .contents(List.of(
                            ResourceContent.blob(uri, mimeType, base64Content)
                        ))
                        .build();
                }
            } catch (IOException e) {
                throw new RuntimeException("讀取檔案錯誤：" + e.getMessage());
            }
        }));

        // 註冊資源訂閱處理器
        server.setResourceSubscribeHandler(request -> CompletableFuture.supplyAsync(() -> {
            String uri = request.getParams().getUri();
            // 實作檔案監控邏輯（可使用 WatchService）
            System.err.println("訂閱資源：" + uri);
            return SubscribeResult.builder().build();
        }));
    }

    public void start() throws Exception {
        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
        System.err.println("Filesystem MCP Server running on stdio");
        System.err.println("Allowed root: " + allowedRoot);
    }

    public static void main(String[] args) throws Exception {
        String rootPath = args.length > 0 ? args[0] : System.getProperty("user.home") + "/documents";
        FilesystemMcpServer server = new FilesystemMcpServer(rootPath);
        server.start();
    }
}
```

#### 4.2.5 進階範例：實作 Prompts（互動模板）

````java
/**
 * 提示模板範例
 * PromptsMcpServer.java
 */
package com.example.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class PromptsMcpServer {

    private final McpServer server;
    private final Map<String, PromptDefinition> prompts = new HashMap<>();

    // 內部類別：提示定義
    private record PromptDefinition(
        String description,
        List<PromptArgument> arguments
    ) {}

    public PromptsMcpServer() {
        // 初始化提示模板
        initializePrompts();

        // 建立 MCP Server
        McpServerOptions options = McpServerOptions.builder()
            .serverName("prompts-server")
            .serverVersion("1.0.0")
            .capabilities(ServerCapabilities.builder()
                .prompts(ServerCapabilities.PromptCapabilities.builder()
                    .listChanged(true)
                    .build())
                .build())
            .build();

        this.server = new McpServer(options);

        // 註冊處理器
        registerHandlers();
    }

    private void initializePrompts() {
        // 程式碼審查模板
        prompts.put("code_review", new PromptDefinition(
            "程式碼審查模板",
            List.of(
                PromptArgument.builder()
                    .name("language")
                    .description("程式語言")
                    .required(true)
                    .build(),
                PromptArgument.builder()
                    .name("code")
                    .description("要審查的程式碼")
                    .required(true)
                    .build(),
                PromptArgument.builder()
                    .name("focus")
                    .description("審查重點（security/performance/style）")
                    .required(false)
                    .build()
            )
        ));

        // 程式碼解說模板
        prompts.put("explain_code", new PromptDefinition(
            "程式碼解說模板",
            List.of(
                PromptArgument.builder()
                    .name("code")
                    .description("要解說的程式碼")
                    .required(true)
                    .build(),
                PromptArgument.builder()
                    .name("level")
                    .description("解說程度（beginner/intermediate/advanced）")
                    .required(false)
                    .build()
            )
        ));

        // 測試案例生成模板
        prompts.put("generate_tests", new PromptDefinition(
            "測試案例生成模板",
            List.of(
                PromptArgument.builder()
                    .name("code")
                    .description("要測試的程式碼")
                    .required(true)
                    .build(),
                PromptArgument.builder()
                    .name("framework")
                    .description("測試框架（junit/testng/mockito）")
                    .required(false)
                    .build()
            )
        ));
    }

    private void registerHandlers() {
        // 列出提示模板
        server.setPromptListHandler(request -> CompletableFuture.supplyAsync(() -> {
            List<Prompt> promptList = new ArrayList<>();

            prompts.forEach((name, definition) -> {
                promptList.add(Prompt.builder()
                    .name(name)
                    .description(definition.description())
                    .arguments(definition.arguments())
                    .build());
            });

            return ListPromptsResult.builder()
                .prompts(promptList)
                .build();
        }));

        // 取得提示內容
        server.setPromptGetHandler(request -> CompletableFuture.supplyAsync(() -> {
            String name = request.getParams().getName();
            Map<String, String> arguments = request.getParams().getArguments();

            List<PromptMessage> messages = switch (name) {
                case "code_review" -> generateCodeReviewPrompt(arguments);
                case "explain_code" -> generateExplainCodePrompt(arguments);
                case "generate_tests" -> generateTestsPrompt(arguments);
                default -> throw new IllegalArgumentException("Unknown prompt: " + name);
            };

            return GetPromptResult.builder()
                .messages(messages)
                .build();
        }));
    }

    private List<PromptMessage> generateCodeReviewPrompt(Map<String, String> args) {
        String language = args.getOrDefault("language", "");
        String code = args.getOrDefault("code", "");
        String focus = args.getOrDefault("focus", "general");

        Map<String, String> focusInstructions = Map.of(
            "security", "請特別關注安全性問題，包括：輸入驗證、SQL 注入、XSS、認證授權等。",
            "performance", "請特別關注效能問題，包括：演算法複雜度、記憶體使用、資料庫查詢效率等。",
            "style", "請特別關注程式碼風格，包括：命名規範、程式碼結構、可讀性、註解等。",
            "general", "請全面審查程式碼，包括：正確性、安全性、效能、可維護性等面向。"
        );

        String promptText = String.format("""
            請審查以下 %s 程式碼：

            ```%s
            %s
            ```

            %s

            請提供：
            1. 發現的問題（按嚴重程度排序）
            2. 改進建議
            3. 整體評分（1-10）
            4. 修改後的程式碼範例（如適用）
            """,
            language,
            language.toLowerCase(),
            code,
            focusInstructions.getOrDefault(focus, focusInstructions.get("general"))
        );

        return List.of(
            PromptMessage.builder()
                .role(Role.USER)
                .content(TextContent.builder()
                    .type("text")
                    .text(promptText)
                    .build())
                .build()
        );
    }

    private List<PromptMessage> generateExplainCodePrompt(Map<String, String> args) {
        String code = args.getOrDefault("code", "");
        String level = args.getOrDefault("level", "intermediate");

        Map<String, String> levelInstructions = Map.of(
            "beginner", "請用簡單易懂的語言解說，避免使用專業術語，多用比喻說明。",
            "intermediate", "請詳細解說程式碼的運作原理，可以使用常見的技術術語。",
            "advanced", "請深入分析程式碼的設計模式、效能考量、潛在問題等進階面向。"
        );

        String promptText = String.format("""
            請解說以下程式碼：

            ```
            %s
            ```

            %s

            請包含：
            1. 程式碼的整體功能說明
            2. 逐行或逐段解說
            3. 使用的程式技巧或設計模式
            4. 可能的使用情境
            """,
            code,
            levelInstructions.getOrDefault(level, levelInstructions.get("intermediate"))
        );

        return List.of(
            PromptMessage.builder()
                .role(Role.USER)
                .content(TextContent.builder()
                    .type("text")
                    .text(promptText)
                    .build())
                .build()
        );
    }

    private List<PromptMessage> generateTestsPrompt(Map<String, String> args) {
        String code = args.getOrDefault("code", "");
        String framework = args.getOrDefault("framework", "junit");

        String promptText = String.format("""
            請為以下程式碼生成測試案例：

            ```
            %s
            ```

            使用 %s 測試框架。

            請包含：
            1. 正常情況測試（Happy Path）
            2. 邊界條件測試
            3. 錯誤處理測試
            4. 效能測試（如適用）

            請提供完整可執行的測試程式碼。
            """,
            code,
            framework
        );

        return List.of(
            PromptMessage.builder()
                .role(Role.USER)
                .content(TextContent.builder()
                    .type("text")
                    .text(promptText)
                    .build())
                .build()
        );
    }

    public void start() throws Exception {
        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
        System.err.println("Prompts MCP Server running on stdio");
    }

    public static void main(String[] args) throws Exception {
        PromptsMcpServer server = new PromptsMcpServer();
        server.start();
    }
}
````

#### 4.2.6 動態工具列表管理與發送通知

```java
/**
 * 動態工具管理範例
 * DynamicToolsMcpServer.java
 */
package com.example.mcp;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

public class DynamicToolsMcpServer {

    private final McpServer server;
    private final Map<String, Tool> registeredTools = new ConcurrentHashMap<>();

    public DynamicToolsMcpServer() {
        // 建立 MCP Server
        McpServerOptions options = McpServerOptions.builder()
            .serverName("dynamic-tools-server")
            .serverVersion("1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(ServerCapabilities.ToolCapabilities.builder()
                    .listChanged(true)
                    .build())
                .build())
            .build();

        this.server = new McpServer(options);

        // 初始化內建工具
        initBuiltinTools();

        // 註冊處理器
        registerHandlers();
    }

    /**
     * 註冊新工具並通知 Client
     */
    public void registerTool(Tool tool) {
        registeredTools.put(tool.getName(), tool);
        // 發送工具列表變更通知
        server.sendNotification("notifications/tools/list_changed", Map.of());
        System.err.println("已註冊工具：" + tool.getName());
    }

    /**
     * 取消註冊工具並通知 Client
     */
    public void unregisterTool(String name) {
        if (registeredTools.remove(name) != null) {
            server.sendNotification("notifications/tools/list_changed", Map.of());
            System.err.println("已移除工具：" + name);
        }
    }

    private void initBuiltinTools() {
        // 註冊管理工具
        registerTool(Tool.builder()
            .name("register_calculator")
            .description("註冊計算機工具")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of()
            ))
            .build());

        registerTool(Tool.builder()
            .name("list_registered_tools")
            .description("列出所有已註冊的工具")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of()
            ))
            .build());
    }

    private void registerHandlers() {
        // 工具列表處理器
        server.setToolListHandler(request -> CompletableFuture.supplyAsync(() ->
            ListToolsResult.builder()
                .tools(new ArrayList<>(registeredTools.values()))
                .build()
        ));

        // 工具呼叫處理器
        server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
            String toolName = request.getParams().getName();
            Map<String, Object> arguments = request.getParams().getArguments();

            return switch (toolName) {
                case "register_calculator" -> handleRegisterCalculator();
                case "calculate" -> handleCalculate(arguments);
                case "list_registered_tools" -> handleListTools();
                default -> CallToolResult.error("Unknown tool: " + toolName);
            };
        }));
    }

    private CallToolResult handleRegisterCalculator() {
        // 動態註冊計算機工具
        Tool calculatorTool = Tool.builder()
            .name("calculate")
            .description("執行數學計算")
            .inputSchema(Map.of(
                "type", "object",
                "properties", Map.of(
                    "expression", Map.of(
                        "type", "string",
                        "description", "數學表達式（例如：2+3*4）"
                    )
                ),
                "required", List.of("expression")
            ))
            .build();

        registerTool(calculatorTool);

        return CallToolResult.text("Calculator tool registered successfully");
    }

    private CallToolResult handleCalculate(Map<String, Object> arguments) {
        String expression = (String) arguments.get("expression");

        if (expression == null || expression.isEmpty()) {
            return CallToolResult.error("表達式不能為空");
        }

        // 安全檢查：僅允許基本運算符和數字
        if (!expression.matches("[0-9+\\-*/().\\s]+")) {
            return CallToolResult.error("不允許的字元，僅支援數字和基本運算符");
        }

        try {
            // 使用 JavaScript 引擎計算表達式
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            if (engine == null) {
                // 如果沒有 JavaScript 引擎，使用簡單的計算方式
                double result = evaluateSimpleExpression(expression);
                return CallToolResult.text("結果：" + result);
            }

            Object result = engine.eval(expression);
            return CallToolResult.text("結果：" + result);

        } catch (Exception e) {
            return CallToolResult.error("計算錯誤：" + e.getMessage());
        }
    }

    private double evaluateSimpleExpression(String expression) {
        // 簡單的表達式計算（僅支援加減乘除）
        expression = expression.replaceAll("\\s+", "");

        // 這是一個簡化的實作，生產環境應使用更完善的表達式解析器
        javax.script.ScriptEngineManager mgr = new javax.script.ScriptEngineManager();
        javax.script.ScriptEngine eng = mgr.getEngineByName("nashorn");

        if (eng != null) {
            try {
                return ((Number) eng.eval(expression)).doubleValue();
            } catch (Exception e) {
                throw new RuntimeException("無法計算表達式：" + expression);
            }
        }

        // 簡單的加法計算作為後備
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException e) {
            throw new RuntimeException("無法計算表達式：" + expression);
        }
    }

    private CallToolResult handleListTools() {
        StringBuilder result = new StringBuilder("已註冊的工具：\n");

        for (Tool tool : registeredTools.values()) {
            result.append("- ").append(tool.getName())
                  .append(": ").append(tool.getDescription())
                  .append("\n");
        }

        return CallToolResult.text(result.toString());
    }

    public void start() throws Exception {
        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
        System.err.println("Dynamic Tools MCP Server running on stdio");
    }

    public static void main(String[] args) throws Exception {
        DynamicToolsMcpServer server = new DynamicToolsMcpServer();
        server.start();
    }
}
```

---

### 4.3 開發 MCP Client

#### 4.3.1 基礎範例：連接到 MCP Server

**Java Client**：

```java
/**
 * MCP Client 基礎範例
 * McpClientExample.java
 */
package com.example.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpClientOptions;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class McpClientExample {

    public static void main(String[] args) throws Exception {
        // 設定 Server 連接參數
        StdioClientTransport transport = new StdioClientTransport(
            "java",                           // 命令
            List.of("-jar", "my-mcp-server.jar")  // 參數
        );

        // 建立 Client
        McpClientOptions options = McpClientOptions.builder()
            .clientInfo(ClientInfo.builder()
                .name("example-client")
                .version("1.0.0")
                .build())
            .build();

        try (McpClient client = new McpClient(options)) {
            // 連接到 Server
            client.connect(transport);

            // 初始化連接
            InitializeResult initResult = client.initialize().get();
            System.out.println("已連接到 Server：" + initResult.getServerInfo().getName());

            // 列出可用工具
            ListToolsResult toolsResult = client.listTools().get();
            System.out.println("\n可用工具：");
            for (Tool tool : toolsResult.getTools()) {
                System.out.println("  - " + tool.getName() + ": " + tool.getDescription());
            }

            // 呼叫工具
            CallToolResult result = client.callTool(
                "hello",
                Map.of("name", "MCP User")
            ).get();

            System.out.println("\n工具回應：" + result.getContent().get(0).getText());
        }
    }
}
```

**TypeScript Client**：

```typescript
/**
 * MCP Client 基礎範例
 */

import { Client } from "@modelcontextprotocol/sdk/client/index.js";
import { StdioClientTransport } from "@modelcontextprotocol/sdk/client/stdio.js";

async function main() {
  // 建立 Client
  const client = new Client(
    {
      name: "example-client",
      version: "1.0.0",
    },
    {
      capabilities: {},
    },
  );

  // 建立 Transport
  const transport = new StdioClientTransport({
    command: "node",
    args: ["./server.js"],
  });

  // 連接到 Server
  await client.connect(transport);

  // 列出可用工具
  const tools = await client.listTools();
  console.log("可用工具：");
  for (const tool of tools.tools) {
    console.log(`  - ${tool.name}: ${tool.description}`);
  }

  // 呼叫工具
  const result = await client.callTool({
    name: "hello",
    arguments: { name: "MCP User" },
  });

  console.log("\n工具回應：", result.content[0]);

  // 關閉連接
  await client.close();
}

main().catch(console.error);
```

#### 4.3.2 初始化與能力協商

```java
/**
 * 能力協商範例
 * CapabilityNegotiationExample.java
 */
package com.example.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpClientOptions;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.*;

import java.util.List;

public class CapabilityNegotiationExample {

    public static void main(String[] args) throws Exception {
        StdioClientTransport transport = new StdioClientTransport(
            "java",
            List.of("-jar", "my-mcp-server.jar")
        );

        McpClientOptions options = McpClientOptions.builder()
            .clientInfo(ClientInfo.builder()
                .name("capability-client")
                .version("1.0.0")
                .build())
            .build();

        try (McpClient client = new McpClient(options)) {
            client.connect(transport);

            // 初始化並取得 Server 能力
            InitializeResult initResult = client.initialize().get();

            System.out.println("Server 資訊：");
            System.out.println("  名稱：" + initResult.getServerInfo().getName());
            System.out.println("  版本：" + initResult.getServerInfo().getVersion());
            System.out.println("  協議版本：" + initResult.getProtocolVersion());

            System.out.println("\nServer 能力：");
            ServerCapabilities caps = initResult.getCapabilities();

            if (caps.getTools() != null) {
                System.out.println("  ✓ Tools 支援");
                if (Boolean.TRUE.equals(caps.getTools().getListChanged())) {
                    System.out.println("    - 支援工具列表變更通知");
                }
            }

            if (caps.getResources() != null) {
                System.out.println("  ✓ Resources 支援");
                if (Boolean.TRUE.equals(caps.getResources().getSubscribe())) {
                    System.out.println("    - 支援資源訂閱");
                }
                if (Boolean.TRUE.equals(caps.getResources().getListChanged())) {
                    System.out.println("    - 支援資源列表變更通知");
                }
            }

            if (caps.getPrompts() != null) {
                System.out.println("  ✓ Prompts 支援");
                if (Boolean.TRUE.equals(caps.getPrompts().getListChanged())) {
                    System.out.println("    - 支援提示列表變更通知");
                }
            }

            if (caps.getLogging() != null) {
                System.out.println("  ✓ Logging 支援");
            }
        }
    }
}
```

#### 4.3.3 進階範例：處理通知與錯誤

```java
/**
 * 進階 Client 範例：通知處理與錯誤重試
 * McpClientWrapper.java
 */
package com.example.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpClientOptions;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 封裝 MCP Client 的進階功能
 */
public class McpClientWrapper implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(McpClientWrapper.class.getName());

    private final String serverCommand;
    private final List<String> serverArgs;
    private McpClient client;
    private final Map<String, Consumer<Map<String, Object>>> notificationHandlers;
    private final int retryCount;
    private final long retryDelayMs;

    public McpClientWrapper(String serverCommand, List<String> serverArgs) {
        this.serverCommand = serverCommand;
        this.serverArgs = serverArgs;
        this.notificationHandlers = new ConcurrentHashMap<>();
        this.retryCount = 3;
        this.retryDelayMs = 1000L;
    }

    /**
     * 註冊通知處理器
     */
    public void onNotification(String method, Consumer<Map<String, Object>> handler) {
        notificationHandlers.put(method, handler);
    }

    /**
     * 處理接收到的通知
     */
    private void handleNotification(String method, Map<String, Object> params) {
        Consumer<Map<String, Object>> handler = notificationHandlers.get(method);
        if (handler != null) {
            handler.accept(params);
        } else {
            logger.fine("未處理的通知：" + method);
        }
    }

    /**
     * 帶重試機制的工具呼叫
     */
    public CompletableFuture<CallToolResult> callToolWithRetry(String name, Map<String, Object> arguments) {
        return CompletableFuture.supplyAsync(() -> {
            Exception lastError = null;

            for (int attempt = 0; attempt < retryCount; attempt++) {
                try {
                    return client.callTool(name, arguments).get();
                } catch (Exception e) {
                    lastError = e;
                    logger.warning(String.format("工具呼叫失敗 (嘗試 %d/%d): %s",
                        attempt + 1, retryCount, e.getMessage()));

                    if (attempt < retryCount - 1) {
                        try {
                            Thread.sleep(retryDelayMs * (attempt + 1));
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new CompletionException(ie);
                        }
                    }
                }
            }

            throw new CompletionException(lastError);
        });
    }

    /**
     * 連接到 Server
     */
    public void connect() throws Exception {
        StdioClientTransport transport = new StdioClientTransport(serverCommand, serverArgs);

        McpClientOptions options = McpClientOptions.builder()
            .clientInfo(ClientInfo.builder()
                .name("advanced-client")
                .version("1.0.0")
                .build())
            .notificationHandler(this::handleNotification)
            .build();

        client = new McpClient(options);
        client.connect(transport);
        client.initialize().get();
    }

    /**
     * 取得 Session
     */
    public McpClient getClient() {
        return client;
    }

    @Override
    public void close() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    // 使用範例
    public static void main(String[] args) throws Exception {
        McpClientWrapper wrapper = new McpClientWrapper(
            "java",
            List.of("-jar", "my-mcp-server.jar")
        );

        // 註冊通知處理器
        wrapper.onNotification("notifications/tools/list_changed", params -> {
            logger.info("工具列表已變更，重新載入...");
            try {
                ListToolsResult tools = wrapper.getClient().listTools().get();
                logger.info("新工具列表：" + tools.getTools().stream()
                    .map(Tool::getName)
                    .toList());
            } catch (Exception e) {
                logger.severe("重新載入工具列表失敗：" + e.getMessage());
            }
        });

        wrapper.onNotification("notifications/message", params -> {
            String level = (String) params.getOrDefault("level", "info");
            String message = (String) params.getOrDefault("data", "");
            logger.log(Level.parse(level.toUpperCase()), "Server: " + message);
        });

        try {
            wrapper.connect();

            // 列出工具
            ListToolsResult tools = wrapper.getClient().listTools().get();
            System.out.println("可用工具數量：" + tools.getTools().size());

            // 使用重試機制呼叫工具
            try {
                CallToolResult result = wrapper.callToolWithRetry(
                    "hello",
                    Map.of("name", "World")
                ).get();
                System.out.println("結果：" + result.getContent().get(0).getText());
            } catch (Exception e) {
                System.out.println("工具呼叫失敗：" + e.getMessage());
            }
        } finally {
            wrapper.close();
        }
    }
}
```

#### 4.3.4 多伺服器管理

```java
/**
 * 多 MCP Server 管理範例
 * MultiServerClient.java
 */
package com.example.mcp.client;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpClientOptions;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * Server 配置
 */
record ServerConfig(
    String name,
    String command,
    List<String> args,
    String description
) {}

/**
 * 管理多個 MCP Server 連接
 */
public class MultiServerClient implements AutoCloseable {

    private final Map<String, McpClient> servers = new ConcurrentHashMap<>();
    private final Map<String, List<String>> serverTools = new ConcurrentHashMap<>();

    /**
     * 添加並連接到 Server
     */
    public void addServer(ServerConfig config) throws Exception {
        StdioClientTransport transport = new StdioClientTransport(
            config.command(),
            config.args()
        );

        McpClientOptions options = McpClientOptions.builder()
            .clientInfo(ClientInfo.builder()
                .name("multi-server-client")
                .version("1.0.0")
                .build())
            .build();

        McpClient client = new McpClient(options);
        client.connect(transport);
        client.initialize().get();

        servers.put(config.name(), client);

        // 快取工具列表
        ListToolsResult tools = client.listTools().get();
        List<String> toolNames = tools.getTools().stream()
            .map(Tool::getName)
            .toList();
        serverTools.put(config.name(), toolNames);

        System.out.println("已連接到 " + config.name() + "，可用工具：" + toolNames);
    }

    /**
     * 根據工具名稱找到對應的 Server
     */
    public Optional<String> findServerForTool(String toolName) {
        for (Map.Entry<String, List<String>> entry : serverTools.entrySet()) {
            if (entry.getValue().contains(toolName)) {
                return Optional.of(entry.getKey());
            }
        }
        return Optional.empty();
    }

    /**
     * 智能路由工具呼叫
     */
    public CompletableFuture<CallToolResult> callTool(String toolName, Map<String, Object> arguments) {
        String serverName = findServerForTool(toolName)
            .orElseThrow(() -> new IllegalArgumentException("找不到提供 " + toolName + " 的 Server"));

        McpClient client = servers.get(serverName);
        return client.callTool(toolName, arguments);
    }

    /**
     * 列出所有 Server 的工具
     */
    public Map<String, List<Map<String, String>>> listAllTools() throws Exception {
        Map<String, List<Map<String, String>>> allTools = new LinkedHashMap<>();

        for (Map.Entry<String, McpClient> entry : servers.entrySet()) {
            ListToolsResult tools = entry.getValue().listTools().get();
            List<Map<String, String>> toolInfoList = tools.getTools().stream()
                .map(t -> Map.of(
                    "name", t.getName(),
                    "description", t.getDescription() != null ? t.getDescription() : ""
                ))
                .toList();
            allTools.put(entry.getKey(), toolInfoList);
        }

        return allTools;
    }

    @Override
    public void close() throws Exception {
        for (McpClient client : servers.values()) {
            try {
                client.close();
            } catch (Exception e) {
                // 記錄錯誤但繼續關閉其他連接
            }
        }
        servers.clear();
        serverTools.clear();
    }

    // 使用範例
    public static void main(String[] args) throws Exception {
        MultiServerClient client = new MultiServerClient();

        // 配置多個 Server
        List<ServerConfig> serverConfigs = List.of(
            new ServerConfig(
                "filesystem",
                "java",
                List.of("-jar", "filesystem-server.jar"),
                "檔案系統操作"
            ),
            new ServerConfig(
                "database",
                "java",
                List.of("-jar", "database-server.jar"),
                "資料庫查詢"
            ),
            new ServerConfig(
                "github",
                "node",
                List.of("./github-server.js"),
                "GitHub 整合"
            )
        );

        try {
            // 連接所有 Server
            for (ServerConfig config : serverConfigs) {
                try {
                    client.addServer(config);
                } catch (Exception e) {
                    System.out.println("無法連接到 " + config.name() + ": " + e.getMessage());
                }
            }

            // 列出所有工具
            Map<String, List<Map<String, String>>> allTools = client.listAllTools();
            System.out.println("\n所有可用工具：");
            for (Map.Entry<String, List<Map<String, String>>> entry : allTools.entrySet()) {
                System.out.println("\n" + entry.getKey() + ":");
                for (Map<String, String> tool : entry.getValue()) {
                    System.out.println("  - " + tool.get("name") + ": " + tool.get("description"));
                }
            }

            // 智能路由呼叫
            CallToolResult result = client.callTool(
                "read_file",
                Map.of("path", "/etc/hosts")
            ).get();
            String content = result.getContent().get(0).getText();
            System.out.println("\n讀取檔案結果：" + content.substring(0, Math.min(100, content.length())) + "...");

        } finally {
            client.close();
        }
    }
}
```

---

### 4.4 整合到 AI 應用

#### 4.4.1 Claude Desktop 整合範例

**配置檔案位置**：

- macOS: `~/Library/Application Support/Claude/claude_desktop_config.json`
- Windows: `%APPDATA%\Claude\claude_desktop_config.json`
- Linux: `~/.config/Claude/claude_desktop_config.json`

**配置範例**：

```json
{
  "mcpServers": {
    "filesystem": {
      "command": "npx",
      "args": [
        "-y",
        "@modelcontextprotocol/server-filesystem",
        "/Users/user/Documents"
      ],
      "env": {}
    },
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_TOKEN": "ghp_xxxxxxxxxxxx"
      }
    },
    "postgres": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-postgres"],
      "env": {
        "DATABASE_URL": "postgresql://user:pass@localhost/mydb"
      }
    },
    "custom-server": {
      "command": "python",
      "args": ["-m", "my_custom_server"],
      "env": {
        "API_KEY": "your-api-key"
      }
    }
  }
}
```

#### 4.4.2 Claude Code 整合範例

VS Code 設定（`settings.json`）：

```json
{
  "claude.mcpServers": {
    "workspace-tools": {
      "command": "node",
      "args": ["${workspaceFolder}/.mcp/server.js"],
      "env": {}
    },
    "docker": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-docker"],
      "env": {}
    }
  }
}
```

#### 4.4.3 自訂 AI 應用整合流程

```mermaid
graph TB
    subgraph "整合流程"
        A[1. 初始化 MCP Client] --> B[2. 連接 MCP Server]
        B --> C[3. 探索可用工具]
        C --> D[4. 建立工具描述]
        D --> E[5. 傳送給 LLM]
        E --> F{LLM 決定使用工具?}
        F -->|是| G[6. 執行工具呼叫]
        G --> H[7. 取得結果]
        H --> I[8. 回傳給 LLM]
        I --> E
        F -->|否| J[9. 輸出最終回應]
    end
```

**整合程式碼範例**：

```java
/**
 * 自訂 AI 應用整合 MCP 範例
 * McpEnabledAssistant.java
 */
package com.example.mcp.assistant;

import com.anthropic.client.AnthropicClient;
import com.anthropic.models.*;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpClientOptions;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 整合 MCP 的 AI 助理
 */
public class McpEnabledAssistant implements AutoCloseable {

    private final AnthropicClient anthropic;
    private McpClient mcpClient;
    private List<Map<String, Object>> availableTools;

    public McpEnabledAssistant(String anthropicApiKey) {
        this.anthropic = AnthropicClient.builder()
            .apiKey(anthropicApiKey)
            .build();
        this.availableTools = new ArrayList<>();
    }

    /**
     * 連接 MCP Server
     */
    public void connectMcp(String command, List<String> args) throws Exception {
        StdioClientTransport transport = new StdioClientTransport(command, args);

        McpClientOptions options = McpClientOptions.builder()
            .clientInfo(ClientInfo.builder()
                .name("ai-assistant")
                .version("1.0.0")
                .build())
            .build();

        mcpClient = new McpClient(options);
        mcpClient.connect(transport);
        mcpClient.initialize().get();

        // 取得並轉換工具格式
        ListToolsResult toolsResponse = mcpClient.listTools().get();
        availableTools = convertToolsForClaude(toolsResponse.getTools());
    }

    /**
     * 將 MCP 工具格式轉換為 Claude API 格式
     */
    private List<Map<String, Object>> convertToolsForClaude(List<Tool> mcpTools) {
        return mcpTools.stream()
            .map(tool -> Map.<String, Object>of(
                "name", tool.getName(),
                "description", tool.getDescription() != null ? tool.getDescription() : "",
                "input_schema", tool.getInputSchema()
            ))
            .collect(Collectors.toList());
    }

    /**
     * 與助理對話
     */
    public String chat(String userMessage) throws Exception {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userMessage));

        while (true) {
            // 呼叫 Claude API
            MessageCreateParams params = MessageCreateParams.builder()
                .model("claude-sonnet-4-20250514")
                .maxTokens(4096)
                .tools(availableTools)
                .messages(messages)
                .build();

            Message response = anthropic.messages().create(params);

            // 檢查是否需要使用工具
            if ("tool_use".equals(response.getStopReason())) {
                // 處理工具呼叫
                List<Map<String, Object>> toolResults = new ArrayList<>();

                for (ContentBlock content : response.getContent()) {
                    if (content instanceof ToolUseBlock toolUse) {
                        String toolName = toolUse.getName();
                        Map<String, Object> toolInput = toolUse.getInput();
                        String toolUseId = toolUse.getId();

                        // 透過 MCP 執行工具
                        String toolResult;
                        boolean isError;

                        try {
                            CallToolResult result = mcpClient.callTool(toolName, toolInput).get();
                            toolResult = result.getContent().get(0).getText();
                            isError = false;
                        } catch (Exception e) {
                            toolResult = "工具執行錯誤：" + e.getMessage();
                            isError = true;
                        }

                        toolResults.add(Map.of(
                            "type", "tool_result",
                            "tool_use_id", toolUseId,
                            "content", toolResult,
                            "is_error", isError
                        ));
                    }
                }

                // 將結果加入對話
                messages.add(Map.of("role", "assistant", "content", response.getContent()));
                messages.add(Map.of("role", "user", "content", toolResults));
            } else {
                // 沒有工具呼叫，回傳最終回應
                StringBuilder finalResponse = new StringBuilder();
                for (ContentBlock content : response.getContent()) {
                    if (content instanceof TextBlock textBlock) {
                        finalResponse.append(textBlock.getText());
                    }
                }
                return finalResponse.toString();
            }
        }
    }

    @Override
    public void close() throws Exception {
        if (mcpClient != null) {
            mcpClient.close();
        }
    }

    // 使用範例
    public static void main(String[] args) throws Exception {
        String apiKey = System.getenv("ANTHROPIC_API_KEY");

        try (McpEnabledAssistant assistant = new McpEnabledAssistant(apiKey)) {
            // 連接 MCP Server
            assistant.connectMcp("java", List.of("-jar", "filesystem-server.jar"));

            // 對話
            String response = assistant.chat(
                "請幫我讀取 /tmp/test.txt 的內容，並摘要其中的重點"
            );
            System.out.println("助理回應：" + response);
        }
    }
}
```

> **💡 實務建議**：
>
> - 在生產環境中，確保正確處理 API Key 和敏感資訊
> - 實作適當的錯誤處理和重試機制
> - 考慮使用連接池管理多個 MCP Server 連接

---

## 第五章：完整實戰範例

本章提供三個完整的端到端範例，包含可直接執行的程式碼。

### 5.1 範例一：檔案系統 MCP Server

#### 5.1.1 功能說明

提供安全的檔案系統操作功能：

- 讀取檔案內容
- 寫入檔案
- 列出目錄
- 搜尋檔案
- 取得檔案資訊

#### 5.1.2 完整程式碼

**專案結構**：

```
filesystem-mcp-server/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── mcp/
                        └── filesystem/
                            └── FilesystemMcpServer.java
```

**pom.xml**：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.mcp</groupId>
    <artifactId>filesystem-mcp-server</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <name>Filesystem MCP Server</name>
    <description>MCP Server for filesystem operations</description>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.modelcontextprotocol</groupId>
            <artifactId>mcp-server</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.mcp.filesystem.FilesystemMcpServer</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

**src/main/java/com/example/mcp/filesystem/FilesystemMcpServer.java**：

```java
package com.example.mcp.filesystem;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 檔案系統 MCP Server
 * 提供安全的檔案讀寫、搜尋功能
 */
public class FilesystemMcpServer {

    private final McpServer server;

    // 配置：允許存取的目錄（可透過環境變數設定）
    private final List<String> allowedDirectories;

    // 最大檔案大小限制（10MB）
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    // 禁止的檔案類型
    private static final Set<String> FORBIDDEN_EXTENSIONS = Set.of(
        ".exe", ".dll", ".so", ".dylib", ".bin"
    );

    public FilesystemMcpServer() {
        // 從環境變數讀取允許的目錄
        String allowedDirs = System.getenv().getOrDefault("MCP_ALLOWED_DIRS", "/tmp,/home");
        this.allowedDirectories = Arrays.stream(allowedDirs.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        // 建立 Server
        McpServerOptions options = McpServerOptions.builder()
            .serverInfo(ServerInfo.builder()
                .name("filesystem-server")
                .version("1.0.0")
                .build())
            .capabilities(ServerCapabilities.builder()
                .tools(ToolsCapability.builder().listChanged(true).build())
                .resources(ResourcesCapability.builder().subscribe(true).listChanged(true).build())
                .build())
            .build();

        this.server = new McpServer(options);
        setupHandlers();
    }

    private boolean isPathAllowed(Path path) {
        try {
            Path resolved = path.toAbsolutePath().normalize();
            return allowedDirectories.stream()
                .anyMatch(dir -> resolved.startsWith(dir));
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isSafeFilename(String filename) {
        // 檢查路徑遍歷攻擊
        if (filename.contains("..") || filename.startsWith("/")) {
            return false;
        }

        // 檢查禁止的副檔名
        String ext = filename.contains(".")
            ? filename.substring(filename.lastIndexOf(".")).toLowerCase()
            : "";
        return !FORBIDDEN_EXTENSIONS.contains(ext);
    }

    private void setupHandlers() {
        // 工具列表處理器
        server.setToolListHandler(request -> CompletableFuture.supplyAsync(() ->
            ListToolsResult.builder()
                .tools(List.of(
                    Tool.builder()
                        .name("read_file")
                        .description("讀取指定檔案的內容。支援文字檔案。")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of(
                                    "type", "string",
                                    "description", "檔案的完整路徑"
                                ),
                                "encoding", Map.of(
                                    "type", "string",
                                    "description", "檔案編碼（預設 utf-8）",
                                    "default", "utf-8"
                                )
                            ),
                            "required", List.of("path")
                        ))
                        .build(),
                    Tool.builder()
                        .name("write_file")
                        .description("寫入內容到指定檔案。如果檔案不存在會建立。")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "檔案的完整路徑"),
                                "content", Map.of("type", "string", "description", "要寫入的內容"),
                                "mode", Map.of(
                                    "type", "string",
                                    "enum", List.of("overwrite", "append"),
                                    "description", "寫入模式：overwrite（覆蓋）或 append（附加）",
                                    "default", "overwrite"
                                )
                            ),
                            "required", List.of("path", "content")
                        ))
                        .build(),
                    Tool.builder()
                        .name("list_directory")
                        .description("列出目錄中的檔案和子目錄")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "目錄路徑"),
                                "recursive", Map.of("type", "boolean", "description", "是否遞迴列出子目錄", "default", false),
                                "pattern", Map.of("type", "string", "description", "檔案名稱過濾模式（如 *.txt）")
                            ),
                            "required", List.of("path")
                        ))
                        .build(),
                    Tool.builder()
                        .name("search_files")
                        .description("在指定目錄中搜尋包含特定內容的檔案")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "directory", Map.of("type", "string", "description", "搜尋的起始目錄"),
                                "query", Map.of("type", "string", "description", "要搜尋的文字內容"),
                                "file_pattern", Map.of("type", "string", "description", "檔案名稱過濾模式", "default", "*"),
                                "max_results", Map.of("type", "integer", "description", "最大結果數量", "default", 20)
                            ),
                            "required", List.of("directory", "query")
                        ))
                        .build(),
                    Tool.builder()
                        .name("get_file_info")
                        .description("取得檔案的詳細資訊（大小、修改時間等）")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "檔案路徑")
                            ),
                            "required", List.of("path")
                        ))
                        .build(),
                    Tool.builder()
                        .name("create_directory")
                        .description("建立新目錄（包含父目錄）")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "目錄路徑")
                            ),
                            "required", List.of("path")
                        ))
                        .build(),
                    Tool.builder()
                        .name("delete_file")
                        .description("刪除指定檔案")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "path", Map.of("type", "string", "description", "檔案路徑"),
                                "confirm", Map.of("type", "boolean", "description", "確認刪除", "default", false)
                            ),
                            "required", List.of("path", "confirm")
                        ))
                        .build()
                ))
                .build()
        ));

        // 工具呼叫處理器
        server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
            String name = request.getParams().getName();
            Map<String, Object> args = request.getParams().getArguments();

            try {
                return switch (name) {
                    case "read_file" -> handleReadFile(args);
                    case "write_file" -> handleWriteFile(args);
                    case "list_directory" -> handleListDirectory(args);
                    case "search_files" -> handleSearchFiles(args);
                    case "get_file_info" -> handleGetFileInfo(args);
                    case "create_directory" -> handleCreateDirectory(args);
                    case "delete_file" -> handleDeleteFile(args);
                    default -> CallToolResult.error("未知的工具：" + name);
                };
            } catch (Exception e) {
                return CallToolResult.error("錯誤：" + e.getMessage());
            }
        }));

        // 資源列表處理器
        server.setResourceListHandler(request -> CompletableFuture.supplyAsync(() -> {
            List<Resource> resources = allowedDirectories.stream()
                .map(dir -> {
                    Path path = Path.of(dir);
                    if (Files.exists(path) && Files.isDirectory(path)) {
                        return Resource.builder()
                            .uri("file://" + path.toAbsolutePath())
                            .name("目錄：" + path)
                            .description("允許存取的目錄：" + path)
                            .mimeType("inode/directory")
                            .build();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

            return ListResourcesResult.builder().resources(resources).build();
        }));
    }

    private CallToolResult handleReadFile(Map<String, Object> args) throws IOException {
        Path path = Path.of((String) args.get("path"));
        String encoding = (String) args.getOrDefault("encoding", "utf-8");

        if (!isPathAllowed(path)) {
            return CallToolResult.error("錯誤：存取被拒絕，路徑不在允許範圍內");
        }

        if (!Files.exists(path)) {
            return CallToolResult.error("錯誤：檔案不存在 - " + path);
        }

        if (!Files.isRegularFile(path)) {
            return CallToolResult.error("錯誤：指定路徑不是檔案");
        }

        if (Files.size(path) > MAX_FILE_SIZE) {
            return CallToolResult.error("錯誤：檔案過大（超過 " + (MAX_FILE_SIZE / 1024 / 1024) + "MB）");
        }

        try {
            String content = Files.readString(path, java.nio.charset.Charset.forName(encoding));
            return CallToolResult.text(content);
        } catch (java.nio.charset.MalformedInputException e) {
            return CallToolResult.error("錯誤：無法以指定編碼讀取檔案，可能是二進位檔案");
        }
    }

    private CallToolResult handleWriteFile(Map<String, Object> args) throws IOException {
        Path path = Path.of((String) args.get("path"));
        String content = (String) args.get("content");
        String mode = (String) args.getOrDefault("mode", "overwrite");

        if (!isPathAllowed(path)) {
            return CallToolResult.error("錯誤：存取被拒絕，路徑不在允許範圍內");
        }

        if (!isSafeFilename(path.getFileName().toString())) {
            return CallToolResult.error("錯誤：不安全的檔案名稱");
        }

        // 確保父目錄存在
        Files.createDirectories(path.getParent());

        if ("append".equals(mode)) {
            Files.writeString(path, content, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return CallToolResult.text("成功：已附加檔案 - " + path);
        } else {
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return CallToolResult.text("成功：已覆寫檔案 - " + path);
        }
    }

    private CallToolResult handleListDirectory(Map<String, Object> args) throws IOException {
        Path path = Path.of((String) args.get("path"));
        boolean recursive = Boolean.TRUE.equals(args.get("recursive"));
        String pattern = (String) args.getOrDefault("pattern", "*");

        if (!isPathAllowed(path)) {
            return CallToolResult.error("錯誤：存取被拒絕");
        }

        if (!Files.exists(path)) {
            return CallToolResult.error("錯誤：目錄不存在 - " + path);
        }

        if (!Files.isDirectory(path)) {
            return CallToolResult.error("錯誤：指定路徑不是目錄");
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
        List<String> results = new ArrayList<>();

        try (Stream<Path> stream = recursive ? Files.walk(path) : Files.list(path)) {
            stream.filter(p -> matcher.matches(p.getFileName()))
                .sorted()
                .limit(100)
                .forEach(p -> {
                    String icon = Files.isDirectory(p) ? "📁" : "📄";
                    Path relPath = path.relativize(p);
                    String size = "";
                    if (Files.isRegularFile(p)) {
                        try {
                            size = " (" + Files.size(p) + " bytes)";
                        } catch (IOException ignored) {}
                    }
                    results.add(icon + " " + relPath + size);
                });
        }

        if (results.isEmpty()) {
            return CallToolResult.text("目錄為空或無符合條件的項目");
        }

        String header = "目錄列表：" + path + "\n" + "=".repeat(50) + "\n";
        return CallToolResult.text(header + String.join("\n", results));
    }

    private CallToolResult handleSearchFiles(Map<String, Object> args) throws IOException {
        Path directory = Path.of((String) args.get("directory"));
        String query = (String) args.get("query");
        String filePattern = (String) args.getOrDefault("file_pattern", "*");
        int maxResults = ((Number) args.getOrDefault("max_results", 20)).intValue();

        if (!isPathAllowed(directory)) {
            return CallToolResult.error("錯誤：存取被拒絕");
        }

        if (!Files.exists(directory) || !Files.isDirectory(directory)) {
            return CallToolResult.error("錯誤：目錄不存在");
        }

        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + filePattern);
        List<Map<String, Object>> results = new ArrayList<>();
        String queryLower = query.toLowerCase();

        try (Stream<Path> stream = Files.walk(directory)) {
            stream.filter(Files::isRegularFile)
                .filter(p -> matcher.matches(p.getFileName()))
                .filter(p -> {
                    try {
                        return Files.size(p) <= MAX_FILE_SIZE;
                    } catch (IOException e) {
                        return false;
                    }
                })
                .forEach(filePath -> {
                    if (results.size() >= maxResults) return;

                    try {
                        String content = Files.readString(filePath, StandardCharsets.UTF_8);
                        if (content.toLowerCase().contains(queryLower)) {
                            String[] lines = content.split("\n");
                            List<String> matchingLines = new ArrayList<>();

                            for (int i = 0; i < lines.length && matchingLines.size() < 5; i++) {
                                if (lines[i].toLowerCase().contains(queryLower)) {
                                    String line = lines[i].length() > 100
                                        ? lines[i].substring(0, 100)
                                        : lines[i];
                                    matchingLines.add("  Line " + (i + 1) + ": " + line);
                                }
                            }

                            results.add(Map.of(
                                "path", filePath.toString(),
                                "matches", matchingLines
                            ));
                        }
                    } catch (IOException ignored) {}
                });
        }

        if (results.isEmpty()) {
            return CallToolResult.text("找不到包含 '" + query + "' 的檔案");
        }

        StringBuilder output = new StringBuilder();
        output.append("搜尋結果：找到 ").append(results.size()).append(" 個檔案包含 '").append(query).append("'\n");
        output.append("=".repeat(50)).append("\n\n");

        for (Map<String, Object> result : results) {
            output.append("📄 ").append(result.get("path")).append("\n");
            @SuppressWarnings("unchecked")
            List<String> matches = (List<String>) result.get("matches");
            for (String match : matches) {
                output.append(match).append("\n");
            }
            output.append("\n");
        }

        return CallToolResult.text(output.toString());
    }

    private CallToolResult handleGetFileInfo(Map<String, Object> args) throws IOException {
        Path path = Path.of((String) args.get("path"));

        if (!isPathAllowed(path)) {
            return CallToolResult.error("錯誤：存取被拒絕");
        }

        if (!Files.exists(path)) {
            return CallToolResult.error("錯誤：路徑不存在 - " + path);
        }

        BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
        String mimeType = Files.probeContentType(path);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

        String info = String.format("""
            檔案資訊：%s
            %s
            類型：%s
            大小：%,d bytes
            MIME 類型：%s
            建立時間：%s
            修改時間：%s
            存取時間：%s
            """,
            path,
            "=".repeat(50),
            Files.isDirectory(path) ? "目錄" : "檔案",
            attrs.size(),
            mimeType != null ? mimeType : "未知",
            formatter.format(attrs.creationTime().toInstant()),
            formatter.format(attrs.lastModifiedTime().toInstant()),
            formatter.format(attrs.lastAccessTime().toInstant())
        );

        return CallToolResult.text(info);
    }

    private CallToolResult handleCreateDirectory(Map<String, Object> args) throws IOException {
        Path path = Path.of((String) args.get("path"));

        if (!isPathAllowed(path)) {
            return CallToolResult.error("錯誤：存取被拒絕");
        }

        Files.createDirectories(path);
        return CallToolResult.text("成功：已建立目錄 - " + path);
    }

    private CallToolResult handleDeleteFile(Map<String, Object> args) throws IOException {
        Path path = Path.of((String) args.get("path"));
        boolean confirm = Boolean.TRUE.equals(args.get("confirm"));

        if (!confirm) {
            return CallToolResult.error("錯誤：請設定 confirm=true 以確認刪除");
        }

        if (!isPathAllowed(path)) {
            return CallToolResult.error("錯誤：存取被拒絕");
        }

        if (!Files.exists(path)) {
            return CallToolResult.error("錯誤：檔案不存在");
        }

        if (Files.isDirectory(path)) {
            return CallToolResult.error("錯誤：不支援刪除目錄");
        }

        Files.delete(path);
        return CallToolResult.text("成功：已刪除檔案 - " + path);
    }

    public void start() throws Exception {
        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
        System.err.println("Filesystem MCP Server 啟動中...");
        System.err.println("允許的目錄：" + allowedDirectories);
    }

    public static void main(String[] args) throws Exception {
        FilesystemMcpServer server = new FilesystemMcpServer();
        server.start();
    }
}
```

#### 5.1.3 測試與除錯步驟

**1. 建置專案**：

```bash
cd filesystem-mcp-server
mvn clean package
```

**2. 使用 MCP Inspector 測試**：

```bash
npx @modelcontextprotocol/inspector java -jar target/filesystem-mcp-server-1.0.0.jar
```

**3. 測試指令**：

```json
// 列出工具
{"jsonrpc": "2.0", "id": 1, "method": "tools/list"}

// 讀取檔案
{
  "jsonrpc": "2.0",
  "id": 2,
  "method": "tools/call",
  "params": {
    "name": "read_file",
    "arguments": {"path": "/tmp/test.txt"}
  }
}

// 列出目錄
{
  "jsonrpc": "2.0",
  "id": 3,
  "method": "tools/call",
  "params": {
    "name": "list_directory",
    "arguments": {"path": "/tmp", "pattern": "*.txt"}
  }
}
```

---

### 5.2 範例二：資料庫查詢 MCP Server

#### 5.2.1 功能說明

提供安全的 PostgreSQL 資料庫操作：

- 列出資料表
- 查看 Schema
- 執行 SELECT 查詢
- 安全的參數化查詢

#### 5.2.2 完整程式碼

**pom.xml**（資料庫 Server）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.mcp</groupId>
    <artifactId>database-mcp-server</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.modelcontextprotocol</groupId>
            <artifactId>mcp-server</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.zaxxer</groupId>
            <artifactId>HikariCP</artifactId>
            <version>5.1.0</version>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <version>42.7.1</version>
        </dependency>
    </dependencies>
</project>
```

**DatabaseMcpServer.java**：

```java
package com.example.mcp.database;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.*;

/**
 * 資料庫查詢 MCP Server
 * 提供安全的 PostgreSQL 資料庫查詢功能
 */
public class DatabaseMcpServer {

    private final McpServer server;
    private final HikariDataSource dataSource;

    // 配置
    private static final int MAX_QUERY_ROWS = Integer.parseInt(
        System.getenv().getOrDefault("MAX_QUERY_ROWS", "100")
    );
    private static final int QUERY_TIMEOUT = Integer.parseInt(
        System.getenv().getOrDefault("QUERY_TIMEOUT", "30")
    );

    // SQL 安全檢查
    private static final Set<String> DANGEROUS_KEYWORDS = Set.of(
        "DROP", "DELETE", "TRUNCATE", "UPDATE", "INSERT",
        "ALTER", "CREATE", "GRANT", "REVOKE", "EXECUTE"
    );

    public DatabaseMcpServer() {
        // 初始化連接池
        String databaseUrl = System.getenv().getOrDefault(
            "DATABASE_URL",
            "jdbc:postgresql://localhost/postgres"
        );

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(databaseUrl);
        config.setMinimumIdle(2);
        config.setMaximumPoolSize(10);
        config.setConnectionTimeout(QUERY_TIMEOUT * 1000L);

        this.dataSource = new HikariDataSource(config);

        // 建立 Server
        McpServerOptions options = McpServerOptions.builder()
            .serverInfo(ServerInfo.builder()
                .name("database-server")
                .version("1.0.0")
                .build())
            .capabilities(ServerCapabilities.builder()
                .tools(ToolsCapability.builder().build())
                .resources(ResourcesCapability.builder().build())
                .build())
            .build();

        this.server = new McpServer(options);
        setupHandlers();
    }

    private record SafeQueryResult(boolean safe, String errorMessage) {}

    private SafeQueryResult isSafeQuery(String query) {
        // 移除註解
        String cleanQuery = query.replaceAll("--.*$", "");
        cleanQuery = cleanQuery.replaceAll("/\\*.*?\\*/", "");
        String upperQuery = cleanQuery.toUpperCase().trim();

        // 必須以 SELECT 開頭
        if (!upperQuery.startsWith("SELECT")) {
            return new SafeQueryResult(false, "僅支援 SELECT 查詢");
        }

        // 檢查危險關鍵字
        for (String keyword : DANGEROUS_KEYWORDS) {
            Pattern pattern = Pattern.compile("\\b" + keyword + "\\b");
            if (pattern.matcher(upperQuery).find()) {
                return new SafeQueryResult(false, "查詢包含不允許的關鍵字：" + keyword);
            }
        }

        // 檢查多語句
        String queryWithoutTrailing = cleanQuery.replaceAll(";\\s*$", "");
        if (queryWithoutTrailing.contains(";")) {
            return new SafeQueryResult(false, "不支援多語句查詢");
        }

        return new SafeQueryResult(true, "");
    }

    private void setupHandlers() {
        // 工具列表處理器
        server.setToolListHandler(request -> CompletableFuture.supplyAsync(() ->
            ListToolsResult.builder()
                .tools(List.of(
                    Tool.builder()
                        .name("list_tables")
                        .description("列出資料庫中的所有資料表")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "schema", Map.of(
                                    "type", "string",
                                    "description", "Schema 名稱（預設 public）",
                                    "default", "public"
                                )
                            )
                        ))
                        .build(),
                    Tool.builder()
                        .name("describe_table")
                        .description("取得資料表的結構資訊（欄位、型別、限制）")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "table_name", Map.of("type", "string", "description", "資料表名稱"),
                                "schema", Map.of("type", "string", "description", "Schema 名稱", "default", "public")
                            ),
                            "required", List.of("table_name")
                        ))
                        .build(),
                    Tool.builder()
                        .name("query")
                        .description("執行 SQL SELECT 查詢（僅支援讀取操作）")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "sql", Map.of("type", "string", "description", "SQL SELECT 查詢語句"),
                                "limit", Map.of("type", "integer", "description", "結果筆數限制", "default", 50)
                            ),
                            "required", List.of("sql")
                        ))
                        .build(),
                    Tool.builder()
                        .name("get_table_sample")
                        .description("取得資料表的範例資料")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "table_name", Map.of("type", "string", "description", "資料表名稱"),
                                "limit", Map.of("type", "integer", "description", "範例筆數", "default", 5)
                            ),
                            "required", List.of("table_name")
                        ))
                        .build(),
                    Tool.builder()
                        .name("get_table_stats")
                        .description("取得資料表的統計資訊")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "table_name", Map.of("type", "string", "description", "資料表名稱")
                            ),
                            "required", List.of("table_name")
                        ))
                        .build()
                ))
                .build()
        ));

        // 工具呼叫處理器
        server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
            String name = request.getParams().getName();
            Map<String, Object> args = request.getParams().getArguments();

            try {
                return switch (name) {
                    case "list_tables" -> handleListTables(args);
                    case "describe_table" -> handleDescribeTable(args);
                    case "query" -> handleQuery(args);
                    case "get_table_sample" -> handleTableSample(args);
                    case "get_table_stats" -> handleTableStats(args);
                    default -> CallToolResult.error("未知的工具：" + name);
                };
            } catch (SQLException e) {
                return CallToolResult.error("資料庫錯誤：" + e.getMessage());
            } catch (Exception e) {
                return CallToolResult.error("錯誤：" + e.getMessage());
            }
        }));
    }

    private CallToolResult handleListTables(Map<String, Object> args) throws SQLException {
        String schema = (String) args.getOrDefault("schema", "public");

        String query = """
            SELECT
                table_name,
                table_type,
                (SELECT count(*) FROM information_schema.columns c
                 WHERE c.table_name = t.table_name
                 AND c.table_schema = t.table_schema) as column_count
            FROM information_schema.tables t
            WHERE table_schema = ?
            ORDER BY table_name
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, schema);
            ResultSet rs = stmt.executeQuery();

            StringBuilder output = new StringBuilder();
            output.append("資料表列表（Schema: ").append(schema).append("）\n");
            output.append("=".repeat(50)).append("\n\n");
            output.append("| 資料表名稱 | 類型 | 欄位數 |\n");
            output.append("| --- | --- | --- |\n");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                output.append("| ").append(rs.getString("table_name"))
                      .append(" | ").append(rs.getString("table_type"))
                      .append(" | ").append(rs.getInt("column_count"))
                      .append(" |\n");
            }

            if (!hasResults) {
                return CallToolResult.text("Schema '" + schema + "' 中沒有資料表");
            }

            return CallToolResult.text(output.toString());
        }
    }

    private CallToolResult handleDescribeTable(Map<String, Object> args) throws SQLException {
        String tableName = (String) args.get("table_name");
        String schema = (String) args.getOrDefault("schema", "public");

        // 驗證資料表名稱（防止 SQL 注入）
        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return CallToolResult.error("錯誤：無效的資料表名稱");
        }

        String columnQuery = """
            SELECT
                column_name,
                data_type,
                character_maximum_length,
                is_nullable,
                column_default
            FROM information_schema.columns
            WHERE table_schema = ? AND table_name = ?
            ORDER BY ordinal_position
            """;

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(columnQuery)) {
            stmt.setString(1, schema);
            stmt.setString(2, tableName);
            ResultSet rs = stmt.executeQuery();

            StringBuilder output = new StringBuilder();
            output.append("資料表結構：").append(schema).append(".").append(tableName).append("\n");
            output.append("=".repeat(60)).append("\n\n");
            output.append("| 欄位名稱 | 資料型別 | 可為空 | 預設值 |\n");
            output.append("| --- | --- | --- | --- |\n");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String dataType = rs.getString("data_type");
                Integer maxLen = rs.getObject("character_maximum_length", Integer.class);
                if (maxLen != null) {
                    dataType += "(" + maxLen + ")";
                }

                output.append("| ").append(rs.getString("column_name"))
                      .append(" | ").append(dataType)
                      .append(" | ").append(rs.getString("is_nullable"))
                      .append(" | ").append(rs.getString("column_default") != null ? rs.getString("column_default") : "-")
                      .append(" |\n");
            }

            if (!hasResults) {
                return CallToolResult.error("資料表 '" + tableName + "' 不存在");
            }

            return CallToolResult.text(output.toString());
        }
    }

    private CallToolResult handleQuery(Map<String, Object> args) throws SQLException {
        String sql = (String) args.get("sql");
        int limit = Math.min(((Number) args.getOrDefault("limit", 50)).intValue(), MAX_QUERY_ROWS);

        // 安全檢查
        SafeQueryResult safeResult = isSafeQuery(sql);
        if (!safeResult.safe()) {
            return CallToolResult.error("查詢被拒絕：" + safeResult.errorMessage());
        }

        // 添加 LIMIT（如果沒有）
        if (!sql.toUpperCase().contains("LIMIT")) {
            sql = sql.replaceAll(";\\s*$", "") + " LIMIT " + limit;
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(QUERY_TIMEOUT);
            ResultSet rs = stmt.executeQuery(sql);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 取得欄位名稱
            List<String> columns = new ArrayList<>();
            for (int i = 1; i <= columnCount; i++) {
                columns.add(metaData.getColumnName(i));
            }

            // 建立輸出
            StringBuilder output = new StringBuilder();
            List<List<String>> rows = new ArrayList<>();

            while (rs.next()) {
                List<String> row = new ArrayList<>();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    String strValue = value != null ? value.toString() : "NULL";
                    if (strValue.length() > 50) {
                        strValue = strValue.substring(0, 50) + "...";
                    }
                    row.add(strValue);
                }
                rows.add(row);
            }

            if (rows.isEmpty()) {
                return CallToolResult.text("查詢無結果");
            }

            output.append("查詢結果（共 ").append(rows.size()).append(" 筆）\n");
            output.append("=".repeat(50)).append("\n\n");
            output.append("| ").append(String.join(" | ", columns)).append(" |\n");
            output.append("| ").append(String.join(" | ", Collections.nCopies(columns.size(), "---"))).append(" |\n");

            for (List<String> row : rows) {
                output.append("| ").append(String.join(" | ", row)).append(" |\n");
            }

            return CallToolResult.text(output.toString());
        }
    }

    private CallToolResult handleTableSample(Map<String, Object> args) throws SQLException {
        String tableName = (String) args.get("table_name");
        int limit = Math.min(((Number) args.getOrDefault("limit", 5)).intValue(), 20);

        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return CallToolResult.error("錯誤：無效的資料表名稱");
        }

        return handleQuery(Map.of(
            "sql", "SELECT * FROM " + tableName,
            "limit", limit
        ));
    }

    private CallToolResult handleTableStats(Map<String, Object> args) throws SQLException {
        String tableName = (String) args.get("table_name");

        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return CallToolResult.error("錯誤：無效的資料表名稱");
        }

        try (Connection conn = dataSource.getConnection()) {
            // 取得行數
            long rowCount;
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT count(*) FROM " + tableName);
                rs.next();
                rowCount = rs.getLong(1);
            }

            // 取得表大小
            String sizeQuery = """
                SELECT
                    pg_size_pretty(pg_total_relation_size(?)) as total_size,
                    pg_size_pretty(pg_relation_size(?)) as table_size,
                    pg_size_pretty(pg_indexes_size(?)) as index_size
                """;

            try (PreparedStatement stmt = conn.prepareStatement(sizeQuery)) {
                stmt.setString(1, tableName);
                stmt.setString(2, tableName);
                stmt.setString(3, tableName);
                ResultSet rs = stmt.executeQuery();
                rs.next();

                StringBuilder output = new StringBuilder();
                output.append("資料表統計：").append(tableName).append("\n");
                output.append("=".repeat(40)).append("\n\n");
                output.append("總筆數：").append(String.format("%,d", rowCount)).append("\n");
                output.append("總大小：").append(rs.getString("total_size")).append("\n");
                output.append("資料大小：").append(rs.getString("table_size")).append("\n");
                output.append("索引大小：").append(rs.getString("index_size")).append("\n");

                return CallToolResult.text(output.toString());
            }
        }
    }

    public void start() throws Exception {
        System.err.println("Database MCP Server 啟動中...");
        System.err.println("資料庫連接池已初始化");

        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public static void main(String[] args) throws Exception {
        DatabaseMcpServer server = new DatabaseMcpServer();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
        server.start();
    }
}
```

#### 5.2.3 安全性考量

```
┌─────────────────────────────────────────────────────────────────┐
│                    資料庫 Server 安全清單                         │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  ✓ 僅允許 SELECT 查詢                                            │
│  ✓ 禁止危險關鍵字（DROP、DELETE、UPDATE 等）                       │
│  ✓ 禁止多語句查詢                                                │
│  ✓ 查詢結果數量限制                                              │
│  ✓ 查詢超時設定                                                  │
│  ✓ 資料表名稱驗證                                                │
│  ✓ 使用連接池管理                                                │
│  ✓ 敏感資訊不輸出到日誌                                          │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

---

### 5.3 範例三：API 整合 MCP Server

#### 5.3.1 功能說明

整合 GitHub API 提供：

- 搜尋儲存庫
- 取得儲存庫資訊
- 列出 Issues
- 讀取檔案內容

#### 5.3.2 完整程式碼

**pom.xml**（GitHub Server）：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example.mcp</groupId>
    <artifactId>github-mcp-server</artifactId>
    <version>1.0.0</version>

    <properties>
        <java.version>17</java.version>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.modelcontextprotocol</groupId>
            <artifactId>mcp-server</artifactId>
            <version>1.0.0</version>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.12.0</version>
        </dependency>
    </dependencies>
</project>
```

**GitHubMcpServer.java**：

````java
package com.example.mcp.github;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerOptions;
import io.modelcontextprotocol.server.transport.StdioServerTransport;
import io.modelcontextprotocol.spec.*;

import com.google.gson.*;
import okhttp3.*;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * GitHub API 整合 MCP Server
 */
public class GitHubMcpServer {

    private final McpServer server;
    private final OkHttpClient httpClient;
    private final Gson gson;

    // GitHub API 配置
    private static final String GITHUB_API_BASE = "https://api.github.com";
    private static final String GITHUB_TOKEN = System.getenv("GITHUB_TOKEN");

    // 速率限制追蹤
    private volatile int rateLimitRemaining = 60;
    private volatile Instant rateLimitReset = null;

    public GitHubMcpServer() {
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
        this.gson = new GsonBuilder().create();

        // 建立 Server
        McpServerOptions options = McpServerOptions.builder()
            .serverInfo(ServerInfo.builder()
                .name("github-server")
                .version("1.0.0")
                .build())
            .capabilities(ServerCapabilities.builder()
                .tools(ToolsCapability.builder().build())
                .build())
            .build();

        this.server = new McpServer(options);
        setupHandlers();
    }

    /**
     * GitHub API 錯誤
     */
    public static class GitHubAPIException extends Exception {
        private final int status;

        public GitHubAPIException(int status, String message) {
            super("GitHub API Error (" + status + "): " + message);
            this.status = status;
        }

        public int getStatus() { return status; }
    }

    /**
     * 發送 GitHub API 請求
     */
    private JsonObject githubRequest(String method, String endpoint, Map<String, String> params)
            throws IOException, GitHubAPIException {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(GITHUB_API_BASE + endpoint).newBuilder();
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }

        Request.Builder requestBuilder = new Request.Builder()
            .url(urlBuilder.build())
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "MCP-GitHub-Server/1.0");

        if (GITHUB_TOKEN != null && !GITHUB_TOKEN.isEmpty()) {
            requestBuilder.header("Authorization", "token " + GITHUB_TOKEN);
        }

        if ("GET".equals(method)) {
            requestBuilder.get();
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            // 更新速率限制
            String remaining = response.header("X-RateLimit-Remaining");
            String reset = response.header("X-RateLimit-Reset");
            if (remaining != null) {
                rateLimitRemaining = Integer.parseInt(remaining);
            }
            if (reset != null) {
                rateLimitReset = Instant.ofEpochSecond(Long.parseLong(reset));
            }

            if (response.code() == 401) {
                throw new GitHubAPIException(401, "認證失敗，請檢查 GITHUB_TOKEN");
            } else if (response.code() == 403) {
                throw new GitHubAPIException(403, "存取被拒絕或已達速率限制（剩餘：" + rateLimitRemaining + "）");
            } else if (response.code() == 404) {
                throw new GitHubAPIException(404, "找不到資源");
            } else if (response.code() >= 400) {
                throw new GitHubAPIException(response.code(), response.body().string());
            }

            String body = response.body().string();
            return JsonParser.parseString(body).getAsJsonObject();
        }
    }

    private JsonArray githubRequestArray(String method, String endpoint, Map<String, String> params)
            throws IOException, GitHubAPIException {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(GITHUB_API_BASE + endpoint).newBuilder();
        if (params != null) {
            params.forEach(urlBuilder::addQueryParameter);
        }

        Request.Builder requestBuilder = new Request.Builder()
            .url(urlBuilder.build())
            .header("Accept", "application/vnd.github.v3+json")
            .header("User-Agent", "MCP-GitHub-Server/1.0");

        if (GITHUB_TOKEN != null && !GITHUB_TOKEN.isEmpty()) {
            requestBuilder.header("Authorization", "token " + GITHUB_TOKEN);
        }

        try (Response response = httpClient.newCall(requestBuilder.build()).execute()) {
            if (response.code() >= 400) {
                throw new GitHubAPIException(response.code(), response.body().string());
            }
            return JsonParser.parseString(response.body().string()).getAsJsonArray();
        }
    }

    private void setupHandlers() {
        // 工具列表處理器
        server.setToolListHandler(request -> CompletableFuture.supplyAsync(() ->
            ListToolsResult.builder()
                .tools(List.of(
                    Tool.builder()
                        .name("search_repositories")
                        .description("搜尋 GitHub 儲存庫")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "query", Map.of("type", "string", "description", "搜尋關鍵字"),
                                "language", Map.of("type", "string", "description", "程式語言過濾"),
                                "sort", Map.of("type", "string", "enum", List.of("stars", "forks", "updated"), "default", "stars"),
                                "limit", Map.of("type", "integer", "description", "結果數量", "default", 10)
                            ),
                            "required", List.of("query")
                        ))
                        .build(),
                    Tool.builder()
                        .name("get_repository")
                        .description("取得儲存庫詳細資訊")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "owner", Map.of("type", "string", "description", "擁有者名稱"),
                                "repo", Map.of("type", "string", "description", "儲存庫名稱")
                            ),
                            "required", List.of("owner", "repo")
                        ))
                        .build(),
                    Tool.builder()
                        .name("list_issues")
                        .description("列出儲存庫的 Issues")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "owner", Map.of("type", "string", "description", "擁有者名稱"),
                                "repo", Map.of("type", "string", "description", "儲存庫名稱"),
                                "state", Map.of("type", "string", "enum", List.of("open", "closed", "all"), "default", "open"),
                                "limit", Map.of("type", "integer", "default", 10)
                            ),
                            "required", List.of("owner", "repo")
                        ))
                        .build(),
                    Tool.builder()
                        .name("get_file_content")
                        .description("取得儲存庫中的檔案內容")
                        .inputSchema(Map.of(
                            "type", "object",
                            "properties", Map.of(
                                "owner", Map.of("type", "string"),
                                "repo", Map.of("type", "string"),
                                "path", Map.of("type", "string", "description", "檔案路徑"),
                                "ref", Map.of("type", "string", "description", "分支或 commit", "default", "main")
                            ),
                            "required", List.of("owner", "repo", "path")
                        ))
                        .build(),
                    Tool.builder()
                        .name("get_rate_limit")
                        .description("取得 GitHub API 速率限制狀態")
                        .inputSchema(Map.of("type", "object", "properties", Map.of()))
                        .build()
                ))
                .build()
        ));

        // 工具呼叫處理器
        server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
            String name = request.getParams().getName();
            Map<String, Object> args = request.getParams().getArguments();

            try {
                return switch (name) {
                    case "search_repositories" -> handleSearchRepos(args);
                    case "get_repository" -> handleGetRepo(args);
                    case "list_issues" -> handleListIssues(args);
                    case "get_file_content" -> handleGetFile(args);
                    case "get_rate_limit" -> handleRateLimit();
                    default -> CallToolResult.error("未知的工具：" + name);
                };
            } catch (GitHubAPIException e) {
                return CallToolResult.error("GitHub API 錯誤：" + e.getMessage());
            } catch (Exception e) {
                return CallToolResult.error("錯誤：" + e.getMessage());
            }
        }));
    }

    private CallToolResult handleSearchRepos(Map<String, Object> args) throws Exception {
        String query = (String) args.get("query");
        String language = (String) args.get("language");
        String sort = (String) args.getOrDefault("sort", "stars");
        int limit = Math.min(((Number) args.getOrDefault("limit", 10)).intValue(), 30);

        String searchQuery = query;
        if (language != null && !language.isEmpty()) {
            searchQuery += " language:" + language;
        }

        JsonObject data = githubRequest("GET", "/search/repositories", Map.of(
            "q", searchQuery,
            "sort", sort,
            "per_page", String.valueOf(limit)
        ));

        JsonArray repos = data.getAsJsonArray("items");

        if (repos == null || repos.isEmpty()) {
            return CallToolResult.text("找不到符合條件的儲存庫");
        }

        StringBuilder output = new StringBuilder();
        output.append("搜尋結果：'").append(query).append("'\n");
        output.append("=".repeat(50)).append("\n\n");

        for (JsonElement item : repos) {
            JsonObject repo = item.getAsJsonObject();
            output.append("### ").append(repo.get("full_name").getAsString()).append("\n");
            output.append("⭐ ").append(String.format("%,d", repo.get("stargazers_count").getAsInt())).append(" | ");
            output.append("🍴 ").append(String.format("%,d", repo.get("forks_count").getAsInt())).append(" | ");
            output.append("📝 ").append(repo.get("language").isJsonNull() ? "N/A" : repo.get("language").getAsString()).append("\n");
            output.append(repo.get("description").isJsonNull() ? "無描述" : repo.get("description").getAsString()).append("\n");
            output.append("🔗 ").append(repo.get("html_url").getAsString()).append("\n\n");
        }

        return CallToolResult.text(output.toString());
    }

    private CallToolResult handleGetRepo(Map<String, Object> args) throws Exception {
        String owner = (String) args.get("owner");
        String repo = (String) args.get("repo");

        JsonObject data = githubRequest("GET", "/repos/" + owner + "/" + repo, null);

        StringBuilder output = new StringBuilder();
        output.append("# ").append(data.get("full_name").getAsString()).append("\n\n");
        output.append("**描述**：").append(data.get("description").isJsonNull() ? "無描述" : data.get("description").getAsString()).append("\n\n");
        output.append("## 統計\n");
        output.append("- ⭐ Stars：").append(String.format("%,d", data.get("stargazers_count").getAsInt())).append("\n");
        output.append("- 🍴 Forks：").append(String.format("%,d", data.get("forks_count").getAsInt())).append("\n");
        output.append("- 👁 Watchers：").append(String.format("%,d", data.get("watchers_count").getAsInt())).append("\n");
        output.append("- 📝 Open Issues：").append(String.format("%,d", data.get("open_issues_count").getAsInt())).append("\n\n");
        output.append("## 資訊\n");
        output.append("- 程式語言：").append(data.get("language").isJsonNull() ? "N/A" : data.get("language").getAsString()).append("\n");
        output.append("- 預設分支：").append(data.get("default_branch").getAsString()).append("\n");
        output.append("- 建立時間：").append(data.get("created_at").getAsString().substring(0, 10)).append("\n");
        output.append("- 最後更新：").append(data.get("updated_at").getAsString().substring(0, 10)).append("\n");

        JsonElement license = data.get("license");
        output.append("- 授權：").append(license.isJsonNull() ? "N/A" : license.getAsJsonObject().get("name").getAsString()).append("\n\n");
        output.append("🔗 **連結**：").append(data.get("html_url").getAsString()).append("\n");

        return CallToolResult.text(output.toString());
    }

    private CallToolResult handleListIssues(Map<String, Object> args) throws Exception {
        String owner = (String) args.get("owner");
        String repo = (String) args.get("repo");
        String state = (String) args.getOrDefault("state", "open");
        int limit = Math.min(((Number) args.getOrDefault("limit", 10)).intValue(), 30);

        JsonArray data = githubRequestArray("GET", "/repos/" + owner + "/" + repo + "/issues",
            Map.of("state", state, "per_page", String.valueOf(limit)));

        if (data.isEmpty()) {
            return CallToolResult.text("沒有 " + state + " 狀態的 Issues");
        }

        StringBuilder output = new StringBuilder();
        output.append("Issues (").append(state).append(") - ").append(owner).append("/").append(repo).append("\n");
        output.append("=".repeat(50)).append("\n\n");

        for (JsonElement item : data) {
            JsonObject issue = item.getAsJsonObject();

            // 排除 Pull Requests
            if (issue.has("pull_request")) continue;

            JsonArray labels = issue.getAsJsonArray("labels");
            String labelStr = labels.isEmpty() ? "無標籤" :
                labels.asList().stream()
                    .map(l -> l.getAsJsonObject().get("name").getAsString())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("無標籤");

            output.append("### #").append(issue.get("number").getAsInt())
                  .append(" ").append(issue.get("title").getAsString()).append("\n");
            output.append("狀態：").append(issue.get("state").getAsString())
                  .append(" | 標籤：").append(labelStr).append("\n");
            output.append("作者：").append(issue.getAsJsonObject("user").get("login").getAsString())
                  .append(" | 建立於：").append(issue.get("created_at").getAsString().substring(0, 10)).append("\n");
            output.append("🔗 ").append(issue.get("html_url").getAsString()).append("\n\n");
        }

        return CallToolResult.text(output.toString());
    }

    private CallToolResult handleGetFile(Map<String, Object> args) throws Exception {
        String owner = (String) args.get("owner");
        String repo = (String) args.get("repo");
        String path = (String) args.get("path");
        String ref = (String) args.getOrDefault("ref", "main");

        JsonObject data = githubRequest("GET",
            "/repos/" + owner + "/" + repo + "/contents/" + path,
            Map.of("ref", ref));

        if (!"file".equals(data.get("type").getAsString())) {
            return CallToolResult.error("指定路徑不是檔案");
        }

        int size = data.get("size").getAsInt();
        if (size > 1024 * 1024) {
            return CallToolResult.error("檔案過大，無法顯示");
        }

        String content = new String(Base64.getDecoder().decode(
            data.get("content").getAsString().replaceAll("\\s", "")));

        StringBuilder output = new StringBuilder();
        output.append("檔案：").append(path).append(" (ref: ").append(ref).append(")\n");
        output.append("=".repeat(50)).append("\n\n");
        output.append("```\n").append(content).append("\n```");

        return CallToolResult.text(output.toString());
    }

    private CallToolResult handleRateLimit() throws Exception {
        JsonObject data = githubRequest("GET", "/rate_limit", null);

        JsonObject core = data.getAsJsonObject("resources").getAsJsonObject("core");
        JsonObject search = data.getAsJsonObject("resources").getAsJsonObject("search");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
        Instant resetTime = Instant.ofEpochSecond(core.get("reset").getAsLong());

        StringBuilder output = new StringBuilder();
        output.append("GitHub API 速率限制狀態\n");
        output.append("=".repeat(40)).append("\n\n");
        output.append("## Core API\n");
        output.append("- 剩餘：").append(core.get("remaining").getAsInt())
              .append("/").append(core.get("limit").getAsInt()).append("\n");
        output.append("- 重置時間：").append(formatter.format(resetTime)).append("\n\n");
        output.append("## Search API\n");
        output.append("- 剩餘：").append(search.get("remaining").getAsInt())
              .append("/").append(search.get("limit").getAsInt()).append("\n");

        return CallToolResult.text(output.toString());
    }

    public void start() throws Exception {
        System.err.println("GitHub MCP Server 啟動中...");

        if (GITHUB_TOKEN == null || GITHUB_TOKEN.isEmpty()) {
            System.err.println("警告：未設定 GITHUB_TOKEN，將使用較低的速率限制");
        }

        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport);
    }

    public static void main(String[] args) throws Exception {
        GitHubMcpServer server = new GitHubMcpServer();
        server.start();
    }
}
````

#### 5.3.3 錯誤處理與重試機制

```java
/**
 * 增強版 API 請求處理（含重試機制）
 * RetryableGitHubClient.java
 */
package com.example.mcp.github;

import java.util.concurrent.*;
import java.util.function.Supplier;

public class RetryableGitHubClient {

    private final int maxRetries;
    private final double backoffFactor;

    public RetryableGitHubClient(int maxRetries, double backoffFactor) {
        this.maxRetries = maxRetries;
        this.backoffFactor = backoffFactor;
    }

    /**
     * 帶重試機制的請求執行
     */
    public <T> T executeWithRetry(Supplier<T> action) throws Exception {
        Exception lastException = null;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                return action.get();
            } catch (GitHubMcpServer.GitHubAPIException e) {
                // 不重試的錯誤
                if (e.getStatus() == 401 || e.getStatus() == 403 || e.getStatus() == 404) {
                    throw e;
                }
                lastException = e;
            } catch (Exception e) {
                lastException = e;
            }

            // 計算等待時間（指數退避）
            long waitTime = (long) (backoffFactor * Math.pow(2, attempt) * 1000);
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("重試被中斷", ie);
            }
        }

        throw new RuntimeException("重試次數已達上限", lastException);
    }

    /**
     * 非同步版本
     */
    public <T> CompletableFuture<T> executeWithRetryAsync(Supplier<T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return executeWithRetry(action);
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });
    }
}
```

---

_（第五章完結，繼續第六章）_

---

## 第六章：最佳實踐與設計模式

### 6.1 MCP Server 設計原則

#### 6.1.1 單一職責原則

每個 MCP Server 應該專注於一個領域或功能集：

```
✅ 好的設計：
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────┐
│ Filesystem      │  │ Database        │  │ GitHub          │
│ Server          │  │ Server          │  │ Server          │
│                 │  │                 │  │                 │
│ - read_file     │  │ - query         │  │ - search_repos  │
│ - write_file    │  │ - list_tables   │  │ - get_issues    │
│ - list_dir      │  │ - describe      │  │ - get_commits   │
└─────────────────┘  └─────────────────┘  └─────────────────┘

❌ 不好的設計：
┌───────────────────────────────────────────────────────────┐
│                   Everything Server                        │
│                                                            │
│ - read_file, write_file, query_db, search_github,         │
│   send_email, parse_json, encrypt_data, ...               │
└───────────────────────────────────────────────────────────┘
```

#### 6.1.2 工具命名規範

| 規範           | 說明             | 好的範例                   | 不好的範例               |
| -------------- | ---------------- | -------------------------- | ------------------------ |
| **動詞開頭**   | 明確表達動作     | `read_file`, `create_user` | `file`, `user`           |
| **snake_case** | 使用底線分隔     | `list_directory`           | `listDirectory`          |
| **具體描述**   | 避免模糊名稱     | `search_by_name`           | `search`                 |
| **一致性**     | 同類工具統一前綴 | `db_query`, `db_insert`    | `query`, `insert_record` |

**命名模板**：

```
{action}_{target}[_{qualifier}]

範例：
- read_file          # 動作 + 目標
- search_users_by_name  # 動作 + 目標 + 限定詞
- list_active_orders   # 動作 + 修飾詞 + 目標
```

#### 6.1.3 參數設計與驗證

**好的參數設計**：

```java
Tool.builder()
    .name("search_records")
    .description("搜尋資料庫記錄")
    .inputSchema(Map.of(
        "type", "object",
        "properties", Map.of(
            // 必填參數放前面
            "table", Map.of(
                "type", "string",
                "description", "資料表名稱",
                "pattern", "^[a-zA-Z_][a-zA-Z0-9_]*$"  // 正則驗證
            ),
            "query", Map.of(
                "type", "string",
                "description", "搜尋關鍵字",
                "minLength", 1,
                "maxLength", 100
            ),
            // 選填參數有預設值
            "limit", Map.of(
                "type", "integer",
                "description", "結果數量限制（1-100）",
                "default", 20,
                "minimum", 1,
                "maximum", 100
            ),
            "offset", Map.of(
                "type", "integer",
                "description", "跳過的記錄數",
                "default", 0,
                "minimum", 0
            ),
            "sort_by", Map.of(
                "type", "string",
                "description", "排序欄位",
                "enum", List.of("created_at", "updated_at", "name")
            ),
            "sort_order", Map.of(
                "type", "string",
                "description", "排序方向",
                "enum", List.of("asc", "desc"),
                "default", "desc"
            )
        ),
        "required", List.of("table", "query"),
        "additionalProperties", false  // 禁止額外參數
    ))
    .build()
```

**參數驗證實作**：

```java
import com.networknt.schema.*;
import com.fasterxml.jackson.databind.*;

/**
 * 工具參數驗證器
 */
public class ArgumentValidator {

    private final JsonSchemaFactory schemaFactory;
    private final ObjectMapper objectMapper;

    public ArgumentValidator() {
        this.schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 驗證工具參數
     */
    public ValidationResult validate(Map<String, Object> schema, Map<String, Object> arguments) {
        try {
            JsonSchema jsonSchema = schemaFactory.getSchema(
                objectMapper.writeValueAsString(schema)
            );

            JsonNode argumentsNode = objectMapper.valueToTree(arguments);
            Set<ValidationMessage> errors = jsonSchema.validate(argumentsNode);

            if (errors.isEmpty()) {
                return new ValidationResult(true, "");
            } else {
                String errorMsg = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .reduce((a, b) -> a + "; " + b)
                    .orElse("驗證失敗");
                return new ValidationResult(false, errorMsg);
            }
        } catch (Exception e) {
            return new ValidationResult(false, "Schema 處理錯誤：" + e.getMessage());
        }
    }

    public record ValidationResult(boolean isValid, String error) {}
}

// 在工具處理器中使用
server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
    String name = request.getParams().getName();
    Map<String, Object> arguments = request.getParams().getArguments();

    // 取得工具定義
    Tool tool = findToolByName(name);
    if (tool == null) {
        return CallToolResult.error("未知工具：" + name);
    }

    // 驗證參數
    ArgumentValidator validator = new ArgumentValidator();
    var result = validator.validate(tool.getInputSchema(), arguments);
    if (!result.isValid()) {
        return CallToolResult.error("參數驗證失敗：" + result.error());
    }

    // 處理工具呼叫...
    return handleToolCall(name, arguments);
}));
```

#### 6.1.4 錯誤處理標準

```java
/**
 * 標準化錯誤處理模式
 */
package com.example.mcp.error;

import io.modelcontextprotocol.spec.CallToolResult;
import java.util.*;
import java.util.function.Supplier;

/**
 * 錯誤代碼枚舉
 */
public enum ErrorCode {
    VALIDATION_ERROR("VALIDATION_ERROR"),
    NOT_FOUND("NOT_FOUND"),
    PERMISSION_DENIED("PERMISSION_DENIED"),
    RATE_LIMITED("RATE_LIMITED"),
    TIMEOUT("TIMEOUT"),
    INTERNAL_ERROR("INTERNAL_ERROR");

    private final String value;

    ErrorCode(String value) {
        this.value = value;
    }

    public String getValue() { return value; }
}

/**
 * 工具錯誤結構
 */
public record ToolError(
    ErrorCode code,
    String message,
    Map<String, Object> details
) {
    public ToolError(ErrorCode code, String message) {
        this(code, message, null);
    }

    /**
     * 轉換為 CallToolResult
     */
    public CallToolResult toCallToolResult() {
        StringBuilder errorText = new StringBuilder();
        errorText.append("❌ 錯誤 [").append(code.getValue()).append("]\n");
        errorText.append("訊息：").append(message).append("\n");
        if (details != null && !details.isEmpty()) {
            errorText.append("詳細資訊：").append(details).append("\n");
        }
        return CallToolResult.error(errorText.toString());
    }
}

/**
 * 錯誤處理工具類
 */
public class ToolErrorHandler {

    /**
     * 包裝工具執行，統一處理錯誤
     */
    public static CallToolResult handleToolExecution(Supplier<CallToolResult> action) {
        try {
            return action.get();
        } catch (java.nio.file.NoSuchFileException e) {
            return new ToolError(ErrorCode.NOT_FOUND, e.getMessage()).toCallToolResult();
        } catch (java.nio.file.AccessDeniedException e) {
            return new ToolError(ErrorCode.PERMISSION_DENIED, e.getMessage()).toCallToolResult();
        } catch (java.util.concurrent.TimeoutException e) {
            return new ToolError(ErrorCode.TIMEOUT, "操作超時").toCallToolResult();
        } catch (Exception e) {
            return new ToolError(
                ErrorCode.INTERNAL_ERROR,
                "內部錯誤",
                Map.of("exception", e.getMessage())
            ).toCallToolResult();
        }
    }
}

// 使用範例
server.setToolHandler(request -> CompletableFuture.supplyAsync(() ->
    ToolErrorHandler.handleToolExecution(() -> {
        String name = request.getParams().getName();
        return switch (name) {
            case "read_file" -> handleReadFile(request.getParams().getArguments());
            case "write_file" -> handleWriteFile(request.getParams().getArguments());
            default -> CallToolResult.error("未知工具：" + name);
        };
    })
));
```

---

### 6.2 效能優化

#### 6.2.1 連接池管理

```java
/**
 * 連接池管理最佳實踐
 */
package com.example.mcp.pool;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.Duration;

/**
 * 通用連接池管理器（抽象類）
 */
public abstract class ConnectionPool<T> implements AutoCloseable {

    private final int minSize;
    private final int maxSize;
    private final Duration timeout;

    private final BlockingQueue<T> pool;
    private final AtomicInteger currentSize;
    private final Object lock = new Object();
    private volatile boolean closed = false;

    protected ConnectionPool(int minSize, int maxSize, Duration timeout) {
        this.minSize = minSize;
        this.maxSize = maxSize;
        this.timeout = timeout;
        this.pool = new LinkedBlockingQueue<>(maxSize);
        this.currentSize = new AtomicInteger(0);
    }

    /**
     * 初始化連接池
     */
    public void initialize() throws Exception {
        for (int i = 0; i < minSize; i++) {
            T conn = createConnection();
            pool.put(conn);
            currentSize.incrementAndGet();
        }
    }

    /**
     * 建立新連接（子類實作）
     */
    protected abstract T createConnection() throws Exception;

    /**
     * 驗證連接是否有效（子類實作）
     */
    protected abstract boolean validateConnection(T conn);

    /**
     * 關閉連接（子類實作）
     */
    protected abstract void closeConnection(T conn);

    /**
     * 取得連接（try-with-resources 友好）
     */
    public PooledConnection<T> acquire() throws Exception {
        if (closed) {
            throw new IllegalStateException("連接池已關閉");
        }

        T conn = null;

        // 嘗試從池中取得連接
        conn = pool.poll(timeout.toMillis(), TimeUnit.MILLISECONDS);

        if (conn == null) {
            // 如果沒有可用連接，嘗試建立新的
            synchronized (lock) {
                if (currentSize.get() < maxSize) {
                    conn = createConnection();
                    currentSize.incrementAndGet();
                } else {
                    throw new TimeoutException("連接池已滿，無法取得連接");
                }
            }
        }

        // 驗證連接
        if (!validateConnection(conn)) {
            closeConnection(conn);
            currentSize.decrementAndGet();
            conn = createConnection();
            currentSize.incrementAndGet();
        }

        final T finalConn = conn;
        return new PooledConnection<>(finalConn, this::release);
    }

    /**
     * 歸還連接
     */
    private void release(T conn) {
        if (!closed && conn != null) {
            pool.offer(conn);
        }
    }

    @Override
    public void close() {
        closed = true;
        T conn;
        while ((conn = pool.poll()) != null) {
            closeConnection(conn);
            currentSize.decrementAndGet();
        }
    }

    /**
     * 可關閉的連接包裝器
     */
    public static class PooledConnection<T> implements AutoCloseable {
        private final T connection;
        private final java.util.function.Consumer<T> releaser;
        private boolean released = false;

        PooledConnection(T connection, java.util.function.Consumer<T> releaser) {
            this.connection = connection;
            this.releaser = releaser;
        }

        public T get() { return connection; }

        @Override
        public void close() {
            if (!released) {
                released = true;
                releaser.accept(connection);
            }
        }
    }
}

/**
 * PostgreSQL 連接池實作（使用 HikariCP 更佳）
 */
public class PostgresPool extends ConnectionPool<java.sql.Connection> {

    private final String jdbcUrl;
    private final String username;
    private final String password;

    public PostgresPool(String jdbcUrl, String username, String password,
                        int minSize, int maxSize, Duration timeout) {
        super(minSize, maxSize, timeout);
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
    }

    @Override
    protected java.sql.Connection createConnection() throws Exception {
        return java.sql.DriverManager.getConnection(jdbcUrl, username, password);
    }

    @Override
    protected boolean validateConnection(java.sql.Connection conn) {
        try {
            return conn.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void closeConnection(java.sql.Connection conn) {
        try {
            conn.close();
        } catch (Exception ignored) {}
    }
}
```

#### 6.2.2 快取策略

```java
/**
 * 快取策略實作
 */
package com.example.mcp.cache;

import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

/**
 * 快取項目
 */
record CacheEntry<V>(V value, Instant expiresAt) {
    boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}

/**
 * 非同步快取
 */
public class AsyncCache<V> {

    private final Duration defaultTtl;
    private final int maxSize;
    private final ConcurrentHashMap<String, CacheEntry<V>> cache;
    private final ScheduledExecutorService cleanupExecutor;

    public AsyncCache(Duration defaultTtl, int maxSize) {
        this.defaultTtl = defaultTtl;
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>();

        // 定期清理過期項目
        this.cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanup,
            1, 1, TimeUnit.MINUTES
        );
    }

    /**
     * 生成快取鍵
     */
    public String makeKey(Object... parts) {
        try {
            String keyData = Arrays.toString(parts);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(keyData.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return Arrays.toString(parts).hashCode() + "";
        }
    }

    /**
     * 取得快取值
     */
    public CompletableFuture<Optional<V>> get(String key) {
        return CompletableFuture.supplyAsync(() -> {
            CacheEntry<V> entry = cache.get(key);
            if (entry != null && !entry.isExpired()) {
                return Optional.of(entry.value());
            } else if (entry != null) {
                cache.remove(key);
            }
            return Optional.empty();
        });
    }

    /**
     * 設定快取值
     */
    public CompletableFuture<Void> set(String key, V value, Duration ttl) {
        return CompletableFuture.runAsync(() -> {
            // 清理過期項目（如果接近上限）
            if (cache.size() >= maxSize) {
                cleanup();
            }

            Instant expiresAt = Instant.now().plus(ttl != null ? ttl : defaultTtl);
            cache.put(key, new CacheEntry<>(value, expiresAt));
        });
    }

    public CompletableFuture<Void> set(String key, V value) {
        return set(key, value, null);
    }

    /**
     * 清理過期快取
     */
    private void cleanup() {
        Instant now = Instant.now();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    /**
     * 關閉快取
     */
    public void close() {
        cleanupExecutor.shutdown();
        cache.clear();
    }
}

/**
 * 帶快取的函數包裝器
 */
public class CachedFunction<T> {

    private final AsyncCache<T> cache;
    private final Duration ttl;

    public CachedFunction(AsyncCache<T> cache, Duration ttl) {
        this.cache = cache;
        this.ttl = ttl;
    }

    /**
     * 帶快取執行函數
     */
    public CompletableFuture<T> execute(
        Supplier<CompletableFuture<T>> function,
        Object... keyParts
    ) {
        String key = cache.makeKey(keyParts);

        return cache.get(key).thenCompose(cached -> {
            if (cached.isPresent()) {
                return CompletableFuture.completedFuture(cached.get());
            }

            return function.get().thenCompose(result ->
                cache.set(key, result, ttl).thenApply(v -> result)
            );
        });
    }
}

// 使用範例
public class GitHubService {

    private final AsyncCache<Map<String, Object>> cache =
        new AsyncCache<>(Duration.ofMinutes(5), 1000);
    private final CachedFunction<Map<String, Object>> cachedFunction =
        new CachedFunction<>(cache, Duration.ofMinutes(1));

    /**
     * 取得儲存庫資訊（帶快取）
     */
    public CompletableFuture<Map<String, Object>> getRepositoryInfo(String owner, String repo) {
        return cachedFunction.execute(
            () -> githubRequest("GET", "/repos/" + owner + "/" + repo),
            "getRepositoryInfo", owner, repo
        );
    }
}
```

#### 6.2.3 批次處理

```java
/**
 * 批次處理模式
 */
package com.example.mcp.batch;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.*;

public class BatchProcessor {

    /**
     * 批次處理項目
     *
     * @param items 要處理的項目列表
     * @param processor 處理函數
     * @param batchSize 批次大小
     * @param delayBetweenBatches 批次間延遲
     * @return 處理結果列表
     */
    public static <T, R> CompletableFuture<List<R>> batchProcess(
        List<T> items,
        Function<T, CompletableFuture<R>> processor,
        int batchSize,
        Duration delayBetweenBatches
    ) {
        if (items.isEmpty()) {
            return CompletableFuture.completedFuture(Collections.emptyList());
        }

        List<List<T>> batches = partition(items, batchSize);
        List<R> results = new CopyOnWriteArrayList<>();

        return processBatchesSequentially(batches, processor, delayBetweenBatches, results)
            .thenApply(v -> new ArrayList<>(results));
    }

    /**
     * 分割列表為批次
     */
    private static <T> List<List<T>> partition(List<T> list, int size) {
        List<List<T>> partitions = new ArrayList<>();
        for (int i = 0; i < list.size(); i += size) {
            partitions.add(list.subList(i, Math.min(i + size, list.size())));
        }
        return partitions;
    }

    /**
     * 順序處理批次
     */
    private static <T, R> CompletableFuture<Void> processBatchesSequentially(
        List<List<T>> batches,
        Function<T, CompletableFuture<R>> processor,
        Duration delay,
        List<R> results
    ) {
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);

        for (int i = 0; i < batches.size(); i++) {
            final List<T> batch = batches.get(i);
            final boolean isLastBatch = (i == batches.size() - 1);

            future = future.thenCompose(v -> {
                // 並行處理批次內的項目
                List<CompletableFuture<R>> futures = batch.stream()
                    .map(item -> processor.apply(item)
                        .exceptionally(e -> null))  // 處理單項錯誤
                    .collect(Collectors.toList());

                return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        futures.forEach(f -> {
                            try {
                                R result = f.get();
                                if (result != null) results.add(result);
                            } catch (Exception ignored) {}
                        });
                    });
            });

            // 批次間延遲（最後一批不延遲）
            if (!isLastBatch) {
                future = future.thenCompose(v ->
                    CompletableFuture.runAsync(() -> {
                        try {
                            Thread.sleep(delay.toMillis());
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    })
                );
            }
        }

        return future;
    }
}

// 使用範例
public class FileProcessor {

    /**
     * 批次處理檔案
     */
    public CompletableFuture<List<FileInfo>> processFiles(List<String> filePaths) {
        return BatchProcessor.batchProcess(
            filePaths,
            this::readAndAnalyze,
            10,                          // 批次大小
            Duration.ofMillis(100)       // 批次間延遲
        );
    }

    private CompletableFuture<FileInfo> readAndAnalyze(String path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String content = java.nio.file.Files.readString(java.nio.file.Path.of(path));
                return new FileInfo(
                    path,
                    content.length(),
                    content.lines().count()
                );
            } catch (Exception e) {
                return null;
            }
        });
    }

    public record FileInfo(String path, long size, long lines) {}
}
```

---

### 6.3 安全性考量

#### 6.3.1 認證與授權

```java
/**
 * 認證授權模式
 */
package com.example.mcp.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.*;
import java.util.*;

/**
 * 認證錯誤
 */
public class AuthError extends Exception {
    public AuthError(String message) {
        super(message);
    }
}

/**
 * JWT Token 管理器
 */
public class TokenManager {

    private final Key secretKey;
    private final Duration tokenExpiry;

    public TokenManager(String secret, Duration tokenExpiry) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenExpiry = tokenExpiry;
    }

    public TokenManager() {
        this(
            System.getenv().getOrDefault("MCP_SECRET_KEY", "change-me-in-production-with-at-least-32-chars"),
            Duration.ofHours(24)
        );
    }

    /**
     * 生成 JWT Token
     */
    public String generateToken(String userId, List<String> permissions) {
        Instant now = Instant.now();

        return Jwts.builder()
            .setSubject(userId)
            .claim("permissions", permissions)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(tokenExpiry)))
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * 驗證 JWT Token
     */
    public TokenPayload verifyToken(String token) throws AuthError {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            @SuppressWarnings("unchecked")
            List<String> permissions = claims.get("permissions", List.class);

            return new TokenPayload(
                claims.getSubject(),
                permissions != null ? permissions : List.of(),
                claims.getIssuedAt().toInstant(),
                claims.getExpiration().toInstant()
            );
        } catch (ExpiredJwtException e) {
            throw new AuthError("Token 已過期");
        } catch (JwtException e) {
            throw new AuthError("無效的 Token");
        }
    }

    public record TokenPayload(
        String userId,
        List<String> permissions,
        Instant issuedAt,
        Instant expiresAt
    ) {}
}

/**
 * 權限檢查器
 */
public class PermissionChecker {

    private final TokenManager tokenManager;

    // 工具層級權限對應
    private static final Map<String, List<String>> TOOL_PERMISSIONS = Map.of(
        "read_file", List.of("file:read"),
        "write_file", List.of("file:write"),
        "delete_file", List.of("file:delete"),
        "query", List.of("db:read"),
        "execute", List.of("db:write")
    );

    public PermissionChecker(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    /**
     * 檢查工具權限
     */
    public void checkToolPermission(String toolName, String token) throws AuthError {
        if (token == null || token.isEmpty()) {
            throw new AuthError("需要認證");
        }

        TokenManager.TokenPayload payload = tokenManager.verifyToken(token);
        List<String> requiredPermissions = TOOL_PERMISSIONS.getOrDefault(toolName, List.of());

        for (String perm : requiredPermissions) {
            if (!payload.permissions().contains(perm)) {
                throw new AuthError("缺少權限：" + perm);
            }
        }
    }
}

// 在工具處理器中使用
server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
    String name = request.getParams().getName();
    String authToken = getAuthTokenFromContext();  // 從 context 取得

    try {
        permissionChecker.checkToolPermission(name, authToken);
    } catch (AuthError e) {
        return CallToolResult.error("權限不足：" + e.getMessage());
    }

    // 執行工具...
    return handleToolCall(name, request.getParams().getArguments());
}));
```

#### 6.3.2 輸入驗證

```java
/**
 * 輸入驗證模式
 */
package com.example.mcp.security;

import java.nio.file.Path;
import java.util.List;
import java.util.regex.*;

/**
 * 輸入驗證器
 */
public class InputValidator {

    /**
     * 清理字串
     */
    public static String sanitizeString(String value, int maxLength) {
        if (value == null) return "";

        // 截斷長度
        String result = value.length() > maxLength ? value.substring(0, maxLength) : value;

        // HTML 編碼
        return result
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#x27;");
    }

    public static String sanitizeString(String value) {
        return sanitizeString(value, 1000);
    }

    /**
     * 驗證路徑安全性
     */
    public static ValidationResult validatePath(String pathStr, List<String> allowedRoots) {
        try {
            Path resolved = Path.of(pathStr).toAbsolutePath().normalize();

            // 檢查路徑遍歷
            for (String root : allowedRoots) {
                Path rootPath = Path.of(root).toAbsolutePath().normalize();
                if (resolved.startsWith(rootPath)) {
                    return new ValidationResult(true, resolved.toString());
                }
            }

            return new ValidationResult(false, "路徑不在允許範圍內");
        } catch (Exception e) {
            return new ValidationResult(false, "無效的路徑：" + e.getMessage());
        }
    }

    /**
     * 驗證 SQL 安全性
     */
    public static ValidationResult validateSql(String query) {
        if (query == null || query.isBlank()) {
            return new ValidationResult(false, "查詢不能為空");
        }

        // 移除註解
        String cleanQuery = query
            .replaceAll("--.*$", "")
            .replaceAll("/\\*.*?\\*/", "");
        String upperQuery = cleanQuery.toUpperCase().trim();

        // 僅允許 SELECT
        if (!upperQuery.startsWith("SELECT")) {
            return new ValidationResult(false, "僅允許 SELECT 查詢");
        }

        // 檢查危險關鍵字
        List<String> dangerous = List.of("DROP", "DELETE", "TRUNCATE", "UPDATE", "INSERT", "ALTER");
        for (String kw : dangerous) {
            Pattern pattern = Pattern.compile("\\b" + kw + "\\b", Pattern.CASE_INSENSITIVE);
            if (pattern.matcher(upperQuery).find()) {
                return new ValidationResult(false, "不允許的關鍵字：" + kw);
            }
        }

        return new ValidationResult(true, "");
    }

    /**
     * 驗證識別符（表名、欄位名）
     */
    public static ValidationResult validateIdentifier(String name, int maxLength) {
        if (name == null || name.isEmpty()) {
            return new ValidationResult(false, "識別符不能為空");
        }

        if (name.length() > maxLength) {
            return new ValidationResult(false, "識別符過長（最大 " + maxLength + "）");
        }

        if (!name.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            return new ValidationResult(false, "識別符包含非法字元");
        }

        return new ValidationResult(true, "");
    }

    public static ValidationResult validateIdentifier(String name) {
        return validateIdentifier(name, 63);
    }

    public record ValidationResult(boolean isValid, String message) {}
}
```

#### 6.3.3 敏感資料處理

```java
/**
 * 敏感資料處理
 */
package com.example.mcp.security;

import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * 敏感資料處理器
 */
public class SensitiveDataHandler {

    // 敏感欄位模式
    private static final List<String> SENSITIVE_PATTERNS = List.of(
        "password", "secret", "token", "api_key", "apikey",
        "authorization", "credential", "private_key"
    );

    private static final Pattern SENSITIVE_REGEX = Pattern.compile(
        String.join("|", SENSITIVE_PATTERNS),
        Pattern.CASE_INSENSITIVE
    );

    /**
     * 遮罩敏感值
     */
    public static String maskSensitiveValue(String value, int visibleChars) {
        if (value == null) return null;
        if (value.length() <= visibleChars) {
            return "*".repeat(value.length());
        }
        return value.substring(0, visibleChars) + "*".repeat(value.length() - visibleChars);
    }

    public static String maskSensitiveValue(String value) {
        return maskSensitiveValue(value, 4);
    }

    /**
     * 清理資料以供日誌記錄
     */
    @SuppressWarnings("unchecked")
    public static Object sanitizeForLogging(Object data, int depth) {
        if (depth > 10) {
            return "<max depth reached>";
        }

        if (data instanceof Map) {
            Map<String, Object> result = new LinkedHashMap<>();
            Map<String, Object> map = (Map<String, Object>) data;

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (SENSITIVE_REGEX.matcher(key).find()) {
                    result.put(key, maskSensitiveValue(String.valueOf(value)));
                } else {
                    result.put(key, sanitizeForLogging(value, depth + 1));
                }
            }
            return result;
        } else if (data instanceof List) {
            List<Object> result = new ArrayList<>();
            for (Object item : (List<?>) data) {
                result.add(sanitizeForLogging(item, depth + 1));
            }
            return result;
        } else if (data instanceof String) {
            String str = (String) data;
            // 檢查是否像 Token
            if (str.length() > 20 && str.matches("^[A-Za-z0-9_\\-]+$")) {
                return maskSensitiveValue(str);
            }
            return str;
        }

        return data;
    }

    public static Object sanitizeForLogging(Object data) {
        return sanitizeForLogging(data, 0);
    }
}

/**
 * 安全日誌類別
 */
public class SecureLogger {

    private final Logger logger;

    public SecureLogger(String name) {
        this.logger = Logger.getLogger(name);
    }

    public void info(String message, Object data) {
        if (data != null) {
            data = SensitiveDataHandler.sanitizeForLogging(data);
        }
        logger.info(data != null ? message + " " + data : message);
    }

    public void info(String message) {
        info(message, null);
    }

    public void error(String message, Object data) {
        if (data != null) {
            data = SensitiveDataHandler.sanitizeForLogging(data);
        }
        logger.severe(data != null ? message + " " + data : message);
    }

    public void error(String message) {
        error(message, null);
    }
}
```

#### 6.3.4 速率限制

```java
/**
 * 速率限制實作
 */
package com.example.mcp.security;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 滑動窗口速率限制器
 */
public class RateLimiter {

    private final int maxRequests;
    private final Duration windowDuration;
    private final ConcurrentHashMap<String, Deque<Instant>> requests;

    public RateLimiter(int maxRequests, Duration windowDuration) {
        this.maxRequests = maxRequests;
        this.windowDuration = windowDuration;
        this.requests = new ConcurrentHashMap<>();
    }

    public RateLimiter() {
        this(100, Duration.ofMinutes(1));
    }

    /**
     * 檢查請求是否允許
     */
    public synchronized RateLimitResult isAllowed(String key) {
        Instant now = Instant.now();
        Instant windowStart = now.minus(windowDuration);

        // 取得或建立請求佇列
        Deque<Instant> keyRequests = requests.computeIfAbsent(key, k -> new ConcurrentLinkedDeque<>());

        // 清理過期記錄
        while (!keyRequests.isEmpty() && keyRequests.peekFirst().isBefore(windowStart)) {
            keyRequests.pollFirst();
        }

        int currentCount = keyRequests.size();
        int remaining = Math.max(0, maxRequests - currentCount);
        Instant resetAt = now.plus(windowDuration);

        RateLimitInfo info = new RateLimitInfo(maxRequests, remaining, resetAt);

        if (currentCount >= maxRequests) {
            return new RateLimitResult(false, info);
        }

        keyRequests.addLast(now);
        return new RateLimitResult(true, new RateLimitInfo(maxRequests, remaining - 1, resetAt));
    }

    public record RateLimitInfo(int limit, int remaining, Instant resetAt) {}
    public record RateLimitResult(boolean allowed, RateLimitInfo info) {}
}

// 在工具處理器中使用
public class RateLimitedToolHandler {

    private final RateLimiter rateLimiter = new RateLimiter(100, Duration.ofMinutes(1));

    public CompletableFuture<CallToolResult> handleToolCall(
        String toolName,
        Map<String, Object> arguments,
        String rateLimitKey
    ) {
        return CompletableFuture.supplyAsync(() -> {
            // 檢查速率限制
            RateLimiter.RateLimitResult result = rateLimiter.isAllowed(rateLimitKey);

            if (!result.allowed()) {
                return CallToolResult.error(String.format(
                    "速率限制：請稍後再試%n剩餘配額：%d/%d",
                    result.info().remaining(),
                    result.info().limit()
                ));
            }

            // 執行工具...
            return executeToolInternal(toolName, arguments);
        });
    }

    private CallToolResult executeToolInternal(String name, Map<String, Object> args) {
        // 實際工具執行邏輯
        return CallToolResult.text("執行結果");
    }
}
```

---

### 6.4 測試策略

#### 6.4.1 單元測試

```java
/**
 * MCP Server 單元測試範例
 */
package com.example.mcp.test;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 工具測試
 */
class ToolsTest {

    @Test
    void testListTools() throws Exception {
        // 取得工具列表
        List<Tool> tools = server.listTools().get();

        assertFalse(tools.isEmpty());
        assertTrue(tools.stream().allMatch(t -> t.getName() != null));
        assertTrue(tools.stream().allMatch(t -> t.getDescription() != null));
        assertTrue(tools.stream().allMatch(t -> t.getInputSchema() != null));
    }

    @Test
    void testReadFileSuccess(@TempDir Path tempDir) throws Exception {
        // 建立測試檔案
        Path testFile = tempDir.resolve("test.txt");
        Files.writeString(testFile, "Hello, World!");

        // 模擬允許的路徑
        List<String> originalAllowed = ALLOWED_DIRECTORIES;
        try {
            ALLOWED_DIRECTORIES = List.of(tempDir.toString());

            CallToolResult result = handleReadFile(Map.of("path", testFile.toString())).get();

            assertTrue(result.isSuccess());
            assertEquals("Hello, World!", result.getText());
        } finally {
            ALLOWED_DIRECTORIES = originalAllowed;
        }
    }

    @Test
    void testReadFileNotFound() throws Exception {
        CallToolResult result = handleReadFile(Map.of("path", "/nonexistent/file.txt")).get();

        assertTrue(result.isError());
        assertTrue(result.getText().contains("不存在") || result.getText().contains("錯誤"));
    }

    @Test
    void testReadFilePermissionDenied(@TempDir Path tempDir) throws Exception {
        // 設定不允許的路徑
        List<String> originalAllowed = ALLOWED_DIRECTORIES;
        try {
            ALLOWED_DIRECTORIES = List.of("/other/path");

            Path testFile = tempDir.resolve("test.txt");
            Files.writeString(testFile, "content");

            CallToolResult result = handleReadFile(Map.of("path", testFile.toString())).get();

            assertTrue(result.isError());
            assertTrue(result.getText().contains("拒絕") || result.getText().contains("錯誤"));
        } finally {
            ALLOWED_DIRECTORIES = originalAllowed;
        }
    }

    @Test
    void testQueryValidation() {
        // 有效查詢
        var validResult = InputValidator.validateSql("SELECT * FROM users WHERE id = 1");
        assertTrue(validResult.isValid());

        // 無效查詢 - DROP
        var dropResult = InputValidator.validateSql("DROP TABLE users");
        assertFalse(dropResult.isValid());
        assertTrue(dropResult.message().contains("DROP") || dropResult.message().contains("SELECT"));

        // 無效查詢 - 多語句
        var multiResult = InputValidator.validateSql("SELECT 1; DELETE FROM users");
        assertFalse(multiResult.isValid());
    }
}

/**
 * 輸入驗證測試
 */
class InputValidationTest {

    @Test
    void testValidatePathSafe() {
        var result = InputValidator.validatePath(
            "/home/user/documents/file.txt",
            List.of("/home/user")
        );
        assertTrue(result.isValid());
    }

    @Test
    void testValidatePathTraversal() {
        // 測試路徑遍歷攻擊
        var result = InputValidator.validatePath(
            "/home/user/../../../etc/passwd",
            List.of("/home/user")
        );
        assertFalse(result.isValid());
    }

    @Test
    void testValidateIdentifierValid() {
        // 測試有效識別符
        var result = InputValidator.validateIdentifier("user_table");
        assertTrue(result.isValid());
    }

    @Test
    void testValidateIdentifierInvalid() {
        // 測試無效識別符
        var result = InputValidator.validateIdentifier("user; DROP TABLE--");
        assertFalse(result.isValid());
    }
}
```

#### 6.4.2 整合測試

```java
/**
 * MCP Server 整合測試
 */
package com.example.mcp.test;

import io.modelcontextprotocol.client.*;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

class IntegrationTest {

    private McpClient client;

    @BeforeEach
    void setUp() throws Exception {
        // 建立 MCP Client
        ProcessBuilder pb = new ProcessBuilder(
            "java", "-jar", "target/my-mcp-server-1.0.0.jar"
        );
        pb.environment().put("MCP_ALLOWED_DIRS", "/tmp");

        Process process = pb.start();
        StdioClientTransport transport = new StdioClientTransport(
            process.getInputStream(),
            process.getOutputStream()
        );

        client = new McpClient(McpClientOptions.builder()
            .clientInfo(ClientInfo.builder()
                .name("test-client")
                .version("1.0.0")
                .build())
            .build());

        client.connect(transport).get(10, TimeUnit.SECONDS);
    }

    @AfterEach
    void tearDown() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    void testFullWorkflow() throws Exception {
        // 測試完整工作流程

        // 1. 列出工具
        ListToolsResult toolsResult = client.listTools().get(5, TimeUnit.SECONDS);
        assertFalse(toolsResult.getTools().isEmpty());

        // 2. 寫入檔案
        CallToolResult writeResult = client.callTool(
            "write_file",
            Map.of(
                "path", "/tmp/test_integration.txt",
                "content", "Integration test content"
            )
        ).get(5, TimeUnit.SECONDS);
        assertTrue(writeResult.getText().contains("成功"));

        // 3. 讀取檔案
        CallToolResult readResult = client.callTool(
            "read_file",
            Map.of("path", "/tmp/test_integration.txt")
        ).get(5, TimeUnit.SECONDS);
        assertTrue(readResult.getText().contains("Integration test content"));

        // 4. 刪除檔案
        CallToolResult deleteResult = client.callTool(
            "delete_file",
            Map.of(
                "path", "/tmp/test_integration.txt",
                "confirm", true
            )
        ).get(5, TimeUnit.SECONDS);
        assertTrue(deleteResult.getText().contains("成功"));
    }
}
```

#### 6.4.3 使用 MCP Inspector 進行測試

```bash
# 啟動 Inspector 並連接到 Server
npx @modelcontextprotocol/inspector java -jar target/my-mcp-server-1.0.0.jar

# Inspector 會開啟 Web UI，可以：
# 1. 查看所有可用工具
# 2. 手動執行工具並查看結果
# 3. 檢視原始 JSON-RPC 訊息
# 4. 監控 Server 日誌
```

#### 6.4.4 模擬與 Mock

```java
/**
 * Mock 測試範例
 */
package com.example.mcp.test;

import org.junit.jupiter.api.*;
import org.mockito.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;

class MockTest {

    @Mock
    private GitHubApiClient mockGitHubClient;

    @Mock
    private DatabaseConnectionPool mockDbPool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGitHubApiWithMock() throws Exception {
        // 模擬 GitHub API 回應
        Map<String, Object> mockResponse = Map.of(
            "full_name", "test/repo",
            "description", "Test repository",
            "stargazers_count", 100
        );

        when(mockGitHubClient.request(eq("GET"), eq("/repos/test/repo"), any()))
            .thenReturn(CompletableFuture.completedFuture(mockResponse));

        // 執行測試
        GitHubMcpServer server = new GitHubMcpServer(mockGitHubClient);
        CallToolResult result = server.handleGetRepo(Map.of(
            "owner", "test",
            "repo", "repo"
        ));

        assertTrue(result.getText().contains("test/repo"));
        assertTrue(result.getText().contains("100"));

        verify(mockGitHubClient).request(eq("GET"), eq("/repos/test/repo"), any());
    }

    @Test
    void testDatabaseWithMock() throws Exception {
        // 模擬資料庫查詢結果
        List<Map<String, Object>> mockRows = List.of(
            Map.of("id", 1, "name", "Alice"),
            Map.of("id", 2, "name", "Bob")
        );

        Connection mockConn = mock(Connection.class);
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockRs = mock(ResultSet.class);

        when(mockDbPool.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(mockStmt);
        when(mockStmt.executeQuery()).thenReturn(mockRs);

        // 模擬 ResultSet 迭代
        when(mockRs.next()).thenReturn(true, true, false);
        when(mockRs.getObject("id")).thenReturn(1, 2);
        when(mockRs.getObject("name")).thenReturn("Alice", "Bob");

        // 執行測試
        DatabaseMcpServer server = new DatabaseMcpServer(mockDbPool);
        CallToolResult result = server.handleQuery(Map.of(
            "sql", "SELECT * FROM users",
            "limit", 10
        ));

        assertTrue(result.getText().contains("Alice"));
        assertTrue(result.getText().contains("Bob"));
    }

    @Test
    void testRateLimiterMock() {
        // 測試速率限制器
        RateLimiter rateLimiter = new RateLimiter(2, Duration.ofSeconds(1));

        // 前兩次應該允許
        assertTrue(rateLimiter.isAllowed("test-key").allowed());
        assertTrue(rateLimiter.isAllowed("test-key").allowed());

        // 第三次應該被限制
        assertFalse(rateLimiter.isAllowed("test-key").allowed());
    }
}
```

---

## 第七章：進階主題

### 7.1 Tasks 實驗性功能

#### 7.1.1 Tasks 概述

Tasks 是 MCP 的實驗性功能，用於處理長時間運行的操作：

```mermaid
sequenceDiagram
    participant C as Client
    participant S as Server

    C->>S: tools/call (長時間操作)
    S->>C: 回應 taskId

    Note over C: 非同步等待

    loop 輪詢狀態
        C->>S: tasks/get (taskId)
        S->>C: 狀態: running/completed
    end

    C->>S: tasks/get (taskId)
    S->>C: 最終結果
```

#### 7.1.2 實作範例

```java
/**
 * Tasks 實作範例
 */
package com.example.mcp.tasks;

import java.time.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * 任務狀態枚舉
 */
public enum TaskStatus {
    PENDING("pending"),
    RUNNING("running"),
    COMPLETED("completed"),
    FAILED("failed"),
    CANCELLED("cancelled");

    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    public String getValue() { return value; }
}

/**
 * 任務實體
 */
public class Task {
    private final String id;
    private final String name;
    private volatile TaskStatus status;
    private volatile double progress;
    private volatile Object result;
    private volatile String error;
    private final Instant createdAt;
    private volatile Instant completedAt;

    public Task(String id, String name) {
        this.id = id;
        this.name = name;
        this.status = TaskStatus.PENDING;
        this.progress = 0.0;
        this.createdAt = Instant.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public String getName() { return name; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public double getProgress() { return progress; }
    public void setProgress(double progress) { this.progress = progress; }
    public Object getResult() { return result; }
    public void setResult(Object result) { this.result = result; }
    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

/**
 * 任務管理器
 */
public class TaskManager {

    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<?>> runningTasks = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * 建立新任務
     */
    public Task createTask(String name) {
        String taskId = UUID.randomUUID().toString();
        Task task = new Task(taskId, name);
        tasks.put(taskId, task);
        return task;
    }

    /**
     * 執行任務
     */
    public <T> CompletableFuture<T> runTask(
        String taskId,
        Callable<T> callable,
        java.util.function.Consumer<Double> progressCallback
    ) {
        Task task = tasks.get(taskId);
        if (task == null) {
            throw new IllegalArgumentException("任務不存在：" + taskId);
        }

        task.setStatus(TaskStatus.RUNNING);

        CompletableFuture<T> future = CompletableFuture.supplyAsync(() -> {
            try {
                T result = callable.call();
                task.setResult(result);
                task.setStatus(TaskStatus.COMPLETED);
                return result;
            } catch (CancellationException e) {
                task.setStatus(TaskStatus.CANCELLED);
                throw e;
            } catch (Exception e) {
                task.setError(e.getMessage());
                task.setStatus(TaskStatus.FAILED);
                throw new CompletionException(e);
            } finally {
                task.setCompletedAt(Instant.now());
            }
        }, executor);

        runningTasks.put(taskId, future);
        return future;
    }

    /**
     * 取得任務狀態
     */
    public Optional<Task> getTask(String taskId) {
        return Optional.ofNullable(tasks.get(taskId));
    }

    /**
     * 取消任務
     */
    public boolean cancelTask(String taskId) {
        CompletableFuture<?> future = runningTasks.get(taskId);
        if (future != null) {
            return future.cancel(true);
        }
        return false;
    }

    /**
     * 更新進度
     */
    public void updateProgress(String taskId, double progress) {
        Task task = tasks.get(taskId);
        if (task != null) {
            task.setProgress(progress);
        }
    }

    /**
     * 關閉管理器
     */
    public void shutdown() {
        executor.shutdown();
    }
}

// 在 MCP Server 中使用任務管理器
public class TaskEnabledMcpServer {

    private final McpServer server;
    private final TaskManager taskManager = new TaskManager();

    public TaskEnabledMcpServer() {
        // 初始化 server...
        this.server = createServer();
        setupHandlers();
    }

    private void setupHandlers() {
        server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
            String name = request.getParams().getName();
            Map<String, Object> args = request.getParams().getArguments();

            if ("long_running_analysis".equals(name)) {
                // 建立任務
                Task task = taskManager.createTask("analysis");

                // 背景執行
                taskManager.runTask(task.getId(), () -> {
                    int totalSteps = 10;
                    for (int i = 0; i < totalSteps; i++) {
                        Thread.sleep(1000);  // 模擬工作
                        taskManager.updateProgress(task.getId(), (i + 1.0) / totalSteps);
                    }
                    return Map.of("result", "Analysis completed", "items_processed", 100);
                }, null);

                return CallToolResult.text("任務已啟動，任務 ID：" + task.getId());

            } else if ("get_task_status".equals(name)) {
                String taskId = (String) args.get("task_id");
                Optional<Task> taskOpt = taskManager.getTask(taskId);

                if (taskOpt.isEmpty()) {
                    return CallToolResult.text("任務不存在");
                }

                Task task = taskOpt.get();
                StringBuilder output = new StringBuilder();
                output.append("任務狀態：").append(task.getId()).append("\n");
                output.append("狀態：").append(task.getStatus().getValue()).append("\n");
                output.append("進度：").append(String.format("%.1f%%", task.getProgress() * 100)).append("\n");

                if (task.getStatus() == TaskStatus.COMPLETED) {
                    output.append("結果：").append(task.getResult()).append("\n");
                } else if (task.getStatus() == TaskStatus.FAILED) {
                    output.append("錯誤：").append(task.getError()).append("\n");
                }

                return CallToolResult.text(output.toString());
            }

            return CallToolResult.error("未知工具：" + name);
        }));
    }
}
```

---

### 7.2 自訂傳輸層

#### 7.2.1 何時需要自訂傳輸層

- WebSocket 通訊
- 自訂加密通道
- 特殊網路環境
- 效能優化需求

#### 7.2.2 實作指南

```java
/**
 * 自訂傳輸層範例：WebSocket Transport
 */
package com.example.mcp.transport;

import com.google.gson.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.*;
import java.util.concurrent.*;

/**
 * WebSocket 傳輸層
 */
public class WebSocketTransport extends WebSocketClient {

    private final BlockingQueue<Map<String, Object>> messageQueue;
    private final Gson gson;

    public WebSocketTransport(URI serverUri) {
        super(serverUri);
        this.messageQueue = new LinkedBlockingQueue<>();
        this.gson = new Gson();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("WebSocket 連接已建立");
    }

    @Override
    public void onMessage(String message) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> data = gson.fromJson(message, Map.class);
            messageQueue.put(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("WebSocket 連接已關閉：" + reason);
    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }

    /**
     * 發送訊息
     */
    public void sendMessage(Map<String, Object> message) {
        send(gson.toJson(message));
    }

    /**
     * 接收訊息
     */
    public Map<String, Object> receiveMessage() throws InterruptedException {
        return messageQueue.take();
    }

    /**
     * 接收訊息（帶超時）
     */
    public Map<String, Object> receiveMessage(long timeout, TimeUnit unit) throws InterruptedException {
        return messageQueue.poll(timeout, unit);
    }
}

/**
 * 使用 WebSocket 的 MCP Client
 */
public class WebSocketMcpClient implements AutoCloseable {

    private final WebSocketTransport transport;
    private final AtomicInteger requestId;
    private final ConcurrentHashMap<Integer, CompletableFuture<Map<String, Object>>> pendingRequests;
    private final ExecutorService executor;

    public WebSocketMcpClient(String uri) throws Exception {
        this.transport = new WebSocketTransport(new URI(uri));
        this.requestId = new AtomicInteger(0);
        this.pendingRequests = new ConcurrentHashMap<>();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * 連接到伺服器
     */
    public CompletableFuture<Void> connect() {
        return CompletableFuture.runAsync(() -> {
            try {
                transport.connectBlocking();
                startResponseHandler();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CompletionException(e);
            }
        });
    }

    /**
     * 啟動回應處理器
     */
    private void startResponseHandler() {
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Map<String, Object> message = transport.receiveMessage();
                    handleMessage(message);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    /**
     * 處理訊息
     */
    @SuppressWarnings("unchecked")
    private void handleMessage(Map<String, Object> message) {
        if (message.containsKey("id")) {
            // 這是對請求的回應
            int id = ((Number) message.get("id")).intValue();
            CompletableFuture<Map<String, Object>> future = pendingRequests.remove(id);
            if (future != null) {
                future.complete(message);
            }
        } else {
            // 這是通知
            handleNotification(message);
        }
    }

    /**
     * 處理通知
     */
    private void handleNotification(Map<String, Object> notification) {
        System.out.println("收到通知：" + notification);
    }

    /**
     * 發送請求
     */
    public CompletableFuture<Map<String, Object>> request(String method, Map<String, Object> params) {
        int id = requestId.incrementAndGet();

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("jsonrpc", "2.0");
        message.put("id", id);
        message.put("method", method);
        if (params != null) {
            message.put("params", params);
        }

        CompletableFuture<Map<String, Object>> future = new CompletableFuture<>();
        pendingRequests.put(id, future);

        transport.sendMessage(message);

        return future.thenApply(response -> {
            if (response.containsKey("error")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                throw new CompletionException(new RuntimeException((String) error.get("message")));
            }
            return response.get("result");
        }).thenApply(result -> {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = (Map<String, Object>) result;
            return resultMap;
        });
    }

    @Override
    public void close() {
        executor.shutdown();
        transport.close();
    }
}
```

---

### 7.3 多語言 SDK 比較

| 特性           | Java SDK          | Python SDK     | TypeScript SDK      |
| -------------- | ----------------- | -------------- | ------------------- |
| **成熟度**     | 穩定              | 穩定           | 穩定                |
| **非同步支援** | CompletableFuture | asyncio        | Promise/async-await |
| **型別系統**   | 強型別            | 選用（typing） | 內建                |
| **套件管理**   | Maven/Gradle      | pip/uv         | npm/pnpm            |
| **適用場景**   | 企業應用、微服務  | 資料處理、ML   | Web 應用、前端整合  |

**Java 特點**：

```java
// Handler 註冊風格
server.setToolListHandler(request -> CompletableFuture.supplyAsync(() -> {
    return ListToolsResult.builder()
        .tools(List.of(...))
        .build();
}));

server.setToolHandler(request -> CompletableFuture.supplyAsync(() -> {
    return handleToolCall(request.getParams().getName(), request.getParams().getArguments());
}));
```

**Python 特點**：

```python
# 裝飾器風格的 Handler 註冊
@server.list_tools()
async def list_tools():
    ...

@server.call_tool()
async def call_tool(name, arguments):
    ...
```

**TypeScript 特點**：

```typescript
// Schema 驗證風格
server.setRequestHandler(ListToolsRequestSchema, async () => {
  ...
});

server.setRequestHandler(CallToolRequestSchema, async (request) => {
  ...
});
```

---

### 7.4 偵錯與監控

#### 7.4.1 日誌記錄最佳實踐

```java
/**
 * 日誌記錄最佳實踐
 */
package com.example.mcp.logging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.PrintStream;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.*;

/**
 * 結構化日誌格式器
 */
public class StructuredFormatter extends Formatter {

    private final Gson gson = new GsonBuilder().create();

    @Override
    public String format(LogRecord record) {
        Map<String, Object> logData = new LinkedHashMap<>();
        logData.put("timestamp", Instant.now().toString());
        logData.put("level", record.getLevel().getName());
        logData.put("logger", record.getLoggerName());
        logData.put("message", record.getMessage());

        // 添加額外資訊（透過 MDC 或參數傳遞）
        Object[] params = record.getParameters();
        if (params != null && params.length > 0) {
            if (params[0] instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> extra = (Map<String, Object>) params[0];
                logData.putAll(extra);
            }
        }

        return gson.toJson(logData) + System.lineSeparator();
    }
}

/**
 * MCP Server 日誌工具
 */
public class McpLogger {

    private static final Logger logger;

    static {
        // MCP Server 日誌應該輸出到 stderr
        logger = Logger.getLogger("mcp_server");
        logger.setUseParentHandlers(false);

        StreamHandler handler = new StreamHandler(System.err, new StructuredFormatter()) {
            @Override
            public synchronized void publish(LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        handler.setLevel(Level.INFO);
        logger.addHandler(handler);
        logger.setLevel(Level.INFO);
    }

    public static Logger getLogger() {
        return logger;
    }

    /**
     * 記錄工具呼叫
     */
    public static void logToolCall(String toolName, double durationMs, boolean success) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("tool_name", toolName);
        extra.put("duration_ms", durationMs);

        if (success) {
            logger.log(Level.INFO, "工具呼叫成功", new Object[]{extra});
        } else {
            logger.log(Level.SEVERE, "工具呼叫失敗", new Object[]{extra});
        }
    }
}
```

#### 7.4.2 效能監控

```java
/**
 * 效能監控
 */
package com.example.mcp.monitoring;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 指標點
 */
record MetricPoint(long timestamp, double value) {}

/**
 * 指標收集器
 */
public class Metrics {

    private final int retentionSeconds;
    private final ConcurrentMap<String, AtomicLong> counters;
    private final ConcurrentMap<String, CopyOnWriteArrayList<MetricPoint>> timings;
    private final ConcurrentMap<String, AtomicReference<Double>> gauges;
    private final ScheduledExecutorService cleaner;

    public Metrics(int retentionSeconds) {
        this.retentionSeconds = retentionSeconds;
        this.counters = new ConcurrentHashMap<>();
        this.timings = new ConcurrentHashMap<>();
        this.gauges = new ConcurrentHashMap<>();

        // 定期清理舊資料
        this.cleaner = Executors.newSingleThreadScheduledExecutor();
        this.cleaner.scheduleAtFixedRate(
            this::cleanupOldData,
            60, 60, TimeUnit.SECONDS
        );
    }

    public Metrics() {
        this(3600);  // 預設保留 1 小時
    }

    /**
     * 增加計數器
     */
    public void increment(String name, long value) {
        counters.computeIfAbsent(name, k -> new AtomicLong(0)).addAndGet(value);
    }

    public void increment(String name) {
        increment(name, 1);
    }

    /**
     * 記錄時間
     */
    public void timing(String name, double durationMs) {
        timings.computeIfAbsent(name, k -> new CopyOnWriteArrayList<>())
            .add(new MetricPoint(System.currentTimeMillis(), durationMs));
    }

    /**
     * 設定量規
     */
    public void gauge(String name, double value) {
        gauges.computeIfAbsent(name, k -> new AtomicReference<>(0.0)).set(value);
    }

    /**
     * 清理舊資料
     */
    private void cleanupOldData() {
        long cutoff = System.currentTimeMillis() - (retentionSeconds * 1000L);

        timings.forEach((name, points) -> {
            points.removeIf(p -> p.timestamp() < cutoff);
        });
    }

    /**
     * 取得統計資訊
     */
    public Map<String, Object> getStats(String name) {
        List<MetricPoint> points = timings.getOrDefault(name, new CopyOnWriteArrayList<>());

        if (points.isEmpty()) {
            return Map.of("count", 0);
        }

        List<Double> values = points.stream()
            .map(MetricPoint::value)
            .sorted()
            .collect(Collectors.toList());

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("count", values.size());
        stats.put("min", values.get(0));
        stats.put("max", values.get(values.size() - 1));
        stats.put("avg", values.stream().mapToDouble(d -> d).average().orElse(0));

        if (values.size() >= 20) {
            int p95Index = (int) (values.size() * 0.95);
            stats.put("p95", values.get(p95Index));
        }

        return stats;
    }

    public void shutdown() {
        cleaner.shutdown();
    }
}

// 全域指標收集器
class MetricsHolder {
    static final Metrics INSTANCE = new Metrics();
}

/**
 * 時間測量包裝器
 */
public class MeasuredExecutor {

    private final String metricName;
    private final Metrics metrics;

    public MeasuredExecutor(String metricName, Metrics metrics) {
        this.metricName = metricName;
        this.metrics = metrics;
    }

    public MeasuredExecutor(String metricName) {
        this(metricName, MetricsHolder.INSTANCE);
    }

    /**
     * 測量並執行
     */
    public <T> CompletableFuture<T> measureAsync(Supplier<CompletableFuture<T>> task) {
        long start = System.nanoTime();

        return task.get()
            .whenComplete((result, error) -> {
                double durationMs = (System.nanoTime() - start) / 1_000_000.0;
                metrics.timing(metricName + "_duration", durationMs);

                if (error != null) {
                    metrics.increment(metricName + "_error");
                } else {
                    metrics.increment(metricName + "_success");
                }
            });
    }

    /**
     * 測量同步操作
     */
    public <T> T measure(Supplier<T> task) {
        long start = System.nanoTime();

        try {
            T result = task.get();
            metrics.increment(metricName + "_success");
            return result;
        } catch (Exception e) {
            metrics.increment(metricName + "_error");
            throw e;
        } finally {
            double durationMs = (System.nanoTime() - start) / 1_000_000.0;
            metrics.timing(metricName + "_duration", durationMs);
        }
    }
}

// 使用範例
class ToolHandler {
    private final MeasuredExecutor readFileMetrics = new MeasuredExecutor("tool_read_file");

    public CompletableFuture<String> handleReadFile(Map<String, Object> args) {
        return readFileMetrics.measureAsync(() ->
            CompletableFuture.supplyAsync(() -> {
                // 實際處理邏輯
                return "file content";
            })
        );
    }
}
```

---

_（第七章完結，繼續第八章）_

---

## 第八章：疑難排解

### 8.1 常見錯誤與解決方案

#### 8.1.1 連接問題

| 錯誤訊息                       | 可能原因        | 解決方案                  |
| ------------------------------ | --------------- | ------------------------- |
| `Connection refused`           | Server 未啟動   | 確認 Server 正在運行      |
| `Timeout waiting for response` | Server 處理過慢 | 增加超時時間或優化 Server |
| `Protocol version mismatch`    | 版本不相容      | 更新 SDK 到相容版本       |
| `Transport error`              | STDIO 管道問題  | 檢查 stdout/stderr 使用   |

**STDIO 連接問題診斷**：

```java
/**
 * STDIO 連接診斷工具
 */
package com.example.mcp.diagnostic;

import java.io.*;
import java.util.logging.*;

public class StdioDiagnostic {

    // 確保日誌輸出到 stderr，不干擾 STDIO 通訊
    private static final Logger logger;

    static {
        logger = Logger.getLogger(StdioDiagnostic.class.getName());
        logger.setUseParentHandlers(false);

        ConsoleHandler handler = new ConsoleHandler() {
            @Override
            protected void setOutputStream(OutputStream out) throws SecurityException {
                super.setOutputStream(System.err);  // 重要！
            }
        };
        handler.setFormatter(new SimpleFormatter());
        logger.addHandler(handler);
        logger.setLevel(Level.ALL);
    }

    public static void diagnoseStdio() {
        // 1. 檢查 stdin 是否可讀
        logger.info("檢查 stdin...");
        if (System.console() != null) {
            logger.warning("stdin 是終端機，可能不適合 STDIO 傳輸");
        }

        // 2. 檢查 stdout 是否可寫
        logger.info("檢查 stdout...");

        // 3. 測試寫入
        try {
            String testMessage = "{\"test\": \"message\"}\n";
            System.out.write(testMessage.getBytes());
            System.out.flush();
            logger.info("stdout 寫入測試成功");
        } catch (IOException e) {
            logger.severe("stdout 寫入失敗：" + e.getMessage());
        }

        // 4. 檢查環境變數
        logger.info("JAVA_HOME: " + System.getenv("JAVA_HOME"));
        String path = System.getenv("PATH");
        logger.info("PATH: " + (path != null ? path.substring(0, Math.min(100, path.length())) + "..." : "not set"));
    }

    public static void main(String[] args) {
        diagnoseStdio();
    }
}
```

#### 8.1.2 工具執行錯誤

**錯誤模式與處理**：

```java
/**
 * 錯誤模式分析
 */
package com.example.mcp.error;

import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.spec.McpSchema.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;

/**
 * 工具錯誤處理
 */
public class ToolErrorHandling {

    /**
     * 1. 參數錯誤處理
     */
    public static CallToolResult validateAndCallTool(
            String name,
            Map<String, Object> arguments,
            Function<Map<String, Object>, CallToolResult> toolHandler) {

        try {
            // 驗證參數
            if ("read_file".equals(name)) {
                Object path = arguments.get("path");

                if (path == null) {
                    return CallToolResult.error(
                        "錯誤：缺少必要參數 'path'\n" +
                        "請使用：read_file(path='/path/to/file')"
                    );
                }

                // 類型檢查
                if (!(path instanceof String)) {
                    return CallToolResult.error(
                        String.format("錯誤：參數 'path' 類型錯誤%n預期：String，收到：%s",
                            path.getClass().getSimpleName())
                    );
                }
            }

            // 呼叫工具
            return toolHandler.apply(arguments);

        } catch (IllegalArgumentException e) {
            return CallToolResult.error("參數錯誤：" + e.getMessage());
        } catch (ClassCastException e) {
            return CallToolResult.error("類型錯誤：" + e.getMessage());
        }
    }

    /**
     * 2. 資源錯誤
     */
    public static class ResourceException extends RuntimeException {
        private final String resourceType;

        public ResourceException(String message, String resourceType) {
            super(message);
            this.resourceType = resourceType;
        }

        public String getResourceType() { return resourceType; }
    }

    /**
     * 資源錯誤處理包裝器
     */
    public static CallToolResult handleResourceErrors(
            java.util.function.Supplier<CallToolResult> operation) {

        try {
            return operation.get();

        } catch (java.nio.file.NoSuchFileException e) {
            return CallToolResult.error(
                String.format("❌ 檔案未找到：%s%n請確認路徑是否正確", e.getFile())
            );
        } catch (java.nio.file.AccessDeniedException e) {
            return CallToolResult.error(
                String.format("❌ 權限被拒絕：%s%n請檢查檔案權限或允許的目錄設定", e.getFile())
            );
        } catch (java.net.ConnectException e) {
            return CallToolResult.error(
                String.format("❌ 連接錯誤：%s%n請檢查網路連接或服務狀態", e.getMessage())
            );
        }
    }
}
```

#### 8.1.3 記憶體與效能問題

```java
/**
 * 效能問題診斷
 */
package com.example.mcp.diagnostic;

import java.lang.management.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.logging.*;

/**
 * 記憶體監控工具
 */
public class MemoryMonitor {

    private static final Logger logger = Logger.getLogger(MemoryMonitor.class.getName());
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    /**
     * 取得記憶體快照
     */
    public static void memorySnapshot() {
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        System.err.println("記憶體使用狀況：");
        System.err.printf("  Heap: 使用 %d MB / 最大 %d MB%n",
            heapUsage.getUsed() / (1024 * 1024),
            heapUsage.getMax() / (1024 * 1024));
        System.err.printf("  Non-Heap: 使用 %d MB%n",
            nonHeapUsage.getUsed() / (1024 * 1024));

        // 執行緒資訊
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        System.err.printf("  執行緒數：%d%n", threadBean.getThreadCount());
    }
}

/**
 * 慢操作偵測
 */
public class SlowOperationDetector {

    private static final Logger logger = Logger.getLogger(SlowOperationDetector.class.getName());
    private final double thresholdSeconds;

    public SlowOperationDetector(double thresholdSeconds) {
        this.thresholdSeconds = thresholdSeconds;
    }

    public SlowOperationDetector() {
        this(5.0);
    }

    /**
     * 偵測慢操作
     */
    public <T> CompletableFuture<T> detectSlow(
            String operationName,
            Supplier<CompletableFuture<T>> operation) {

        long start = System.nanoTime();

        return operation.get()
            .whenComplete((result, error) -> {
                double duration = (System.nanoTime() - start) / 1_000_000_000.0;
                if (duration > thresholdSeconds) {
                    logger.warning(String.format(
                        "慢操作偵測：%s 耗時 %.2f 秒",
                        operationName, duration));
                }
            });
    }

    /**
     * 同步版本
     */
    public <T> T detectSlowSync(String operationName, Supplier<T> operation) {
        long start = System.nanoTime();

        try {
            return operation.get();
        } finally {
            double duration = (System.nanoTime() - start) / 1_000_000_000.0;
            if (duration > thresholdSeconds) {
                logger.warning(String.format(
                    "慢操作偵測：%s 耗時 %.2f 秒",
                    operationName, duration));
            }
        }
    }
}

/**
 * 連接池監控
 */
public class PoolMonitor<T> {

    private final int maxSize;
    private final java.util.concurrent.BlockingQueue<T> pool;
    private final java.util.concurrent.atomic.AtomicInteger inUse;

    public PoolMonitor(BlockingQueue<T> pool, int maxSize) {
        this.pool = pool;
        this.maxSize = maxSize;
        this.inUse = new java.util.concurrent.atomic.AtomicInteger(0);
    }

    public Map<String, Object> getStats() {
        int available = pool.size();
        int used = inUse.get();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("available", available);
        stats.put("in_use", used);
        stats.put("max_size", maxSize);
        return stats;
    }

    public record HealthCheckResult(boolean healthy, String message) {}

    public HealthCheckResult checkHealth() {
        Map<String, Object> stats = getStats();

        int used = (int) stats.get("in_use");
        double usageRatio = (double) used / maxSize;

        if (usageRatio > 0.9) {
            return new HealthCheckResult(false, "連接池使用率過高 (>90%)");
        }

        if ((int) stats.get("available") == 0) {
            return new HealthCheckResult(false, "沒有可用連接");
        }

        return new HealthCheckResult(true, "正常");
    }
}
```

---

### 8.2 除錯技巧

#### 8.2.1 MCP Inspector 使用

```bash
# 基本使用
npx @modelcontextprotocol/inspector python -m my_server

# 帶環境變數
npx @modelcontextprotocol/inspector \
    -e MCP_ALLOWED_DIRS=/path/to/allowed \
    -e DEBUG=true \
    python -m my_server

# 連接到 HTTP Server
npx @modelcontextprotocol/inspector --url http://localhost:8080/mcp
```

**Inspector 功能**：

1. **工具檢視**：查看所有可用工具及其 Schema
2. **手動執行**：直接呼叫工具並查看結果
3. **訊息監控**：檢視原始 JSON-RPC 訊息
4. **日誌查看**：即時查看 Server 日誌

#### 8.2.2 日誌分析

```java
/**
 * 日誌分析工具
 */
package com.example.mcp.diagnostic;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class LogAnalyzer {

    private final Gson gson = new Gson();

    public record LogStats(
        int totalRequests,
        int errors,
        Map<String, Integer> toolCalls,
        List<Double> durations,
        Map<String, Integer> errorTypes
    ) {
        public double getErrorRate() {
            return totalRequests > 0 ? (double) errors / totalRequests * 100 : 0;
        }

        public Double getAverageDuration() {
            return durations.isEmpty() ? null :
                durations.stream().mapToDouble(d -> d).average().orElse(0);
        }
    }

    /**
     * 分析 MCP Server 日誌
     */
    public LogStats analyzeLogs(String logFile) throws IOException {
        int totalRequests = 0;
        int errors = 0;
        Map<String, Integer> toolCalls = new HashMap<>();
        List<Double> durations = new ArrayList<>();
        Map<String, Integer> errorTypes = new HashMap<>();

        try (BufferedReader reader = Files.newBufferedReader(Path.of(logFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Map<String, Object> entry = gson.fromJson(
                        line,
                        new TypeToken<Map<String, Object>>(){}.getType()
                    );

                    totalRequests++;

                    if ("ERROR".equals(entry.get("level"))) {
                        errors++;
                        String errorType = (String) entry.getOrDefault("error_type", "unknown");
                        errorTypes.merge(errorType, 1, Integer::sum);
                    }

                    if (entry.containsKey("tool_name")) {
                        String toolName = (String) entry.get("tool_name");
                        toolCalls.merge(toolName, 1, Integer::sum);
                    }

                    if (entry.containsKey("duration_ms")) {
                        durations.add(((Number) entry.get("duration_ms")).doubleValue());
                    }

                } catch (Exception e) {
                    // 跳過無法解析的行
                }
            }
        }

        return new LogStats(totalRequests, errors, toolCalls, durations, errorTypes);
    }

    /**
     * 列印分析報告
     */
    public void printReport(LogStats stats) {
        System.out.println("=".repeat(50));
        System.out.println("MCP Server 日誌分析報告");
        System.out.println("=".repeat(50));
        System.out.printf("總請求數：%d%n", stats.totalRequests());
        System.out.printf("錯誤數：%d%n", stats.errors());
        System.out.printf("錯誤率：%.2f%%%n", stats.getErrorRate());

        Double avgDuration = stats.getAverageDuration();
        System.out.printf("平均回應時間：%s ms%n",
            avgDuration != null ? String.format("%.2f", avgDuration) : "N/A");

        System.out.println("\n工具呼叫統計：");
        stats.toolCalls().entrySet().stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEach(e -> System.out.printf("  %s: %d%n", e.getKey(), e.getValue()));

        System.out.println("\n錯誤類型分布：");
        stats.errorTypes().forEach((type, count) ->
            System.out.printf("  %s: %d%n", type, count));
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("用法：java LogAnalyzer <log-file>");
            System.exit(1);
        }

        LogAnalyzer analyzer = new LogAnalyzer();
        LogStats stats = analyzer.analyzeLogs(args[0]);
        analyzer.printReport(stats);
    }
}
```

#### 8.2.3 網路除錯

```java
/**
 * 網路除錯工具
 */
package com.example.mcp.diagnostic;

import okhttp3.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class NetworkDebugger {

    private final OkHttpClient client;

    public NetworkDebugger() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();
    }

    /**
     * 除錯 HTTP 連接
     */
    public void debugHttpConnection(String url) {
        System.out.println("測試連接：" + url);

        try {
            // 1. 基本連接測試
            Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

            try (Response response = client.newCall(request).execute()) {
                System.out.println("✅ 連接成功");
                System.out.println("   狀態碼：" + response.code());
                System.out.println("   Headers：");
                response.headers().forEach(pair ->
                    System.out.println("     " + pair.getFirst() + ": " + pair.getSecond()));
            }

            // 2. SSE 端點測試
            String sseUrl = url.contains("/mcp") ?
                url.replace("/mcp", "/sse") : url + "/sse";

            try {
                Request sseRequest = new Request.Builder()
                    .url(sseUrl)
                    .get()
                    .build();

                try (Response sseResponse = client.newCall(sseRequest).execute()) {
                    System.out.println("✅ SSE 端點可用");
                    System.out.println("   Content-Type：" +
                        sseResponse.header("Content-Type"));
                }
            } catch (Exception e) {
                System.out.println("⚠️ SSE 端點無法連接");
            }

        } catch (java.net.ConnectException e) {
            System.out.println("❌ 連接錯誤：" + e.getMessage());
        } catch (java.net.SocketTimeoutException e) {
            System.out.println("❌ 連接超時");
        } catch (Exception e) {
            System.out.println("❌ 未知錯誤：" + e.getMessage());
        }
    }

    /**
     * 追蹤 HTTP 請求
     */
    public void traceRequest(String method, String url, RequestBody body) throws IOException {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("請求：" + method + " " + url);

        long start = System.currentTimeMillis();

        Request.Builder requestBuilder = new Request.Builder().url(url);

        switch (method.toUpperCase()) {
            case "GET" -> requestBuilder.get();
            case "POST" -> requestBuilder.post(body != null ? body : RequestBody.create("", null));
            case "PUT" -> requestBuilder.put(body != null ? body : RequestBody.create("", null));
            case "DELETE" -> requestBuilder.delete(body);
            default -> throw new IllegalArgumentException("不支援的方法：" + method);
        }

        try (Response response = client.newCall(requestBuilder.build()).execute()) {
            long duration = System.currentTimeMillis() - start;

            System.out.println("\n回應：");
            System.out.println("  狀態：" + response.code());
            System.out.println("  耗時：" + duration + " ms");
            System.out.println("  Headers：");
            response.headers().forEach(pair ->
                System.out.println("    " + pair.getFirst() + ": " + pair.getSecond()));

            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String bodyStr = responseBody.string();
                System.out.println("  Body：" +
                    (bodyStr.length() > 500 ? bodyStr.substring(0, 500) + "..." : bodyStr));
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("用法：java NetworkDebugger <url>");
            System.exit(1);
        }

        NetworkDebugger debugger = new NetworkDebugger();
        debugger.debugHttpConnection(args[0]);
    }
}
```

---

### 8.3 錯誤訊息參考

#### 8.3.1 JSON-RPC 錯誤碼

| 錯誤碼 | 名稱             | 說明           | 解決方案                  |
| ------ | ---------------- | -------------- | ------------------------- |
| -32700 | Parse error      | JSON 解析錯誤  | 檢查 JSON 格式            |
| -32600 | Invalid Request  | 無效的請求物件 | 檢查 jsonrpc, method 欄位 |
| -32601 | Method not found | 方法不存在     | 確認方法名稱正確          |
| -32602 | Invalid params   | 參數錯誤       | 檢查參數格式和類型        |
| -32603 | Internal error   | 內部錯誤       | 檢查 Server 日誌          |

#### 8.3.2 MCP 特定錯誤

```java
/**
 * MCP 錯誤處理參考
 */
package com.example.mcp.error;

import java.util.*;

/**
 * MCP 錯誤定義
 */
public class McpErrors {

    public record ErrorDefinition(
        int code,
        String message,
        String solution
    ) {}

    // 資源相關錯誤
    public static final Map<String, ErrorDefinition> RESOURCE_ERRORS = Map.of(
        "resource_not_found", new ErrorDefinition(
            -32001,
            "找不到指定的資源",
            "確認資源 URI 正確且資源存在"
        ),
        "resource_access_denied", new ErrorDefinition(
            -32002,
            "資源存取被拒絕",
            "檢查權限設定和允許的路徑"
        ),
        "resource_read_error", new ErrorDefinition(
            -32003,
            "資源讀取錯誤",
            "檢查檔案是否可讀取、編碼是否正確"
        )
    );

    // 工具相關錯誤
    public static final Map<String, ErrorDefinition> TOOL_ERRORS = Map.of(
        "tool_not_found", new ErrorDefinition(
            -32010,
            "找不到指定的工具",
            "使用 tools/list 確認可用工具"
        ),
        "tool_execution_error", new ErrorDefinition(
            -32011,
            "工具執行錯誤",
            "檢查參數和 Server 日誌"
        ),
        "tool_timeout", new ErrorDefinition(
            -32012,
            "工具執行超時",
            "增加超時時間或優化操作"
        )
    );

    // 連接相關錯誤
    public static final Map<String, ErrorDefinition> CONNECTION_ERRORS = Map.of(
        "session_expired", new ErrorDefinition(
            -32020,
            "Session 已過期",
            "重新初始化連接"
        ),
        "rate_limited", new ErrorDefinition(
            -32021,
            "請求過於頻繁",
            "減少請求頻率或等待配額重置"
        )
    );

    /**
     * 根據錯誤碼取得錯誤定義
     */
    public static Optional<ErrorDefinition> getByCode(int code) {
        return java.util.stream.Stream.of(RESOURCE_ERRORS, TOOL_ERRORS, CONNECTION_ERRORS)
            .flatMap(map -> map.values().stream())
            .filter(e -> e.code() == code)
            .findFirst();
    }

    /**
     * 根據名稱取得錯誤定義
     */
    public static Optional<ErrorDefinition> getByName(String name) {
        return java.util.stream.Stream.of(RESOURCE_ERRORS, TOOL_ERRORS, CONNECTION_ERRORS)
            .filter(map -> map.containsKey(name))
            .map(map -> map.get(name))
            .findFirst();
    }
}
```

---

---

## 第九章：實際案例研究

### 9.1 案例一：企業知識庫 MCP Server

#### 9.1.1 需求背景

- **場景**：企業內部有大量文件、Wiki、FAQ 資料
- **目標**：讓 AI 助手能搜尋和引用內部知識
- **挑戰**：資料分散、權限控制、即時更新

#### 9.1.2 架構設計

```mermaid
graph TB
    subgraph "AI 層"
        Claude[Claude/AI 助手]
    end

    subgraph "MCP 層"
        KBServer[Knowledge Base<br/>MCP Server]
    end

    subgraph "資料層"
        ES[(Elasticsearch)]
        Wiki[(Confluence)]
        SharePoint[(SharePoint)]
    end

    subgraph "認證層"
        LDAP[LDAP/AD]
        OAuth[OAuth 2.0]
    end

    Claude --> KBServer
    KBServer --> ES
    KBServer --> Wiki
    KBServer --> SharePoint
    KBServer --> LDAP
    KBServer --> OAuth
```

#### 9.1.3 核心實作

```java
/**
 * 企業知識庫 MCP Server
 */
package com.example.mcp.kb;

import co.elastic.clients.elasticsearch.ElasticsearchAsyncClient;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.*;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.spec.McpSchema.*;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.util.*;
import java.util.concurrent.*;

public class KnowledgeBaseMcpServer {

    // 配置
    private static final String ES_HOST = "elasticsearch";
    private static final int ES_PORT = 9200;
    private static final String WIKI_URL = "http://confluence.internal";

    private final McpServer server;
    private final ElasticsearchAsyncClient esClient;

    public KnowledgeBaseMcpServer() {
        this.server = McpServer.builder()
            .name("knowledge-base")
            .version("1.0.0")
            .build();

        // 初始化 Elasticsearch 客戶端
        RestClient restClient = RestClient.builder(
            new HttpHost(ES_HOST, ES_PORT)
        ).build();

        this.esClient = new ElasticsearchAsyncClient(
            new RestClientTransport(restClient, new JacksonJsonpMapper())
        );

        setupHandlers();
    }

    private void setupHandlers() {
        // 工具列表
        server.setToolListHandler(request -> CompletableFuture.supplyAsync(() -> {
            List<Tool> tools = List.of(
                Tool.builder()
                    .name("search_knowledge")
                    .description("搜尋企業知識庫，包含文件、Wiki、FAQ")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "query", Map.of(
                                "type", "string",
                                "description", "搜尋關鍵字或問題"
                            ),
                            "sources", Map.of(
                                "type", "array",
                                "items", Map.of(
                                    "type", "string",
                                    "enum", List.of("documents", "wiki", "faq", "all")
                                ),
                                "default", List.of("all"),
                                "description", "搜尋來源"
                            ),
                            "department", Map.of(
                                "type", "string",
                                "description", "限制特定部門的文件"
                            ),
                            "date_from", Map.of(
                                "type", "string",
                                "format", "date",
                                "description", "起始日期過濾"
                            ),
                            "max_results", Map.of(
                                "type", "integer",
                                "default", 10,
                                "maximum", 50
                            )
                        ),
                        "required", List.of("query")
                    ))
                    .build(),

                Tool.builder()
                    .name("get_document")
                    .description("取得特定文件的完整內容")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "doc_id", Map.of(
                                "type", "string",
                                "description", "文件 ID"
                            ),
                            "include_metadata", Map.of(
                                "type", "boolean",
                                "default", true
                            )
                        ),
                        "required", List.of("doc_id")
                    ))
                    .build(),

                Tool.builder()
                    .name("ask_faq")
                    .description("從 FAQ 中找尋最相關的問答")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "question", Map.of(
                                "type", "string",
                                "description", "用戶問題"
                            ),
                            "category", Map.of(
                                "type", "string",
                                "enum", List.of("hr", "it", "finance", "general")
                            )
                        ),
                        "required", List.of("question")
                    ))
                    .build()
            );

            return ListToolsResult.builder().tools(tools).build();
        }));

        // 工具呼叫處理
        server.setToolHandler(request -> {
            String name = request.getParams().getName();
            Map<String, Object> args = request.getParams().getArguments();

            return switch (name) {
                case "search_knowledge" -> searchKnowledge(args);
                case "get_document" -> getDocument(args);
                case "ask_faq" -> askFaq(args);
                default -> CompletableFuture.completedFuture(
                    CallToolResult.error("未知工具：" + name)
                );
            };
        });
    }

    /**
     * 搜尋知識庫
     */
    private CompletableFuture<CallToolResult> searchKnowledge(Map<String, Object> args) {
        String query = (String) args.get("query");
        @SuppressWarnings("unchecked")
        List<String> sources = (List<String>) args.getOrDefault("sources", List.of("all"));
        String department = (String) args.get("department");
        String dateFrom = (String) args.get("date_from");
        int maxResults = ((Number) args.getOrDefault("max_results", 10)).intValue();

        // 構建查詢
        List<Query> mustQueries = new ArrayList<>();
        mustQueries.add(MultiMatchQuery.of(mm -> mm
            .query(query)
            .fields("title^3", "content", "tags^2")
            .type(TextQueryType.BestFields)
            .fuzziness("AUTO")
        )._toQuery());

        List<Query> filterQueries = new ArrayList<>();

        if (!sources.contains("all")) {
            filterQueries.add(TermsQuery.of(t -> t
                .field("source")
                .terms(TermsQueryField.of(tf -> tf
                    .value(sources.stream().map(FieldValue::of).toList())
                ))
            )._toQuery());
        }

        if (department != null) {
            filterQueries.add(TermQuery.of(t -> t
                .field("department")
                .value(department)
            )._toQuery());
        }

        if (dateFrom != null) {
            filterQueries.add(RangeQuery.of(r -> r
                .field("updated_at")
                .gte(co.elastic.clients.json.JsonData.of(dateFrom))
            )._toQuery());
        }

        BoolQuery boolQuery = BoolQuery.of(b -> b
            .must(mustQueries)
            .filter(filterQueries)
        );

        return esClient.search(s -> s
            .index("knowledge_base")
            .query(boolQuery._toQuery())
            .size(maxResults)
            .highlight(h -> h
                .fields("content", hf -> hf.fragmentSize(200))
            ),
            Map.class
        ).thenApply(response -> {
            List<Hit<Map>> hits = response.hits().hits();

            if (hits.isEmpty()) {
                return CallToolResult.text("未找到與「" + query + "」相關的內容");
            }

            StringBuilder output = new StringBuilder();
            output.append("## 搜尋結果：「").append(query).append("」\n\n");
            output.append("找到 ").append(response.hits().total().value())
                  .append(" 筆相關文件\n\n");

            int i = 1;
            for (Hit<Map> hit : hits) {
                @SuppressWarnings("unchecked")
                Map<String, Object> source = hit.source();

                output.append("### ").append(i++).append(". ")
                      .append(source.get("title")).append("\n");
                output.append("- **來源**：").append(source.get("source")).append("\n");
                output.append("- **部門**：")
                      .append(source.getOrDefault("department", "N/A")).append("\n");
                output.append("- **更新日期**：").append(source.get("updated_at")).append("\n");
                output.append("- **文件 ID**：`").append(hit.id()).append("`\n");

                Map<String, List<String>> highlight = hit.highlight();
                if (highlight != null && highlight.containsKey("content")) {
                    output.append("- **摘要**：...")
                          .append(highlight.get("content").get(0)).append("...\n");
                }

                output.append("\n");
            }

            return CallToolResult.text(output.toString());
        }).exceptionally(e -> CallToolResult.error("搜尋失敗：" + e.getMessage()));
    }

    /**
     * 取得文件內容
     */
    private CompletableFuture<CallToolResult> getDocument(Map<String, Object> args) {
        String docId = (String) args.get("doc_id");
        boolean includeMetadata = (Boolean) args.getOrDefault("include_metadata", true);

        return esClient.get(g -> g
            .index("knowledge_base")
            .id(docId),
            Map.class
        ).thenApply(response -> {
            if (!response.found()) {
                return CallToolResult.text("無法取得文件 " + docId + "：文件不存在");
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> source = response.source();

            StringBuilder output = new StringBuilder();
            output.append("# ").append(source.get("title")).append("\n\n");

            if (includeMetadata) {
                output.append("## 文件資訊\n");
                output.append("- **作者**：")
                      .append(source.getOrDefault("author", "N/A")).append("\n");
                output.append("- **建立日期**：")
                      .append(source.getOrDefault("created_at", "N/A")).append("\n");
                output.append("- **更新日期**：")
                      .append(source.getOrDefault("updated_at", "N/A")).append("\n");

                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) source.getOrDefault("tags", List.of());
                output.append("- **標籤**：").append(String.join(", ", tags)).append("\n\n");
            }

            output.append("## 內容\n\n");
            output.append(source.getOrDefault("content", "（無內容）"));

            return CallToolResult.text(output.toString());
        }).exceptionally(e -> CallToolResult.error("無法取得文件 " + docId + "：" + e.getMessage()));
    }

    /**
     * FAQ 問答
     */
    private CompletableFuture<CallToolResult> askFaq(Map<String, Object> args) {
        String question = (String) args.get("question");
        String category = (String) args.get("category");

        List<Query> mustQueries = List.of(
            MultiMatchQuery.of(mm -> mm
                .query(question)
                .fields("question^2", "answer")
                .type(TextQueryType.BestFields)
            )._toQuery()
        );

        List<Query> filterQueries = new ArrayList<>();
        filterQueries.add(TermQuery.of(t -> t.field("type").value("faq"))._toQuery());

        if (category != null) {
            filterQueries.add(TermQuery.of(t -> t
                .field("category")
                .value(category)
            )._toQuery());
        }

        return esClient.search(s -> s
            .index("knowledge_base")
            .query(q -> q.bool(b -> b
                .must(mustQueries)
                .filter(filterQueries)
            ))
            .size(5),
            Map.class
        ).thenApply(response -> {
            List<Hit<Map>> hits = response.hits().hits();

            if (hits.isEmpty()) {
                return CallToolResult.text("FAQ 中沒有找到與「" + question + "」相關的問答");
            }

            // 取最相關的答案
            Hit<Map> bestHit = hits.get(0);
            @SuppressWarnings("unchecked")
            Map<String, Object> bestMatch = bestHit.source();
            double score = bestHit.score();

            StringBuilder output = new StringBuilder();
            output.append("## FAQ 回答\n\n");
            output.append("**問題**：").append(bestMatch.get("question")).append("\n\n");
            output.append("**答案**：").append(bestMatch.get("answer")).append("\n\n");
            output.append("**分類**：")
                  .append(bestMatch.getOrDefault("category", "general")).append("\n");
            output.append("**相關度**：").append(String.format("%.2f", score)).append("\n\n");

            if (hits.size() > 1) {
                output.append("### 其他相關問題\n");
                for (int i = 1; i < Math.min(4, hits.size()); i++) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> otherSource = hits.get(i).source();
                    output.append("- ").append(otherSource.get("question")).append("\n");
                }
            }

            return CallToolResult.text(output.toString());
        }).exceptionally(e -> CallToolResult.error("FAQ 查詢失敗：" + e.getMessage()));
    }
}
```

---

### 9.2 案例二：DevOps 整合 MCP Server

#### 9.2.1 需求背景

- **場景**：開發團隊需要 AI 協助管理 CI/CD、監控、部署
- **目標**：透過對話式介面操作 DevOps 工具鏈
- **整合工具**：Jenkins、Kubernetes、Prometheus、GitLab

#### 9.2.2 架構設計

```mermaid
graph LR
    subgraph "AI 助手"
        Claude[Claude]
    end

    subgraph "DevOps MCP"
        DevServer[DevOps Server]

        subgraph "工具集"
            CI[CI/CD 工具]
            K8s[K8s 工具]
            Monitor[監控工具]
        end
    end

    subgraph "基礎設施"
        Jenkins[Jenkins]
        Kubernetes[Kubernetes]
        Prometheus[Prometheus]
        GitLab[GitLab]
    end

    Claude --> DevServer
    DevServer --> CI
    DevServer --> K8s
    DevServer --> Monitor

    CI --> Jenkins
    CI --> GitLab
    K8s --> Kubernetes
    Monitor --> Prometheus
```

#### 9.2.3 核心工具

````java
/**
 * DevOps MCP Server
 */
package com.example.mcp.devops;

import io.kubernetes.client.openapi.*;
import io.kubernetes.client.openapi.apis.*;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.spec.McpSchema.*;
import okhttp3.*;

import java.io.IOException;
import java.time.*;
import java.util.*;
import java.util.concurrent.*;

public class DevOpsMcpServer {

    // 配置
    private static final String JENKINS_URL = "http://jenkins.internal";
    private static final String PROMETHEUS_URL = "http://prometheus.internal:9090";

    private final McpServer server;
    private final CoreV1Api k8sCoreApi;
    private final AppsV1Api k8sAppsApi;
    private final OkHttpClient httpClient;

    public DevOpsMcpServer() throws IOException {
        this.server = McpServer.builder()
            .name("devops")
            .version("1.0.0")
            .build();

        // 載入 K8s 配置
        ApiClient k8sClient;
        try {
            k8sClient = Config.fromCluster();
        } catch (Exception e) {
            k8sClient = Config.defaultClient();
        }
        Configuration.setDefaultApiClient(k8sClient);

        this.k8sCoreApi = new CoreV1Api();
        this.k8sAppsApi = new AppsV1Api();
        this.httpClient = new OkHttpClient();

        setupHandlers();
    }

    private void setupHandlers() {
        // 工具列表
        server.setToolListHandler(request -> CompletableFuture.supplyAsync(() -> {
            List<Tool> tools = List.of(
                // CI/CD 工具
                Tool.builder()
                    .name("trigger_pipeline")
                    .description("觸發 CI/CD Pipeline")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "project", Map.of("type", "string", "description", "專案名稱"),
                            "branch", Map.of("type", "string", "default", "main"),
                            "parameters", Map.of("type", "object", "description", "Pipeline 參數")
                        ),
                        "required", List.of("project")
                    ))
                    .build(),

                Tool.builder()
                    .name("get_pipeline_status")
                    .description("取得 Pipeline 執行狀態")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "project", Map.of("type", "string"),
                            "build_number", Map.of("type", "integer")
                        ),
                        "required", List.of("project")
                    ))
                    .build(),

                // Kubernetes 工具
                Tool.builder()
                    .name("list_pods")
                    .description("列出 Kubernetes Pods")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "namespace", Map.of("type", "string", "default", "default"),
                            "label_selector", Map.of("type", "string"),
                            "status", Map.of(
                                "type", "string",
                                "enum", List.of("all", "running", "pending", "failed")
                            )
                        )
                    ))
                    .build(),

                Tool.builder()
                    .name("scale_deployment")
                    .description("調整 Deployment 副本數")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "name", Map.of("type", "string", "description", "Deployment 名稱"),
                            "namespace", Map.of("type", "string", "default", "default"),
                            "replicas", Map.of("type", "integer", "minimum", 0, "maximum", 50)
                        ),
                        "required", List.of("name", "replicas")
                    ))
                    .build(),

                Tool.builder()
                    .name("get_pod_logs")
                    .description("取得 Pod 日誌")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "pod_name", Map.of("type", "string"),
                            "namespace", Map.of("type", "string", "default", "default"),
                            "container", Map.of("type", "string"),
                            "tail_lines", Map.of("type", "integer", "default", 100)
                        ),
                        "required", List.of("pod_name")
                    ))
                    .build(),

                // 監控工具
                Tool.builder()
                    .name("query_metrics")
                    .description("查詢 Prometheus 指標")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "query", Map.of("type", "string", "description", "PromQL 查詢"),
                            "time_range", Map.of(
                                "type", "string",
                                "enum", List.of("5m", "15m", "1h", "6h", "24h"),
                                "default", "15m"
                            )
                        ),
                        "required", List.of("query")
                    ))
                    .build(),

                Tool.builder()
                    .name("get_service_health")
                    .description("取得服務健康狀態摘要")
                    .inputSchema(Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "service", Map.of("type", "string"),
                            "namespace", Map.of("type", "string", "default", "default")
                        ),
                        "required", List.of("service")
                    ))
                    .build()
            );

            return ListToolsResult.builder().tools(tools).build();
        }));

        // 工具呼叫處理
        server.setToolHandler(request -> {
            String name = request.getParams().getName();
            Map<String, Object> args = request.getParams().getArguments();

            return switch (name) {
                case "trigger_pipeline" -> triggerPipeline(args);
                case "get_pipeline_status" -> getPipelineStatus(args);
                case "list_pods" -> listPods(args);
                case "scale_deployment" -> scaleDeployment(args);
                case "get_pod_logs" -> getPodLogs(args);
                case "query_metrics" -> queryMetrics(args);
                case "get_service_health" -> getServiceHealth(args);
                default -> CompletableFuture.completedFuture(
                    CallToolResult.error("未知工具：" + name)
                );
            };
        });
    }

    // ===== 工具實作 =====

    /**
     * 觸發 Pipeline
     */
    private CompletableFuture<CallToolResult> triggerPipeline(Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            String project = (String) args.get("project");
            String branch = (String) args.getOrDefault("branch", "main");
            @SuppressWarnings("unchecked")
            Map<String, String> parameters = (Map<String, String>) args.getOrDefault("parameters", Map.of());

            HttpUrl.Builder urlBuilder = HttpUrl.parse(JENKINS_URL + "/job/" + project + "/buildWithParameters")
                .newBuilder()
                .addQueryParameter("BRANCH", branch);

            parameters.forEach(urlBuilder::addQueryParameter);

            Request request = new Request.Builder()
                .url(urlBuilder.build())
                .post(RequestBody.create("", null))
                .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.code() == 201) {
                    String location = response.header("Location", "");
                    return CallToolResult.text(String.format(
                        "✅ Pipeline 已觸發%n專案：%s%n分支：%s%n追蹤：%s",
                        project, branch, location
                    ));
                } else {
                    return CallToolResult.error("❌ 觸發失敗：" + response.code());
                }
            } catch (IOException e) {
                return CallToolResult.error("❌ 觸發失敗：" + e.getMessage());
            }
        });
    }

    /**
     * 取得 Pipeline 狀態
     */
    private CompletableFuture<CallToolResult> getPipelineStatus(Map<String, Object> args) {
        // 實作類似，省略詳細程式碼
        return CompletableFuture.completedFuture(CallToolResult.text("Pipeline 狀態查詢"));
    }

    /**
     * 列出 Pods
     */
    private CompletableFuture<CallToolResult> listPods(Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            String namespace = (String) args.getOrDefault("namespace", "default");
            String labelSelector = (String) args.get("label_selector");
            String statusFilter = (String) args.getOrDefault("status", "all");

            try {
                V1PodList podList = k8sCoreApi.listNamespacedPod(
                    namespace, null, null, null, null,
                    labelSelector, null, null, null, null, null
                );

                StringBuilder output = new StringBuilder();
                output.append("## Pods in ").append(namespace).append("\n\n");
                output.append("| Name | Status | Restarts | Age |\n");
                output.append("|------|--------|----------|-----|\n");

                for (V1Pod pod : podList.getItems()) {
                    String status = pod.getStatus().getPhase();

                    // 過濾狀態
                    if (!"all".equals(statusFilter)) {
                        if ("running".equals(statusFilter) && !"Running".equals(status)) continue;
                        if ("pending".equals(statusFilter) && !"Pending".equals(status)) continue;
                        if ("failed".equals(statusFilter) &&
                            !"Failed".equals(status) && !"Error".equals(status)) continue;
                    }

                    // 計算重啟次數
                    int restarts = 0;
                    if (pod.getStatus().getContainerStatuses() != null) {
                        restarts = pod.getStatus().getContainerStatuses().stream()
                            .mapToInt(V1ContainerStatus::getRestartCount)
                            .sum();
                    }

                    // 計算年齡
                    OffsetDateTime creationTime = pod.getMetadata().getCreationTimestamp();
                    Duration age = Duration.between(creationTime.toInstant(), Instant.now());
                    String ageStr = age.toDays() > 0 ?
                        age.toDays() + "d" : age.toHours() + "h";

                    output.append(String.format("| %s | %s | %d | %s |%n",
                        pod.getMetadata().getName(), status, restarts, ageStr));
                }

                return CallToolResult.text(output.toString());

            } catch (ApiException e) {
                return CallToolResult.error("❌ 列出 Pods 失敗：" + e.getMessage());
            }
        });
    }

    /**
     * 調整 Deployment 副本
     */
    private CompletableFuture<CallToolResult> scaleDeployment(Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            String name = (String) args.get("name");
            String namespace = (String) args.getOrDefault("namespace", "default");
            int replicas = ((Number) args.get("replicas")).intValue();

            try {
                // 取得當前狀態
                V1Deployment deployment = k8sAppsApi.readNamespacedDeployment(
                    name, namespace, null
                );
                int current = deployment.getSpec().getReplicas();

                // 更新副本數
                deployment.getSpec().setReplicas(replicas);
                k8sAppsApi.replaceNamespacedDeployment(name, namespace, deployment, null, null, null, null);

                return CallToolResult.text(String.format(
                    "✅ Deployment %s 已調整%n副本數：%d → %d",
                    name, current, replicas
                ));

            } catch (ApiException e) {
                return CallToolResult.error("❌ 調整失敗：" + e.getMessage());
            }
        });
    }

    /**
     * 取得 Pod 日誌
     */
    private CompletableFuture<CallToolResult> getPodLogs(Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            String podName = (String) args.get("pod_name");
            String namespace = (String) args.getOrDefault("namespace", "default");
            String container = (String) args.get("container");
            int tailLines = ((Number) args.getOrDefault("tail_lines", 100)).intValue();

            try {
                String logs = k8sCoreApi.readNamespacedPodLog(
                    podName, namespace, container, null, null, null, null,
                    null, null, tailLines, null
                );

                return CallToolResult.text("## Pod 日誌：" + podName + "\n\n```\n" + logs + "\n```");

            } catch (ApiException e) {
                return CallToolResult.error("❌ 取得日誌失敗：" + e.getMessage());
            }
        });
    }

    /**
     * 查詢 Prometheus 指標
     */
    private CompletableFuture<CallToolResult> queryMetrics(Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            String query = (String) args.get("query");
            String timeRange = (String) args.getOrDefault("time_range", "15m");

            HttpUrl url = HttpUrl.parse(PROMETHEUS_URL + "/api/v1/query_range")
                .newBuilder()
                .addQueryParameter("query", query)
                .addQueryParameter("start", "now()-" + timeRange)
                .addQueryParameter("end", "now()")
                .addQueryParameter("step", "60s")
                .build();

            Request request = new Request.Builder().url(url).get().build();

            try (Response response = httpClient.newCall(request).execute()) {
                // 解析 Prometheus 回應並格式化輸出
                String body = response.body().string();

                StringBuilder output = new StringBuilder();
                output.append("## 指標查詢結果\n\n");
                output.append("查詢：`").append(query).append("`\n");
                output.append("時間範圍：").append(timeRange).append("\n\n");
                output.append("回應：").append(body.substring(0, Math.min(500, body.length())));

                return CallToolResult.text(output.toString());

            } catch (IOException e) {
                return CallToolResult.error("查詢失敗：" + e.getMessage());
            }
        });
    }

    /**
     * 取得服務健康狀態
     */
    private CompletableFuture<CallToolResult> getServiceHealth(Map<String, Object> args) {
        return CompletableFuture.supplyAsync(() -> {
            String service = (String) args.get("service");
            String namespace = (String) args.getOrDefault("namespace", "default");

            StringBuilder output = new StringBuilder();
            output.append("## 服務健康狀態：").append(service).append("\n\n");

            try {
                // 1. 檢查 Pods 狀態
                V1PodList pods = k8sCoreApi.listNamespacedPod(
                    namespace, null, null, null, null,
                    "app=" + service, null, null, null, null, null
                );

                long running = pods.getItems().stream()
                    .filter(p -> "Running".equals(p.getStatus().getPhase()))
                    .count();
                int total = pods.getItems().size();

                output.append("### Pods\n");
                output.append("- 運行中：").append(running).append("/").append(total).append("\n");
                output.append("- 狀態：").append(running == total ? "✅ 健康" : "⚠️ 異常").append("\n\n");

                // 2. 查詢錯誤率（透過 Prometheus）
                String errorQuery = String.format(
                    "sum(rate(http_requests_total{service=\"%s\",status=~\"5..\"}[5m]))",
                    service
                );

                HttpUrl url = HttpUrl.parse(PROMETHEUS_URL + "/api/v1/query")
                    .newBuilder()
                    .addQueryParameter("query", errorQuery)
                    .build();

                Request request = new Request.Builder().url(url).get().build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        output.append("### 錯誤率\n");
                        output.append("- 查詢已執行（詳見 Prometheus）\n\n");
                    }
                } catch (Exception e) {
                    // 忽略 Prometheus 錯誤
                }

                return CallToolResult.text(output.toString());

            } catch (ApiException e) {
                return CallToolResult.error("❌ 取得健康狀態失敗：" + e.getMessage());
            }
        });
    }
}
````

---

_（第九章完結，繼續第十章）_

---

## 第十章：資源與參考

### 10.1 官方資源

#### 10.1.1 核心文件

| 資源                   | URL                                                                                            | 說明           |
| ---------------------- | ---------------------------------------------------------------------------------------------- | -------------- |
| **MCP 規格書**         | [spec.modelcontextprotocol.io](https://spec.modelcontextprotocol.io)                           | 完整協議規格   |
| **官方文件**           | [modelcontextprotocol.io/docs](https://modelcontextprotocol.io/docs)                           | 入門教學與指南 |
| **GitHub 組織**        | [github.com/modelcontextprotocol](https://github.com/modelcontextprotocol)                     | 所有官方專案   |
| **Anthropic MCP 介紹** | [anthropic.com/news/model-context-protocol](https://anthropic.com/news/model-context-protocol) | 官方公告與說明 |

#### 10.1.2 SDK 與工具

| 專案                          | 語言       | 說明                |
| ----------------------------- | ---------- | ------------------- |
| **mcp**                       | Python     | 官方 Python SDK     |
| **@modelcontextprotocol/sdk** | TypeScript | 官方 TypeScript SDK |
| **mcp-inspector**             | TypeScript | 互動式除錯工具      |
| **mcp-cli**                   | TypeScript | 命令列測試工具      |

**安裝指令**：

```bash
# Python SDK
pip install mcp
# 或使用 uv
uv add mcp

# TypeScript SDK
npm install @modelcontextprotocol/sdk

# Inspector
npx @modelcontextprotocol/inspector <server-command>
```

#### 10.1.3 官方範例 Server

| Server           | 功能              | 來源     |
| ---------------- | ----------------- | -------- |
| **filesystem**   | 檔案系統操作      | 官方範例 |
| **memory**       | 知識圖譜記憶      | 官方範例 |
| **puppeteer**    | 瀏覽器自動化      | 官方範例 |
| **brave-search** | 網路搜尋          | 官方範例 |
| **github**       | GitHub 整合       | 官方範例 |
| **gitlab**       | GitLab 整合       | 官方範例 |
| **google-maps**  | Google Maps API   | 官方範例 |
| **slack**        | Slack 整合        | 官方範例 |
| **postgres**     | PostgreSQL 資料庫 | 官方範例 |
| **sqlite**       | SQLite 資料庫     | 官方範例 |

---

### 10.2 社群資源

#### 10.2.1 第三方 Server 集合

| 資源                    | 說明                       |
| ----------------------- | -------------------------- |
| **Awesome MCP Servers** | 社群維護的 MCP Server 列表 |
| **MCP Hub**             | MCP Server 集中倉庫        |

#### 10.2.2 學習資源

| 類型         | 資源                                                            |
| ------------ | --------------------------------------------------------------- |
| **教學文章** | - Anthropic 官方 Blog<br>- Dev.to MCP 標籤<br>- Medium MCP 文章 |
| **影片教學** | - YouTube MCP 系列<br>- Anthropic 官方直播                      |
| **社群討論** | - Discord: MCP Community<br>- Reddit: r/mcp                     |

---

### 10.3 開發環境建議

#### 10.3.1 VS Code 擴充套件

| 擴充套件              | 用途             |
| --------------------- | ---------------- |
| **Python**            | Python 開發支援  |
| **Pylance**           | Python 型別檢查  |
| **Ruff**              | Python Linter    |
| **REST Client**       | HTTP 請求測試    |
| **YAML**              | YAML 格式支援    |
| **JSON Schema Store** | JSON Schema 驗證 |

#### 10.3.2 推薦專案結構

```
my-mcp-server/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── mcp/
│       │               ├── McpServerApp.java       # Server 主程式
│       │               ├── tools/                  # 工具實作
│       │               │   ├── FileTools.java
│       │               │   └── ApiTools.java
│       │               ├── resources/              # 資源實作
│       │               │   └── DataResources.java
│       │               └── util/                   # 工具函數
│       │                   ├── ValidationUtils.java
│       │                   └── CacheUtils.java
│       └── resources/
│           └── log4j2.xml
├── src/
│   └── test/
│       └── java/
│           └── com/
│               └── example/
│                   └── mcp/
│                       ├── tools/
│                       │   └── FileToolsTest.java
│                       └── resources/
│                           └── DataResourcesTest.java
├── pom.xml
├── README.md
└── .env.example
```

#### 10.3.3 pom.xml 範本

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.example</groupId>
    <artifactId>my-mcp-server</artifactId>
    <version>0.1.0</version>
    <packaging>jar</packaging>

    <name>My MCP Server</name>
    <description>My MCP Server Implementation</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.source>${java.version}</maven.compiler.source>
        <maven.compiler.target>${java.version}</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <dependencies>
        <!-- MCP SDK -->
        <dependency>
            <groupId>io.modelcontextprotocol</groupId>
            <artifactId>mcp-sdk</artifactId>
            <version>1.1.0</version>
        </dependency>

        <!-- JSON 處理 -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>

        <!-- HTTP 客戶端 -->
        <dependency>
            <groupId>com.squareup.okhttp3</groupId>
            <artifactId>okhttp</artifactId>
            <version>4.12.0</version>
        </dependency>

        <!-- 日誌 -->
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-core</artifactId>
            <version>2.22.1</version>
        </dependency>
        <dependency>
            <groupId>org.apache.logging.log4j</groupId>
            <artifactId>log4j-slf4j2-impl</artifactId>
            <version>2.22.1</version>
        </dependency>

        <!-- 測試 -->
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.10.1</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <version>5.8.0</version>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.12.1</version>
                <configuration>
                    <source>${java.version}</source>
                    <target>${java.version}</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-jar-plugin</artifactId>
                <version>3.3.0</version>
                <configuration>
                    <archive>
                        <manifest>
                            <mainClass>com.example.mcp.McpServerApp</mainClass>
                        </manifest>
                    </archive>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-shade-plugin</artifactId>
                <version>3.5.1</version>
                <executions>
                    <execution>
                        <phase>package</phase>
                        <goals>
                            <goal>shade</goal>
                        </goals>
                        <configuration>
                            <transformers>
                                <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                    <mainClass>com.example.mcp.McpServerApp</mainClass>
                                </transformer>
                            </transformers>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
```

---

### 10.4 版本相容性

#### 10.4.1 MCP 協議版本

| 版本           | 日期    | 重大變更                             |
| -------------- | ------- | ------------------------------------ |
| **2025-11-25** | 2025-11 | 新增 Elicitation、Tasks 等實驗性功能 |
| **2024-11-05** | 2024-11 | 初始穩定版本                         |

#### 10.4.2 SDK 版本對照

| Python SDK | TypeScript SDK | 協議版本   |
| ---------- | -------------- | ---------- |
| 1.1.x      | 1.1.x          | 2025-11-25 |
| 1.0.x      | 1.0.x          | 2024-11-05 |

#### 10.4.3 Client 支援狀態

| Client             | 支援功能                            | 備註         |
| ------------------ | ----------------------------------- | ------------ |
| **Claude Desktop** | Tools, Resources, Prompts, Sampling | 完整支援     |
| **Claude Code**    | Tools, Resources                    | VS Code 整合 |
| **自訂 Client**    | 依實作而定                          | 可使用 SDK   |

---

### 10.5 快速參考

#### 10.5.1 JSON-RPC 方法列表

| 方法                                   | 方向 | 說明                   |
| -------------------------------------- | ---- | ---------------------- |
| `initialize`                           | C→S  | 初始化連接             |
| `initialized`                          | C→S  | 確認初始化完成（通知） |
| `tools/list`                           | C→S  | 列出工具               |
| `tools/call`                           | C→S  | 呼叫工具               |
| `resources/list`                       | C→S  | 列出資源               |
| `resources/read`                       | C→S  | 讀取資源               |
| `resources/subscribe`                  | C→S  | 訂閱資源變更           |
| `prompts/list`                         | C→S  | 列出提示詞             |
| `prompts/get`                          | C→S  | 取得提示詞             |
| `sampling/createMessage`               | S→C  | 請求 AI 生成           |
| `logging/setLevel`                     | C→S  | 設定日誌等級           |
| `notifications/tools/list_changed`     | S→C  | 工具列表變更通知       |
| `notifications/resources/list_changed` | S→C  | 資源列表變更通知       |
| `notifications/resources/updated`      | S→C  | 資源更新通知           |

#### 10.5.2 常用程式碼片段

**快速建立 Server**：

```java
package com.example.mcp;

import io.modelcontextprotocol.server.*;
import io.modelcontextprotocol.server.transport.*;
import io.modelcontextprotocol.spec.McpSchema.*;

import java.util.*;
import java.util.concurrent.*;

public class McpServerApp {

    public static void main(String[] args) {
        McpServer server = McpServer.builder()
            .name("my-server")
            .version("1.0.0")
            .build();

        // 工具列表
        server.setToolListHandler(request ->
            CompletableFuture.completedFuture(
                ListToolsResult.builder()
                    .tools(List.of(/* 工具定義 */))
                    .build()
            )
        );

        // 工具呼叫
        server.setToolHandler(request ->
            CompletableFuture.supplyAsync(() -> {
                String name = request.getParams().getName();
                Map<String, Object> args = request.getParams().getArguments();
                // 處理工具呼叫
                return CallToolResult.text("結果");
            })
        );

        // 使用 STDIO 傳輸啟動
        StdioServerTransport transport = new StdioServerTransport();
        server.connect(transport).join();
    }
}
```

**Claude Desktop 配置**：

```json
{
  "mcpServers": {
    "my-server": {
      "command": "java",
      "args": ["-jar", "/path/to/my-mcp-server.jar"],
      "env": {
        "MY_VAR": "value"
      }
    }
  }
}
```

---

## 附錄：檢查清單（Checklist）

### A. Server 開發檢查清單

#### A.1 基本功能

- [ ] Server 可以成功啟動
- [ ] 可以透過 STDIO 連接
- [ ] `initialize` 正確回應 Server 能力
- [ ] `tools/list` 回傳所有工具定義
- [ ] 所有工具有完整的 `inputSchema`
- [ ] 工具描述清楚明確
- [ ] `tools/call` 正確處理所有工具

#### A.2 資源與提示詞（如適用）

- [ ] `resources/list` 回傳資源清單
- [ ] `resources/read` 正確讀取資源
- [ ] 資源 URI 格式正確
- [ ] `prompts/list` 回傳提示詞清單
- [ ] `prompts/get` 正確回傳提示詞內容

#### A.3 錯誤處理

- [ ] 處理無效參數並回傳有意義的錯誤
- [ ] 處理資源不存在的情況
- [ ] 處理權限被拒絕的情況
- [ ] 處理網路錯誤和超時
- [ ] 不會因為單一工具錯誤而崩潰

#### A.4 安全性

- [ ] 實作輸入驗證
- [ ] 路徑操作防止目錄遍歷
- [ ] SQL 操作使用參數化查詢
- [ ] 敏感資訊不會記錄在日誌
- [ ] API 金鑰從環境變數讀取
- [ ] 實作適當的認證機制（如適用）
- [ ] 實作速率限制（如適用）

#### A.5 效能

- [ ] 長時間操作有超時設定
- [ ] 資源重用（連接池等）
- [ ] 適當的快取策略
- [ ] 批次處理大量資料

---

### B. 部署檢查清單

#### B.1 環境準備

- [ ] Python 3.10+ 或 Node.js 18+ 已安裝
- [ ] 所有依賴套件已安裝
- [ ] 環境變數已正確設定
- [ ] 必要的認證資訊已配置
- [ ] 日誌目錄存在且可寫入

#### B.2 配置驗證

- [ ] Claude Desktop/Client 配置正確
- [ ] Server 路徑指向正確
- [ ] 環境變數傳遞正確
- [ ] 允許的目錄/資源已配置

#### B.3 連接測試

- [ ] 使用 MCP Inspector 測試連接
- [ ] 所有工具可以手動呼叫
- [ ] 資源可以正確讀取
- [ ] 錯誤情況有適當回應

#### B.4 監控設定

- [ ] 日誌輸出到 stderr
- [ ] 日誌等級可配置
- [ ] 效能指標收集（如適用）
- [ ] 錯誤追蹤設定（如適用）

---

### C. 程式碼審查檢查清單

#### C.1 程式碼品質

- [ ] 遵循 PEP 8 / ESLint 規範
- [ ] 函數有適當的型別標註
- [ ] 有完整的 docstring / JSDoc
- [ ] 沒有硬編碼的敏感資訊
- [ ] 沒有未使用的程式碼
- [ ] 異常處理適當

#### C.2 測試涵蓋

- [ ] 每個工具有單元測試
- [ ] 錯誤情況有測試
- [ ] 整合測試通過
- [ ] 測試涵蓋率 > 80%

#### C.3 文件完整

- [ ] README 說明如何安裝和使用
- [ ] 有 API 文件或工具說明
- [ ] 有配置範例
- [ ] 有常見問題說明

---

### D. 故障排除檢查清單

#### D.1 Server 無法啟動

- [ ] 檢查 Python/Node.js 版本
- [ ] 檢查依賴套件是否安裝
- [ ] 檢查環境變數是否設定
- [ ] 檢查程式碼是否有語法錯誤
- [ ] 查看 stderr 輸出

#### D.2 Client 無法連接

- [ ] 確認 Server 正在運行
- [ ] 檢查 Client 配置路徑
- [ ] 確認使用正確的傳輸方式
- [ ] 檢查防火牆設定（HTTP 模式）
- [ ] 使用 Inspector 測試

#### D.3 工具執行失敗

- [ ] 檢查參數格式是否正確
- [ ] 確認資源/檔案存在
- [ ] 檢查權限設定
- [ ] 查看 Server 日誌
- [ ] 使用 Inspector 手動測試

#### D.4 效能問題

- [ ] 檢查是否有 N+1 查詢
- [ ] 確認連接池正常運作
- [ ] 檢查快取是否生效
- [ ] 監控記憶體使用
- [ ] 檢查是否有資源洩漏

---

## 結語

Model Context Protocol (MCP) 代表了 AI 應用整合的新典範。透過標準化的協議，開發者可以輕鬆地讓 AI 助手與各種資料來源和工具進行互動，同時保持安全性和可控性。

本教學手冊涵蓋了 MCP 的核心概念、技術架構、實作指南、最佳實踐以及實際案例。希望這份資源能幫助您：

1. **理解 MCP 的價值** — 認識到為何需要標準化的 AI 整合協議
2. **掌握技術細節** — 深入了解傳輸層、JSON-RPC、核心原語
3. **快速上手開發** — 透過範例程式碼建立自己的 MCP Server
4. **遵循最佳實踐** — 建立安全、高效、可維護的解決方案
5. **解決實際問題** — 透過案例研究學習實務應用

隨著 MCP 生態系統的持續發展，我們期待看到更多創新的應用場景。歡迎加入 MCP 社群，一起推動 AI 工具整合的未來！

---

**文件版本**：1.0  
**最後更新**：2026 年  
**協議版本**：2026-1-9

---

_（教學手冊完結）_
