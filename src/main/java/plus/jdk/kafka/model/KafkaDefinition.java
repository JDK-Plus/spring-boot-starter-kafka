package plus.jdk.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import plus.jdk.kafka.annotation.KafkaClient;
import plus.jdk.kafka.global.IKafkaQueue;

@Data
@AllArgsConstructor
public class KafkaDefinition {

    /**
     * 当前topic的信息
     */
    private KafkaTopicDefinition kafkaTopicDefinition;

    /**
     * 关于kafka的定义
     */
    private KafkaClient kafkaClient;

    /**
     * 实现的bean实例
     */
    private IKafkaQueue<?, ?> beanInstance;
}
