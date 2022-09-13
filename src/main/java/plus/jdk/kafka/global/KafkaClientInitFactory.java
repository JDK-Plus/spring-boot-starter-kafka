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

    private final KafkaClientProperties clientProperties;

    private final List<KafkaDefinition> kafkaDefinitions = new ArrayList<>();

    public KafkaClientInitFactory(BeanFactory beanFactory, ApplicationContext applicationContext,
                                  KafkaClientProperties properties, Environment environment) {
        this.beanFactory = beanFactory;
        this.applicationContext = applicationContext;
        this.clientProperties = properties;
    }

    protected void initializationDefinition() throws KafkaClientInitException {
        String[] beanNames =
                this.applicationContext.getBeanNamesForAnnotation(KafkaClient.class);
        HashMap<String, KafkaTopicDefinition> kafkaClientMap = new HashMap<>();
        for(KafkaTopicDefinition clientInfo: clientProperties.getTopicDefinitions()) {
            clientInfo.verifyConfiguration();
            if(kafkaClientMap.containsKey(clientInfo.getName())) {
                throw new KafkaClientInitException(String.format("topic-definitions name %s cannot be repeated", clientInfo.getName()));
            }
            kafkaClientMap.put(clientInfo.getName(), clientInfo);
        }
        for (String beanName : beanNames) {
            IKafkaQueue<?, ?> kafkaQueue = this.applicationContext.getBean(beanName, IKafkaQueue.class);
            KafkaClient kafkaClient = this.applicationContext.findAnnotationOnBean(beanName, KafkaClient.class);
            assert kafkaClient != null;
            KafkaTopicDefinition clientInfo = kafkaClientMap.get(kafkaClient.value());
            if(clientInfo == null) {
                throw new KafkaClientInitException(String.format("cannot find topic conf %s config", kafkaClient.value()));
            }
            KafkaDefinition kafkaDefinition = new KafkaDefinition(clientInfo, kafkaClient, kafkaQueue);
            kafkaQueue.kafkaDefinition = kafkaDefinition;
            kafkaQueue.clientProperties = clientProperties;
            kafkaDefinitions.add(kafkaDefinition);
        }
    }

    protected void startConsumingServices() throws KafkaClientInitException {
        for (KafkaDefinition kafkaDefinition : kafkaDefinitions) {
            if(kafkaDefinition.getKafkaTopicDefinition() == null) {
                continue;
            }
            KafkaTopicDefinition clientInfo = kafkaDefinition.getKafkaTopicDefinition();
            if(!StringUtils.hasText(clientInfo.getGroupName()) || !StringUtils.hasText(clientInfo.getConsumeBrokers())) {
                throw new KafkaClientInitException(String.format("start consumer failed, topic:%s groupName or consumeBrokers is null", clientInfo.getTopic()));
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
        if(!consumerDecider.consume()) {
            return;
        }
        IKafkaQueue<?, ?> kafkaQueue = kafkaDefinition.getBeanInstance();
        for(int i = 0; i < clientInfo.getConsumerNum(); i ++) {
            Thread consumerThread = new Thread(() -> {
                while (true) {
                    try {
                        kafkaQueue.run();
                    }catch (Exception e) {
                        e.printStackTrace();
                        log.error("start consumer failed, message:{}", e.getMessage());
                    }
                }
            });
            consumerThread.start();
        }
    }
}
