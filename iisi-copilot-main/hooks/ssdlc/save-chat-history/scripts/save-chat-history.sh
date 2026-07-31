#!/usr/bin/env bash
# save-chat-history.sh
# Stop Hook: appends the session transcript to two files in the workspace root.
#   chat_history.md        - Q&A only (user messages + assistant final text)
#   chat_detail_history.md - Full detail including all tool calls
# Cross-platform (Linux / macOS / WSL) counterpart of save-chat-history.ps1.
# Receives the VS Code / Copilot Stop event JSON on stdin. Requires: jq.
#
# VS Code Copilot transcripts are stored per OS at:
#   Linux : ~/.config/Code/User/workspaceStorage/*/GitHub.copilot-chat/transcripts/{session_id}.jsonl
#   macOS : ~/Library/Application Support/Code/User/workspaceStorage/*/GitHub.copilot-chat/transcripts/{session_id}.jsonl
#   WSL   : $APPDATA/Code/User/workspaceStorage/... (when APPDATA is exported)
set -uo pipefail

INPUT=$(cat)

# --- Guard against infinite loop when a Stop hook re-triggers ---
STOP_ACTIVE=$(printf '%s' "$INPUT" | jq -r '.stop_hook_active // false' 2>/dev/null || echo false)
if [ "$STOP_ACTIVE" = "true" ]; then
  echo '{}'
  exit 0
fi

TIMESTAMP=$(date +"%Y-%m-%d %H:%M:%S")
SESSION_ID=$(printf '%s' "$INPUT" | jq -r '.session_id // .sessionId // ""' 2>/dev/null)
STOP_REASON=$(printf '%s' "$INPUT" | jq -r '.stop_reason // "n/a"' 2>/dev/null)

# Resolve workspace root: prefer the cwd supplied in the event, else $PWD.
WORKSPACE_ROOT=$(printf '%s' "$INPUT" | jq -r '.cwd // ""' 2>/dev/null)
[ -z "$WORKSPACE_ROOT" ] && WORKSPACE_ROOT="$PWD"

SIMPLE="$WORKSPACE_ROOT/chat_history.md"
DETAIL="$WORKSPACE_ROOT/chat_detail_history.md"
DEBUG="$WORKSPACE_ROOT/chat_history.debug.json"

# --- Locate the transcript JSONL by session_id across known OS locations ---
find_transcript() {
  local sid="$1"
  [ -z "$sid" ] && return 1
  local bases=(
    "$HOME/.config/Code/User/workspaceStorage"
    "$HOME/Library/Application Support/Code/User/workspaceStorage"
    "${APPDATA:-}/Code/User/workspaceStorage"
  )
  local b f
  for b in "${bases[@]}"; do
    [ -n "$b" ] && [ -d "$b" ] || continue
    f=$(find "$b" -path "*GitHub.copilot-chat/transcripts/$sid.jsonl" 2>/dev/null | head -1)
    [ -n "$f" ] && { printf '%s' "$f"; return 0; }
  done
  return 1
}

TRANSCRIPT=$(find_transcript "$SESSION_ID" || true)
if [ -z "$TRANSCRIPT" ]; then
  TP=$(printf '%s' "$INPUT" | jq -r '.transcript_path // ""' 2>/dev/null)
  [ -n "$TP" ] && [ -f "$TP" ] && TRANSCRIPT="$TP"
fi

# --- Ensure both files exist with a title ---
[ -f "$SIMPLE" ] || printf '# Chat History\n' > "$SIMPLE"
[ -f "$DETAIL" ] || printf '# Chat Detail History\n' > "$DETAIL"

# --- Shared session header ---
write_header() {
  local f="$1"
  {
    printf '\n---\n\n## Session %s\n\n' "$TIMESTAMP"
    [ -n "$SESSION_ID" ] && printf -- '- **Session ID**: %s\n' "$SESSION_ID"
    printf -- '- **Stop reason**: %s\n\n' "$STOP_REASON"
  } >> "$f"
}
write_header "$SIMPLE"
write_header "$DETAIL"

LOADED=false
if [ -n "$TRANSCRIPT" ] && [ -f "$TRANSCRIPT" ]; then
  while IFS= read -r line || [ -n "$line" ]; do
    [ -z "$line" ] && continue
    TYPE=$(printf '%s' "$line" | jq -r '.type // ""' 2>/dev/null) || continue
    case "$TYPE" in
      user.message)
        CONTENT=$(printf '%s' "$line" | jq -r '.data.content // ""' 2>/dev/null)
        if [ -n "$CONTENT" ]; then
          for f in "$SIMPLE" "$DETAIL"; do
            printf '### [USER]\n\n%s\n\n' "$CONTENT" >> "$f"
          done
          LOADED=true
        fi
        ;;
      assistant.message)
        CONTENT=$(printf '%s' "$line" | jq -r '.data.content // ""' 2>/dev/null)
        HASTOOLS=$(printf '%s' "$line" | jq -r '(.data.toolRequests // []) | length' 2>/dev/null)
        [ -n "$CONTENT" ] && { printf '### [ASSISTANT]\n\n%s\n\n' "$CONTENT" >> "$SIMPLE"; LOADED=true; }
        if [ -n "$CONTENT" ] || { [ -n "$HASTOOLS" ] && [ "$HASTOOLS" -gt 0 ]; }; then
          printf '### [ASSISTANT]\n\n' >> "$DETAIL"
          [ -n "$CONTENT" ] && printf '%s\n\n' "$CONTENT" >> "$DETAIL"
          if [ -n "$HASTOOLS" ] && [ "$HASTOOLS" -gt 0 ]; then
            printf '%s' "$line" | jq -r '.data.toolRequests[] | "_[Tool: \(.name) \(.arguments)]_\n"' >> "$DETAIL"
          fi
          LOADED=true
        fi
        ;;
    esac
  done < "$TRANSCRIPT"
fi

# --- Fallback: dump the raw Stop event so nothing is lost ---
if [ "$LOADED" = "false" ]; then
  printf '%s' "$INPUT" > "$DEBUG"
  for f in "$SIMPLE" "$DETAIL"; do
    {
      printf '_Could not parse transcript. Raw Stop event saved to_ `chat_history.debug.json`.\n\n'
      printf '```json\n%s\n```\n\n' "$INPUT"
    } >> "$f"
  done
elif [ -f "$DEBUG" ]; then
  rm -f "$DEBUG"
fi

echo '{}'
exit 0
