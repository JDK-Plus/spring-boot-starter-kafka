package plus.jdk.kafka.global;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import plus.jdk.kafka.annotation.KafkaConsumerDesc;
import plus.jdk.kafka.common.IMessageCallback;
import plus.jdk.kafka.model.KafkaDefinition;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
public abstract class IKafkaQueue<K, V> implements Runnable {

    /**
     * 生产实例
     */
    private KafkaProducer<K, V> producer;

    /**
     * 关于topic的定义
     */
    private KafkaDefinition kafkaDefinition;

    protected boolean processMessage(V data) {
        return true;
    }

    public List<Future<RecordMetadata>> pushMessage(List<V> messageList, IMessageCallback<V> iMessageCallback, boolean flush) {
        if(producer == null) {
            producer = KafkaClientInitFactory.getProducer(kafkaDefinition);
        }
        String topicName = kafkaDefinition.getKafkaClient().topicName();
        List<Future<RecordMetadata>> futureList = Collections.synchronizedList(new ArrayList<>());
        for (V message : messageList) {
            Future<RecordMetadata> result = producer.send(new ProducerRecord<>(topicName, message), (recordMetadata, exception) -> iMessageCallback.onCompletion(message, recordMetadata, exception));
            futureList.add(result);
        }
        if(flush) {
            producer.flush();
        }
        return futureList;
    }

    public List<Future<RecordMetadata>> pushMessage(List<V> messageList, boolean flush) {
        return pushMessage(messageList, (message, recordMetadata, exception) -> {}, flush);
    }


    public List<Future<RecordMetadata>> pushMessage(List<V> messageList) {
        return pushMessage(messageList, (message, recordMetadata, exception) -> {}, false);
    }


    @Override
    public void run() {
        KafkaConsumerDesc consumerDesc = kafkaDefinition.getKafkaConsumerDesc();
        if(consumerDesc == null) {
            return;
        }
        KafkaConsumer<K, V> consumer = KafkaClientInitFactory.getConsumer(kafkaDefinition);
        String topicName = kafkaDefinition.getKafkaClient().topicName();
        consumer.subscribe(Collections.singletonList(topicName));
        while (true) {
            try {
                ConsumerRecords<K, V> records = consumer.poll(Duration.ofSeconds(consumerDesc.pollTimeout()));
                for (ConsumerRecord<K, V> record : records) {
                    // 保证每次只拉取一条消息，处理成功以后则开始提交，否则重试
                    boolean ret = processMessage(record.value());
                    if (ret && consumerDesc.autoCommit()) {
                        consumer.commitSync();
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
                log.error("{}", e.getMessage());
            }
        }
    }
}
