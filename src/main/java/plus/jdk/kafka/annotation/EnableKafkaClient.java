package plus.jdk.kafka.annotation;

import org.springframework.context.annotation.Import;
import plus.jdk.kafka.selector.KafkaClientSelector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Import(KafkaClientSelector.class)
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface EnableKafkaClient {

}
