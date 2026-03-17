package quarkus.examples.tutorial;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.logging.Logger;

@QuarkusTest
class StandaloneBeanTest {
    private static final Logger logger = Logger.getLogger("standalone");

    @Inject
    private StandaloneBean standaloneBean;

    @Test
    public void testReturnMessage() throws Exception {
        logger.info("Testing StandaloneBean.returnMessage()");
        String expResult = "Greetings!";
        String result = standaloneBean.returnMessage();
        assertEquals(expResult, result);
    }
}
