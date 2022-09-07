package plus.jdk.kafka.common;

import org.apache.kafka.clients.producer.RecordMetadata;

public interface IMessageCallback<T> {
    void onCompletion(T message, RecordMetadata recordMetadata, Exception e);
}
