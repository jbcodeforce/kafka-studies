
docker build -t ibmcase/mm2ocp:v0.0.2 . 
docker push ibmcase/mm2ocp:v0.0.2

oc new-app --docker-image ibmcase/mm2ocp:v0.0.2

oc create secret generic mm2-std-properties --from-file=kafka-to-es.properties 


 oc delete all -l app=mm2ocp
