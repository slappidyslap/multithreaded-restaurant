package kg.musabaev;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
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
                case HANDLE_ORDER: { // продолжить проверку
                    Order orderFromManager = checkPreparedOrders();

                    boolean isMyOrder = checkIsMyOrder(orderFromManager);

                    if (!isMyOrder) {
                        action = WaiterAction.FIND_CLIENT;
                        break;
                    };

                    deliverPreparedOrder(orderFromManager);
                    CountDownLatch deliveredLatch = new CountDownLatch(1);
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
            foundTable.getWaiterLocker().lock();

            logger.info(format("%s waiting for %s-client's order occupying %s-table",
                    currentThreadName(),
                    foundTable.getOccupiedClientId(),
                    foundTable.getId()));

            while (foundTable.getClientOrder() == null) {
                foundTable.foodOrdered().await();
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
            throw new RuntimeException(Thread.currentThread().getName() + "interrupted", e);
        } finally {
            foundTable.getWaiterLocker().unlock();
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
        return ordersManager.pollReadyOrderForWaiter(this);
    }

    private boolean checkIsMyOrder(Order orderFromManager) {
        if (orderFromManager == null) return false;
        orderFromManager.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        return true;
    }

    private void deliverPreparedOrder(Order orderFromManager) {
        requireOrderStatus(orderFromManager, OrderStatus.OUT_FOR_DELIVERY);
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
