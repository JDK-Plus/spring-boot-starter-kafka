package plus.jdk.kafka.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import plus.jdk.kafka.annotation.KafkaClient;
import plus.jdk.kafka.common.IConsumeDecider;
import plus.jdk.kafka.common.KafkaClientInitException;
import plus.jdk.kafka.config.KafkaClientProperties;
import plus.jdk.kafka.model.KafkaTopicDefinition;
import plus.jdk.kafka.model.KafkaDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import plus.jdk.kafka.model.NamePair;

@Slf4j
public class KafkaClientInitFactory {

    private final BeanFactory beanFactory;

    private final ApplicationContext applicationContext;

    private static KafkaClientProperties clientProperties;

    private final List<KafkaDefinition> kafkaDefinitions = new ArrayList<>();

    public KafkaClientInitFactory(BeanFactory beanFactory, ApplicationContext applicationContext,
                                  KafkaClientProperties properties, Environment environment) {
        this.beanFactory = beanFactory;
        this.applicationContext = applicationContext;
        clientProperties = properties;
    }

    protected void initializationDefinition() throws KafkaClientInitException {
        String[] beanNames =
                this.applicationContext.getBeanNamesForAnnotation(KafkaClient.class);
        HashMap<String, KafkaTopicDefinition> kafkaClientMap = new HashMap<>();
        for(KafkaTopicDefinition clientInfo: clientProperties.getTopicDefinitions()) {
            clientInfo.verifyConfiguration();
            kafkaClientMap.put(clientInfo.getTopic(), clientInfo);
        }
        for (String beanName : beanNames) {
            IKafkaQueue<?, ?> kafkaQueue = this.applicationContext.getBean(beanName, IKafkaQueue.class);
            KafkaClient kafkaClient = this.applicationContext.findAnnotationOnBean(beanName, KafkaClient.class);
            KafkaTopicDefinition clientInfo = kafkaClientMap.get(kafkaClient.topicName());
            if(clientInfo == null) {
                throw new KafkaClientInitException(String.format("cannot find topic %s config", kafkaClient.topicName()));
            }
            KafkaDefinition kafkaDefinition = new KafkaDefinition(clientInfo, kafkaClient, kafkaQueue);
            kafkaQueue.kafkaDefinition = kafkaDefinition;
            kafkaDefinitions.add(kafkaDefinition);
        }
    }

    protected void startConsumingServices() {
        for (KafkaDefinition kafkaDefinition : kafkaDefinitions) {
            if(kafkaDefinition.getKafkaTopicDefinition() == null) {
                continue;
            }
            KafkaTopicDefinition clientInfo = kafkaDefinition.getKafkaTopicDefinition();
            IConsumeDecider iConsumeDecider = beanFactory.getBean(clientInfo.getDecider());
            if(iConsumeDecider.consume()) {
                continue;
            }
            if(!StringUtils.hasText(clientInfo.getGroupName()) || !StringUtils.hasText(clientInfo.getConsumeBrokers())) {
                log.error("start consumer failed, topic:{} groupName or consumeBrokers is null", clientInfo.getTopic());
                continue;
            }
            createConsumer(kafkaDefinition);
        }
    }

    private void createConsumer(KafkaDefinition kafkaDefinition) {
        KafkaTopicDefinition clientInfo = kafkaDefinition.getKafkaTopicDefinition();
        if(clientInfo == null) {
            return;
        }
        IConsumeDecider consumerDecider = beanFactory.getBean(clientInfo.getDecider());
        if(consumerDecider.consume()) {
            return;
        }
        IKafkaQueue<?, ?> kafkaQueue = kafkaDefinition.getBeanInstance();
        for(int i = 0; i < clientInfo.getConsumerNum(); i ++) {
            Thread consumerThread = new Thread(() -> {
                while (true) {
                    try {
                        kafkaQueue.run();
                    }catch (Exception e) {
                        log.error("start consumer failed, message:{}", e.getMessage());
                    }
                }
            });
            consumerThread.start();
        }
    }

    protected static <K, V> KafkaConsumer<K, V> getConsumer(KafkaDefinition kafkaDefinition) {
        KafkaTopicDefinition clientInfo = kafkaDefinition.getKafkaTopicDefinition();
        Properties properties = new Properties();
        String brokers = clientInfo.getConsumeBrokers();
        String groupName = clientInfo.getGroupName();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, clientInfo.getConsumerMaxPollRecord());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, clientInfo.getAutoCommit());
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.mechanism", "PLAIN");
        String username = StringUtils.hasText(clientInfo.getUserName()) ? clientInfo.getUserName() : clientProperties.getUserName();
        String password = StringUtils.hasText(clientInfo.getPassword()) ? clientInfo.getPassword() : clientProperties.getPassword();
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + username + "\"  password=\"" + password + "\";");
        for (NamePair namePair : clientProperties.getConsumerGlobalConfig()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        for (NamePair namePair : clientInfo.getConsumerConfigs()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        return new KafkaConsumer<>(properties);
    }

    protected static <K, V> KafkaProducer<K, V> getProducer(KafkaDefinition kafkaDefinition) {
        KafkaClient kafkaClient = kafkaDefinition.getKafkaClient();
        KafkaTopicDefinition clientInfo = kafkaDefinition.getKafkaTopicDefinition();
        Properties properties = new Properties();
        String brokers = clientInfo.getProducerBrokers();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.mechanism", "PLAIN");
        String username = StringUtils.hasText(clientInfo.getUserName()) ? clientInfo.getUserName() : clientProperties.getUserName();
        String password = StringUtils.hasText(clientInfo.getPassword()) ? clientInfo.getPassword() : clientProperties.getPassword();
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + username + "\"  password=\"" + password + "\";");
        for (NamePair namePair : clientProperties.getConsumerGlobalConfig()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        for (NamePair namePair : clientInfo.getConsumerConfigs()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        return new KafkaProducer<>(properties);
    }
}
