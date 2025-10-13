package kg.musabaev;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.requireNonNull;
import static kg.musabaev.util.Utils.currentThreadName;
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
            throw new RuntimeException(currentThreadName() + "interrupted", e);
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

    /**
     * Извлекает и удаляет из очереди следующий готовый {@link Order}, если он принадлежит
     * указанному {@link Waiter}.
     * <br>
     * Если заказ в начале очереди отсутствует ({@code null}) или принадлежит другому официанту,
     * метод возвращает {@code null}.
     * В противном случае заказ извлекается из очереди и возвращается вызывающему потоку.
     *
     * @param waiter официант, пытающийся получить свой готовый заказ; не должен быть {@code null}
     * @return готовый {@link Order}, принадлежащий указанному официанту,
     *         или {@code null}, если очередь пуста,
     *         либо следующий заказ в ней предназначен другому официанту
     * @throws IllegalStateException если статус заказа не соответствует {@link OrderStatus#PREPARED}
     * @throws IllegalStateException если извлечённый и просмотренный заказы не совпадают (нарушение целостности очереди)
     */
    public synchronized Order pollReadyOrderForWaiter(Waiter waiter) {
        Order peekedOrder = readyOrderQueue.peek();
        if (peekedOrder == null) return null;
        else if (!peekedOrder.getAssignedWaiter().equals(waiter)) return null;
        else if (!waiter.getOrders().contains(peekedOrder)) return null;

        requireOrderStatus(peekedOrder, OrderStatus.PREPARED);

        Order polledOrder = readyOrderQueue.poll();
        if(polledOrder != peekedOrder) throw new IllegalStateException("taken and peeked order from queue must be same");
        return polledOrder;
    }
}
