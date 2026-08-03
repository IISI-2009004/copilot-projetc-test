---
description: "為 HTML 元素制定顏色使用指南和樣式規則，以確保設計易於存取且專業。"
applyTo: "**/*.html, **/*.css, **/*.js"
---

# HTML CSS Style Color Guide

在更新或建立用於瀏覽器渲染的 HTML/CSS 樣式時，請遵循下列準則。顏色名稱代表其各自色調範圍的完整光譜（例如，「藍色」包括海軍藍、天藍色等）。

## Color Definitions

- **Hot Colors**: 橙色、紅色和黃色
- **Cool Colors**: 藍色、綠色和紫色
- **Neutral Colors**: 灰色及其變化
- **Binary Colors**: 黑色和白色
- **60-30-10 Rule**
  - **Primary Color**: 使用 60% 的時間 (_冷色或淺色_)
  - **Secondary Color**: 使用 30% 的時間 (_冷色或淺色_)
  - **Accent**: 使用 10% 的時間 (_互補的熱色_)

## Color Usage Guidelines

平衡使用的顏色，應用 **60-30-10 規則** 到圖形設計元素，如背景、按鈕、卡片等...

### Background Colors

**Never Use:**

- 紫色或洋紅色
- 紅色、橙色或黃色
- 粉紅色
- 任何熱色
  **Recommended:**

- 白色或接近白色
- 淺色冷色調（例如，淺藍色、淺綠色）
- 細微的中性色調
- 顏色變化最小的淺色漸層

### Text Colors

**Never Use:**

- 黃色（對比度和可讀性差）
- 粉紅色
- 純白色或淺色文字在淺色背景上
- 純黑色或深色文字在深色背景上

**Recommended:**

- 深中性色（例如，#1f2328, #24292f）
- 接近黑色的變化（#000000 到 #333333）
  - 確保背景是淺色
- 深灰色（#4d4d4d, #6c757d）
- 高對比度組合以確保可訪問性
- 接近白色的變化（#ffffff 到 #f0f2f3）
  - 確保背景是深色

### Colors to Avoid

除非設計規範或使用者要求明確規定，否則應避免：

- 明亮的紫色和洋紅色
- 明亮的粉紅色和霓虹色
- 高飽和度的熱色
- 對比度低的顏色（不符合 WCAG 可訪問性標準）

### Colors to Use Sparingly

**Hot Colors** (red, orange, yellow):

- 僅用於關鍵警報、警告或錯誤訊息
- 僅在傳達緊急性或重要性時使用
- 限制在小面積的點綴區域，而非大面積區域
- 在使用熱色之前，考慮使用圖示或粗體文字作為替代

## Gradients

使用漸層色並進行微妙的顏色過渡，以保持專業美感。

### Best Practices

- 保持顏色變化最小（例如，#E6F2FF 到 #F5F7FA）
- 在同一色系內使用漸層
- 避免在單一漸層中混合熱色和冷色
- 優先使用線性漸層而非徑向漸層作為背景

### Appropriate Use Cases

- 背景容器和部分
- 按鈕懸停狀態和互動元素
- 投影和深度效果
- 標題和導航欄
- 卡片組件和面板

## Additional Resources

- [Color Tool](https://civicactions.github.io/uswds-color-tool/)
- [Government or Professional Color Standards](https://designsystem.digital.gov/design-tokens/color/overview/)
- [UI Color Palette Best Practices](https://www.interaction-design.org/literature/article/ui-color-palette)
- [Color Combination Resource](https://www.figma.com/resource-library/color-combinations/)
