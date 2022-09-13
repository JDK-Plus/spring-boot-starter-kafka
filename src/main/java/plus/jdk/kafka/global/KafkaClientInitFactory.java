package plus.jdk.kafka.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import plus.jdk.kafka.annotation.KafkaConsumerDesc;
import plus.jdk.kafka.annotation.KafkaClient;
import plus.jdk.kafka.annotation.KafkaProducerDesc;
import plus.jdk.kafka.annotation.KafkaProperty;
import plus.jdk.kafka.common.IConsumeDecider;
import plus.jdk.kafka.config.KafkaClientProperties;
import plus.jdk.kafka.model.KafkaDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import plus.jdk.kafka.selector.KafkaClientSelector;

@Slf4j
public class KafkaClientInitFactory {

    private final BeanFactory beanFactory;

    private final ApplicationContext applicationContext;

    private static KafkaClientProperties clientProperties;

    private final List<KafkaDefinition> kafkaDefinitions = new ArrayList<>();

    private final Environment environment;

    public KafkaClientInitFactory(BeanFactory beanFactory, ApplicationContext applicationContext,
                                  KafkaClientProperties properties, Environment environment) {
        this.beanFactory = beanFactory;
        this.applicationContext = applicationContext;
        clientProperties = properties;
        this.environment = environment;
    }

    protected void initializationDefinition() {
        String[] beanNames =
                this.applicationContext.getBeanNamesForAnnotation(KafkaClient.class);
        for (String beanName : beanNames) {
            IKafkaQueue<?, ?> kafkaQueue = this.applicationContext.getBean(beanName, IKafkaQueue.class);
            KafkaConsumerDesc kafkaConsumerDesc = this.applicationContext.findAnnotationOnBean(beanName, KafkaConsumerDesc.class);
            KafkaProducerDesc kafkaProducerDesc = this.applicationContext.findAnnotationOnBean(beanName, KafkaProducerDesc.class);
            KafkaClient kafkaClient = this.applicationContext.findAnnotationOnBean(beanName, KafkaClient.class);
            kafkaDefinitions.add(new KafkaDefinition(kafkaConsumerDesc, kafkaProducerDesc, kafkaClient, kafkaQueue));
        }
    }

    protected void startConsumingServices() {
        for (KafkaDefinition kafkaDefinition : kafkaDefinitions) {
            if(kafkaDefinition.getKafkaConsumerDesc() == null) {
                continue;
            }
            createConsumer(kafkaDefinition);
        }
    }

    private void createConsumer(KafkaDefinition kafkaDefinition) {
        KafkaConsumerDesc consumerDesc = kafkaDefinition.getKafkaConsumerDesc();
        if(consumerDesc == null) {
            return;
        }
        IConsumeDecider consumerDecider = beanFactory.getBean(consumerDesc.decider());
        if(!consumerDecider.consume()) {
            return;
        }
        IKafkaQueue<?, ?> kafkaQueue = kafkaDefinition.getBeanInstance();
        for(int i = 0; i < consumerDesc.consumerNum(); i ++) {
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
        KafkaClient kafkaClient = kafkaDefinition.getKafkaClient();
        KafkaConsumerDesc kafkaConsumerDesc = kafkaDefinition.getKafkaConsumerDesc();
        Properties properties = new Properties();
        String brokers = String.join(",", kafkaConsumerDesc.brokers());
        Environment environment = KafkaClientSelector.beanFactory.getBean(Environment.class);
        if(environment.containsProperty(brokers)) {
            brokers = environment.getProperty(brokers);
        }
        String groupName = kafkaConsumerDesc.groupName();
        if(environment.containsProperty(groupName)) {
            groupName = environment.getProperty(groupName);
        }
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupName);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConsumerDesc.maxPollRecord());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, kafkaConsumerDesc.autoCommit());
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.mechanism", "PLAIN");
        String username = !kafkaClient.userName().equals("") ? kafkaClient.userName() : clientProperties.getUserName();
        username = !kafkaConsumerDesc.userName().equals("") ? kafkaConsumerDesc.userName() : username;
        String password = !kafkaClient.password().equals("") ? kafkaClient.password() : clientProperties.getPassword();
        password = !kafkaConsumerDesc.password().equals("") ? kafkaConsumerDesc.password() : password;
        if(environment.containsProperty(username)) {
            username = environment.getProperty(username);
        }
        if(environment.containsProperty(password)) {
            password = environment.getProperty(password);
        }
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + username + "\"  password=\"" + password + "\";");
        for (KafkaProperty kafkaProperty : kafkaConsumerDesc.properties()) {
            String propertyValue = kafkaProperty.value();
            if(environment.containsProperty(propertyValue)) {
                propertyValue = environment.getProperty(propertyValue);
            }
            properties.put(kafkaProperty.key(), propertyValue);
        }
        return new KafkaConsumer<>(properties);
    }

    protected static <K, V> KafkaProducer<K, V> getProducer(KafkaDefinition kafkaDefinition) {
        KafkaClient kafkaClient = kafkaDefinition.getKafkaClient();
        KafkaProducerDesc kafkaProducerDesc = kafkaDefinition.getKafkaProducerDesc();
        Properties properties = new Properties();
        String brokers = String.join(",", kafkaProducerDesc.brokers());
        Environment environment = KafkaClientSelector.beanFactory.getBean(Environment.class);
        if(environment.containsProperty(brokers)) {
            brokers = environment.getProperty(brokers);
        }
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        properties.put(ProducerConfig.LINGER_MS_CONFIG, 50);
        properties.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1);
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.mechanism", "PLAIN");
        String username = !kafkaClient.userName().equals("") ? kafkaClient.userName() : clientProperties.getUserName();
        username = !kafkaProducerDesc.userName().equals("") ? kafkaProducerDesc.userName() : username;
        String password = !kafkaClient.password().equals("") ? kafkaClient.password() : clientProperties.getPassword();
        password = !kafkaProducerDesc.password().equals("") ? kafkaProducerDesc.password() : password;
        if(environment.containsProperty(username)) {
            username = environment.getProperty(username);
        }
        if(environment.containsProperty(password)) {
            password = environment.getProperty(password);
        }
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                + username + "\"  password=\"" + password + "\";");
        for (KafkaProperty kafkaProperty : kafkaProducerDesc.properties()) {
            String propertyValue = kafkaProperty.value();
            if(environment.containsProperty(propertyValue)) {
                propertyValue = environment.getProperty(propertyValue);
            }
            properties.put(kafkaProperty.key(), propertyValue);
        }
        return new KafkaProducer<>(properties);
    }
}
