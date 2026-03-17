package org.springframework.tutorial.guessnumber.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class GuessNumberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMainPage() throws Exception {
        mockMvc
                .perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("userNumberBean"));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc
                .perform(
                        post("/guess")
                                .param("number", "65")
                                .param("minimum", "0")
                                .param("maximum", "100")
                                .param("remainingGuesses", "10")
                                .param("userNumber", "101"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("userNumberBean"));
    }

    @Test
    void guessNumber() throws Exception {
        mockMvc
                .perform(
                        post("/guess")
                                .param("number", "65")
                                .param("minimum", "0")
                                .param("maximum", "100")
                                .param("remainingGuesses", "10")
                                .param("userNumber", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andExpect(request().sessionAttribute("userNumberBean",
                        Matchers.hasProperty("remainingGuesses", Matchers.equalTo(9))))
                .andExpect(request().sessionAttribute("userNumberBean",
                        Matchers.hasProperty("minimum", Matchers.equalTo(11))));
    }

    @Test
    void resetShouldRedirect() throws Exception {
        mockMvc
                .perform(post("/reset")
                        .param("number", "65")
                        .param("minimum", "11")
                        .param("maximum", "100")
                        .param("remainingGuesses", "9")
                        .param("userNumber", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andExpect(request().sessionAttribute("userNumberBean",
                        Matchers.hasProperty("remainingGuesses", Matchers.equalTo(10))));
    }

}
