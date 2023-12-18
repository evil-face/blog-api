package com.epam.unit.controller;

import com.epam.controller.PostController;
import com.epam.exception.PostNotFoundException;
import com.epam.model.Post;
import com.epam.model.Tag;
import com.epam.service.PostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostControllerTest {
    @InjectMocks
    PostController postController;
    @Mock
    PostService postService;

    @Test
    void testGetAll_returnsListOfPosts() {
        Set<String> requestTags  = Set.of("Tag");
        List<Post> expectedList = getListOfTestPosts();

        when(postService.findByAllTags(requestTags, Pageable.unpaged())).thenReturn(expectedList);

        ResponseEntity<List<Post>> response = postController.getAll(requestTags, Pageable.unpaged());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedList);

        verify(postService).findByAllTags(requestTags, Pageable.unpaged());
        verifyNoMoreInteractions(postService);
    }

    @Test
    void testGetAll_returnsEmptyListOfPosts() {
        when(postService.findAll(Pageable.unpaged())).thenReturn(new ArrayList<>());

        ResponseEntity<List<Post>> response = postController.getAll(null, Pageable.unpaged());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();

        verify(postService).findAll(Pageable.unpaged());
        verifyNoMoreInteractions(postService);
    }

    @Test
    void testGetOne_returnsOkResponseEntity() {
        long testId = 1;
        when(postService.findById(testId)).thenReturn(getTestPost());

        ResponseEntity<Post> response = postController.getOne(testId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(getTestPost());

        verify(postService).findById(testId);
    }

    @Test
    void testGetOne_throwsPostNotFoundException() {
        long testId = 1;
        when(postService.findById(testId)).thenThrow(PostNotFoundException.class);

        Assertions.assertThrows(PostNotFoundException.class, () -> postController.getOne(testId));

        verify(postService).findById(testId);
    }

    @Test
    void testCreatePost_returnsCreatedResponseEntity() {
        Post requestBody = new Post(0, "Title", "Content", null);
        Post expected = getTestPost();
        BindingResult bindingResult = new BeanPropertyBindingResult(requestBody, "requestBody");
        URI expectedLocation = UriComponentsBuilder.fromUriString(
                "http://localhost:8080/api/v1/posts/" + expected.getId()).build().toUri();

        when(postService.create(requestBody)).thenReturn(expected);

        ResponseEntity<Post> response = postController.createPost(
                requestBody, bindingResult, UriComponentsBuilder.fromUriString("http://localhost:8080"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getHeaders().getLocation()).isEqualTo(expectedLocation);
        assertThat(response.getBody()).isEqualTo(expected);

        verify(postService).create(requestBody);
    }

    @Test
    void testUpdatePostTags_returnsOkResponseEntity() {
        long testId = 1;
        Set<Tag> requestBody = Set.of(
                new Tag(2, "Tag 2", new HashSet<>()),
                new Tag(3, "Tag 3", new HashSet<>())
        );
        Post expected = getUpdatedPost();

        when(postService.updateTags(testId, requestBody)).thenReturn(expected);

        ResponseEntity<Post> response = postController.updatePostTags(testId, requestBody);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expected);

        verify(postService).updateTags(testId, requestBody);
    }

    @Test
    void testDeletePost_returnsNoContentResponseEntity() {
        long testId = 1;

        doNothing().when(postService).delete(testId);

        ResponseEntity<String> response = postController.deletePost(testId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        verify(postService).delete(testId);
    }

    private Post getTestPost() {
        return new Post(1, "Title", "Content", Set.of(
                new Tag(1, "Tag", new HashSet<>())));
    }

    private Post getUpdatedPost() {
        return new Post(1, "Title", "Content",
                Set.of(
                        new Tag(2, "Tag 2", new HashSet<>()),
                        new Tag(3, "Tag 3", new HashSet<>())));
    }

    private List<Post> getListOfTestPosts() {
        return List.of(
                new Post(1, "Title 1", "Content 1", Set.of(
                        new Tag(1, "Tag 1", new HashSet<>()))),
                new Post(2, "Title 2", "Content 2", Set.of(
                        new Tag(2, "Tag 2", new HashSet<>()))),
                new Post(3, "Title 3", "Content 3", Set.of(
                        new Tag(3, "Tag 3", new HashSet<>())))
        );
    }
}
