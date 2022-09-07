package plus.jdk.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "plus.jdk.kafka.client")
public class KafkaClientProperties {

    private boolean enabled = false;

    /**
     * 全局用户名.
     * 若注解里面声明了，则优先使用注解里面的值
     */
    private String userName;

    /**
     * 全局密码.
     * 若注解里面声明了，则优先使用注解里面的值
     */
    private String password;
}
