package kg.musabaev;

import java.util.Random;

public class Chef {

    private final String name;

    public Chef(String name) {
        this.name = name;
    }

    public Task createTask(Order order) {
        return new Task(order);
    }

    public class Task implements Runnable {

        private final Order order;
        private Random random;

        public Task(Order order) {
            this.order = order;
            this.order.setAssignedChef(Chef.this);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(name + "-chef start order-" + order.getId());
            order.setStatus(OrderStatus.IN_PREPARATION);

            sleepChef();

            order.setStatus(OrderStatus.READY_FOR_PICKUP);
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
}
