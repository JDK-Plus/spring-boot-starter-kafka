package plus.jdk.kafka.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import plus.jdk.kafka.annotation.EnableKafkaClient;
import plus.jdk.kafka.config.KafkaClientProperties;

@Slf4j
@Configuration
@EnableKafkaClient
@ConditionalOnProperty(prefix = "plus.jdk.kafka.client", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(KafkaClientProperties.class)
public class KafkaClientAutoConfiguration {

}
