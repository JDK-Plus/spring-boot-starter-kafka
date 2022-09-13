package plus.jdk.kafka.model;

import lombok.Data;

@Data
public class NamePair {

    /**
     * 配置的key
     */
    private String key;

    /**
     * 配置的value
     */
    private String value;
}
