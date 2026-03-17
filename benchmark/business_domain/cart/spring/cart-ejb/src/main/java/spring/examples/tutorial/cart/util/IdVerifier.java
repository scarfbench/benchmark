package spring.examples.tutorial.cart.util;

public class IdVerifier {
    public IdVerifier() {}

    public boolean validate(String id) {
        boolean result = true;

        for (int i = 0; i < id.length(); i++) {
            if (Character.isDigit(id.charAt(i)) == false) {
                result = false;
            }
        }

        return result;
    }
}
