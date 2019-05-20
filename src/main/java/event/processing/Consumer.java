package event.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Consumer<V> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);
    private final BlockingQueue<TimeCallableEvent> queue;
    private final Queue<String> resultQueue;

    public Consumer(BlockingQueue<TimeCallableEvent> queue, Queue<String> resultQueue) {
        this.queue = queue;
        this.resultQueue = resultQueue;
    }

    @Override
    public void run() {
        process();
    }

    public void process() {
        while (!Thread.currentThread().isInterrupted()) {
            TimeCallableEvent event = null;
            try {
                event = queue.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            if (event == null) {
                continue;
            }
            resultQueue.add(event.getCallable().toString());

            try {
                FutureTask<V> futureTask = new FutureTask<>(event.getCallable());
                futureTask.run();
                V result = futureTask.get();
                log.info("Result:" + result);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
