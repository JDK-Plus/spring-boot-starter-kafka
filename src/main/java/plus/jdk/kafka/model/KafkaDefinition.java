package plus.jdk.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import plus.jdk.kafka.annotation.KafkaConsumerDesc;
import plus.jdk.kafka.annotation.KafkaProducerDesc;
import plus.jdk.kafka.annotation.KafkaClient;
import plus.jdk.kafka.global.IKafkaQueue;

@Data
@AllArgsConstructor
public class KafkaDefinition {

    /**
     * 关于消费配置相关
     */
    private KafkaConsumerDesc kafkaConsumerDesc;

    /**
     * 关于生产配置相关
     */
    private KafkaProducerDesc kafkaProducerDesc;

    /**
     * 关于kafka的定义
     */
    private KafkaClient kafkaClient;

    /**
     * 实现的bean实例
     */
    private IKafkaQueue<?, ?> beanInstance;
}
