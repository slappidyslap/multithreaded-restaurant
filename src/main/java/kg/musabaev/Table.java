package kg.musabaev;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Table {

    private final int id;
    private Client occupiedClient;
    private Waiter servingWaiter;
    private Order clientFoodOrder;
    private final ReentrantLock waiterLocker;
    private final Condition foodOrdered;

    public Table(int id) {
        this.id = id;
        this.occupiedClient = null;
        this.servingWaiter = null;
        this.clientFoodOrder = null;
        this.waiterLocker = new ReentrantLock();
        this.foodOrdered = waiterLocker.newCondition();
    }

    // офик должен к этому момент "представиться" с клиентами и обслуживать
    public void callWaiter(Order order) {
        if (occupiedClient == null)
            throw new RuntimeException("table must be busy to place order");
        if (order == null)
            throw new RuntimeException("order can not be null");
        if (clientFoodOrder != null)
            throw new RuntimeException("previous order must be completed");
        if (servingWaiter == null)
            throw new RuntimeException("why is no one serving " + id + "-table?");

        waiterLocker.lock();

        foodOrdered.signal();
        clientFoodOrder = order;

        waiterLocker.unlock();
    }
    @Override
    public String toString() {
        return "Table{" +
                "id=" + id +
                ", occupiedClient=" + occupiedClient +
                ", servingWaiter=" + servingWaiter +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Table table = (Table) o;
        return id == table.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public int getId() {
        return id;
    }

    public Condition foodOrdered() {
        return foodOrdered;
    }

    public Order getClientFoodOrder() {
        return clientFoodOrder;
    }

    public Client getOccupiedClient() {
        return occupiedClient;
    }

    public void setOccupiedClient(Client occupiedClient) {
        this.occupiedClient = occupiedClient;
    }

    public Waiter getServingWaiter() {
        return servingWaiter;
    }

    public void setServingWaiter(Waiter servingWaiter) {
        this.servingWaiter = servingWaiter;
    }
}
