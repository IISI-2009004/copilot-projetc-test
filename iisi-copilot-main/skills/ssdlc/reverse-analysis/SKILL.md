---
name: "reverse-analysis"
description: '分析遺留系統模組，產出架構文件與依賴圖'
---

# Reverse Analysis Skill

## 能力
此 Skill 可以：
1. 掃描模組目錄結構
2. 分析程式碼依賴關係
3. 提取業務邏輯規則
4. 產出 Mermaid 架構圖
5. 評估技術債務

## 分析模板

### 模組分析報告模板
使用 `templates/module-report.md` 格式。

### 依賴關係圖模板
使用 `templates/dependency-map.md` 格式。

## 分析流程
1. 列出模組內所有檔案及行數
2. 識別進入點（Controller、Main、Scheduler）
3. 追蹤核心流程的呼叫鏈
4. 建立依賴關係圖
5. 識別外部系統整合點
6. 評估程式碼品質與技術債務
7. 提出現代化建議
