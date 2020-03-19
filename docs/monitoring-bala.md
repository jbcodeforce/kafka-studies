



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



            Edit the ServiceMonitor resource in strimzi-service-monitor.yaml to define Prometheus jobs that will scrape the metrics data.

            To use another role:

            Create a Secret resource:

            oc create secret generic additional-scrape-configs --from-file=prometheus-additional.yaml
            Edit the additionalScrapeConfigs property in the prometheus.yaml file to include the name of the Secret and the YAML file (prometheus-additional.yaml) that contains the additional configuration.

           
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
