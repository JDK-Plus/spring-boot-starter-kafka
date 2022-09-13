package plus.jdk.kafka.model;

import lombok.Data;
import plus.jdk.kafka.common.DefaultConsumeDecider;
import plus.jdk.kafka.common.IConsumeDecider;

@Data
public class KafkaTopicDefinition {

    /**
     * 消费或生产的topic
     */
    private String topic = "";

    /**
     * 消费组
     */
    private String groupName = "";

    /**
     * 用户名.
     */
    private String userName = "";

    /**
     * 密码.
     */
    private String password = "";


    /**
     * 消费的broker列表
     */
    private String consumeBrokers = "";

    /**
     * 生产的broker列表
     */
    private String producerBrokers = "";

    /**
     * 决定是否开启消费
     */
    private Class<? extends IConsumeDecider> decider = DefaultConsumeDecider.class;

    /**
     * 消费配置
     */
    private NamePair[] consumerConfigs = {};

    /**
     * 要开启的消费量
     */
    private Integer consumerNum = 1;

    /**
     * 是否自动提交
     */
    private Boolean autoCommit = false;

    /**
     * 数据拉取超时时间
     */
    private Integer pollTimeout = 2;

    /**
     * 消费每次最大拉取多少条
     */
    private Integer consumerMaxPollRecord = 1;

    /**
     * 生产配置
     */
    private NamePair[] producerConfigs = {};

    /**
     * 验证配置是否合法
     */
    public void verifyConfiguration() {

    }
}
