package quarkus.tutorial.cart.util;

public class BookException extends Exception {
    private static final long serialVersionUID = 6274585742564840905L;

    public BookException() {}

    public BookException(String msg) {
        super(msg);
    }
}
