package jakarta.tutorial.concurrency.jobs.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Qualifier;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.eclipse.microprofile.context.ThreadContext;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Qualifier @Retention(RUNTIME) @Target({TYPE, METHOD, FIELD, PARAMETER})
@interface High {}
@Qualifier @Retention(RUNTIME) @Target({TYPE, METHOD, FIELD, PARAMETER})
@interface Low {}
@ApplicationScoped
public class ExecutorProducers {

    // High priority: more threads, bigger queue
    @Produces @ApplicationScoped @High
    ManagedExecutor high() {
        return ManagedExecutor.builder()
                .maxAsync(32)            // max concurrent async tasks
                .propagated(ThreadContext.ALL_REMAINING) // propagate contexts
                .build();
    }

    // Low priority: fewer threads
    @Produces @ApplicationScoped @Low
    ManagedExecutor low() {
        return ManagedExecutor.builder()
                .maxAsync(8)
                .propagated(ThreadContext.ALL_REMAINING)
                .build();
    }
}