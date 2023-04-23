#!/bin/bash

# sample run:
#     ./run-experiments.sh experimentsdir:~/final-experiments datasetsdir:src/seeds/bigfuzz

EXPERIMENTS_DIR=$1
DIR_BIGFUZZ_SAMPLES=$2

get-dataset-paths() {
  QUERY=$1

  case $QUERY in
    Q1)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{store_returns,date_dim,store,customer}"
      ;;
    Q3)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{store_sales,date_dim,item}"
      ;;
    Q6)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{customer_address,customer,store_sales,date_dim,item}"
      ;;
    Q7)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{customer_demographics,promotion,store_sales,date_dim,item}"
      ;;
    Q12)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{web_sales,date_dim,item}"
      ;;
    Q15)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{catalog_sales,customer,customer_address,date_dim}"
      ;;
    Q19)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{date_dim,store_sales,item,customer,customer_address,store}"
      ;;
    Q20)
      eval echo "$DIR_BIGFUZZ_SAMPLES/{catalog_sales,date_dim,item}"
      ;;
    *)
      echo "Invalid query"
      ;;
  esac
}

drop-path-and-ext() {
  file=$1
  FILE=$(basename "$file") # drop path prefix
  echo ${FILE%.*} # drop .scala extension
}

fuzz-mutant() {
  QUERY=$1
  MUTANT=$2
  FUZZ_DIR=$3
  echo "starting fuzzing for mutant $MUTANT of program $QUERY"
  echo "RIGFUZZ"
#  echo ./run-rigfuzz.sh $QUERY $MUTANT faulty 20 $FUZZ_DIR/{qrs.pkl,reduced_data/dataset_*}
  echo "BIGFUZZ"
  echo ./run-bigfuzz.sh $QUERY $MUTANT faulty 20 $(get-dataset-paths $QUERY)

}

run-fuzzing-test() {
  QUERY=$1
  FUZZ_DIR=$2
  echo "starting fuzzing tests for $QUERY"
  DIR="src/main/scala/examples/mutants/$QUERY"
  for mutant in "$DIR"/Q*; do
    MUTANT=$(drop-path-and-ext $mutant)
    fuzz-mutant $QUERY $MUTANT $FUZZ_DIR
  done
  unset QUERY
  unset MUTANT
  unset DIR
}


for file in $(ls -hatr1 $EXPERIMENTS_DIR | grep Q); do
  PROGRAM=$(echo $file | sed -E 's/(Q[0-9]{1,2}).+/\1/')
  echo "fuzzing $PROGRAM from $file"
  run-fuzzing-test $PROGRAM "$EXPERIMENTS_DIR/$file"
done
