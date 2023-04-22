#!/bin/bash


fuzz-mutant() {
  QUERY=$1
  MUTANT=$2
  echo "starting fuzzing for mutant $MUTANT of program $QUERY"
}

run-fuzzing() {
  QUERY=$1
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

run-overhead-test() {
  QUERY=$1
  echo "starting overhead test for $QUERY"
#  echo "./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest Q1 spark://zion-headnode:7077 ~/overheadtests /TPCDS_1G_NOHEADER_NOCOMMAS/{store_returns,date_dim,store,customer}"
  unset QUERY
}


# Loop from 1 to 10
for i in $(seq 1 10); do
    for file in src/main/scala/examples/faulty/Q*; do
      PROGRAM=$(drop-path-and-ext $file)
      echo "running EXPERIMENT $i for $PROGRAM"
      run-overhead-test $PROGRAM
      run-fuzzing-test $PROGRAM
    done
done