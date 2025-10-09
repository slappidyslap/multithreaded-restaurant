package kg.musabaev;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static kg.musabaev.util.Utils.requireOrderStatus;

// как терминал в пиццериях у кассы
public class OrdersManager {

    private final Restaurant restaurant;
    private final BlockingQueue<Order> incomingOrderQueue;
    private final BlockingQueue<Order> readyOrderQueue;
    private final AtomicInteger orderCount;

    public OrdersManager(Restaurant restaurant) {
        this.restaurant = restaurant;
        this.incomingOrderQueue = new LinkedBlockingQueue<>();
        this.readyOrderQueue = new LinkedBlockingQueue<>();
        this.orderCount = new AtomicInteger(0);
    }

    // FIXME тут надо synchronized?
    public synchronized void addOrderToIncomingQueue(Order order) {
        requireOrderStatus(order, OrderStatus.WAITING_FOR_KITCHEN_QUEUE);
        requireNonNull(order.getOrderedClient(), "adding order to incoming queue requires non null client");
        requireNonNull(order.getAssignedWaiter(), "adding order to incoming queue requires non null waiter");

        try {
            incomingOrderQueue.add(order);
            order.setId(orderCount.getAndIncrement());
            order.setStatus(OrderStatus.QUEUED_FOR_COOKING);
        } catch (Exception e) {
            throw new RuntimeException("could not add order to incoming queue", e);
        }
    }

    public synchronized Order takeOrderFromIncomingQueue() {
        try {
            Order order = incomingOrderQueue.take();

            requireOrderStatus(order, OrderStatus.QUEUED_FOR_COOKING);

            return order;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(Thread.currentThread().getName() + "interrupted", e);
        }
    }

    public synchronized void addOrderToReadyQueue(Order order) {
        requireOrderStatus(order, OrderStatus.PREPARED);
        requireNonNull(order.getOrderedClient(), "adding order to incoming queue requires non null client");
        requireNonNull(order.getAssignedWaiter(), "adding order to incoming queue requires non null waiter");
        requireNonNull(order.getAssignedChef(), "adding order to incoming queue requires non null chef");

        try {
            readyOrderQueue.add(order);
            order.setStatus(OrderStatus.READY_FOR_PICKUP);
        } catch (Exception e){
            throw new RuntimeException("could not add order to ready queue", e);
        }
    }

    public synchronized Order pollReadyOrderForWaiter(Waiter waiter) {
        Order peekedOrder = readyOrderQueue.peek();

        requireNonNull(peekedOrder);
        requireOrderStatus(peekedOrder, OrderStatus.PREPARED);
        requireOrderContainsInWaiter(peekedOrder, waiter);

        if (!peekedOrder.getAssignedWaiter().equals(waiter)) return null;
        return readyOrderQueue.poll();
    }

    public synchronized Order peekOrderFromReadyQueue() {
        return readyOrderQueue.peek();
    }

    private void requireOrderContainsInWaiter(Order order, Waiter waiter) {
        if (waiter.getOrders().contains(order)) return;
        throw new IllegalStateException("order " + order + "must contains in waiter " + waiter + "'s notepad ");
    }
}