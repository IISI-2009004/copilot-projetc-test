---
description: "bash、sh、zsh 和其他 shell 的 shell 腳本最佳實務和約定"
applyTo: "**/*.sh"
---

# Shell Scripting Guidelines 腳本編寫指南

編寫簡潔、安全、易於維護的 shell 腳本（適用於 bash、sh、zsh 和其他 shell）的說明。

## General Principles 一般原則

- 生成乾淨、簡單且簡潔的代碼
- 確保腳本易於閱讀和理解
- 在有助於理解腳本工作原理的地方添加註釋
- 生成簡潔且簡單的 echo 輸出以提供執行狀態
- 避免不必要的 echo 輸出和過多的日誌記錄
- 在可用時使用 shellcheck 進行靜態分析
- 假設腳本用於自動化和測試，而不是生產系統，除非另有說明
- 優先使用安全的擴展：雙引號變量引用（`"$var"`）、使用 `${var}` 以提高清晰度，並避免使用 `eval`
- 在可移植性要求允許的情況下使用現代 Bash 功能（`[[ ]]`、`local`、數組）；僅在需要時回退到 POSIX 構造
- 為結構化數據選擇可靠的解析器，而不是臨時的文本處理

## Error Handling & Safety 錯誤處理與安全

- 始終啟用 `set -euo pipefail` 以在錯誤發生時快速失敗，捕獲未設置的變量，並顯示管道失敗
- 在執行前驗證所有必需的參數
- 提供具有上下文的清晰錯誤訊息
- 使用 `trap` 清理臨時資源或在腳本終止時處理意外退出
- 使用 `readonly`（或 `declare -r`）聲明不可變值以防止意外重新賦值
- 使用 `mktemp` 安全地創建臨時文件或目錄，並確保在清理處理程序中將其刪除

## Script Structure 腳本結構

- 首先要有一個清晰的 shebang： #!/bin/bash 除非另有說明
- 包含一個標頭註解，說明腳本的用途
- 在頂部定義所有變量的默認值
- 使用函數來封裝可重用的代碼塊
- 創建可重用的函數，而不是重複類似的代碼塊
- 保持主執行流程清晰且易於閱讀

## Working with JSON and YAML 處理 JSON 和 YAML

- 優先使用專用解析器（JSON 使用 jq ，YAML 使用 yq ，或用 jq 解析透過 yq 轉換的 JSON），而不是使用 grep 、 awk 或 shell 字串分割等臨時文字處理工具。
- 當 `jq`/`yq` 不可用或不適合時，選擇環境中可用的下一個最可靠的解析器，並明確說明如何安全使用
- 驗證所需字段是否存在，並明確處理缺失/無效的數據路徑（例如，通過檢查 `jq` 退出狀態或使用 `// empty`）
- 引用 jq/yq 過濾器以防止 shell 展開，並在需要純字符串時優先使用 `--raw-output`
- 將解析器錯誤視為致命：與 `set -euo pipefail` 結合使用，或在使用結果之前測試命令成功
- 在腳本頂部記錄解析器依賴，並在需要但未安裝 `jq`/`yq`（或替代工具）時快速失敗並提供有用的訊息

```bash
#!/bin/bash

# ============================================================================
# Script Description Here
# ============================================================================

set -euo pipefail

cleanup() {
    # Remove temporary resources or perform other teardown steps as needed
    if [[ -n "${TEMP_DIR:-}" && -d "$TEMP_DIR" ]]; then
        rm -rf "$TEMP_DIR"
    fi
}

trap cleanup EXIT

# Default values
RESOURCE_GROUP=""
REQUIRED_PARAM=""
OPTIONAL_PARAM="default-value"
readonly SCRIPT_NAME="$(basename "$0")"

TEMP_DIR=""

# Functions
usage() {
    echo "Usage: $SCRIPT_NAME [OPTIONS]"
    echo "Options:"
    echo "  -g, --resource-group   Resource group (required)"
    echo "  -h, --help            Show this help"
    exit 0
}

validate_requirements() {
    if [[ -z "$RESOURCE_GROUP" ]]; then
        echo "Error: Resource group is required"
        exit 1
    fi
}

main() {
    validate_requirements

    TEMP_DIR="$(mktemp -d)"
    if [[ ! -d "$TEMP_DIR" ]]; then
        echo "Error: failed to create temporary directory" >&2
        exit 1
    fi

    echo "============================================================================"
    echo "Script Execution Started"
    echo "============================================================================"

    # Main logic here

    echo "============================================================================"
    echo "Script Execution Completed"
    echo "============================================================================"
}

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -g|--resource-group)
            RESOURCE_GROUP="$2"
            shift 2
            ;;
        -h|--help)
            usage
            ;;
        *)
            echo "Unknown option: $1"
            exit 1
            ;;
    esac
done

# Execute main function
main "$@"

```
