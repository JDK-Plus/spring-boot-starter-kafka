package plus.jdk.kafka.annotation;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.lang.annotation.*;

@Bean
@Service
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaClient {

    /**
     * 消费的topic列表
     */
    String topicName();
}
