package kg.musabaev;

import kg.musabaev.util.RunnerWithDelayAndRepeat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

public class Restaurant {

    private final OrdersManager ordersManager;
    private final RestaurantSystemDispatcher dispatcher;
    private final List<Waiter> waiters;
    private final List<Chef> chefs;
    private final List<Client> clients;
    private final List<Table> tables;
    private Random random;

    public Restaurant() {
        ordersManager = new OrdersManager(this);
        dispatcher = new RestaurantSystemDispatcher(this);
        clients = new ArrayList<>();
        waiters = IntStream
                .rangeClosed(1, parseInt(getProperty("WORKING_WAITERS")))
                .mapToObj(Integer::toString)
                .map(i -> new Waiter(i, this))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList
                ));
        chefs = IntStream
                .rangeClosed(1, parseInt(getProperty("WORKING_CHEFS")))
                .mapToObj(Integer::toString)
                .map(i -> new Chef(i, this))
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList
                ));
        tables = IntStream
                .rangeClosed(1, parseInt(getProperty("AVAILABLE_TABLES")))
                .mapToObj(Table::new)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    public void open() {
        RunnerWithDelayAndRepeat
                .withRandomDelayInMillis(2000)
                .times(5)
                .run(() -> {
                    Client newClient = Client.chooseTable(this);
                    newClient.orderFood();
                });
    }

    public void waitersStartWork() {
        waiters.forEach(Thread::start);
    }

    public void startDispatcher() {
        dispatcher.start();
//        dispatcher.join();
    }

    public void close() {

    }

    public List<Table> getTables() {
        return tables;
    }

    public Table getRandomTable() {
        if (random == null) random = new Random();
        return tables.get(random.nextInt(tables.size()));
    }

    public Chef getRandomChef() {
        if (random == null) random = new Random();
        return chefs.get(random.nextInt(chefs.size()));
    }

    public OrdersManager getOrdersManager() {
        return ordersManager;
    }
}
