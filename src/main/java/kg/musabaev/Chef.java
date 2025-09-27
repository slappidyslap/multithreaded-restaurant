package kg.musabaev;

import java.util.Random;

public class Chef implements Runnable {

    private String name;
    private Order order;
    private Random random;

    public Chef(String name) {
        this.name = name;
    }

    @Override
    public void run() {
        Thread.currentThread().setName(name + "-chef");
        sleepChef();

    }

    private void sleepChef() {
        try {
            if (random == null) random = new Random(System.currentTimeMillis());
            int max = 15000;
            int min = 10000;
            int randomDelay = random.nextInt(max - min + 1) + min;
            Thread.sleep(randomDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chef interrupted", e);
        }
    }
}
