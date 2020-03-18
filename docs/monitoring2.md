# Monitoring Mirror Maker and kafka connect cluster

The goal of this note is to go over some of the details on how to monitor Mirror maker metrics to Prometheus and use Grafana dashboard.

[Prometheus](https://prometheus.io/docs/introduction/overview/) is an open source systems monitoring and alerting toolkit that, with Kubernetes, is part of the Cloud Native Computing Foundation. It can monitor multiple workloads but is normally used with container workloads. 

Here is simple diagram to explain prometheus generic architecture from their main website:

![](https://prometheus.io/assets/architecture.png)

In the context of data replication between kafka cluster, what we want to monitor the mirror maker 2.0 metrics:

![](mm2-monitoring.png)

For the interest of monitoring we can add Kafka Exporter to assess the consumer lag.

## Installation and configuration

Prometheus deployment inside Kubernetes uses operator as defined in [the coreos github](https://github.com/coreos/prometheus-operator). The CRDs define a set of resources. The ServiceMonitor, PodMonitor, PrometheusRule are used.

Inside the [Strimzi github repository](https://github.com/strimzi/strimzi-kafka-operator), we can get a [prometheus.yml](https://github.com/strimzi/strimzi-kafka-operator/blob/master/examples/metrics/prometheus-install/prometheus.yaml) file to deploy prometheus server. This configuration defines, ClusterRole, ServiceAccount, ClusterRoleBinding, and the Prometheus resource instance. 
*For your own deployment you have to change the target namespace, and the rules*

You need to deploy Prometheus and all the other elements inside the same namespace or OpenShift project as the Kafka Cluster or the KafkaConnect Cluster.

* The first time you need to deploy the Prometheus operator

```shell
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-deployment.yaml | sed -e "s/namespace: default/namespace: jb-kafka-strimzi/" > prometheus-operator-deployment.yaml
```

```shell
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role.yaml > prometheus-operator-cluster-role.yaml
```

```shell
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-cluster-role-binding.yaml | sed -e "s/namespace: default/namespace: jb-kafka-strimzi/" > prometheus-operator-cluster-role-binding.yaml
```

```shell
curl -s https://raw.githubusercontent.com/coreos/prometheus-operator/master/example/rbac/prometheus-operator/prometheus-operator-service-account.yaml | sed -e "s/namespace: default/namespace: jb-kafka-strimzi/" > prometheus-operator-service-account.yaml
```

Deploy the operator, cluster role, binding and service account:

```
kubectl apply -f prometheus-operator-deployment.yaml
kubectl apply -f prometheus-operator-cluster-role.yaml
kubectl apply -f prometheus-operator-cluster-role-binding.yaml
kubectl apply -f prometheus-operator-service-account.yaml
```

* Deploy the prometheus server



The Prometheus server configuration uses service discovery to discover the pods in the cluster from which it gets metrics. 

## Mirror maker monitoring
