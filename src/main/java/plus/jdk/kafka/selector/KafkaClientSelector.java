package plus.jdk.kafka.selector;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import plus.jdk.kafka.common.DefaultConsumeDecider;
import plus.jdk.kafka.config.KafkaClientProperties;
import plus.jdk.kafka.global.KafkaClientLifecycle;

@Configuration
public class KafkaClientSelector extends WebApplicationObjectSupport implements BeanFactoryAware, WebMvcConfigurer {

    public static BeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        KafkaClientSelector.beanFactory = beanFactory;
    }

    @Bean
    public KafkaClientLifecycle getKafkaClientLifecycle(KafkaClientProperties properties, Environment environment) {
        return new KafkaClientLifecycle(beanFactory, getApplicationContext(), properties, environment);
    }

    @Bean
    public DefaultConsumeDecider getDefaultConsumerDecider() {
        return new DefaultConsumeDecider();
    }

}
