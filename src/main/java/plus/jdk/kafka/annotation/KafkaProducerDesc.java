package plus.jdk.kafka.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaProducerDesc {

    /**
     * 用户名
     */
    String userName() default "";

    /**
     * 密码
     */
    String password() default "";

    /**
     * 生产的broker列表
     */
    String[] brokers() default {};

    /**
     * 消费组kafka 配置项，若通过注解配置，则优先级高于配置文件中的数据
     */
    KafkaProperty[] properties() default {};
}
