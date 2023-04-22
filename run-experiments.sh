#!/bin/bash

fuzz-mutants() {
  QUERY=$1
  DIR="src/main/scala/examples/mutants/$QUERY"
  for mutant in "$DIR"/Q*; do
    echo "MUTANT $mutant"
  done
}


# Loop from 1 to 10
for i in $(seq 1 10); do
    for file in src/main/scala/examples/faulty/Q*; do
      SCALAFILE=$(basename "$file") # drop path prefix
      PROGRAM=${SCALAFILE%.*} # drop .scala extension
      echo "Running EXPERIMENT $i for $PROGRAM"
      fuzz-mutants $PROGRAM
    done
done