package kg.musabaev;

import kg.musabaev.util.Utils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static kg.musabaev.util.Utils.requireOrderStatus;

public class Waiter extends Thread {

    private final String firstName;
    private final Set<Order> orders;
    private final Set<Table> servingTables;
    private final Restaurant restaurant;
    private final OrdersManager ordersManager;

    public Waiter(String name, Restaurant restaurant) {
        super("waiter-" + name);
        this.firstName = name;
        this.orders = new HashSet<>();
        this.servingTables = new HashSet<>();
        this.restaurant = restaurant;
        this.ordersManager = restaurant.getOrdersManager();
    }

    @Override
    public void run() {
        while (true) { // FIXME цикл работает пока ресторан открыт
            Table foundTable = findTableAndClaimClient(); // 1. Ищем новых клиентов

            if (foundTable == null) continue; // FIXME поочередно искать стол и проверять ordersmanager

            // 2. Ждем когда клиенты сделают заказ
            Order clientOrder = waitForClientPlaceOrder(foundTable);

            // 3. Идем к терминалу и делаем добавляем заказ в систему
            acceptOrderAndAddToQueue(clientOrder);

            // ----------------

            Order orderFromManager = checkPreparedOrders();

            boolean isMyOrder = checkIsMyOrder(orderFromManager);

            if (!isMyOrder) continue;

            deliverPreparedOrder(orderFromManager);

        }

    }

    private Table findTableAndClaimClient() {
        for (Table table : restaurant.getTables()) {
            if (table.getOccupiedClient() == null) {
                table.setServingWaiter(this);
                // Если стол найден, то добавляем в блокнот (список обслуживаемых столиков)
                servingTables.add(table);
                return table;
            }
        }
        return null;
    }

    private Order waitForClientPlaceOrder(Table foundTable) {
        try {
            foundTable.getWaiterLocker().lock();

            foundTable.foodOrdered().await();
            Order clientOrder = foundTable.getClientOrder();
            if (clientOrder == null)
                throw new RuntimeException("order can not be null");
            // Записываем себе в блокнот (список заказов)
            orders.add(clientOrder);
            clientOrder.setAssignedWaiter(this);
            clientOrder.setStatus(OrderStatus.WAITING_FOR_KITCHEN_QUEUE);

            return clientOrder;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("RunnerWithDelayAndRepeat interrupted", e);
        } finally {
            foundTable.getWaiterLocker().unlock();
        }
    }

    private void acceptOrderAndAddToQueue(Order clientOrder) {
        if (ordersManager == null)
            throw new RuntimeException("orders manager must works");
        ordersManager.addOrderToIncomingQueue(clientOrder);
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
        // TODO
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
