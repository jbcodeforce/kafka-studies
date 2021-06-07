package ibm.labs.kc.utils;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class KafkaStreamConfig {

	private static final Logger logger = LoggerFactory.getLogger(KafkaStreamConfig.class.getName());

	private static Config config = ConfigProvider.getConfig();

	private static String ORDERS_TOPIC;
	private static String REJECTED_ORDERS_TOPIC;
	private static String ALLOCATED_ORDERS_TOPIC;
	private static String CONTAINERS_TOPIC;

	private static final String ORDERS_TOPIC_DEFAULT = "orders";
	private static final String REJECTED_ORDERS_TOPIC_DEFAULT = "rejected-orders";
	private static final String ALLOCATED_ORDERS_TOPIC_DEFAULT = "allocated-orders";
	private static final String CONTAINERS_TOPIC_DEFAULT = "containers";

	public static final String CONTAINERS_STORE_NAME = "queryable-container-store";

	public static final long PRODUCER_TIMEOUT_SECS = 10;
	public static final long PRODUCER_CLOSE_TIMEOUT_SEC = 10;
	public static final String CONSUMER_GROUP_ID = "order-command-grp";
	public static final Duration CONSUMER_POLL_TIMEOUT = Duration.ofSeconds(10);
	public static final Duration CONSUMER_CLOSE_TIMEOUT = Duration.ofSeconds(10);
	public static final long TERMINATION_TIMEOUT_SEC = 10;

	public static String getOrderTopic() {
		ORDERS_TOPIC = config.getValue("order.topic", String.class);
		if(ORDERS_TOPIC.isEmpty()) {
			ORDERS_TOPIC = ORDERS_TOPIC_DEFAULT;
		}
		logger.info("Get Order Topic: {}", ORDERS_TOPIC);
		return ORDERS_TOPIC;
	}

	public static String getRejectedOrdersTopic() {
		REJECTED_ORDERS_TOPIC = config.getValue("rejected.order.topic", String.class);
		if(REJECTED_ORDERS_TOPIC.isEmpty()) {
			REJECTED_ORDERS_TOPIC = REJECTED_ORDERS_TOPIC_DEFAULT;
		}
		logger.info("Get Rejected Orders Topic: {}", REJECTED_ORDERS_TOPIC);
		return REJECTED_ORDERS_TOPIC;
	}

	public static String getAllocatedOrdersTopic() {
		ALLOCATED_ORDERS_TOPIC = config.getValue("allocated.order.topic", String.class);
		if(ALLOCATED_ORDERS_TOPIC.isEmpty()) {
			ALLOCATED_ORDERS_TOPIC = ALLOCATED_ORDERS_TOPIC_DEFAULT;
		}
		logger.info("Get Allocated Orders Topic: {}", ALLOCATED_ORDERS_TOPIC);
		return ALLOCATED_ORDERS_TOPIC;
	}

	public static String getContainerTopic() {
		CONTAINERS_TOPIC = config.getValue("container.topic", String.class);
		if(CONTAINERS_TOPIC.isEmpty()) {
			CONTAINERS_TOPIC = CONTAINERS_TOPIC_DEFAULT;
		}
		logger.info("Get Container Topic: {}", CONTAINERS_TOPIC);
		return CONTAINERS_TOPIC;
	}

	/**
	 * Take into account the environment variables if set
	 *
	 * @return common kafka properties
	 */
	private static Properties buildCommonProperties() {
		Properties properties = new Properties();
		Map<String, String> env = System.getenv();

		if (env.get("KAFKA_BROKERS") == null) {
			throw new IllegalStateException("Missing environment variable KAFKA_BROKERS");
		}
		properties.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, env.get("KAFKA_BROKERS"));

		if (env.get("KAFKA_APIKEY") != null && !env.get("KAFKA_APIKEY").isEmpty()) {
			properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
			properties.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
			properties.put(SaslConfigs.SASL_JAAS_CONFIG,
					"org.apache.kafka.common.security.plain.PlainLoginModule required username=\"token\" password=\""
							+ env.get("KAFKA_APIKEY") + "\";");
			properties.put(SslConfigs.SSL_PROTOCOL_CONFIG, "TLSv1.2");
			properties.put(SslConfigs.SSL_ENABLED_PROTOCOLS_CONFIG, "TLSv1.2");
			properties.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "HTTPS");

			if ("true".equals(env.get("TRUSTSTORE_ENABLED"))) {
				properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, env.get("TRUSTSTORE_PATH"));
				properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, env.get("TRUSTSTORE_PWD"));
			}
		}

		return properties;
	}

	// Kafka Streams requires at least the APPLICATION_ID_CONFIG and
	// BOOTSTRAP_SERVERS_CONFIG
	public static Properties getStreamsProperties(String appID) {
		Properties properties = buildCommonProperties();
		properties.put(StreamsConfig.APPLICATION_ID_CONFIG, appID);
		properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
		properties.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);
		return properties;
	}

	public static Properties getProducerProperties(String clientId) {
		Properties properties = buildCommonProperties();
		properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
		properties.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
		properties.put(ProducerConfig.ACKS_CONFIG, "1");
		return properties;
	}

}
