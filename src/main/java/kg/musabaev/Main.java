package kg.musabaev;

public class Main {
    public static void main(String[] args) {
        System.setProperty("AVAILABLE_TABLES", "5");
        System.setProperty("WORKING_WAITERS", "5");
        System.setProperty("WORKING_CHEFS", "2");

        OrdersManager ordersManager = new OrdersManager();
        Restaurant dodoPizza = new Restaurant(ordersManager);
        RestaurantSystemDispatcher dispatcher = new RestaurantSystemDispatcher(dodoPizza);

//        dodoPizza.open();
//        dodoPizza.waitersStartWork();
//        dispatcher.start();
    }
}