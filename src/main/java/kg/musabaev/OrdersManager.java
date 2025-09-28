package kg.musabaev;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

// как терминал в пиццериях у кассы
public class OrdersManager {

    private final BlockingQueue<Order> queue;
    private final AtomicInteger count;

    public OrdersManager() {
        this.queue = new LinkedBlockingQueue<>();
        this.count = new AtomicInteger(0);
    }

    public synchronized void addOrder(Order order) {
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
        return count.getAndIncrement();
    }
}