package plus.jdk.kafka.annotation;

import plus.jdk.kafka.common.DefaultConsumeDecider;
import plus.jdk.kafka.common.IConsumeDecider;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KafkaConsumerDesc {


    /**
     * 消费组名称
     */
    String groupName();

    /**
     * 用户名
     */
    String userName() default "";

    /**
     * 密码
     */
    String password() default "";


    /**
     * 开启几个线程来消费
     */
    int consumerNum() default 1;

    /**
     * 消费的broker列表
     */
    String[] brokers() default {};

    /**
     * 是否自动提交
     */
    boolean autoCommit() default false;

    /**
     * 拉取超时时间，默认2秒
     */
    int pollTimeout() default 2;

    /**
     * 每次最大拉取数量
     */
    int maxPollRecord() default 1;

    /**
     * 消费组kafka 配置项，若通过注解配置，则优先级高于配置文件中的数据
     */
    KafkaProperty[] properties() default {};

    /**
     * 一个回调类，用于判定是否开启消费，默认开启
     */
    Class<? extends IConsumeDecider> decider() default DefaultConsumeDecider.class;
}
