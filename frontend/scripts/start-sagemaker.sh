#!/bin/bash
export SAGEMAKER=1
export NEXT_PUBLIC_SAGEMAKER=1
export NEXT_PUBLIC_BASE_PATH="/codeeditor/default/absports/3000"
cd /home/sagemaker-user/adsi-workshop-template/frontend
npx next start -H 127.0.0.1 -p 3001 &
sleep 4
node scripts/sagemaker-proxy.mjs &
sleep 2
echo "DONE: $(curl -s -o /dev/null -w '%{http_code}' http://localhost:3000/absports/3000/login)"
