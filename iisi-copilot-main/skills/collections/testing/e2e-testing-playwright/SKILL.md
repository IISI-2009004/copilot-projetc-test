---
name: "e2e-testing-playwright"
description: "驗證完整使用者流程（跨頁面、跨前後端）是否符合使用者故事時使用"
skill: e2e-testing-playwright
version: 1.0
tags: [e2e-testing, playwright, typescript]
applicable-to: [TypeScript, Playwright]
last-updated: 2026-07-20
---

# E2E 測試技能（Playwright）

## 適用情境

驗證完整使用者流程（跨頁面、跨前後端）是否符合使用者故事時使用。

## 核心原則

1. 使用 Page Object Model 封裝頁面互動，測試案例本身只描述業務流程
2. 優先使用 `expect()` 的自動重試斷言，避免手動 `waitForTimeout`
3. 測試資料使用獨立 Fixture，不依賴共享的環境資料
4. 失敗時自動截圖與錄影，方便除錯

## 標準做法

### Page Object Model

```typescript
export class TransferPage {
  constructor(private page: Page) {}

  async fillAmount(amount: string) {
    await this.page.getByLabel("轉帳金額").fill(amount);
  }

  async submit() {
    await this.page.getByRole("button", { name: "確認轉帳" }).click();
  }
}

test("餘額不足時應顯示錯誤訊息", async ({ page }) => {
  const transferPage = new TransferPage(page);
  await page.goto("/transfer");
  await transferPage.fillAmount("999999");
  await transferPage.submit();
  await expect(page.getByText("餘額不足")).toBeVisible();
});
```

## 禁止事項

- 不得使用 `page.waitForTimeout()` 做固定延遲等待（改用條件式的 `expect`）
- 不得讓測試依賴生產或共享的測試環境資料（用 Fixture 建立獨立資料）
- 不得忽略行動裝置尺寸（viewport）與無障礙（a11y）情境

## 驗收標準

- [ ] 核心使用者故事（Happy Path + 主要錯誤路徑）皆有對應 E2E 測試
- [ ] 測試失敗時自動保留截圖／錄影供除錯
- [ ] 測試資料獨立於共享環境，可重複執行不互相干擾

## 參考資料

- Playwright 官方文件（https://playwright.dev）
- 本手冊 14.3 節 Prompt-008（E2E 測試 Playwright Prompt）
