#!/bin/bash
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$(dirname "$SCRIPT_DIR")"

# 既存プロセス停止
kill $(fuser 3000/tcp 2>/dev/null) 2>/dev/null || true
kill $(fuser 3001/tcp 2>/dev/null) 2>/dev/null || true
sleep 1

export SAGEMAKER=1
export NEXT_PUBLIC_SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/absports/3000"

cd "$FRONTEND_DIR"

# ビルド
echo "Building Next.js..."
npm run build

# Next.js を 3001 で起動（127.0.0.1 にバインド）
echo "Starting Next.js on :3001..."
npm run start -- -H 127.0.0.1 -p 3001 &
sleep 3

# 復元プロキシを 3000 で起動
echo "Starting SageMaker proxy on :3000..."
node scripts/sagemaker-proxy.mjs &

echo ""
echo "=== SageMaker Preview Ready ==="
echo "PORTS タブで 3000 の地球儀を押し、URL の ports → absports に置換してアクセス"
echo ""
