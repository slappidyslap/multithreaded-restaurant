package kg.musabaev;

import kg.musabaev.util.CallerWithDelay;

import java.util.List;
import java.util.Random;

public class Client {

    private Table occupiedTable;
    private Restaurant visitedRestaurant;

    public static synchronized Client chooseTable(Restaurant visitedRestaurant) {
        List<Table> allTables = visitedRestaurant.getTables();
        if (allTables == null | allTables.isEmpty())
            throw new RuntimeException("tables must be initialized and not be empty");

        Random rnd = new Random(System.currentTimeMillis());

        Table choseTable = allTables.get(rnd.nextInt(allTables.size()));
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
