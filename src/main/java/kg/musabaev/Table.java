package kg.musabaev;

import java.util.Objects;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static java.util.Objects.requireNonNull;
import static kg.musabaev.util.Utils.requireBeNull;

public class Table {

    private final int id;
    private Client occupiedClient;
    private Waiter servingWaiter;
    private Order clientOrder;
    private final ReentrantLock locker;
    private final Condition clientOrdered;
    private final Condition orderDelivered;


    public Table(int id) {
        this.id = id;
        this.occupiedClient = null;
        this.servingWaiter = null;
        this.clientOrder = null;
        this.locker = new ReentrantLock();
        this.clientOrdered = locker.newCondition();
        this.orderDelivered = locker.newCondition();
    }

    public boolean tryAssign(Waiter servingWaiter) {
        locker.lock();

        if (this.occupiedClient != null && servingWaiter != null) {
            this.servingWaiter = servingWaiter;
            locker.unlock();
            return true;
        }
        locker.unlock();
        return false;
    }

    // офик должен к этому момент "представиться" с клиентами и обслуживать
    public void signalOrderPlaced(Order order) {
        requireNonNull(this.occupiedClient, "table must be busy to place order");
        requireNonNull(order, "order can not be null");
        requireNonNull(this.servingWaiter, "why is no one serving " + id + "-table?");
        requireBeNull(this.clientOrder, "previous order must be completed");

        locker.lock();
        this.clientOrder = order;
        clientOrdered.signal();
        locker.unlock();
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

    public Condition clientOrdered() {
        return clientOrdered;
    }

    public Condition orderDelivered() {
        return orderDelivered;
    }

    public ReentrantLock getLocker() {
        return locker;
    }

    public Order getClientOrder() {
        return clientOrder;
    }

    public Client getOccupiedClient() {
        return occupiedClient;
    }

    public void setOccupiedClient(Client occupiedClient) {
        this.occupiedClient = occupiedClient;
    }

    public int getOccupiedClientId() {
        return this.occupiedClient.getId();
    }

    public Waiter getServingWaiter() {
        return servingWaiter;
    }

    public void setServingWaiter(Waiter servingWaiter) {
        this.servingWaiter = servingWaiter;
    }
}
