package event.processing;

import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

public class Producer<E extends Comparable, C> implements Runnable {
    private final BlockingQueue<E> queue;
    private LocalDateTime time;
    private Callable<C> callable;

    public Producer(BlockingQueue<E> queue) {
        this.queue = queue;
    }

    public void add(LocalDateTime time, Callable<C> callable) {
        this.time = time;
        this.callable = callable;
    }

    @Override
    public void run() {
        queue.add((E) new TimeCallableEvent(time, callable));
    }
}
