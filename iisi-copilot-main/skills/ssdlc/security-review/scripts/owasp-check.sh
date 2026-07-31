#!/bin/bash
# OWASP Dependency Check 執行腳本
echo "=== OWASP Dependency Check ==="
if [ -f "pom.xml" ]; then
    mvn org.owasp:dependency-check-maven:check -DfailBuildOnCVSS=7
elif [ -f "package.json" ]; then
    npm audit --audit-level=high
elif [ -f "requirements.txt" ]; then
    pip-audit -r requirements.txt
else
    echo "未找到支援的依賴管理檔案"
fi
