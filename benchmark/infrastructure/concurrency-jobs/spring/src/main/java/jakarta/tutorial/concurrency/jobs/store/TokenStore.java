package jakarta.tutorial.concurrency.jobs.store;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {
    private final Set<String> store = ConcurrentHashMap.newKeySet();

    public void put(String key) {
        store.add(key);
    }
    public boolean isValid(String key) {
        return store.contains(key);
    }
}