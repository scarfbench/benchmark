package org.springframework.tutorial.producerfields.controller;

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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
public class ListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getMainPage() throws Exception {
        mockMvc
                .perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("toDoForm"));
    }

    @Test
    void shouldRejectInvalidPayload() throws Exception {
        mockMvc
                .perform(post("/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().hasErrors())
                .andExpect(model().attributeHasErrors("toDoForm"));
    }

    @Test
    @Transactional
    void createTodoElement() throws Exception {
        mockMvc
                .perform(post("/create").param("inputString", "Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"))
                .andExpect(flash().attributeExists("toDoForm"));
    }

    @Test
    @Transactional
    void viewTodoList() throws Exception {
        // Add some test data
        mockMvc
                .perform(post("/create").param("inputString", "Test 1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));

        mockMvc
                .perform(post("/create").param("inputString", "Test 2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", "/"));

        mockMvc
                .perform(get("/todolist"))
                .andExpect(status().isOk())
                .andExpect(view().name("todolist"))
                .andExpect(model().attribute("toDos", Matchers.hasSize(2)))
                .andExpect(model().attribute("toDos", Matchers
                        .hasItems(Matchers.hasProperty("taskText", Matchers.equalTo("Test 1")),
                                Matchers.hasProperty("taskText", Matchers.equalTo("Test 2")))));
    }

}
