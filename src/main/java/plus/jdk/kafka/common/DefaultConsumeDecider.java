package plus.jdk.kafka.common;

public class DefaultConsumeDecider implements IConsumeDecider {

    @Override
    public boolean consume() {
        return false;
    }
}
