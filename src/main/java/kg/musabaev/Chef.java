package kg.musabaev;

import java.util.Random;
import java.util.logging.Logger;

import static kg.musabaev.util.Utils.currentThreadName;

public class Chef {

    private final String name;
    private final Restaurant restaurant;
    private final OrdersManager ordersManager;
    private final Logger logger = Logger.getLogger(Chef.class.getName());

    public Chef(String name, Restaurant restaurant) {
        this.name = name;
        this.restaurant = restaurant;
        this.ordersManager = restaurant.getOrdersManager();
    }

    public String getName() {
        return name;
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
            Thread.currentThread().setName(name + "-chef");

            logger.info(currentThreadName() + " started to cook " + order.getId() + "-order");

            order.setStatus(OrderStatus.IN_PREPARATION);
            cookOrder(order);
            order.setStatus(OrderStatus.PREPARED);
            ordersManager.addOrderToReadyQueue(order);

            logger.info(currentThreadName() + " cooked " + order.getId() + "-order and added to ready order queue");
        }

        private void cookOrder(Order _) {
            try {
                if (random == null) random = new Random(System.currentTimeMillis());
                int max = 15000;
                int min = 10000;
                int randomDelay = random.nextInt(max - min + 1) + min;
                Thread.sleep(randomDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(Thread.currentThread().getName() + "interrupted", e);
            }
        }
    }
}
