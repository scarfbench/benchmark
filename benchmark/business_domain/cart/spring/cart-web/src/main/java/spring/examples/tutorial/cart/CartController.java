package spring.examples.tutorial.cart;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import spring.examples.tutorial.cart.util.BookException;
import spring.examples.tutorial.cart.service.Cart;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final Cart cart;

    public CartController(Cart cart) {
        this.cart = cart;
    }

    /**
     * Initialize a new cart session.
     * 
     * POST /api/cart/initialize
     * 
     * Body: {"customerName": "John Doe", "customerId": "123"}
     */
    @PostMapping("/initialize")
    public ResponseEntity<Object> initializeCart(@RequestBody CustomerRequest request) {
        try {
            if (request.customerId != null && !request.customerId.isEmpty()) {
                cart.initialize(request.customerName, request.customerId);
            } else {
                cart.initialize(request.customerName);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Cart initialized successfully");
            response.put("customerName", request.customerName);

            return ResponseEntity.ok(response);
        } catch (BookException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Add a book to the cart.
     * 
     * POST /api/cart/books/{title}
     */
    @PostMapping("/books/{title}")
    public ResponseEntity<Object> addBook(@PathVariable("title") String title) {
        try {
            cart.addBook(title);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book added successfully");
            response.put("title", title);
            response.put("cartSize", cart.getContents().size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Remove a book from the cart.
     * 
     * DELETE /api/cart/books/{title}
     */
    @DeleteMapping("/books/{title}")
    public ResponseEntity<Object> removeBook(@PathVariable("title") String title) {
        try {
            cart.removeBook(title);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Book removed successfully");
            response.put("title", title);
            response.put("cartSize", cart.getContents().size());

            return ResponseEntity.ok(response);
        } catch (BookException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all books in the cart.
     * 
     * GET /api/cart/books
     */
    @GetMapping("/books")
    public ResponseEntity<Object> getBooks() {
        try {
            List<String> contents = cart.getContents();

            Map<String, Object> response = new HashMap<>();
            response.put("books", contents);
            response.put("count", contents.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Clear the cart and end the session.
     * 
     * DELETE /api/cart
     */
    @DeleteMapping
    public ResponseEntity<Object> clearCart(HttpSession session) {
        try {
            // Simulate EJB @Remove by clearing the session and destroying the bean
            session.invalidate();

            return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Health check endpoint.
     * 
     * GET /api/cart/health
     */
    @GetMapping("/health")
    public ResponseEntity<Object> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "cart-api"));
    }

    // Request/Response DTOs
    public static class CustomerRequest {
        public String customerName;
        public String customerId;
    }

}
