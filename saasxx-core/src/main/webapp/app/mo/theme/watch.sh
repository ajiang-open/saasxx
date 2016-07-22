path=$(dirname $0)
nohup sass --watch $path/scss:$path/css &
tail -f ~/nohup.out