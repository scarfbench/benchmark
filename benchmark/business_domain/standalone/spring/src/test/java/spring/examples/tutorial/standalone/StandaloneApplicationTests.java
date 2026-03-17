package spring.examples.tutorial.standalone;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.logging.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import spring.examples.tutorial.standalone.service.StandaloneService;

@SpringBootTest
class StandaloneApplicationTests {

	private static final Logger logger = Logger.getLogger("standalone.service");

	@Autowired
	private StandaloneService standaloneService;

	@Test
	void contextLoads() {}

	@Test
	public void testReturnMessage() throws Exception {
		logger.info("Testing standalone.service.StandaloneService.returnMessage()");
		String expResult = "Greetings!";
		String result = standaloneService.returnMessage();
		assertEquals(expResult, result);
	}

}
