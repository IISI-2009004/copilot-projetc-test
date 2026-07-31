# save-chat-history.ps1
# Stop hook: appends session transcript to two files in the workspace root.
#   chat_history.md        - Q&A only (user messages + assistant final text)
#   chat_detail_history.md - Full detail including all tool calls
# Receives VS Code Copilot hook Stop event JSON on stdin.
# Transcripts stored at:
#   %APPDATA%\Code\User\workspaceStorage\*\GitHub.copilot-chat\transcripts\{session_id}.jsonl

param()

# --- Read stdin ---
$rawInput = $input | Out-String
$data = $null
try { $data = $rawInput | ConvertFrom-Json } catch { }

$timestamp  = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
$stopReason = if ($data.stop_reason) { $data.stop_reason } else { "n/a" }
$sessionId  = if ($data.session_id)  { $data.session_id  } else { "" }

# Resolve workspace root (script lives at .github/hooks/scripts/, go up 3 levels)
$workspaceRoot = Split-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) -Parent
$simpleHistPath  = Join-Path $workspaceRoot "chat_history.md"
$detailHistPath  = Join-Path $workspaceRoot "chat_detail_history.md"
$debugPath       = Join-Path $workspaceRoot "chat_history.debug.json"

# --- Find the VS Code Copilot transcript JSONL by session_id ---
function Find-TranscriptPath($sid) {
    if ([string]::IsNullOrWhiteSpace($sid)) { return $null }
    $base = Join-Path $env:APPDATA "Code\User\workspaceStorage"
    if (-not (Test-Path $base)) { return $null }
    $found = Get-ChildItem -Path $base -Recurse -Filter "$sid.jsonl" -ErrorAction SilentlyContinue |
             Where-Object { $_.FullName -match "GitHub\.copilot-chat.transcripts" } |
             Select-Object -First 1
    if ($found) { return $found.FullName }
    return $null
}

# --- Build session header (shared) ---
$header = [System.Collections.Generic.List[string]]::new()
$header.Add("")
$header.Add("---")
$header.Add("")
$header.Add("## Session $timestamp")
$header.Add("")
if ($sessionId) { $header.Add("- **Session ID**: $sessionId") }
$header.Add("- **Stop reason**: $stopReason")
$header.Add("")

$simpleLines = [System.Collections.Generic.List[string]]::new()
$simpleLines.AddRange($header)
$detailLines = [System.Collections.Generic.List[string]]::new()
$detailLines.AddRange($header)

# --- Parse the transcript JSONL ---
$transcriptLoaded = $false
$transcriptPath = Find-TranscriptPath $sessionId
if (-not $transcriptPath -and $data.transcript_path -and (Test-Path $data.transcript_path)) {
    $transcriptPath = $data.transcript_path
}

if ($transcriptPath -and (Test-Path $transcriptPath)) {
    $jsonlLines = Get-Content $transcriptPath -Encoding UTF8 -ErrorAction SilentlyContinue
    foreach ($jLine in $jsonlLines) {
        if ([string]::IsNullOrWhiteSpace($jLine)) { continue }
        try {
            $entry = $jLine | ConvertFrom-Json
            switch ($entry.type) {
                "user.message" {
                    $content = $entry.data.content
                    if (-not [string]::IsNullOrWhiteSpace($content)) {
                        # Both files get user messages
                        foreach ($target in @($simpleLines, $detailLines)) {
                            $target.Add("### [USER]")
                            $target.Add("")
                            $target.Add($content)
                            $target.Add("")
                        }
                        $transcriptLoaded = $true
                    }
                }
                "assistant.message" {
                    $content  = $entry.data.content
                    $tools    = $entry.data.toolRequests
                    $hasText  = -not [string]::IsNullOrWhiteSpace($content)
                    $hasTools = $tools -and $tools.Count -gt 0

                    # chat_history.md: only assistant messages that have text (final responses)
                    if ($hasText) {
                        $simpleLines.Add("### [ASSISTANT]")
                        $simpleLines.Add("")
                        $simpleLines.Add($content)
                        $simpleLines.Add("")
                        $transcriptLoaded = $true
                    }

                    # chat_detail_history.md: all assistant messages including tool calls
                    if ($hasText -or $hasTools) {
                        $detailLines.Add("### [ASSISTANT]")
                        $detailLines.Add("")
                        if ($hasText) {
                            $detailLines.Add($content)
                            $detailLines.Add("")
                        }
                        if ($hasTools) {
                            foreach ($t in $tools) {
                                $argStr = ""
                                try { $argStr = ($t.arguments | ConvertFrom-Json | ConvertTo-Json -Compress) } catch { $argStr = $t.arguments }
                                $detailLines.Add("_[Tool: $($t.name) $argStr]_")
                                $detailLines.Add("")
                            }
                        }
                        $transcriptLoaded = $true
                    }
                }
            }
        } catch { }
    }
}

# --- Last resort: write debug file and note in history ---
if (-not $transcriptLoaded) {
    $rawInput.Trim() | Set-Content $debugPath -Encoding UTF8
    $fallback = @(
        "_Could not parse transcript. Raw Stop event saved to_ ``chat_history.debug.json``.",
        "",
        '```json',
        $rawInput.Trim(),
        '```',
        ""
    )
    $simpleLines.AddRange([string[]]$fallback)
    $detailLines.AddRange([string[]]$fallback)
} elseif (Test-Path $debugPath) {
    Remove-Item $debugPath -Force
}

# --- Write / append both files ---
function Append-HistoryFile($path, $title, $content) {
    if (-not (Test-Path $path)) {
        "# $title" | Set-Content $path -Encoding UTF8
    }
    $content | Add-Content $path -Encoding UTF8
}

Append-HistoryFile $simpleHistPath "Chat History" $simpleLines
Append-HistoryFile $detailHistPath "Chat Detail History" $detailLines

exit 0
