package com.epam.controller;

import com.epam.model.Post;
import com.epam.model.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.List;
import java.util.Set;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Blog Post", description = "the blog post API")
public interface PostApi {
    @Operation(summary = "Get all posts with or without pagination/sorting and filtering by tags")
    public List<Post> getAll(Set<String> tags, Pageable pageable);

    @Operation(summary = "Get one post")
    public ResponseEntity<Post> getOne(long id);

    @Operation(summary = "Create new post",
            responses = {
                    @ApiResponse(responseCode = "201", description = "New post created successfully"),
                    @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<Post> createPost(Post post, BindingResult bindingResult);

    @Operation(summary = "Update tags of the post. You can provide list of tag objects or a simple string list of names",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tags updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Post not found"),
                    @ApiResponse(responseCode = "422", description = "Bad input, check body for error messages")})
    public ResponseEntity<Post> updatePostTags(long id, Set<Tag> tags);

    @Operation(summary = "Delete the post",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Post deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Post not found")})
    public ResponseEntity<String> deletePost(long id);
}
