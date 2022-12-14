package plus.jdk.kafka.global;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.env.Environment;
import plus.jdk.kafka.config.KafkaClientProperties;

@Slf4j
public class KafkaClientLifecycle  implements SmartLifecycle {


    private final KafkaClientInitFactory kafkaClientInitFactory;

    private boolean running = false;

    public KafkaClientLifecycle(BeanFactory beanFactory, ApplicationContext applicationContext,
                                KafkaClientProperties properties, Environment environment) {
        this.kafkaClientInitFactory = new KafkaClientInitFactory(beanFactory, applicationContext, properties, environment);
    }

    @SneakyThrows
    @Override
    public void start() {
        kafkaClientInitFactory.initializationDefinition();
        kafkaClientInitFactory.startConsumingServices();
        running = true;
    }

    @Override
    public void stop() {
        running = false;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
