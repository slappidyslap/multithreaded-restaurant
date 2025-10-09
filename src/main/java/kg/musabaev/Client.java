package kg.musabaev;

import kg.musabaev.util.CallerWithDelay;

import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;

public class Client {

    private int id;
    private Table occupiedTable;
    private Restaurant visitedRestaurant;

    private final Logger logger = Logger.getLogger(Client.class.getName());

    public static synchronized Client chooseTable(Restaurant visitedRestaurant) {
        Table choseTable = visitedRestaurant.getRandomTable();
        int clientCount = visitedRestaurant.clientCount().getAndIncrement();
        requireNonNull(choseTable, "table can not be null");

        Client newClient = new Client(clientCount, visitedRestaurant, choseTable);
        choseTable.setOccupiedClient(newClient);
        return newClient;
    }

    public void orderFood() {
        logger.info(currentThread().getName() + " is choosing food");

        requireNonNull(this.occupiedTable, "client must occupy table");

        Order newOrder = CallerWithDelay
                .withRandomDelayInMillis(6000)
                .call(() -> new Order(this));
        this.occupiedTable.signalOrderPlaced(newOrder);

        logger.info(currentThread().getName() + " placed order in " + occupiedTable.getId() + "-table");
    }

    public Client(int id, Restaurant visitedRestaurant, Table occupiedTable) {
        this.visitedRestaurant = visitedRestaurant;
        this.occupiedTable = occupiedTable;
    }

    public int getId() {
        return id;
    }

    public Table getOccupiedTable() {
        return occupiedTable;
    }
}
