# 案例回饋 (Case Studies)

本資料夾收錄各專案導入 [agents/](../agents/)、[instructions/](../instructions/)、[prompts/](../prompts/)、[hooks/](../hooks/)、[plugins/](../plugins/) 等 SSDLC 範本後的實務回饋，讓後續採用的團隊能參考真實案例，也讓本 repo 的範本能持續依實務經驗優化。

貢獻方式請參閱根目錄 [CONTRIBUTING.md](../CONTRIBUTING.md#案例回饋-case-studies)，本文件補充資料夾結構與命名規則的細節。

## 目的

- 累積各專案導入 SSDLC Agent Team 的**實務心得**，降低後續採用者的摸索成本
- 收集依實際專案需求**客製化後**的 Agent / Instructions / Prompts，作為其他專案的參考範例
- 保存**驗證測試結果**，佐證範本在真實專案中的有效性

## 可回饋的內容

| 類型               | 說明                                                                                            | 是否必要 |
| ------------------ | ----------------------------------------------------------------------------------------------- | -------- |
| 使用心得（README） | 專案背景、採用哪些 Agent/Instructions/Prompts/Hooks/Plugins、遇到的問題與解法                   | 必要     |
| 客製化後的檔案     | 依專案需求調整過的 `.agent.md`、`.instructions.md`、`.prompt.md`、Hooks 設定與腳本、Plugin 配置 | 選填     |
| 驗證測試結果       | 測試報告、驗收報告（可對應 [templates/ssdlc/testing/](../templates/ssdlc/testing/) 格式）       | 選填     |
| 成效數據           | 導入前後的量化比較（審查耗時、缺陷密度、安全掃描發現數等）                                      | 選填     |

## 資料夾命名規則

```
case-studies/<project-slug>/
```

- `project-slug` 一律小寫、以連字號分隔（例如 `ecommerce-backend-revamp`）
- 請使用**去識別化**的專案代號，不要使用真實公司或客戶名稱

## 資料夾結構

```
case-studies/<project-slug>/
├── README.md          # 必要：專案背景、採用元件、使用心得（依 TEMPLATE 填寫）
├── agents/            # 選填：客製化後的 .agent.md
├── instructions/       # 選填：客製化後的 .instructions.md
├── prompts/            # 選填：客製化後的 .prompt.md
├── hooks/              # 選填：客製化後的 Hooks 設定與腳本
├── plugins/            # 選填：客製化後的 Plugin 配置
└── reports/            # 選填：驗證測試報告、成效數據
```

沒有內容的子目錄可以整個省略，不需要保留空資料夾。

## 送出前必做檢查

⚠️ 送出 PR 前，請務必確認已移除：

- 公司 / 客戶名稱、內部網域、IP、伺服器位址
- API 金鑰、密碼、Token、憑證
- 內部專案代號、未公開的商業資訊
- 個資或任何可識別特定人員的資訊

## 開始貢獻

1. 複製 [TEMPLATE/](TEMPLATE/) 資料夾並重新命名為你的專案代號
2. 依範本填寫 `README.md`
3. 放入需要回饋的客製化檔案／報告（可選）
4. 完成上方檢查後，依 [CONTRIBUTING.md](../CONTRIBUTING.md#提交-pull-request-流程) 送出 Pull Request
