package jakarta.tutorial.concurrency.jobs.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.ws.rs.core.Response;
import java.util.logging.Logger;

@ApplicationScoped
public class JobClient {
    private static final Logger logger = Logger.getLogger(JobClient.class.getName());

    @Inject
    @RestClient
    JobServiceClient jobService;

    private String token;
    private int jobID;

    public boolean submit(int jobID, String token) {
        Response response = jobService.processJob(jobID, token);
        boolean ok = response.getStatus() == 200;
        logger.info(ok
                ? String.format("Job %d successfully submitted", jobID)
                : String.format("Job %d was NOT submitted", jobID));
        return ok;
    }
    private void clear() {
        this.token = "";
    }
    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * @return the jobID
     */
    public int getJobID() {
        return jobID;
    }

    /**
     * @param jobID the jobID to set
     */
    public void setJobID(int jobID) {
        this.jobID = jobID;
    }
}