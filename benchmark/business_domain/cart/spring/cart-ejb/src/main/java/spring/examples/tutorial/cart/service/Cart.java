
package spring.examples.tutorial.cart.service;

import java.util.List;
import spring.examples.tutorial.cart.util.BookException;

public interface Cart {
    public void initialize(String person) throws BookException;

    public void initialize(
            String person,
            String id) throws BookException;

    public void addBook(String title);

    public void removeBook(String title) throws BookException;

    public List<String> getContents();

    public void remove();
}
