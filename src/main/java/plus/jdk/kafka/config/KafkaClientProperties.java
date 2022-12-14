package plus.jdk.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import plus.jdk.kafka.model.KafkaTopicDefinition;
import plus.jdk.kafka.model.NamePair;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "plus.jdk.kafka.client")
public class KafkaClientProperties {

    private boolean enabled = false;

    /**
     * 全局用户名.
     * 若配置项里面声明了，则优先使用配置里面的值
     */
    private String userName = "";

    /**
     * 全局密码.
     * 若配置项里面声明了，则优先使用配置里面的值
     */
    private String password = "";

    /**
     * 消费全局配置项
     */
    private List<NamePair> consumerGlobalConfig = new ArrayList<>();

    /**
     * 生产全局配置项
     */
    private List<NamePair> producerGlobalConfig = new ArrayList<>();

    /**
     * 其他的topic配置项
     */
    private List<KafkaTopicDefinition> topicDefinitions = new ArrayList<>();
}
