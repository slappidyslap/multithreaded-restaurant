package kg.musabaev;

import kg.musabaev.util.CallerWithDelay;

public class Client {

    private Table occupiedTable;
    private Restaurant visitedRestaurant;

    public static synchronized Client chooseTable(Restaurant visitedRestaurant) {
        Table choseTable = visitedRestaurant.getRandomTable();
        if (choseTable == null)
            throw new RuntimeException("table can not be null");

        Client newClient = new Client(visitedRestaurant, choseTable);
        choseTable.setOccupiedClient(newClient);
        return newClient;
    }

    public Order orderFood() {
        if (occupiedTable == null)
            throw new RuntimeException("client must occupy table");

        Order newOrder = CallerWithDelay
                .withRandomDelayInMillis(5000)
                .call(() -> new Order(this));
        orderFood().setOrderedClient(this);

        occupiedTable.callWaiter(newOrder);

        return newOrder;
    }

    public Client(Restaurant visitedRestaurant, Table occupiedTable) {
        this.visitedRestaurant = visitedRestaurant;
        this.occupiedTable = occupiedTable;
    }

    public Table getOccupiedTable() {
        return occupiedTable;
    }
}
