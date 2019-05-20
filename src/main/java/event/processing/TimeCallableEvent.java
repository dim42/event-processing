package event.processing;

import java.time.LocalDateTime;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TimeCallableEvent<V> implements Delayed {

    private static final AtomicLong seq = new AtomicLong();

    private final long seqNum;
    private final LocalDateTime time;
    private final Callable<V> callable;

    public TimeCallableEvent(LocalDateTime time, Callable<V> callable) {
        this.time = time;
        this.callable = callable;
        seqNum = seq.getAndIncrement();
    }

    @Override
    public int compareTo(Delayed arg) {
        TimeCallableEvent other = (TimeCallableEvent) arg;
        int result = time.compareTo(other.time);
        if (result == 0 && time != other.time && callable != other.callable) {
            result = Long.compare(seqNum, other.seqNum);
        }
        return result;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        LocalDateTime now = LocalDateTime.now();
        return (long) time.compareTo(now);
    }

    public Callable<V> getCallable() {
        return callable;
    }
}
