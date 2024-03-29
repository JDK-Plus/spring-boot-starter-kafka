package plus.jdk.kafka.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.util.StringUtils;
import plus.jdk.kafka.annotation.KafkaClient;
import plus.jdk.kafka.common.IMessageCallback;
import plus.jdk.kafka.config.KafkaClientProperties;
import plus.jdk.kafka.model.KafkaTopicDefinition;
import plus.jdk.kafka.model.KafkaDefinition;
import plus.jdk.kafka.model.NamePair;

import javax.annotation.Resource;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class IKafkaQueue<K, V> implements Runnable {

    /**
     * 生产实例
     */
    private KafkaProducer<K, V> producer;

    /**
     * 配置内容
     */
    protected KafkaClientProperties clientProperties;

    /**
     * 关于topic的定义
     */
    protected KafkaDefinition kafkaDefinition;

    protected boolean processMessage(V data) throws Exception {
        return true;
    }

    public List<Future<RecordMetadata>> pushMessage(List<V> messageList, IMessageCallback<V> iMessageCallback, boolean flush) {
        if (producer == null) {
            producer = getProducer(kafkaDefinition);
        }
        String topicName = kafkaDefinition.getKafkaTopicDefinition().getTopic();
        List<Future<RecordMetadata>> futureList = Collections.synchronizedList(new ArrayList<>());
        for (V message : messageList) {
            Future<RecordMetadata> result = producer.send(new ProducerRecord<>(topicName, message), (recordMetadata, exception) -> iMessageCallback.onCompletion(message, recordMetadata, exception));
            futureList.add(result);
        }
        if (flush) {
            producer.flush();
        }
        return futureList;
    }

    public List<Future<RecordMetadata>> pushMessage(List<V> messageList, IMessageCallback<V> iMessageCallback) {
        return pushMessage(messageList, iMessageCallback, false);
    }

    public List<Future<RecordMetadata>> pushMessage(List<V> messageList, boolean flush) {
        return pushMessage(messageList, (message, recordMetadata, exception) -> {
        }, flush);
    }


    public List<Future<RecordMetadata>> pushMessage(List<V> messageList) {
        return pushMessage(messageList, (message, recordMetadata, exception) -> {
        }, false);
    }

    public List<Future<RecordMetadata>> pushMessage(List<V> messageList, IMessageCallback<V> iMessageCallback, int retry) {
        final int retryNum = Math.max(retry, 1);
        return pushMessage(messageList, new IMessageCallback<V>() {
            @Override
            public void onCompletion(V message, RecordMetadata recordMetadata, Exception exception) {
                if (exception != null && retryNum > 1) {
                    pushMessage(messageList, this, retryNum - 1);
                    return;
                }
                iMessageCallback.onCompletion(message, recordMetadata, exception);
            }
        }, false);
    }


    @Override
    public void run() {
        KafkaTopicDefinition clientInfo = kafkaDefinition.getKafkaTopicDefinition();
        KafkaConsumer<K, V> consumer = getConsumer(kafkaDefinition);
        String topicName = kafkaDefinition.getKafkaTopicDefinition().getTopic();
        consumer.subscribe(Collections.singletonList(topicName));
        while (true) {
            boolean success = false;
            try {
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofSeconds(clientInfo.getPollTimeout()));
                for (ConsumerRecord<K, V> record : records) {
                    // 保证每次只拉取一条消息，处理成功以后则开始提交，否则重试
                    for (int i = 0; i < clientInfo.getMaxRetry() || clientInfo.getMaxRetry() < 1; i++) {
                        try {
                            success = processMessage(record.value());
                            if(success) {
                                break;
                            }
                        } catch (Exception e) {
                            log.error("processMessage failed, msg:{}", e.getMessage());
                        }
                    }
                }
                if (!clientInfo.getAutoCommit() && success) {
                    if (clientInfo.getCommitAsync()) {
                        consumer.commitAsync();
                        continue;
                    }
                    consumer.commitSync();
                }
                TimeUnit.SECONDS.sleep(0);
            } catch (Exception | Error e) {
                e.printStackTrace();
                log.error("{}", e.getMessage());
            }
        }
    }

    private KafkaConsumer<K, V> getConsumer(KafkaDefinition kafkaDefinition) {
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
        if (kafkaDefinition.getKafkaTopicDefinition().getAuthentication()) {
            properties.put("security.protocol", "SASL_PLAINTEXT");
            properties.put("sasl.mechanism", "PLAIN");
            String username = StringUtils.hasText(clientInfo.getUserName()) ? clientInfo.getUserName() : clientProperties.getUserName();
            String password = StringUtils.hasText(clientInfo.getPassword()) ? clientInfo.getPassword() : clientProperties.getPassword();
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\"  password=\"" + password + "\";");
        }
        for (NamePair namePair : clientProperties.getConsumerGlobalConfig()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        for (NamePair namePair : clientInfo.getConsumerConfigs()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        return new KafkaConsumer<>(properties);
    }

    private KafkaProducer<K, V> getProducer(KafkaDefinition kafkaDefinition) {
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
        if (kafkaDefinition.getKafkaTopicDefinition().getAuthentication()) {
            properties.put("security.protocol", "SASL_PLAINTEXT");
            properties.put("sasl.mechanism", "PLAIN");
            String username = StringUtils.hasText(clientInfo.getUserName()) ? clientInfo.getUserName() : clientProperties.getUserName();
            String password = StringUtils.hasText(clientInfo.getPassword()) ? clientInfo.getPassword() : clientProperties.getPassword();
            properties.put("sasl.jaas.config", "org.apache.kafka.common.security.plain.PlainLoginModule required username=\"" + username + "\"  password=\"" + password + "\";");
        }
        for (NamePair namePair : clientProperties.getProducerGlobalConfig()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        for (NamePair namePair : clientInfo.getProducerConfigs()) {
            properties.put(namePair.getKey(), namePair.getValue());
        }
        return new KafkaProducer<>(properties);
    }
}
