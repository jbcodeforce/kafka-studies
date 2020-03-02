
echo "##########################################"
echo " Build Container Streams microservice war and docker image  "
echo "##########################################"

if [[ $PWD = */scripts ]]; then
 cd ..
fi
if [[ $# -eq 0 ]];then
  kcenv="LOCAL"
else
  kcenv=$1
fi

export msname="containerkstreams"
export chart=$(ls ./chart/| grep $msname | head -1)
export kname="kc-"$chart
export ns="greencompute"
export CLUSTER_NAME=streamer.icp

source ../../refarch-kc/scripts/setenv.sh $kcenv

if [[ $kcenv == "IBMCLOUD" ]]
then
  log=$(ibmcloud target | grep "Not logged in")
  if [[ -n "$log" ]]
  then
    echo "You must login to IBMCLOUD before building"
    exit
  fi
fi

find target -iname "*SNAPSHOT*" -print | xargs rm -rf
# rm -rf target/liberty/wlp/usr/servers/defaultServer/apps/expanded
tools=$(docker images | grep javatools)
if [[ -z "$tools" ]]
then
   mvn clean package -DskipITs
else
   docker run -v $(pwd):/home -ti ibmcase/javatools bash -c "cd /home &&  mvn clean install -DskipITs"
fi

if [[ $kcenv == "ICP" ]]
then
  if [ -f  es-cert.jks ]
  then
  	 mkdir -p target/liberty/wlp/usr/servers/defaultServer/resources/security
     cp es-cert.jks target/liberty/wlp/usr/servers/defaultServer/resources/security
  fi 
fi

docker build --network host \
            --build-arg KAFKA_ENV=$kcenv \
            --build-arg KAFKA_BROKERS=${KAFKA_BROKERS} \
            --build-arg KAFKA_APIKEY=${KAFKA_APIKEY} \
            --build-arg POSTGRESQL_URL=${POSTGRESQL_URL}  \
            --build-arg POSTGRESQL_USER=${POSTGRESQL_USER} \
            --build-arg POSTGRESQL_PWD=${POSTGRESQL_PWD} \
            --build-arg JKS_LOCATION=${JKS_LOCATION} \
            --build-arg TRUSTSTORE_PWD=${TRUSTSTORE_PWD} \
            --build-arg POSTGRESQL_CA_PEM="${POSTGRESQL_CA_PEM}"  -t ibmcase/$kname .

if [[ $kcenv == "IBMCLOUD" ]]
then
   # image for private registry in IBM Cloud
   echo "Tag docker image for $kname to deploy on $kcenv"
   docker tag ibmcase/$kname  us.icr.io/ibmcaseeda/$kname 
fi

if [[ $kcenv == "ICP" ]]
then
   # image for private registry in IBM Cloud Private
   echo "Tag docker image for $kname to deploy on $kcenv"
   docker tag ibmcase/$kname  $CLUSTER_NAME:8500/$ns/$kname 
fi