<h3 align="center">A Springboot extension that integrates Kafka dependencies</h3>
<p align="center">
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/blob/master/LICENSE"><img src="https://img.shields.io/github/license/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/releases"><img src="https://img.shields.io/github/release/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/stargazers"><img src="https://img.shields.io/github/stars/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
    <a href="https://github.com/JDK-Plus/spring-boot-starter-kafka/network/members"><img src="https://img.shields.io/github/forks/JDK-Plus/spring-boot-starter-kafka.svg" /></a>
</p>

- [中文文档](README-CN.md)

## Use maven to import

```xml
<dependency>
    <groupId>plus.jdk</groupId>
    <artifactId>spring-boot-starter-kafka</artifactId>
    <version>1.0.6</version>
</dependency>
```

For more import methods, see：[spring-boot-starter-kafka](https://search.maven.org/artifact/plus.jdk/spring-boot-starter-kafka/1.0.2/jar)

## Configuration items to be added

### Enable the component

```bash
# Enable the component
plus.jdk.kafka.client.enabled=true
```

### Specify the username and password

You can specify global usernames and passwords using the following configuration items

```bash
# Specifies the user name for kafka globally
plus.jdk.kafka.client.user-name=root

# Specifies the kafka global password
plus.jdk.kafka.client.password=123456
```

But what if there are special topic user names and passwords that differ from the global configuration items? 
You can specify a username and password for a topic with the following configuration items

```bash
# This parameter is optional. If you do not want to use the global user name, 
# you can use this parameter to specify it
plus.jdk.kafka.client.topic-definitions[0].user-name=root
# This parameter is optional. If you do not want to use the global password, 
# you can use this parameter separately
plus.jdk.kafka.client.topic-definitions[0].password=123456
```


### Specify consumer configuration items

You can use the following configuration to set up the global consumer configuration items：

```bash
# Optional configuration item, which specifies the global consumption configuration item
plus.jdk.kafka.client.consumer-global-config[0].key=sasl.mechanism
plus.jdk.kafka.client.consumer-global-config[0].value=PLAIN
plus.jdk.kafka.client.consumer-global-config[1].key=sasl.mechanism
plus.jdk.kafka.client.consumer-global-config[1].value=PLAIN
```

Obviously, there will always be exceptions. In your daily development work, 

there will certainly be special topic configuration items that are not consistent with others. 

In this case, you can specify a separate consumption configuration item like this:

```bash
# Optional configuration items. 
# If you want to overwrite the global consumption configuration items, 
# you can specify them as follows
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].value=xxx

```

### Specifies the producer configuration item

```bash
# This parameter is optional. It specifies global production configuration items
plus.jdk.kafka.client.producer-global-config[0].key=sasl.mechanism
plus.jdk.kafka.client.producer-global-config[0].value=PLAIN
```

Obviously, there will always be exceptions. 

In your daily development work, there will certainly be special topic configuration items that are not consistent with others. 

In this case, you can specify the production configuration items as follows:

```bash
# This parameter is optional. 
# If you want to overwrite all production configuration items, you can specify it as follows
plus.jdk.kafka.client.topic-definitions[0].producer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].producer-configs[0].value=xxx
```

### How to describe a topic production and consumption

#### Specify the current topic configuration name

First, we define a topic configuration group name, which is mandatory. 
This value should be unique and not repeatable, and consumer and producer instances will be initialized based on this 
value when consumption is started or messages are pushed

One might wonder about this design: "Why not just use the kafka topic as the configuration name?" .

In fact, both are possible, but this is written to take into account the fact that the online environment 
and the test environment have different topic names, which is very common.

You can specify the configuration group name for the current topic using the following configuration

```bash
plus.jdk.kafka.client.topic-definitions[0].name=my_first_topic_conf_name
```
This name is used directly with '@KafkaClient'. When you specify the configuration group name in '@KafkaClient', 
you initialize the consumer and producer based on the contents of the specified configuration group.

#### Define a producer using the configuration description

This allows you to specify the name of the current configuration group, the topic name, 
and the list of brokers that the producer uses to produce messages.

```bash
# Configuration Group Name
plus.jdk.kafka.client.topic-definitions[0].name=my_first_topic_conf_name
# The name of the current topic to be consumed or produced. This parameter is mandatory
plus.jdk.kafka.client.topic-definitions[0].topic=test_topic_name
# This field is optional if there is no need for production messages. If it is empty, messages cannot be pushed
plus.jdk.kafka.client.topic-definitions[0].producer-brokers=127.0.0.1:9090,10.11.1.102:9090
```

#### Define a consumer using the configuration description

As for consumption, I added configuration items according to my daily business requirements when writing the code.
If you want to customize it, You can use the `plus.jdk.kafka.client.topic-definitions[0].consumer-configs` configuration group specified

```bash
# Configuration Group Name
plus.jdk.kafka.client.topic-definitions[0].name=my_first_topic_conf_name
# The name of the current topic to be consumed or produced. This parameter is mandatory
plus.jdk.kafka.client.topic-definitions[0].topic=test_topic_name
# The list of consuming brokers is optional if consumption messages are not required, 
# otherwise it must be filled in. If empty, consumption cannot be started
plus.jdk.kafka.client.topic-definitions[0].consume-brokers=127.0.0.1:8888,127.0.0.1:8889
# This parameter is optional if no consumption message is required. Otherwise, 
# this parameter is mandatory. If this parameter is empty, consumption cannot be started
plus.jdk.kafka.client.topic-definitions[0].group-name=groupName
# This parameter is optional. Whether to automatically commit consumption
plus.jdk.kafka.client.topic-definitions[0].auto-commit=false
# Enable several consumers as optional configuration items
plus.jdk.kafka.client.topic-definitions[0].consumer-num=1
# An optional configuration item that specifies the maximum number of messages pulled per consumption
plus.jdk.kafka.client.topic-definitions[0].consumer-max-poll-record=1
# This parameter is optional. If you do not want to use the global user name, 
# you can use this parameter to specify it
plus.jdk.kafka.client.topic-definitions[0].user-name=root
# This parameter is optional. If you do not want to use the global password, you can use this parameter separately
plus.jdk.kafka.client.topic-definitions[0].password=123456
# An optional configuration item that consumes the timeout period for pulling messages
plus.jdk.kafka.client.topic-definitions[0].poll-timeout=2
# Optional configuration item, an instance of the bean. Need to implement plus.jdk.kafka.com mon. 
# IConsumeDecider interface, to judge whether the topic to start process, the default is not open
plus.jdk.kafka.client.topic-definitions[0].decider=plus.jdk.kafka.common.DefaultConsumeDecider

# Optional configuration items. If you want to overwrite the global consumption configuration items, 
# you can specify them as follows
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].value=xxx

```
In many cases, we need the service startup decide whether need to start the process of consumption, 
so here you can claim an inheritance from `plus.jdk.kafka.common.DefaultConsumeDecider` to determine whether to start the consuming process. 
Use the following configuration items to specify it in the configuration.

```bash
plus.jdk.kafka.client.topic-definitions[0].decider=plus.jdk.kafka.common.DefaultConsumeDecider
```

Interface `plus.jdk.kafka.common.DefaultConsumeDecider` is defined as follows

```java
package plus.jdk.kafka.common;

public class DefaultConsumeDecider implements IConsumeDecider {
    @Override
    public boolean consume() {
        return false; // If true, the consumption process starts
    }
}
```


**A configuration item that contains both producers and consumers is described below**

```bash
# Configuration Group Name
plus.jdk.kafka.client.topic-definitions[0].name=my_first_topic_conf_name
# The name of the current topic to be consumed or produced. This parameter is mandatory
plus.jdk.kafka.client.topic-definitions[0].topic=test_topic_name
# The list of consuming brokers is optional if consumption messages are not required, 
# otherwise it must be filled in. If empty, consumption cannot be started
plus.jdk.kafka.client.topic-definitions[0].consume-brokers=127.0.0.1:8888,127.0.0.1:8889
# This field is optional if there is no need for production messages. 
# If it is empty, messages cannot be pushed
plus.jdk.kafka.client.topic-definitions[0].producer-brokers=127.0.0.1:9090,10.11.1.102:9090
# This parameter is optional if no consumption message is required. Otherwise, 
# this parameter is mandatory. If this parameter is empty, consumption cannot be started
plus.jdk.kafka.client.topic-definitions[0].group-name=groupName
# This parameter is optional. Whether to automatically commit consumption
plus.jdk.kafka.client.topic-definitions[0].auto-commit=false
# Enable several consumers as optional configuration items
plus.jdk.kafka.client.topic-definitions[0].consumer-num=1
# An optional configuration item that specifies the maximum number of messages pulled per consumption
plus.jdk.kafka.client.topic-definitions[0].consumer-max-poll-record=1
# This parameter is optional. If you do not want to use the global user name, 
# you can use this parameter to specify it
plus.jdk.kafka.client.topic-definitions[0].user-name=root
# This parameter is optional. If you do not want to use the global password, you can use this parameter separately
plus.jdk.kafka.client.topic-definitions[0].password=123456
# An optional configuration item that consumes the timeout period for pulling messages
plus.jdk.kafka.client.topic-definitions[0].poll-timeout=2
# Optional configuration item, an instance of the bean. Need to implement plus.jdk.kafka.com mon. 
# IConsumeDecider interface, to judge whether the topic to start process, the default is not open
plus.jdk.kafka.client.topic-definitions[0].decider=plus.jdk.kafka.common.DefaultConsumeDecider

# Optional configuration items. If you want to overwrite the global consumption configuration items, 
# you can specify them as follows
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].key=xxx
plus.jdk.kafka.client.topic-definitions[0].consumer-configs[0].value=xxx

# This parameter is optional. If you want to overwrite all production configuration items, you can specify it as follow
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
