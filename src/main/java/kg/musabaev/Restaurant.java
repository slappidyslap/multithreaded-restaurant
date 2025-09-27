package kg.musabaev;

import kg.musabaev.util.RunnerWithDelayAndRepeat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

public class Restaurant {

    private List<Waiter> waiters;
    private List<Chef> chefs;
    private List<Client> clients;
    private final List<Table> tables;

    public Restaurant() {
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
                .map(Chef::new)
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        Collections::unmodifiableList
                ));
        tables = IntStream
                .rangeClosed(1, parseInt(getProperty("AVAILABLE_TABLES")))
                .mapToObj(Table::new)
                .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    @Debug
    void open() {
        RunnerWithDelayAndRepeat
                .withRandomDelayInMillis(2000)
                .times(5)
                .run(() -> {
                    Client newClient = Client.chooseTable(this);
                    newClient.orderFood();
                });
    }

    void waitersStartWork() {
        waiters.forEach(Thread::start);
    }

    public List<Table> getTables() {
        return tables;
    }


    @Debug
    void test() {
        System.out.println("ыы");
    }
}
