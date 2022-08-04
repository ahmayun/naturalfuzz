
DIR_WATCH=$1
FILE_ITER=target/scala-2.11/target/jazzer-output/WebpageSegmentation/measurements/iter
FILE_WRITE=target/scala-2.11/target/jazzer-output/WebpageSegmentation/measurements/errors.csv
inotifywait -m $DIR_WATCH -e create |
    while read dir action file; do
        ITER=$(cat $FILE_ITER)
        echo "$ITER,$file" >> $FILE_WRITE
        # do something with the file
    done
