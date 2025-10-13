package kg.musabaev;

import kg.musabaev.util.Utils;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static kg.musabaev.util.Utils.*;

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
                        action = action.next();
                        break;
                    }
                    // 2. Ждем когда клиенты сделают заказ
                    Order clientOrder = awaitClientOrder(foundTable);
                    // 3. Идем к терминалу и делаем добавляем заказ в систему
                    addOrderToQueue(clientOrder);
                    break;
                }
                case HANDLE_PREPARED_ORDER: {
                    Order orderFromManager = checkPreparedOrders();

                    boolean isMyOrder = isMyOrder(orderFromManager);

                    if (!isMyOrder) {
                        action = action.next();
                        break;
                    }

                    deliverPreparedOrder(orderFromManager);
                    break;
                } case HANDLE_CHECKOUT: {
                    List<Table> tablesWhereClientsFinishedEating = checkClientsFinishedEating();

                    if (tablesWhereClientsFinishedEating.isEmpty()) {
                        action = action.next();
                        break;
                    }

                    handleCheckouts(tablesWhereClientsFinishedEating);
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
        requireEqualRefs(foundTable.getOccupiedClient(), foundTable.getClientOrder().getOrderedClient());

        try {
            foundTable.getLocker().lock();

            logger.info(format("%s is waiting for %s-client's order occupying %s-table",
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
        return ordersManager.pollReadyOrderForWaiter(this);
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
        Utils.delay(random, 2000, 4000);
    }

    private List<Table> checkClientsFinishedEating() {
        logger.info(currentThreadName() + " is checking tables where clients finished eating");

        if (servingTables.isEmpty()) {
            logger.info(currentThreadName() + " hasn't any table to handle checkout");
            return Collections.emptyList();
        }
        List<Table> tablesWhereClientsFinishedEating = new ArrayList<>();
        for (Table table : servingTables) {
            requireOrderStatus(table.getClientOrder(), OrderStatus.DELIVERED);

            boolean isClientFinishedEating = table.isClientFinishedEating().get();
            if (isClientFinishedEating) {
                tablesWhereClientsFinishedEating.add(table);
            }
        }
        logger.info(format("%s found %s tables where clients finished eating. tables ids: %s",
                currentThreadName(),
                tablesWhereClientsFinishedEating.size(),
                tablesWhereClientsFinishedEating.stream().map(Table::getId).collect(Collectors.toList())));
        return tablesWhereClientsFinishedEating;
    }

    private void handleCheckouts(List<Table> tables) {
        logger.info(format("%s starts handling checkout for tables: %s",
                currentThreadName(),
                tables.stream().map(Table::getId).collect(Collectors.toList())
                ));
        for (Table table : tables) {
            table.getClientOrder().setStatus(OrderStatus.COMPLETED);
            logger.info(format("%s checkout money by %d-client occupied %d-table",
                    currentThreadName(),
                    table.getOccupiedClientId(),
                    table.getId()));
            cleanTable(table);
        }
        tables.forEach(t -> t.getClientOrder().setStatus(OrderStatus.COMPLETED));
    }

    private void cleanTable(Table table) {
        table.setOccupiedClient(null);
        this.servingTables.remove(table);
        table.setServingWaiter(null);
        this.orders.remove(table.getClientOrder());
        table.setClientOrder(null);
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
