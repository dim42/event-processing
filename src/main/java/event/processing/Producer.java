package event.processing;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class Producer<E extends Comparable<? super E>, V> {
    private final BlockingQueue<E> queue;

    public Producer(BlockingQueue<E> queue) {
        this.queue = queue;
    }

    public void add(LocalDateTime time, Callable<V> callable) {
        queue.add((E) new TimeCallableEvent<>(time, callable));
    }
}
