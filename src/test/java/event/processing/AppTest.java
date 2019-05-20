package event.processing;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;

import static java.lang.String.format;
import static org.junit.Assert.assertTrue;

public class AppTest {

    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void testEvents() {
        ExecutorService threadPool = Executors.newCachedThreadPool();
        BlockingQueue<TimeCallableEvent> queue = new DelayQueue<>();

        Queue<String> resultQueue = new ConcurrentLinkedQueue<>();
        threadPool.execute(new Consumer<>(queue, resultQueue));

        int size = 10;
        int nowId = 5;
        LocalDateTime now = LocalDateTime.now();
        Producer<TimeCallableEvent, Integer> producer;
        for (int i = 0; i < size; i++) {
            if (i == 2) {
                producer = new Producer<>(queue);
                producer.add(now.plusNanos(30_000_000), new DelayedTask(13));
                threadPool.execute(producer);
            }

            producer = new Producer<>(queue);
            producer.add(now.plusNanos((nowId - i) * 10_000_000), new DelayedTask(i));
            threadPool.execute(producer);
        }
        producer = new Producer<>(queue);
        producer.add(now.plusNanos(30_000_000), new DelayedTask(11));
        threadPool.execute(producer);

        threadPool.execute(() -> queue.add(new TimeCallableEvent<>(now.plusSeconds(1), new Callable<>() {
            @Override
            public Integer call() {
                log.info("resultQueue:{}", resultQueue);

                List<String> ids = Arrays.asList(resultQueue.toArray(new String[0]));
                int first = ids.indexOf("9");
                int middle = ids.indexOf("4");
                int last = ids.indexOf("2");

                assertTrue(format("f:%d,m:%d", first, middle), first < middle);
                assertTrue(format("m:%d,l:%d", middle, last), middle < last);

                Thread.currentThread().interrupt();
                threadPool.shutdownNow();
                return -1;
            }

            @Override
            public String toString() {
                return String.valueOf(-1);
            }
        })));

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
