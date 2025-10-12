package kg.musabaev;

public class Main {
    public static void main(String[] args) {
        System.setProperty("AVAILABLE_TABLES", "6");
        System.setProperty("WORKING_WAITERS", "4");
        System.setProperty("WORKING_CHEFS", "2");

        Restaurant dodoPizza = new Restaurant();
        ClientThreadGenerator clientThreadGenerator = new ClientThreadGenerator(dodoPizza);

//        TODO добавить чтобы клиентаы рандомно приходили
//        TODO добавить время день итд
//        dodoPizza.open();
//        dodoPizza.waitersStartWork();
//        dodoPizza.startDispatcher();
//        clientThreadGenerator.start();
//        dodoPizza.close();
    }
}