package kg.musabaev.util;

import java.util.Random;

public class RunnerWithDelayAndRepeat {

    private final int runNTimes;
    private final int runWithRandomDelay;

    private RunnerWithDelayAndRepeat(int times, int delay) {
        this.runNTimes = times;
        this.runWithRandomDelay = delay;
    }

    public static Builder times(int times) {
        Builder rrb = new Builder();
        rrb.times(times);
        return rrb;
    }

    public static Builder withRandomDelayInMillis(int delay) {
        Builder rrb = new Builder();
        rrb.withRandomDelayInMillis(delay);
        return rrb;
    }

    public void run(Runnable runnable) {
        Random random = new Random();
        for (int i = 0; i < this.runNTimes; i++) {
            if (runWithRandomDelay < 0)
                throw new RuntimeException("runWithRandomDelay can not be lower than 0");
            int randomDelay = random.nextInt(this.runWithRandomDelay + 1);
            try {
                Thread.sleep(randomDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(Thread.currentThread().getName() + "interrupted", e);
            }
            runnable.run();
        }
    }

    public static class Builder {
        private int times = 1;
        private int delay = 0;

        public Builder times(int times) {
            this.times = times;
            return this;
        }

        public Builder withRandomDelayInMillis(int delayInMillis) {
            this.delay = delayInMillis;
            return this;
        }

        public RunnerWithDelayAndRepeat run(Runnable runnable) {
            RunnerWithDelayAndRepeat runner = new RunnerWithDelayAndRepeat(this.times, this.delay);
            runner.run(runnable);
            return runner;
        }

        private Builder() {
        }
    }
}