package kg.musabaev;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// как терминал в пиццериях у кассы
public class OrdersManager {

    private static final OrdersManager INSTANCE = new OrdersManager();

    public static final AtomicInteger count = new AtomicInteger(0);
    private final BlockingQueue<Order> queue;

    private OrdersManager() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public static synchronized OrdersManager getInstance() {
        return INSTANCE;
    }

    public void addOrder(Order order) {
        if (queue.offer(order)) {
            order.setId(count.get());
            count.incrementAndGet();
        }
    }

    public Order take() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("OrdersManager interrupted", e);
        }
    }

    public int getCount() {
        return count.get();
    }
}