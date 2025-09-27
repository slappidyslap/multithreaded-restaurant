package kg.musabaev;

import java.util.Objects;

public class Order {

    private int id;
    private Client orderedClient;
    private Waiter assignedWaiter;
    private Chef assignedChef;
    private boolean isReady;
    // cyclicbarrier офики пачкой несут

    private Order() {
    }

    public Order(Client orderedClient) {
        this.orderedClient = orderedClient;
        this.isReady = false;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", orderedClient=" + orderedClient +
                ", assignedWaiter=" + assignedWaiter +
                ", assignedChef=" + assignedChef +
                ", isReady=" + isReady +
                '}';
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

    public void setOrderedClient(Client orderedClient) {
        this.orderedClient = orderedClient;
    }

    public Waiter getAssignedWaiter() {
        return assignedWaiter;
    }

    public void setAssignedWaiter(Waiter assignedWaiter) {
        this.assignedWaiter = assignedWaiter;
    }

    public Chef getAssignedChef() {
        return assignedChef;
    }

    public void setAssignedChef(Chef assignedChef) {
        this.assignedChef = assignedChef;
    }

    public boolean isReady() {
        return isReady;
    }

    public void setReady(boolean ready) {
        isReady = ready;
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
