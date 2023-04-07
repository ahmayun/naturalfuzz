import os
import re
import sys

def generate_output(name, directory):
    # iterate over all files in the directory
    for filename in os.listdir(directory):
        # check if the file is a Scala file
        if filename.endswith('.scala'):
            # extract the mutation ID and operation from the filename
            classname = os.path.basename(filename).split(".")[0]
            # generate the output line
            output_line = f'"{classname}" -> mutants.{name}.{classname}.main,'
            print(output_line)

print(sys.argv)
# call the generate_output function with a directory path
NAME = sys.argv[1]
OUT_DIR = sys.argv[2]
generate_output(NAME, OUT_DIR)