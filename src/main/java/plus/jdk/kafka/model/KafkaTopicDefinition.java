package plus.jdk.kafka.model;

import lombok.Data;
import org.springframework.util.StringUtils;
import plus.jdk.kafka.common.DefaultConsumeDecider;
import plus.jdk.kafka.common.IConsumeDecider;
import plus.jdk.kafka.common.KafkaClientInitException;

import java.util.ArrayList;
import java.util.List;

@Data
public class KafkaTopicDefinition {

    /**
     * 关于当前组topic定义的名称，
     * 后续生产和消费都会根据当前组名称找到配置定义来来初始化生产和消费
     */
    private String name = "";

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
     * 是否进行用户名密码鉴权，默认需要指定
     */
    private Boolean authentication = true;


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
    private List<NamePair> consumerConfigs = new ArrayList<>();

    /**
     * 要开启的消费量
     */
    private Integer consumerNum = 1;

    /**
     * 是否自动提交
     */
    private Boolean autoCommit = false;

    /**
     * 是否启动异步提交
     */
    private Boolean commitAsync = false;

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
    private List<NamePair> producerConfigs = new ArrayList<>();

    /**
     * 验证配置是否合法
     */
    public void verifyConfiguration() throws KafkaClientInitException {
        if(!StringUtils.hasText(topic)) {
            throw new KafkaClientInitException(String.format("topic-definitions name or topic %s cannot be empty", this));
        }
    }
}
