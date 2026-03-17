package spring.examples.tutorial.counter.service;

import org.springframework.stereotype.Service;

@Service
public class CounterService {
    private int hits = 1;

    public int getHits() {
        return hits++;
    }
}
