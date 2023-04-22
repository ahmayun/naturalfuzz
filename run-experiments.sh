#!/bin/bash

# Loop from 1 to 10
for i in $(seq 1 10); do
  # Command to be executed in the loop
  echo "Loop iteration: $i"
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q1 spark://zion-headnode:7077 ~/rig-experiments-test /TPCDS_1G_NOHEADER_NOCOMMAS/{store_returns,date_dim,store,customer}
  DIR="examples/mutants/Q1"
  # Loop through all files ending with ".info"
  for file in "$DIR"/Q*; do
    echo "Processing file $file"
  done
#  ./run-rigfuzz.sh Q1 Q1_M0 faulty 300 ~/overheadtests/Q1_day18-15hr-51m-53s/{qrs.pkl
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q3 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{store_sales,date_dim,item}
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q6 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{customer_address,customer,store_sales,date_dim,item}
#  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q7 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{customer_demographics,promotion,store_sales,date_dim,item}
done