// src/test/java/spring/tutorial/mood/web/MoodControllerTest.java
package spring.tutorial.mood.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MoodControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void reportLoads() throws Exception {
        mvc.perform(get("/report").param("name", "Duke"))
           .andExpect(status().isOk())
           .andExpect(content().string(org.hamcrest.Matchers.containsString("Duke's mood is")))
           .andExpect(content().string(org.hamcrest.Matchers.containsString("/images/duke.waving.gif")));
    }
}
