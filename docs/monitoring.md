# Monitoring cluster and kafka connect

## Installation and configuration

## Mirror maker monitoring

This section describes how to monitor Strimzi Kafka, ZooKeeper and Kafka Connect clusters using Prometheus to provide monitoring data for example Grafana dashboards.

In order to run the example Grafana dashboards to monitor MM2, you must:

  Add metrics configuration to your Kafka cluster resource

  Deploy Prometheus and Prometheus Alertmanager

  Deploy Grafana

 Example Metrics files
  You can find the example metrics configuration files in the examples/metrics directory.

  metrics
  ├── grafana-install
  │   ├── grafana.yaml (1)
  ├── grafana-dashboards (2)
  │   ├── strimzi-kafka-connect.json
  │   ├── strimzi-kafka.json
  │   └── strimzi-zookeeper.json
  │   └── strimzi-kafka-exporter.json (3)
  ├── kafka-connect-metrics.yaml (4)
  ├── kafka-metrics.yaml (5)
  ├── prometheus-additional-properties
  │   └── prometheus-additional.yaml (6)
  ├── prometheus-alertmanager-config
  │   └── alert-manager-config.yaml (7)
  └── prometheus-install
      ├── alert-manager.yaml (8)
      ├── prometheus-rules.yaml (9)
      ├── prometheus.yaml (10)
      └── strimzi-service-monitor.yaml (11)
  Installation file for the Grafana image

  Grafana dashboard configuration

  Grafana dashboard configuration specific to Kafka Exporter

  Metrics configuration that defines Prometheus JMX Exporter relabeling rules for Kafka Connect

  Metrics configuration that defines Prometheus JMX Exporter relabeling rules for Kafka and ZooKeeper

  Configuration to add roles for service monitoring

  Hook definitions for sending notifications through Alertmanager

  Resources for deploying and configuring Alertmanager

  Alerting rules examples for use with Prometheus Alertmanager (deployed with Prometheus)

  Installation file for the Prometheus image

  Prometheus job definitions to scrape metrics data

  Prometheus metrics
  Strimzi uses the Prometheus JMX Exporter to expose JMX metrics from Kafka and ZooKeeper using an HTTP endpoint, which is then scraped by the Prometheus server.

 Prometheus metrics configuration
  Strimzi provides example configuration files for Grafana.

  Grafana dashboards are dependent on Prometheus JMX Exporter relabeling rules, which are defined for:

  Kafka and ZooKeeper as a Kafka resource configuration in the example kafka-metrics.yaml file

  Kafka Connect as KafkaConnect and KafkaConnectS2I resources in the example kafka-connect-metrics.yaml file

Prometheus metrics deployment options

 Deploying a Kafka cluster with Prometheus metrics configuration
To use Grafana dashboards for monitoring, you can deploy an example Kafka cluster with metrics configuration.

Procedure
Deploy the Kafka cluster with the metrics configuration:

kubectl apply -f kafka-metrics.yaml

Prometheus
Prometheus provides an open source set of components for systems monitoring and alert notification.

We describe here how you can use the CoreOS Prometheus Operator to run and manage a Prometheus server that is suitable for use in production environments, but with the correct configuration you can run any Prometheus server.

Prometheus configuration
Strimzi provides example configuration files for the Prometheus server.

A Prometheus image is provided for deployment:

prometheus.yaml

Additional Prometheus-related configuration is also provided in the following files:

prometheus-additional.yaml

prometheus-rules.yaml

strimzi-service-monitor.yaml

Alerting rules
The prometheus-rules.yaml file provides example alerting rule examples for use with Alertmanager.

Prometheus resources
When you apply the Prometheus configuration, the following resources are created in your Kubernetes cluster and managed by the Prometheus Operator:

A ClusterRole that grants permissions to Prometheus to read the health endpoints exposed by the Kafka and ZooKeeper pods, cAdvisor and the kubelet for container metrics.

A ServiceAccount for the Prometheus pods to run under.

A ClusterRoleBinding which binds the ClusterRole to the ServiceAccount.

A Deployment to manage the Prometheus Operator pod.

A ServiceMonitor to manage the configuration of the Prometheus pod.

A Prometheus to manage the configuration of the Prometheus pod.

A PrometheusRule to manage alerting rules for the Prometheus pod.

A Secret to manage additional Prometheus settings.

A Service to allow applications running in the cluster to connect to Prometheus (for example, Grafana using Prometheus as datasource).

8.3.3. Deploying the Prometheus Operator
To deploy the Prometheus Operator to your Kafka cluster, apply the YAML resource files from the Prometheus CoreOS repository.

Procedure
Download the resource files from the repository and replace the example namespace with your own:

On Linux, use:

curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-deployment.yaml | sed -e 's/namespace: .\*/namespace: my-namespace/' > prometheus-operator-deployment.yaml
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role.yaml > prometheus-operator-cluster-role.yaml
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role-binding.yaml | sed -e 's/namespace: .*/namespace: my-namespace/' > prometheus-operator-cluster-role-binding.yaml
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-service-account.yaml | sed -e 's/namespace: .*/namespace: my-namespace/' > prometheus-operator-service-account.yaml
On MacOS, use:

curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-deployment.yaml | sed -e '' 's/namespace: .\*/namespace: my-namespace/' > prometheus-operator-deployment.yaml
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role.yaml > prometheus-operator-cluster-role.yaml
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role-binding.yaml | sed -e '' 's/namespace: .*/namespace: my-namespace/' > prometheus-operator-cluster-role-binding.yaml
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-service-account.yaml | sed -e '' 's/namespace: .*/namespace: my-namespace/' > prometheus-operator-service-account.yaml
Note
If it is not required, you can manually remove the spec.template.spec.securityContext property from the prometheus-operator-deployment.yaml file.
Deploy the Prometheus Operator:

kubectl apply -f prometheus-operator-deployment.yaml
kubectl apply -f prometheus-operator-cluster-role.yaml
kubectl apply -f prometheus-operator-cluster-role-binding.yaml
kubectl apply -f prometheus-operator-service-account.yaml
8.3.4. Deploying Prometheus
To deploy Prometheus to your Kafka cluster to obtain monitoring data, apply the example resource file for the Prometheus docker image and the YAML files for Prometheus-related resources.

The deployment process creates a ClusterRoleBinding and discovers an Alertmanager instance in the namespace specified for the deployment.

Note
By default, the Prometheus Operator only supports jobs that include an endpoints role for service discovery. Targets are discovered and scraped for each endpoint port address. For endpoint discovery, the port address may be derived from service (role: service) or pod (role: pod) discovery.
Prerequisites
Check the example alerting rules provided

Procedure
Modify the Prometheus installation file (prometheus.yaml) according to the namespace Prometheus is going to be installed in:

On Linux, use:

sed -i 's/namespace: .*/namespace: my-namespace/' prometheus.yaml
On MacOS, use:

sed -i '' 's/namespace: .*/namespace: my-namespace/' prometheus.yaml
Edit the ServiceMonitor resource in strimzi-service-monitor.yaml to define Prometheus jobs that will scrape the metrics data.

To use another role:

Create a Secret resource:

oc create secret generic additional-scrape-configs --from-file=prometheus-additional.yaml
Edit the additionalScrapeConfigs property in the prometheus.yaml file to include the name of the Secret and the YAML file (prometheus-additional.yaml) that contains the additional configuration.

Edit the prometheus-rules.yaml file that creates sample alert notification rules:

On Linux, use:

sed -i 's/namespace: .*/namespace: my-namespace/' prometheus-rules.yaml
On MacOS, use:

sed -i '' 's/namespace: .*/namespace: my-namespace/' prometheus-rules.yaml
Deploy the Prometheus resources:

kubectl apply -f strimzi-service-monitor.yaml
kubectl apply -f prometheus-rules.yaml
kubectl apply -f prometheus.yaml

Prometheus Alertmanager
Prometheus Alertmanager is a plugin for handling alerts and routing them to a notification service. Alertmanager supports an essential aspect of monitoring, which is to be notified of conditions that indicate potential issues based on alerting rules.

 Grafana
Grafana provides visualizations of Prometheus metrics.

You can deploy and enable the example Grafana dashboards provided with Strimzi.

 Grafana configuration
Strimzi provides example dashboard configuration files for Grafana.

A Grafana docker image is provided for deployment:

grafana.yaml

Example dashboards are also provided as JSON files:

strimzi-kafka.json

strimzi-kafka-connect.json

strimzi-zookeeper.json

The example dashboards are a good starting point for monitoring key metrics, but they do not represent all available metrics. You may need to modify the example dashboards or add other metrics, depending on your infrastructure.

For Grafana to present the dashboards, use the configuration files to:

Deploy Grafana

Deploying Grafana
To deploy Grafana to provide visualizations of Prometheus metrics, apply the example configuration file.

Prerequisites
Metrics are configured for the Kafka cluster resource

Prometheus and Prometheus Alertmanager are deployed

Procedure
Deploy Grafana:

kubectl apply -f grafana.yaml
Enable the Grafana dashboards.

Enabling the example Grafana dashboards
Set up a Prometheus data source and example dashboards to enable Grafana for monitoring.

Note
No alert notification rules are defined.
When accessing a dashboard, you can use the port-forward command to forward traffic from the Grafana pod to the host.

For example, you can access the Grafana user interface by:

Running kubectl port-forward grafana-1-fbl7s 3000:3000

Pointing a browser to http://localhost:3000

Note
The name of the Grafana pod is different for each user.
Procedure
Access the Grafana user interface using admin/admin credentials.

On the initial view choose to reset the password.

Grafana login
Click the Add data source button.

Grafana home
Add Prometheus as a data source.

Specify a name

Add Prometheus as the type

Specify the connection string to the Prometheus server (http://prometheus-operated:9090) in the URL field

Click Add to test the connection to the data source.

Add Prometheus data source
Click Dashboards, then Import to open the Import Dashboard window and import the example dashboards (or paste the JSON).

Add Grafana dashboard
After importing the dashboards, the Grafana dashboard homepage presents Kafka and ZooKeeper dashboards.

When the Prometheus server has been collecting metrics for a Strimzi cluster for some time, the dashboards are populated.

Kafka dashboard
Kafka dashboardKafka dashboard
ZooKeeper dashboard
ZooKeeper dashboardZooKeeper dashboard
