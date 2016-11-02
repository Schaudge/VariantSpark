#!/bin/bash

PWD=$(cd `dirname "$0"`/..; pwd)
PATH=${PATH}:${PWD}
export VS_ECHO_CMDLINE=YES

FLUSH_ROOT=${VS_FLUSH_DIR:-.}
FLUSH_DIR=${FLUSH_ROOT}/variant-spark-flush
GEN_DIR=${FLUSH_DIR}/gen

variant-spark --spark --master yarn-client --num-executors 32 --executor-memory 4G --driver-memory 4G -- \
 gen-labels -if ${GEN_DIR}/synthetic_2M_5K_3.parquet -sp 256 -sr 13 -v -ff ${GEN_DIR}/synthetic_2M_5K_3_noise-labels.csv -fc resp -ge v_10:0.85 -ge v_100:0.9 -ge v_1000:1.1 -ge v_10000:1.0 -ge v_100000:1.15 -gm 0.2 -gvf 0.001 -fcc resp_cont -fiv  "$@"

