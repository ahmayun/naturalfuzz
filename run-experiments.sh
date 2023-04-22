#!/bin/bash


fuzz-mutant() {
  QUERY=$1
  MUTANT=$2
  echo "starting fuzzing for mutant $MUTANT of program $QUERY"
}

run-fuzzing-test() {
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

get-dataset-paths() {
  QUERY=$1

  case $QUERY in
    Q1)
      echo "Path for Q1 dataset"
      ;;
    Q3)
      echo "Path for Q3 dataset"
      ;;
    Q6)
      echo "Path for Q6 dataset"
      ;;
    Q7)
      echo "Path for Q7 dataset"
      ;;
    Q12)
      echo "Path for Q12 dataset"
      ;;
    Q15)
      echo "Path for Q15 dataset"
      ;;
    Q19)
      echo "Path for Q19 dataset"
      ;;
    Q20)
      echo "Path for Q20 dataset"
      ;;
    *)
      echo "Invalid query"
      ;;
  esac
}

run-overhead-test() {
  QUERY=$1
  echo "starting overhead test for $QUERY"
  echo "./cluster-assemble-and-distribute.sh runners.RunRIGFuzzOverheadTest $QUERY spark://zion-headnode:7077 ~/overheadtests $(get-dataset-paths $QUERY)"
  unset QUERY
}


# Loop from 1 to 10
for i in $(seq 1 1); do
    for file in src/main/scala/examples/faulty/Q*; do
      PROGRAM=$(drop-path-and-ext $file)
      echo "running EXPERIMENT $i for $PROGRAM"
      run-overhead-test $PROGRAM
      run-fuzzing-test $PROGRAM
    done
done