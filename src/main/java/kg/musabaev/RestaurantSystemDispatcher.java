package kg.musabaev;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.Integer.parseInt;
import static java.lang.System.getProperty;

public class RestaurantSystemDispatcher extends Thread {

    private ExecutorService chefsPool;
    private OrdersManager ordersManager;

    public RestaurantSystemDispatcher() {
        super("RestaurantSystemDispatcher");
        chefsPool = Executors.newFixedThreadPool(parseInt(getProperty("WORKING_CHEFS")));
    }

    @Override
    public void run() {
        Order orderToCook = ordersManager.take();
//            chefsPool.submit();
    }
}
