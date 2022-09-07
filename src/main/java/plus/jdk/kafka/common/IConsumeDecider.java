package plus.jdk.kafka.common;

public interface IConsumeDecider {

    /**
     * 决定是否开启消费
     */
    boolean consume();
}
