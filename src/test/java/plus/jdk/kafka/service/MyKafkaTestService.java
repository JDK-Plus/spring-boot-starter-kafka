package plus.jdk.kafka.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.stereotype.Service;
import plus.jdk.kafka.queue.MyFirstTopicQueue;

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

    public void test() throws ExecutionException, InterruptedException {
        List<Future<RecordMetadata>> futureList = myFirstTopicQueue.pushMessage(Arrays.asList("data1", "data2"), (message, recordMetadata, e) -> {
            if (e != null) {
                log.error("push message {} failed, message:{}", message, e.getMessage());
                return;
            }
            log.info("push message {} success", message);
        });
    }
}
