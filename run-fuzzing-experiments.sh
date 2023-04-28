#!/bin/bash

# sample run:
#     ./run-fuzzing-experiments.sh ~/final-overhead-tests seeds/bigfuzz jazzer 305

EXPERIMENTS_DIR=$1
DIR_BIGFUZZ_SAMPLES=$2
TOOL=$3
DURATION=$4

get-dataset-paths() {
  QUERY=$1

  case $QUERY in
    Q1)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{store_returns,date_dim,store,customer}"
      ;;
    Q3)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{store_sales,date_dim,item}"
      ;;
    Q6)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{customer_address,customer,store_sales,date_dim,item}"
      ;;
    Q7)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{customer_demographics,promotion,store_sales,date_dim,item}"
      ;;
    Q12)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{web_sales,date_dim,item}"
      ;;
    Q15)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{catalog_sales,customer,customer_address,date_dim}"
      ;;
    Q19)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{date_dim,store_sales,item,customer,customer_address,store}"
      ;;
    Q20)
      eval echo "$DIR_BIGFUZZ_SAMPLES/$QUERY/{catalog_sales,date_dim,item}"
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
  SUFFIX=$4
  echo "starting fuzzing for mutant $MUTANT of program $QUERY"
  case $TOOL in
    rigfuzz)
      ./run-rigfuzz.sh $QUERY $MUTANT faulty $DURATION $FUZZ_DIR/{qrs.pkl,reduced_data/dataset_*} || exit 1
      mv target/RIG-output/$MUTANT "target/RIG-output/$MUTANT$SUFFIX"
      ;;
    bigfuzz)
      ./run-bigfuzz.sh $QUERY $MUTANT faulty $DURATION $(get-dataset-paths $QUERY) || exit 1
      mv target/bigfuzz-output/$MUTANT "target/bigfuzz-output/$MUTANT$SUFFIX"
      ;;
    jazzer)
      ./run-jazzer-cluster.sh $QUERY $MUTANT mutant faulty $DURATION || exit 1
      mv target/jazzer-output/$MUTANT "target/jazzer-output/$MUTANT$SUFFIX"
      ;;
  esac

}

run-fuzzing-test() {
  QUERY=$1
  SUFFIX=$2
  FUZZ_DIR=$3
  echo "starting fuzzing tests for $QUERY"
  DIR="src/main/scala/examples/mutants/$QUERY"
  for mutant in "$DIR"/Q*; do
    MUTANT=$(drop-path-and-ext $mutant)
    fuzz-mutant $QUERY $MUTANT $FUZZ_DIR $SUFFIX
  done
  unset QUERY
  unset MUTANT
  unset DIR
}


for file in $(ls -hatr1 $EXPERIMENTS_DIR | grep Q); do
  PROGRAM=$(echo $file | sed -E 's/(Q[0-9]{1,2}).+/\1/')
  SUFFIX=$(echo $file | sed -E 's/Q[0-9]{1,2}(.+)/\1/')
  echo "fuzzing $PROGRAM from $file"
  run-fuzzing-test $PROGRAM $SUFFIX "$EXPERIMENTS_DIR/$file"
done
