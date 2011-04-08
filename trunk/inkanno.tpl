#! /bin/bash
export COLUMNS=$(tput cols)

tmp=${INKANNO_HOME:=$basedir$}


if [ "$1" = "--get-cp" ] 
then
    plugins=$(ls $INKANNO_HOME/plugins/*/*.jar | paste -s -d ':')
    echo "$INKANNO_HOME/inkanno.jar:$INKANNO_HOME/lib/libInkML/libinkml.jar:$plugins"
    exit 0
fi

if [ "$1" = "--get-inkannohome" ] 
then
    echo $INKANNO_HOME
    exit 0
fi



java -jar $INKANNO_HOME/inkanno.jar $*