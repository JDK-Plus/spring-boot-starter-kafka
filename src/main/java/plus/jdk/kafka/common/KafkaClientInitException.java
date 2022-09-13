package plus.jdk.kafka.common;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class KafkaClientInitException extends Exception {

    public KafkaClientInitException(String message) {
        super(message);
    }
}
