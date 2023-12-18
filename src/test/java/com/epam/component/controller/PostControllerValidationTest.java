package com.epam.component.controller;

import com.epam.controller.PostController;
import com.epam.model.Post;
import com.epam.model.Tag;
import com.epam.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostController.class)
public class PostControllerValidationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper mapper;

    @MockBean
    PostService postService;

    @Test
    void testCreatePost_returns_422() throws Exception {
        Post requestBody = new Post();
        String request = mapper.writeValueAsString(requestBody);

        mockMvc.perform(post("/api/v1/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().is(422))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        content().string(containsString("errors")),
                        content().string(containsString("Title cannot be empty")),
                        content().string(containsString("Post content cannot be empty"))
                        );
    }

    @Test
    void testUpdatePostTags_returns_422() throws Exception {
        Set<Tag> requestBody = Set.of(new Tag(0, "", null));
        String request = mapper.writeValueAsString(requestBody);

        mockMvc.perform(patch("/api/v1/posts/1/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().is(422))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpectAll(
                        content().string(containsString("errors")),
                        content().string(containsString("Tag name cannot be empty"))
                );
    }
}
