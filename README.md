<h3 align="center">A Springboot extension that integrates Kafka dependencies</h3>
<p align="center">
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/blob/master/LICENSE"><img src="https://img.shields.io/github/license/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/releases"><img src="https://img.shields.io/github/release/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/stargazers"><img src="https://img.shields.io/github/stars/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/network/members"><img src="https://img.shields.io/github/forks/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
</p>

## Import Dependencies

```xml
<dependency>
    <groupId>plus.jdk</groupId>
    <artifactId>spring-boot-starter-kafka</artifactId>
    <version>1.0.2</version>
</dependency>
```
For more import methods, see：[spring-boot-starter-kafka](https://search.maven.org/artifact/plus.jdk/spring-boot-starter-kafka/1.0.2/jar)

## Configuration items that need to be added
```
# start the component
plus.jdk.kafka.client.enabled=true

# Specify the user name of kafka global
plus.jdk.kafka.client.user-name=root

# Specify kafka global password
plus.jdk.kafka.client.password=123456

# Specify global consumption configuration items
plus.jdk.kafka.client.consumer-global-config[0].key=sasl.mechanism
plus.jdk.kafka.client.consumer-global-config[0].value=PLAIN
plus.jdk.kafka.client.consumer-global-config[1].key=sasl.mechanism
plus.jdk.kafka.client.consumer-global-config[1].value=PLAIN

# Specify a global production configuration item
plus.jdk.kafka.client.producer-global-config[0].key=sasl.mechanism
plus.jdk.kafka.client.producer-global-config[0].value=PLAIN


# Define a set of kafka configurations
# Define the topic configuration name, a required option, this value should be unique and cannot be defined repeatedly.
# When starting consumption or pushing messages, the consumer and producer instances will be initialized according to this value
# Regarding the design here, some people may have questions: "Why not directly use kafka's topic as the configuration name?".
# In fact, they are all possible, but the author considered the situation that the topic names of the online environment and 
# the test environment are not the same when writing, and this situation is very common
plus.jdk.kafka.client.topic-definitions[0].name=my_first_topic_conf_name
# Required, the name of the topic currently to be consumed or produced
plus.jdk.kafka.client.topic-definitions[0].topic=test_topic_name
# The list of production brokers, optional if no production message is required, otherwise required, if it is empty, the message cannot be pushed
plus.jdk.kafka.client.topic-definitions[0].producer-brokers=127.0.0.1:9090,10.11.1.102:9090
# The list of brokers to consume, optional if you do not need to consume messages, otherwise required, if it is empty, it will cause the consumption to fail to start.
plus.jdk.kafka.client.topic-definitions[0].consume-brokers=127.0.0.1:8888,127.0.0.1:8889
# The groupName of the consumption, optional if the message does not need to be consumed, otherwise required, if it is empty, the consumption cannot be started
plus.jdk.kafka.client.topic-definitions[0].group-name=groupName
# Optional configuration item, whether consumption is automatically submitted
plus.jdk.kafka.client.topic-definitions[0].auto-commit=false
# Optional configuration item to enable several consumers
plus.jdk.kafka.client.topic-definitions[0].consumer-num=1
# Optional configuration item, the maximum number of messages to be pulled per consumption
plus.jdk.kafka.client.topic-definitions[0].consumer-max-poll-record=1
# Optional configuration item, if you do not want to use the global user name, you can use this item to specify it separately
plus.jdk.kafka.client.topic-definitions[0].user-name=root
# Optional configuration item, if you don't want to use the global password, you can use this item to specify it separately
plus.jdk.kafka.client.topic-definitions[0].password=123456
# Optional configuration item, timeout for consuming pull messages
plus.jdk.kafka.client.topic-definitions[0].poll-timeout=2
# Optional configuration item, a bean instance. The plus.jdk.kafka.common.
# IConsumeDecider interface needs to be implemented to be responsible for judging whether the topic starts the consumption process, 
# which is not enabled by default
plus.jdk.kafka.client.topic-definitions[0].decider=plus.jdk.kafka.common.DefaultConsumeDecider

# Optional configuration item, if you want to override the global consumption configuration item, 
# you can specify it through the following configuration
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].value=xxx

# Optional configuration item. If you want to override the global production configuration item, 
# you can specify it through the following configuration
plus.jdk.kafka.client.topic-definitions[0].producer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].producer-configs[0].value=xxx

```
## Define a kafka queue that can produce and consume messages

### The consumption and production of the kafka queue can be defined as follows:

```java
import plus.jdk.kafka.global.IKafkaQueue;
import plus.jdk.kafka.annotation.KafkaClient;

@KafkaClient("my_first_topic_conf_name")
public class MyFirstTopicQueue extends IKafkaQueue<String, String> {

    /**
     * Process the data in the message queue. 
     * If consumption is not required, this method may not be implemented
     */
    @Override
    protected boolean processMessage(String data) {
        // Process consumption data based on incoming data
        return true;
    }
}
```

The `@KafkaClient` annotation in the above example declares the queue definition as a bean instance. 
If consumption-related content is configured, the consumption service will be started according to the configuration above.

### How to push messages

When pushing a message, you can call the `pushMessage` method of the `MyFirstTopicQueue` queue defined above to complete the message push.
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
