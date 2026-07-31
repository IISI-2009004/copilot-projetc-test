---
description: '本指南指導您如何使用 GitHub Copilot 編寫註釋，以更少的註釋實現程式碼的自解釋性。範例使用 JavaScript，但適用於任何支援註解的語言。'
applyTo: '**'
---

# Self-explanatory Code Commenting Instructions 自解釋性程式碼註釋說明

## Core Principle 核心原則
**編寫能夠自我解釋的程式碼。僅在必要時添加註釋來解釋“為什麼”，而不是“是什麼”。**
大多數情況下，我們不需要註釋。

## Commenting Guidelines 註釋指南

### ❌ AVOID These Comment Types

**Obvious Comments**
```javascript
// Bad: States the obvious
let counter = 0;  // Initialize counter to zero
counter++;  // Increment counter by one
```

**Redundant Comments**
```javascript
// Bad: Comment repeats the code
function getUserName() {
    return user.name;  // Return the user's name
}
```

**Outdated Comments**
```javascript
// Bad: Comment doesn't match the code
// Calculate tax at 5% rate
const tax = price * 0.08;  // Actually 8%
```

### ✅ WRITE These Comment Types 

**Complex Business Logic**
```javascript
// Good: Explains WHY this specific calculation
// Apply progressive tax brackets: 10% up to 10k, 20% above
const tax = calculateProgressiveTax(income, [0.10, 0.20], [10000]);
```

**Non-obvious Algorithms**
```javascript
// Good: Explains the algorithm choice
// Using Floyd-Warshall for all-pairs shortest paths
// because we need distances between all nodes
for (let k = 0; k < vertices; k++) {
    for (let i = 0; i < vertices; i++) {
        for (let j = 0; j < vertices; j++) {
            // ... implementation
        }
    }
}
```

**Regex Patterns**
```javascript
// Good: Explains what the regex matches
// Match email format: username@domain.extension
const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
```

**API Constraints or Gotchas**
```javascript
// Good: Explains external constraint
// GitHub API rate limit: 5000 requests/hour for authenticated users
await rateLimiter.wait();
const response = await fetch(githubApiUrl);
```

## Decision Framework 決策框架

在發表評論之前，請先問自己：
1. **程式碼是否清晰易懂？** → 不需要註釋
2. **更好的變數/函數名稱是否能消除註釋的需求？** → 重構代碼
3. **這是否解釋了為什麼，而不是做了什麼？** → 好的註釋
4. **這會幫助未來的維護者嗎？** → 好的註釋

## Special Cases for Comments 特例註釋

### Public APIs
```javascript
/**
 * Calculate compound interest using the standard formula.
 * 
 * @param {number} principal - Initial amount invested
 * @param {number} rate - Annual interest rate (as decimal, e.g., 0.05 for 5%)
 * @param {number} time - Time period in years
 * @param {number} compoundFrequency - How many times per year interest compounds (default: 1)
 * @returns {number} Final amount after compound interest
 */
function calculateCompoundInterest(principal, rate, time, compoundFrequency = 1) {
    // ... implementation
}
```

### Configuration and Constants 配置與常量
```javascript
// Good: Explains the source or reasoning
const MAX_RETRIES = 3;  // Based on network reliability studies
const API_TIMEOUT = 5000;  // AWS Lambda timeout is 15s, leaving buffer
```

### Annotations 註解
```javascript
// TODO: Replace with proper user authentication after security review
// FIXME: Memory leak in production - investigate connection pooling
// HACK: Workaround for bug in library v2.1.0 - remove after upgrade
// NOTE: This implementation assumes UTC timezone for all calculations
// WARNING: This function modifies the original array instead of creating a copy
// PERF: Consider caching this result if called frequently in hot path
// SECURITY: Validate input to prevent SQL injection before using in query
// BUG: Edge case failure when array is empty - needs investigation
// REFACTOR: Extract this logic into separate utility function for reusability
// DEPRECATED: Use newApiFunction() instead - this will be removed in v3.0
```

## Anti-Patterns to Avoid 應避免的反模式

### Dead Code Comments 死碼註釋
```javascript
// Bad: Don't comment out code
// const oldFunction = () => { ... };
const newFunction = () => { ... };
```

### Changelog Comments 變更日誌註釋
```javascript
// Bad: Don't maintain history in comments
// Modified by John on 2023-01-15
// Fixed bug reported by Sarah on 2023-02-03
function processData() {
    // ... implementation
}
```

### Divider Comments 
```javascript
// Bad: Don't use decor ative comments
//=====================================
// UTILITY FUNCTIONS
//=====================================
```

## Quality Checklist 質量檢查清單

在提交之前，請確保您的註釋：
- [ ] 解釋為什麼，而不是解釋什麼。
- [ ] 語法正確且清晰
- [ ] 隨著代碼演變保持準確
- [ ] 為代碼理解增加真正的價值
- [ ] 放置在適當的位置（在它們描述的代碼上方）
- [ ] 使用正確的拼寫和專業語言

## Summary 總結

記住：**最好的註釋是你不需要寫的註釋，因為代碼本身已經自我說明。**
