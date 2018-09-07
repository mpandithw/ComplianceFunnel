#!/bin/bash
dataset=$1

echo "submit the spark job"
spark-submit --class com.ak.sensitive_tagger.App /usr/hdf/current/nifi/bin/command/stagger.jar /tmp/hortonhacks/rules/rules.json /tmp/hortonhacks/data/$dataset ip:6667 team04_to_nifi
