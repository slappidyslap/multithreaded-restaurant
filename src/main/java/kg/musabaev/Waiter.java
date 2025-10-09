package kg.musabaev;

import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static kg.musabaev.util.Utils.currentThreadName;
import static kg.musabaev.util.Utils.requireOrderStatus;

public class Waiter extends Thread {

    private final String firstName;
    private final Set<Order> orders;
    private final Set<Table> servingTables;
    private final Restaurant restaurant;
    private final OrdersManager ordersManager;
    private final Logger logger;
    private WaiterAction action;
    private Random random;

    public Waiter(String name, Restaurant restaurant) {
        super(name + "-waiter");
        this.firstName = name;
        this.orders = new HashSet<>();
        this.servingTables = new HashSet<>();
        this.restaurant = restaurant;
        this.ordersManager = restaurant.getOrdersManager();
        this.logger = Logger.getLogger(Waiter.class.getName());
        this.action = WaiterAction.FIND_CLIENT;
    }

    @Override
    public void run() {
        while (restaurant.isOpen()) {
            switch (action) {
                case FIND_CLIENT: {
                    Table foundTable = findAndAssignTable(); // 1. Ищем новых клиентов
                    if (foundTable == null) {
                        action = WaiterAction.HANDLE_ORDER;
                        break;
                    }
                    // 2. Ждем когда клиенты сделают заказ
                    Order clientOrder = awaitClientOrder(foundTable);
                    // 3. Идем к терминалу и делаем добавляем заказ в систему
                    addOrderToQueue(clientOrder);
                    break;
                }
                case HANDLE_ORDER: {
                    Order orderFromManager = checkPreparedOrders();

                    boolean isMyOrder = isMyOrder(orderFromManager);

                    if (!isMyOrder) {
                        action = WaiterAction.FIND_CLIENT;
                        break;
                    };

                    deliverPreparedOrder(orderFromManager);
                    break;
                }
            }
        }
    }

    private Table findAndAssignTable() {
        logger.info(currentThreadName() + " is finding table to serve client");
        for (Table table : restaurant.getTables()) {
            if (table.tryAssign(this)) {
                // Если стол найден, то добавляем в блокнот (список обслуживаемых столиков)
                this.servingTables.add(table);

                logger.info(currentThreadName() + " found " + table.getId() + "-table");

                return table;
            }
        }
        return null;
    }

    private Order awaitClientOrder(Table foundTable) {
        try {
            foundTable.getLocker().lock();

            logger.info(format("%s waiting for %s-client's order occupying %s-table",
                    currentThreadName(),
                    foundTable.getOccupiedClientId(),
                    foundTable.getId()));

            while (foundTable.getClientOrder() == null) {
                foundTable.clientOrdered().await();
            }
            Order clientOrder = foundTable.getClientOrder();

            // Записываем себе в блокнот (список заказов)
            orders.add(clientOrder);
            clientOrder.setAssignedWaiter(this);
            clientOrder.setStatus(OrderStatus.WAITING_FOR_KITCHEN_QUEUE);

            logger.info(format("%s accepted %s-client's order occupying %s-table",
                    currentThreadName(),
                    foundTable.getOccupiedClientId(),
                    foundTable.getId()));

            return clientOrder;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(currentThreadName() + "interrupted", e);
        } finally {
            foundTable.getLocker().unlock();
        }
    }

    private void addOrderToQueue(Order clientOrder) {
        requireNonNull(ordersManager, "orders manager must works");
        ordersManager.addOrderToIncomingQueue(clientOrder);

        logger.info(format("%s added %s-client's order with id %s to incoming order queue",
                currentThreadName(),
                clientOrder.getOrderedClientId(),
                clientOrder.getId()));
    }

    private Order checkPreparedOrders() {
        logger.info(currentThreadName() + " is checking orders manager for prepared order");
        Order order = ordersManager.pollReadyOrderForWaiter(this);
        return order;
    }

    private boolean isMyOrder(Order order) {
        if (order == null) {
            logger.info(currentThreadName() + " checked orders manager and there no order assigned to him");
            return false;
        }
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        logger.info(format("%s saw order assigned to him and is going to deliver to %d-table",
                currentThreadName(),
                order.getClientOccupiedTableId()));
        return true;
    }

    private void deliverPreparedOrder(Order order) {
        requireOrderStatus(order, OrderStatus.OUT_FOR_DELIVERY);

        logger.info(format("%s picked up %s-order and is going to %s-table occupying by %s-client",
                currentThreadName(),
                order.getId(),
                order.getClientOccupiedTableId(),
                order.getOrderedClientId()));

        order.getClientOccupiedTable().getLocker().lock();
        deliverOrder();
        order.getClientOccupiedTable().orderDelivered().signal();
        order.setStatus(OrderStatus.DELIVERED);
        order.getClientOccupiedTable().getLocker().unlock();

        logger.info(format("%s delivered %s-order to going to %s-table occupying by %s-client",
                currentThreadName(),
                order.getId(),
                order.getClientOccupiedTableId(),
                order.getOrderedClientId()));
    }

    // simulate delivering
    private void deliverOrder() {
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

    public String getFirstName() {
        return firstName;
    }

    public Set<Order> getOrders() {
        return orders;
    }

    @Override
    public String toString() {
        return "Waiter{" +
                "name='" + firstName + '\'' +
                ", orders=" + orders +
                ", servingTables=" + servingTables +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Waiter waiter = (Waiter) o;
        return Objects.equals(firstName, waiter.firstName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(firstName);
    }
}
