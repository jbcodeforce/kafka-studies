



  3. Grafana dashboard configuration specific to Kafka Exporter

  4. Metrics configuration that defines Prometheus JMX Exporter relabeling rules for Kafka Connect

  5. Metrics configuration that defines Prometheus JMX Exporter relabeling rules for Kafka and ZooKeeper

  6. Configuration to add roles for service monitoring

  7. Hook definitions for sending notifications through Alertmanager

  8. Resources for deploying and configuring Alertmanager

  9. Alerting rules examples for use with Prometheus Alertmanager (deployed with Prometheus)

  10. Installation file for the Prometheus image

  11. Prometheus job definitions to scrape metrics data

  Prometheus metrics
         Strimzi uses the Prometheus JMX Exporter to expose JMX metrics from Kafka and ZooKeeper using an HTTP endpoint,        which isthen scraped by the Prometheus server.

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

  
          Note
          If it is not required, you can manually remove the spec.template.spec.securityContext property from the prometheus-operator-deployment.yaml file.
          

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
