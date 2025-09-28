package kg.musabaev;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

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
        while (true) {
            Table foundTable = findTableAndClaimClient(); // 1. Ищем новых клиентов
            if (foundTable == null) continue;

            servingTables.add(foundTable); // 2. Если стол найден, то добавляем в блокнот (список обслуживаемых столиков)

            Order clientOrder;
            try {
                foundTable.getWaiterLocker().lock();

                foundTable.foodOrdered().await(); // 3. Ждем когда клиенты сделают заказ
                clientOrder = foundTable.getClientOrder();
                if (clientOrder == null)
                    throw new RuntimeException("order can not be null");
                orders.add(clientOrder); // 4. Записываем себе в блокнот (список заказанных продуктов)
                clientOrder.setAssignedWaiter(this);
                clientOrder.setStatus(OrderStatus.WAITING_FOR_KITCHEN_QUEUE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("RunnerWithDelayAndRepeat interrupted", e);
            } finally {
                foundTable.getWaiterLocker().unlock();
            }

            if (ordersManager == null)
                throw new RuntimeException("orders manager must works");
            ordersManager.addOrder(clientOrder); // 5. Идем к терминалу и делаем добавляем заказ в систему
            clientOrder.setStatus(OrderStatus.QUEUED_FOR_COOKING);

            // 6. *Ждем* пока повар не приготовит заказ (TODO щяс бы замутить реактившину)


        }

    }

    private Table findTableAndClaimClient() {
        for (Table table : restaurant.getTables()) {
            if (table.getOccupiedClient() == null) {
                table.setServingWaiter(this);
                return table;
            }
        }
        return null;
    }

    public String getFirstName() {
        return firstName;
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
