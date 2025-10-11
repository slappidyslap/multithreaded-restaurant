package kg.musabaev;

public enum WaiterAction {
    FIND_CLIENT,
    HANDLE_PREPARED_ORDER,
    HANDLE_CHECKOUT;

    private static final WaiterAction[] vals = values();

    public WaiterAction next() {
        return vals[this.ordinal() + 1 % vals.length];
    }

}
