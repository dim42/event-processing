package event.processing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class Consumer<V> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Consumer.class);

    private final BlockingQueue<TimeCallableEvent<V>> eventQueue;
    private final Queue<String> callableQueue;
    private final Queue<CompletableFuture<V>> resultQueue;

    public Consumer(BlockingQueue<TimeCallableEvent<V>> eventQueue, Queue<String> callableQueue, Queue<CompletableFuture<V>> resultQueue) {
        this.eventQueue = eventQueue;
        this.callableQueue = callableQueue;
        this.resultQueue = resultQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                final TimeCallableEvent<V> event = eventQueue.take();
                callableQueue.add(event.getCallable().toString());
                CompletableFuture<V> cf = supplyAsync(() -> {
                    try {
                        Callable<V> callable = event.getCallable();
                        return callable.call();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                });
                resultQueue.add(cf);
            } catch (InterruptedException e) {
                log.error("Process interrupted", e);
                Thread.currentThread().interrupt();// Reset/restore interrupted status
            }
        }
    }
}
