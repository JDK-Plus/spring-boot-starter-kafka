package plus.jdk.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
public class KafkaApplication {


    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);
    }
}
