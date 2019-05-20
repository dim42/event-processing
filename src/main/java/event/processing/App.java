package event.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Queue;
import java.util.concurrent.*;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);

    public static void main(String[] args) {
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

                String[] array = resultQueue.toArray(new String[0]);

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
