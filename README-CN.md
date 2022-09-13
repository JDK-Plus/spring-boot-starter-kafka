<h3 align="center">一个集成Kafka依赖的Springboot扩展</h3>
<p align="center">
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/blob/master/LICENSE"><img src="https://img.shields.io/github/license/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/releases"><img src="https://img.shields.io/github/release/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/stargazers"><img src="https://img.shields.io/github/stars/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/network/members"><img src="https://img.shields.io/github/forks/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
</p>

## 引入依赖

```xml
<dependency>
    <groupId>plus.jdk</groupId>
    <artifactId>spring-boot-starter-kafka</artifactId>
    <version>1.0.2</version>
</dependency>
```

其他更多引入方法请参见：[spring-boot-starter-kafka](https://search.maven.org/artifact/plus.jdk/spring-boot-starter-kafka/1.0.2/jar)

## 需要添加的配置项
```
# 启动该组件
plus.jdk.kafka.client.enabled=true

# 指定kafka全局的用户名
plus.jdk.kafka.client.user-name=root

# 指定kafka全局的密码
plus.jdk.kafka.client.password=123456

# 指定全局的消费配置项
plus.jdk.kafka.client.consumer-global-config[0].key=sasl.mechanism
plus.jdk.kafka.client.consumer-global-config[0].value=PLAIN
plus.jdk.kafka.client.consumer-global-config[1].key=sasl.mechanism
plus.jdk.kafka.client.consumer-global-config[1].value=PLAIN

# 指定全局的生产配置项
plus.jdk.kafka.client.producer-global-config[0].key=sasl.mechanism
plus.jdk.kafka.client.producer-global-config[0].value=PLAIN


# 定义一组kafka配置
# 定义topic配置名称，必选项，该值应该为唯一的，不可以重复定义，当启动消费或推送消息时会根据该值初始化消费者和生产者实例
# 关于这里的设计，有人可能会存在疑问："为什么不直接用kafka的topic来作为配置名呢？"。
# 其实都是可以的，只不过这里笔者在编写的时候考虑到了线上环境和测试环境topic名称不是同一个的情况，而且这种情况很常见。
plus.jdk.kafka.client.topic-definitions[0].name=my_first_topic_conf_name
# 当前要消费或生产的topic名称，必选项
plus.jdk.kafka.client.topic-definitions[0].topic=test_topic_name
# 生产的broker列表，若无需生产消息，则可选，否则必填，若为空，则会导致消息无法推送
plus.jdk.kafka.client.topic-definitions[0].producer-brokers=127.0.0.1:9090,10.11.1.102:9090
# 消费的broker列表，若无需消费消息，则可选，否则必填，若为空，则会导致无法启动消费
plus.jdk.kafka.client.topic-definitions[0].consume-brokers=127.0.0.1:8888,127.0.0.1:8889
# 消费的groupName，若无需消费消息，则可选，否则必填，若为空，则会导致无法启动消费
plus.jdk.kafka.client.topic-definitions[0].group-name=groupName
# 可选配置项，消费是是否自动提交
plus.jdk.kafka.client.topic-definitions[0].auto-commit=false
# 可选配置项，开启几个消费者
plus.jdk.kafka.client.topic-definitions[0].consumer-num=1
# 可选配置项，每次消费最大拉取多少条消息
plus.jdk.kafka.client.topic-definitions[0].consumer-max-poll-record=1
# 可选配置项，若不想使用全局的用户名，则可使用该项单独指定
plus.jdk.kafka.client.topic-definitions[0].user-name=root
# 可选配置项，若不想使用全局的密码，则可使用该项单独指定
plus.jdk.kafka.client.topic-definitions[0].password=123456
# 可选配置项，消费拉取消息的超时时间
plus.jdk.kafka.client.topic-definitions[0].poll-timeout=2
# 可选配置项，一个bean实例。需要实现plus.jdk.kafka.common.IConsumeDecider接口，来负责判断该topic是否启动消费进程，默认不开启
plus.jdk.kafka.client.topic-definitions[0].decider=plus.jdk.kafka.common.DefaultConsumeDecider

# 可选配置项，若想覆盖全局的消费配置项，则可通过如下配置指定
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].value=xxx

# 可选配置项，若想覆盖全局的生产配置项，则可通过如下配置指定
plus.jdk.kafka.client.topic-definitions[0].producer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].producer-configs[0].value=xxx

```
## 定义一个可以生产消息和消费消息的kafka队列

### 关于kafka队列的消费和生产可以定义如下：

```java
import plus.jdk.kafka.global.IKafkaQueue;
import plus.jdk.kafka.annotation.KafkaClient;

@KafkaClient("my_first_topic_conf_name")
public class MyFirstTopicQueue extends IKafkaQueue<String, String> {

    /**
     * 处理消息队列中的数据，若不需要消费，则该方法可不实现
     */
    @Override
    protected boolean processMessage(String data) {
        // 根据传入的data处理消费数据
        return true;
    }
}
```

上文示例中的 `@KafkaClient` 注解会将该队列的定义申明为一个bean实例。 若配置了消费相关的内容，则会根据上文中的配置启动消费服务。

### 如何推送消息

推送消息时，可以调用上文中已经定义好的 `MyFirstTopicQueue` 队列的`pushMessage`方法来完成消息推送. 

```java
import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
public class MyKafkaTestService {

    @Resource
    private MyFirstTopicQueue myFirstTopicQueue;

    public void test() {
        List<Future<RecordMetadata>> futureList = myFirstTopicQueue.pushMessage(Arrays.asList("data1", "data2"), (message, recordMetadata, e) -> {
            if (e != null) {
                log.error("push message {} failed, message:{}", message, e.getMessage());
                return;
            }
            log.info("push message {} success", message);
        });
    }
}
```
