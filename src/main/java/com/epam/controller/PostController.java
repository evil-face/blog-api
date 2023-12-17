package com.epam.controller;

import com.epam.model.Post;
import com.epam.model.Tag;
import com.epam.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/posts")
@Validated
public class PostController {
    private final PostService postService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<Post> getAll(@RequestParam(value = "tag", required = false) Set<String> tags, Pageable pageable) {
        if (tags != null && !tags.isEmpty()) {
            return postService.findByAllTags(tags, pageable);
        }

        return postService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getOne(@PathVariable("id") long id) {
        return ResponseEntity.ok(postService.findById(id));
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@RequestBody @Valid Post post, BindingResult bindingResult) {
        Post createdPost = postService.create(post);
        URI location = UriComponentsBuilder.fromPath("/api/v1/posts/{id}").buildAndExpand(createdPost.getId()).toUri();

        return ResponseEntity.created(location).body(createdPost);
    }

    @PatchMapping("/{id}/tags")
    public ResponseEntity<Post> updatePostTags(@PathVariable("id") long id, @RequestBody @Valid Set<Tag> tags) {
        Post updatedPost = postService.updateTags(id, tags);

        return ResponseEntity.ok(updatedPost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePost(@PathVariable("id") long id) {
        postService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
