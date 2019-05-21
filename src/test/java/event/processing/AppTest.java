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
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertTrue;

public class AppTest {

    private static final Logger log = LoggerFactory.getLogger(AppTest.class);

    @Test
    public void testEvents() {
        BlockingQueue<TimeCallableEvent<Integer>> eventQueue = new DelayQueue<>();
        Queue<String> callableQueue = new ConcurrentLinkedQueue<>();
        Queue<CompletableFuture<Integer>> resultQueue = new ConcurrentLinkedQueue<>();

        runAsync(new Consumer<>(eventQueue, callableQueue, resultQueue));

        int size = 10;
        int nowId = 5;
        LocalDateTime now = LocalDateTime.now();
        Producer<TimeCallableEvent<Integer>, Integer> producer = new Producer<>(eventQueue);
        for (int i = 0; i < size; i++) {
            if (i == 2) {
                runAsync(() -> {
                    LocalDateTime time = now.plusNanos(30_000_000);
                    producer.add(time, new DelayedTask(13));
                    producer.add(time, new DelayedTask(2));
                    producer.add(time, new DelayedTask(11));
                });
            } else {
                int finalI = i;
                runAsync(() -> producer.add(now.plusNanos((nowId - finalI) * 10_000_000), new DelayedTask(finalI)));
            }
        }

        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Thread.currentThread().interrupt();// stop Consumer

        log.info("callableQueue:{}", callableQueue);
        List<String> ids = Arrays.asList(callableQueue.toArray(new String[0]));
        int first = ids.indexOf("9");
        int middle = ids.indexOf("4");
        int last1 = ids.indexOf("13");
        int last2 = ids.indexOf("2");
        int last3 = ids.indexOf("11");

        assertTrue(format("f:%d,m:%d", first, middle), first < middle);
        assertTrue(format("m:%d,l:%d", middle, last2), middle < last2);
        assertTrue(format("l1:%d,l2:%d", last1, last2), last1 < last2);
        assertTrue(format("l2:%d,l3:%d", last2, last3), last2 < last3);

        log.info("resultQueue:{}", resultQueue);
        List<Integer> results = resultQueue.stream().map(CompletableFuture::join).collect(toList());
        log.info("results:{}", results);
        System.out.println("test exit");
    }
}
