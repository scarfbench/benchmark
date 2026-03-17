package jakarta.tutorial.concurrency.jobs.web;


import jakarta.tutorial.concurrency.jobs.exec.High;
import jakarta.tutorial.concurrency.jobs.exec.Low;
import jakarta.tutorial.concurrency.jobs.store.TokenStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.concurrent.ListenableFutureTask;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/webapi/JobService")
public class JobsController{

    private static final Logger log = LoggerFactory.getLogger(JobsController.class);
    public static final String API_TOKEN_HEADER = "X-REST-API-Key";

    private final ThreadPoolExecutor highExecutor;
    private final ThreadPoolExecutor lowExecutor;
    private final TokenStore tokenStore;

    public JobsController(@High ThreadPoolExecutor highExecutor,
                         @Low ThreadPoolExecutor lowExecutor,
                         TokenStore tokenStore) {
        this.highExecutor = highExecutor;
        this.lowExecutor = lowExecutor;
        this.tokenStore = tokenStore;
    }

    @GetMapping("/token")
    public ResponseEntity<String> getToken() {
        final String token = "123X5-" + UUID.randomUUID();
        tokenStore.put(token);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/process")
    public ResponseEntity<String> process(@RequestHeader(name = API_TOKEN_HEADER, required = false) String token,
                                          @RequestParam("jobID") int jobID) {
        try {
            if (token != null && tokenStore.isValid(token)) {
                log.info("Token accepted. Execution with high priority.");
                highExecutor.execute(new JobTask("HIGH-" + jobID));
            } else {
                log.info("Invalid or missing token! {}", token);
                lowExecutor.execute(new JobTask("LOW-" + jobID));
            }
        } catch (RejectedExecutionException ree) {
            return ResponseEntity.status(503)
                    .body("Job " + jobID + " NOT submitted. " + ree.getMessage());
        }
        return ResponseEntity.ok("Job " + jobID + " successfully submitted.");
    }

    static class JobTask implements Runnable {
        private static final Logger LOG = LoggerFactory.getLogger(JobTask.class);
        private final String jobID;
        private static final int JOB_EXECUTION_TIME_MS = 10_000;

        JobTask(String id) { this.jobID = id; }

        @Override public void run() {
            try {
                LOG.info("Task started {}", jobID);
                Thread.sleep(JOB_EXECUTION_TIME_MS);
                LOG.info("Task finished {}", jobID);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                LOG.error("Task interrupted {}", jobID, ex);
            }
        }
    }
}