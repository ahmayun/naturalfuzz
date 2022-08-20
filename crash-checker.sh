
DIR_WATCH=$1
FILE_ITER=$2
FILE_WRITE=$3
inotifywait -m $DIR_WATCH -e create |
    while read dir action file; do
        ITER=$(cat $FILE_ITER)
        echo "$ITER,$file" >> $FILE_WRITE
        # do something with the file
    done
