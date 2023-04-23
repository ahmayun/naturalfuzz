#!/bin/bash

# sample run:
#     ./run-experiments.sh experimentsdir:~/final-experiments datasetsdir:src/seeds/bigfuzz

EXPERIMENTS_DIR=$1

fuzz-mutant() {
  QUERY=$1
  MUTANT=$2
  echo "starting fuzzing for mutant $MUTANT of program $QUERY"

}

run-fuzzing-test() {
  QUERY=$1
  FUZZ_DIR=$2
  echo "starting fuzzing tests for $QUERY"
  DIR="src/main/scala/examples/mutants/$QUERY"
  for mutant in "$DIR"/Q*; do
    MUTANT=$(drop-path-and-ext $mutant)
    fuzz-mutant $QUERY $MUTANT
  done
  unset QUERY
  unset MUTANT
  unset DIR
}

drop-path-and-ext() {
  file=$1
  FILE=$(basename "$file") # drop path prefix
  echo ${FILE%.*} # drop .scala extension
}

get-dataset-paths() {
  QUERY=$1

  case $QUERY in
    Q1)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{store_returns,date_dim,store,customer}"
      ;;
    Q3)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{store_sales,date_dim,item}"
      ;;
    Q6)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{customer_address,customer,store_sales,date_dim,item}"
      ;;
    Q7)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{customer_demographics,promotion,store_sales,date_dim,item}"
      ;;
    Q12)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{web_sales,date_dim,item}"
      ;;
    Q15)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{catalog_sales,customer,customer_address,date_dim}"
      ;;
    Q19)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{date_dim,store_sales,item,customer,customer_address,store}"
      ;;
    Q20)
      eval echo "/TPCDS_1G_NOHEADER_NOCOMMAS/{catalog_sales,date_dim,item}"
      ;;
    *)
      echo "Invalid query"
      ;;
  esac
}

run-overhead-test() {
  QUERY=$1
  echo "starting overhead test for $QUERY"
  sed -i "s/var benchmarkName = \".*\"/var benchmarkName = \"$QUERY\"/" src/main/scala/runners/Config.scala
  ./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest $QUERY spark://zion-headnode:7077 $EXPERIMENT_DIR $(get-dataset-paths $QUERY) || exit 1
  unset QUERY
}


for file in $(ls -hatr1 $EXPERIMENTS_DIR | grep Q); do
  PROGRAM=$(echo $file | sed -E 's/(Q[0-9]{1,2}).+/\1/')
  echo "fuzzing $PROGRAM from $file"
  run-fuzzing-test $PROGRAM $file
done
