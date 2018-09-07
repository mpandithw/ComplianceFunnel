#!/bin/bash
dataset=$1
a=`date +%s`
policy_id="${a}"
policy_name="lockdown-${a}"

jsonMessage="'{"id":$policy_id,"guid":"b09e2e8d-a4cc-4984-a0c0-a1d257a9a5d2","isEnabled":true,"createdBy":"Admin","updatedBy":"Admin","createTime":1536238226928,"updateTime":1536238226930,"version":1,"service":"team04_hadoop","name":"$policy_name","policyType":0,"policyPriority":0,"description":"","resourceSignature":"a192e2624e570741e9d22a5c09d6c9b6522591933f337225bb1a2cee16a1899f","isAuditEnabled":true,"resources":{"path":{"values":["/tmp/hortonhacks/data"],"isExcludes":false,"isRecursive":true}},"policyItems":[{"accesses":[{"type":"read","isAllowed":true},{"type":"write","isAllowed":true},{"type":"execute","isAllowed":true}],"users":["complianceofficer","nifi"],"groups":[],"conditions":[],"delegateAdmin":false}],"denyPolicyItems":[],"allowExceptions":[],"denyExceptions":[],"dataMaskPolicyItems":[],"rowFilterPolicyItems":[],"options":{},"validitySchedules":[],"policyLabels":[]}'"

echo "creating ranger policy for data feed"

echo $jsonMessage
echo  "curl -iv -u admin:admin -d ${jsonMessage} -H \"Content-Type: application/json\" -X POST http://ip:6080/service/public/v2/api/policy -vvv"

curl -iv -u admin:admin -d ${jsonMessage} -H "Content-Type: application/json" -X POST http://ip:6080/service/public/v2/api/policy -vvv
[
