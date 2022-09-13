package plus.jdk.kafka.queue;

import plus.jdk.kafka.global.IKafkaQueue;
import plus.jdk.kafka.annotation.KafkaClient;

@KafkaClient("my_first_topic")
public class MyFirstTopicQueue extends IKafkaQueue<String, String> {

    /**
     * 处理消息队列中的数据，若不需要消费，则该方法可不实现
     */
    @Override
    protected boolean processMessage(String data) {
        // 根据传入的data处理消费数据
        return true;
    }
}
