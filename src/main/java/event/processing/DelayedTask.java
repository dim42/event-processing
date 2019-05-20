package event.processing;

import java.util.concurrent.Callable;

public class DelayedTask implements Callable<Integer> {

    private final int id;

    public DelayedTask(int id) {
        this.id = id;
    }

    @Override
    public Integer call() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
