package kg.musabaev;

import java.util.Objects;

public class Order {

    private int id;
    private Client orderedClient;
    private Waiter assignedWaiter;
    private Chef assignedChef;

    private OrderStatus status;

    // cyclicbarrier офики пачкой несут
    public Order(Client orderedClient) {
        this.orderedClient = orderedClient;
        this.status = OrderStatus.PENDING_WAITER_ASSIGNMENT;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Client getOrderedClient() {
        return orderedClient;
    }

    public synchronized void setOrderedClient(Client orderedClient) {
        this.orderedClient = orderedClient;
    }

    public synchronized Waiter getAssignedWaiter() {
        return assignedWaiter;
    }

    public synchronized void setAssignedWaiter(Waiter assignedWaiter) {
        this.assignedWaiter = assignedWaiter;
    }

    public synchronized Chef getAssignedChef() {
        return assignedChef;
    }

    public synchronized void setAssignedChef(Chef assignedChef) {
        this.assignedChef = assignedChef;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderedClient=" + orderedClient +
                ", assignedWaiter=" + assignedWaiter +
                ", assignedChef=" + assignedChef +
                ", status=" + status +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
