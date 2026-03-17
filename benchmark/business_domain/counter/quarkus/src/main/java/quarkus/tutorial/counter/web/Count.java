package quarkus.tutorial.counter.web;

import java.io.Serializable;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import quarkus.tutorial.counter.ejb.CounterBean;

@Named
@SessionScoped // not exactly ConversationScoped, but it's not supported in Quarkus
public class Count implements Serializable {
    @Inject
    private CounterBean counterBean;

    private int hitCount;

    public Count() {
        this.hitCount = 0;
    }

    public int getHitCount() {
        hitCount = counterBean.getHits();
        return hitCount;
    }

    public void setHitCount(int newHits) {
        this.hitCount = newHits;
    }
}
