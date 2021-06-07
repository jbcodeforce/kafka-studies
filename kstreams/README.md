# Reefer container management microservice done with kafka streams and microprofile

We recommend to read how we build this service explanations in [this book view](https://ibm-cloud-architecture.github.io/refarch-kc-container-ms/kstreams/)

### Build

Build with maven
```
mvn package
```

Compile, tests and build container
```
./scripts/buildDocker.sh LOCAL | IBMCLOUD | ICP
```

### Run

Use our script  `./script/startLocalLiberty` or use maven `mvn liberty:run-server`

## Deployments

### Deployment prerequisites

Regardless of specific deployment targets (OCP, IKS, k8s), the following prerequisite Kubernetes artifacts need to be created to support the deployments of application components.  These artifacts need to be created once per unique deployment of the entire application and can be shared between application components in the same overall application deployment.

1. Create `kafka-brokers` ConfigMap
  - Command: `kubectl create configmap kafka-brokers --from-literal=brokers='<replace with comma-separated list of brokers>' -n <namespace>`
  - Example: `kubectl create configmap kafka-brokers --from-literal=brokers='broker-3-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-2-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-1-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-5-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-0-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093,broker-4-j7fxtxtp5fs84205.kafka.svc01.us-south.eventstreams.cloud.ibm.com:9093' -n eda-refarch`
2. Create optional `eventstreams-apikey` Secret, if you are using Event Streams as your Kafka broker provider
  - Command: `kubectl create secret generic eventstreams-apikey --from-literal=binding='<replace with api key>' -n <namespace>`
  - Example: `kubectl create secret generic eventstreams-apikey --from-literal=binding='z...12345...notanactualkey...67890...a' -n eda-refarch`
3. If you are using Event Streams as your Kafka broker provider and it is deployed via the IBM Cloud Pak for Integration (ICP4I), you will need to create an additional Secret to store the generated Certificates & Truststores.
  - From the "Connect to this cluster" tab on the landing page of your Event Streams installation, download both the **Java truststore** and the **PEM certificate**.
  - Create the Java truststore Secret:
    - Command: `kubectl create secret generic <secret-name> --from-file=/path/to/downloaded/file.jks`
    - Example: `kubectl create secret generic es-truststore-jks --from-file=/Users/osowski/Downloads/es-cert.jks`
  - Create the PEM certificate Secret:
    - Command: `kubectl create secret generic <secret-name> --from-file=/path/to/downloaded/file.pem`
    - Example: `kubectl create secret generic es-ca-pemfile --from-file=/Users/osowski/Downloads/es-cert.pem`

### Deploy to OpenShift Container Platform (OCP) 3.11

**Cross-component deployment prerequisites:** _(needs to be done once per unique deployment of the entire application)_
1. If desired, create a non-default Service Account for usage of deploying and running the K Container reference implementation.  This will become more important in future iterations, so it's best to start small:
  - Command: `oc create serviceaccount -n <target-namespace> kcontainer-runtime`
  - Example: `oc create serviceaccount -n eda-refarch kcontainer-runtime`
2. The target Service Account needs to be allowed to run containers as `anyuid` for the time being:
  - Command: `oc adm policy add-scc-to-user anyuid -z <service-account-name> -n <target-namespace>`
  - Example: `oc adm policy add-scc-to-user anyuid -z kcontainer-runtime -n eda-refarch`
  - NOTE: This requires `cluster-admin` level privileges.

**Perform the following for the `kstreams` microservice:**
1. Build and push the Docker image by one of the two options below:
  - Create a Jenkins project, pointing to the remote GitHub repository for the `kstreams` microservice, and manually creating the necessary parameters.  Refer to the `kstreams` [`Jenkinsfile.NoKubernetesPlugin`](./Jenkinsfile.NoKubernetesPlugin) for appropriate parameter values.
  - Manually build the Docker image and push it to a registry that is accessible from your cluster (Docker Hub, IBM Cloud Container Registry, manually deployed Quay instance):
    - `docker build -t <private-registry>/<image-namespace>/kc-container-kstreams:latest kstreams/`
    - `docker login <private-registry>`
    - `docker push <private-registry>/<image-namespace>/kc-container-kstreams:latest`
2. Generate application YAMLs via `helm template` for `kstreams`:
  - Parameters:
    - `--set image.repository=<private-registry>/<image-namespace>/<image-repository>`
    - `--set image.pullSecret=<private-registry-pullsecret>` (optional or set to blank)
    - `--set kafka.brokersConfigMap=<kafka brokers ConfigMap name>`
    - `--set eventstreams.enabled=(true/false)` (`true` when connecting to Event Streams of any kind, `false` when connecting to Kafka directly)
    - `--set eventstreams.apikeyConfigMap=<kafka api key Secret name>`
    - `--set eventstreams.truststoreRequired=(true/false)` (`true` when connecting to Event Streams via ICP4I)
    - `--set eventstreams.truststoreSecret=<eventstreams jks file secret name>` (only used when connecting to Event Streams via ICP4I)
    - `--set eventstreams.truststorePassword=<eventstreams jks password>` (only used when connecting to Event Streams via ICP4I)
    - `--set serviceAccountName=<service-account-name>`
    - `--namespace <target-namespace>`
    - `--output-dir <local-template-directory>`
  - Example using Event Streams via ICP4I:
   ```shell
   helm template --set image.repository=rhos-quay.internal-network.local/browncompute/kc-container-kstreams --set image.pullSecret= --set kafka.brokersConfigMap=es-kafka-brokers --set eventstreams.enabled=true --set eventstreams.apikeyConfigMap=es-eventstreams-apikey --set serviceAccountName=kcontainer-runtime --set eventstreams.truststoreRequired=true --set eventstreams.truststoreSecret=es-truststore-jks --set eventstreams.truststorePassword=password --output-dir templates --namespace eda-refarch chart/kstreams
   ```
  - Example using Event Streams hosted on IBM Cloud:
   ```shell
   helm template --set image.repository=rhos-quay.internal-network.local/browncompute/kc-container-kstreams --set image.pullSecret= --set kafka.brokersConfigMap=kafka-brokers --set eventstreams.enabled=true --set eventstreams.apikeyConfigMap=eventstreams-apikey --set serviceAccountName=kcontainer-runtime --output-dir templates --namespace eda-refarch chart/kstreams
   ```
4. Deploy application using `oc apply`:
  - `oc apply -f templates/kstreams/templates`
