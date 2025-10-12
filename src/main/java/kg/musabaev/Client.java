package kg.musabaev;

import kg.musabaev.util.CallerWithDelay;
import kg.musabaev.util.Utils;

import java.util.Random;
import java.util.logging.Logger;

import static java.lang.Thread.currentThread;
import static java.util.Objects.requireNonNull;
import static kg.musabaev.util.Utils.currentThreadName;

public class Client {

    private final int id;
    private final Table occupiedTable;
    private final Restaurant visitedRestaurant;
    private Random random;
    private final Logger logger;

    public Client(int id, Restaurant visitedRestaurant, Table occupiedTable) {
        this.id = id;
        this.visitedRestaurant = visitedRestaurant;
        this.occupiedTable = occupiedTable;
        this.logger = Logger.getLogger(Client.class.getName());
    }

    public static synchronized Client chooseTable(Restaurant visitedRestaurant) {
        Table choseTable = visitedRestaurant.getRandomTable();
        int clientCount = visitedRestaurant.clientCount().getAndIncrement();
        requireNonNull(choseTable, "table can not be null");

        Client newClient = new Client(clientCount, visitedRestaurant, choseTable);
        choseTable.setOccupiedClient(newClient);
        return newClient;
    }

    public void orderFood() {
        logger.info(currentThreadName() + " is choosing food");

        requireNonNull(this.occupiedTable, "client must occupy table");

        Order newOrder = CallerWithDelay
                .withRandomDelayInMillis(6000)
                .call(() -> new Order(this));
        this.occupiedTable.signalOrderPlaced(newOrder);

        logger.info(currentThreadName() + " placed order in " + occupiedTable.getId() + "-table");
    }

    public void awaitDeliveredOrder() {
        occupiedTable.getLocker().lock();
        try {
            occupiedTable.orderDelivered().await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(currentThread() + "interrupted", e);
        } finally {
            occupiedTable.getLocker().unlock();
        }
    }

    public void startEating() {
        logger.info(currentThreadName() + " starts eating " + occupiedTable.getClientOrderId() + "-order");
        Utils.delay(random, 20_000, 40_000);
        occupiedTable.isClientFinishedEating().set(true);
        logger.info(currentThreadName() + " finished eating " + occupiedTable.getClientOrderId() + "-order");
    }

    public void payForOrder() {
        logger.info(currentThreadName() + " paid for " + occupiedTable.getClientOrderId() + "-order");
    }

    public void leaveRestaurant() {
        logger.info(currentThreadName() + "leaves our restaurant");
        occupiedTable.setOccupiedClient(null);
        occupiedTable.getClientOrder().setOrderedClient(null);
    }

    public int getId() {
        return id;
    }

    public Table getOccupiedTable() {
        return occupiedTable;
    }
}
