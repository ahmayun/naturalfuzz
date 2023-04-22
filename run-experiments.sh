#!/bin/bash

fuzz-mutants() {
  QUERY=$1
  DIR="src/main/scala/examples/mutants/$QUERY"
  for file in "$DIR"/Q*; do
    echo "Processing file $file"
  done
}

# Loop from 1 to 10
for i in $(seq 1 10); do
  # Command to be executed in the loop
  echo "Loop iteration: $i"
  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q1 spark://zion-headnode:7077 ~/rig-experiments-test /TPCDS_1G_NOHEADER_NOCOMMAS/{store_returns,date_dim,store,customer}
  fuzz-mutants Q1
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q3 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{store_sales,date_dim,item}
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q6 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{customer_address,customer,store_sales,date_dim,item}
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q7 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{customer_demographics,promotion,store_sales,date_dim,item}
done