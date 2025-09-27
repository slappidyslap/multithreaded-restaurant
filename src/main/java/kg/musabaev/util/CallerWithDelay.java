package kg.musabaev.util;

import java.util.Random;
import java.util.concurrent.Callable;

public class CallerWithDelay {

    private final int runWithRandomDelay;

    private CallerWithDelay(int delay) {
        this.runWithRandomDelay = delay;
    }

    public static CallerWithDelay withRandomDelayInMillis(int delay) {
        return new CallerWithDelay(delay);
    }

    public <T> T call(Callable<T> callable) {
        Random random = new Random();

        if (runWithRandomDelay < 0)
            throw new RuntimeException("runWithRandomDelay can not be lower than 0");
        int randomDelay = random.nextInt(this.runWithRandomDelay + 1);
        try {
            Thread.sleep(randomDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("CallerWithDelay interrupted during delay", e);
        }
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}