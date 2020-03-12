#!/bin/bash
echo "##########################################################"
echo " A docker image for python 3.7 development: "
echo
name="jbpython"
port=5000
if [[ $# != 0 ]]
then
    name=$1
    port=$2
fi


docker run --network="kafkanet" --name $name -v $(pwd):/home -it --rm -p $port:$port jbcodeforce/python37 bash 
