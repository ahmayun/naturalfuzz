#!~/anaconda3/bin/python3

# SAMPLE RUN:
#       ./run-rigfuzz.py FlightDistance faulty 86400 --email=ahmad35@vt.edu --compile=True

# Temporarily hard-coded, should be parsed from args

import sys
import os
import subprocess
import datetime
from socket import gethostname


NAME=sys.argv[1]
PACKAGE=sys.argv[2]
DURATION=sys.argv[3]
TO_EMAIL="ahmad35@vt.edu"

PATH_SCALA_SRC=f"src/main/scala/examples/{PACKAGE}/{NAME}.scala"
PATH_INSTRUMENTED_CLASSES=f"examples/{PACKAGE}/{NAME}*"
DIR_RIGFUZZ_OUT=f"target/RIG-output/{NAME}"

def remove_directory(directory_path):
    """
    Remove a directory and all of its contents.
    """
    if os.path.exists(directory_path):
        # Remove all the sub-directories and files within the directory
        for filename in os.listdir(directory_path):
            file_path = os.path.join(directory_path, filename)
            try:
                if os.path.isfile(file_path):
                    os.unlink(file_path)
                elif os.path.isdir(file_path):
                    remove_directory(file_path)
            except Exception as e:
                print(f"Failed to delete {file_path}. Reason: {e}")
        
        # Remove the directory itself
        try:
            os.rmdir(directory_path)
        except Exception as e:
            print(f"Failed to delete {directory_path}. Reason: {e}")
    else:
        print(f"{directory_path} does not exist.")

def mkdirs_p(s):
    """
    Create directories from a string in the format "a/{b,c,d}".
    """
    # Split the string into the directory name and subdirectory names
    dir_name, subdirs_str = s.split("/", 1)
    subdirs = subdirs_str.strip("{}").split(",")
    
    # Create the directory and subdirectories
    try:
        os.makedirs(dir_name)
        for subdir in subdirs:
            os.makedirs(os.path.join(dir_name, subdir))
    except OSError as e:
        print(f"Directory creation failed: {e}")
        exit(1)

def execute_command(command, wait=True):
    """
    Execute a command and return the output and error messages.
    """
    process = subprocess.Popen(" ".join(command), shell=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    if(wait):
        process.wait()
        
    output, error = process.communicate()
    if process.returncode != 0:
        print(f"Command `{command}` failed with error:\n\t{error.decode()}")
        exit(process.returncode)
    print(output.decode())
    print(error.decode())


def get_current_time():
    """
    Get the current time and date as a string.
    """
    now = datetime.datetime.now()
    return now.strftime("%H:%M:%S %d-%m-%y")



remove_directory(DIR_RIGFUZZ_OUT)
execute_command((f"mkdir -p {DIR_RIGFUZZ_OUT}/"+"{scoverage-results,report,log,reproducers,crashes}").split())


# sbt assembly || exit 1


execute_command(f"""java -cp  target/scala-2.12/ProvFuzz-assembly-1.0.jar
          utils.ScoverageInstrumenter
          {PATH_SCALA_SRC}
          {DIR_RIGFUZZ_OUT}/scoverage-results""".split())

execute_command(f"""pushd target/scala-2.12/classes && 
                jar uvf ../ProvFuzz-assembly-1.0.jar {PATH_INSTRUMENTED_CLASSES}""".split())

START_TIME=get_current_time()

execute_command(f"echo 'Subject:[START-RIG] {gethostname()}\\n\\nprogram: {NAME}\\nstart time: {START_TIME}' | sendmail {TO_EMAIL}".split())

execute_command(f"""java -cp  target/scala-2.12/ProvFuzz-assembly-1.0.jar
          runners.RunRIGFuzzJar
          {NAME}
          {PACKAGE}
          {DURATION}
          {DIR_RIGFUZZ_OUT}""".split())

execute_command(f"echo 'Subject:[END-RIG] {gethostname()}\\n\\nprogram: {NAME}\\nstart time: {START_TIME}\\end time: {get_current_time()}' | sendmail {TO_EMAIL}".split())

