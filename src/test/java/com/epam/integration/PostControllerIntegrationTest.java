package com.epam.integration;

import com.epam.model.Post;
import com.epam.repository.PostRepository;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PostControllerIntegrationTest {
    @Autowired
    TestRestTemplate restTemplate;

    @Autowired
    PostRepository postRepository;

    @LocalServerPort
    private int port;

    private String baseUrl;

    @BeforeAll
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/v1/posts";
        CloseableHttpClient client = HttpClients.createDefault();
        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory(client));
    }

    @Test
    void testGetAll_noPagination() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<Post>>() {});

        List<Post> actual = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual).hasSize(20);
        assertThat(actual.get(0).getTitle()).isEqualTo("Article about Java, Spring, and Cloud");
        assertThat(actual.get(actual.size() - 1).getTitle()).isEqualTo("Optimizing Java Code for Performance");
    }

    @Test
    void testGetAll_withPagination() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl + "?size=5&page=1", HttpMethod.GET, request, new ParameterizedTypeReference<List<Post>>() {});

        List<Post> actual = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual).hasSize(5);
        assertThat(actual.get(0).getTitle()).isEqualTo("Microservices Architecture Overview");
        assertThat(actual.get(actual.size() - 1).getTitle()).isEqualTo("Advanced Java Concepts");
    }

    @Test
    void testGetAll_withFiltering() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl + "?tag=java", HttpMethod.GET, request, new ParameterizedTypeReference<List<Post>>() {});

        List<Post> actual = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual).hasSize(11);
        actual.forEach(post ->
                assertThat(post.getTags().stream()
                        .anyMatch(tag ->
                                tag.getName().equals("java")))
                        .isTrue());
    }

    @Test
    void testGetAll_withFilteringAndPagination() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<List<Post>> response = restTemplate.exchange(
                baseUrl + "?tag=java&size=7", HttpMethod.GET, request, new ParameterizedTypeReference<List<Post>>() {});

        List<Post> actual = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual).hasSize(7);
        actual.forEach(post ->
                assertThat(post.getTags().stream()
                        .anyMatch(tag ->
                                tag.getName().equals("java")))
                        .isTrue());
    }

    @Test
    void testGetOne_returnsPost() {
        ResponseEntity<Post> response = restTemplate.getForEntity(baseUrl + "/1", Post.class);

        Post actual = response.getBody();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual.getId()).isEqualTo(1);
        assertThat(actual.getTitle()).isEqualTo("Article about Java, Spring, and Cloud");
        assertThat(actual.getContent()).isEqualTo("Content about Java, Spring, and Cloud");
        assertThat(actual.getTags()).hasSize(3);
    }

    @Test
    void testGetOne_returns404() {
        ResponseEntity<Post> response = restTemplate.getForEntity(baseUrl + "/100", Post.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void testCreatePost_returnsCreatedPost() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> jsonMap = Map.of(
                "title", "NewPost",
                "content", "NewContent",
                "tags", Set.of("NewTag"));
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonMap, headers);

        ResponseEntity<Post> response = restTemplate.postForEntity(baseUrl, request, Post.class);

        Post actual = response.getBody();
        long id = Long.parseLong(
                response.getHeaders().getLocation().getPath()
                        .replace("/api/v1/posts/", ""));
        Post expected = postRepository.findById(id).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testCreatePost_returns422WithErrors() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, Object> jsonMap = Map.of(
                "title", "",
                "content", "");
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(jsonMap, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("errors", "Post content cannot be empty", "Title cannot be empty");
    }

    @Test
    void testUpdatePostTags_returnsUpdatedPost() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Set<String> jsonMap = Set.of("1", "2", "3");
        HttpEntity<Set<String>> request = new HttpEntity<>(jsonMap, headers);

        ResponseEntity<Post> response = restTemplate.exchange(
                baseUrl + "/21/tags", HttpMethod.PATCH, request, Post.class);

        Post actual = response.getBody();
        Post expected = postRepository.findById(21L).get();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testUpdatePostTags_returns422WithErrors() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Set<Map<String, Object>> jsonMap = Set.of(Collections.singletonMap("name", null));
        HttpEntity<Set<Map<String, Object>>> request = new HttpEntity<>(jsonMap, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/21/tags", HttpMethod.PATCH, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(response.getBody()).contains("errors", "Tag name cannot be empty");
    }

    @Test
    void testDeletePost() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/22", HttpMethod.DELETE, request, String.class);

        Optional<Post> expected = postRepository.findById(22L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(expected).isEmpty();
    }
}
