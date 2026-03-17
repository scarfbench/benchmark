package org.springframework.tutorial.encoder.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("alternative")
public class AlternateCoderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMainPage() throws Exception {
        mockMvc
                .perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("coderForm"));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc
                .perform(
                        post("/encode")
                                .param("inputString", "aa")
                                .param("transVal", "124"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("coderForm"));
    }

    @Test
    void encodeString() throws Exception {
        mockMvc
                .perform(
                        post("/encode")
                                .param("inputString", "aa")
                                .param("transVal", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andExpect(flash().attributeExists("coderForm"))
                .andExpect(flash().attribute("coderForm",
                        Matchers.hasProperty("codedString", Matchers
                                .equalTo("input string is aa, shift value is 2"))));
    }

    @Test
    void resetShouldRedirect() throws Exception {
        mockMvc
                .perform(post("/reset"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andExpect(flash().attributeExists("coderForm"));
    }

}
