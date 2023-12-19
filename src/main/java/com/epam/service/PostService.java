package com.epam.service;

import com.epam.exception.PostNotFoundException;
import com.epam.model.Post;
import com.epam.model.Tag;
import com.epam.repository.PostRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PostService {
    private final PostRepository postRepository;
    private final TagService tagService;
    private static final Logger LOGGER = LoggerFactory.getLogger(PostService.class);

    @Autowired
    public PostService(PostRepository postRepository, TagService tagService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
    }

    public List<Post> findAll(Pageable pageRequest) {
        return postRepository.findAll(pageRequest).getContent();
    }

    public List<Post> findByAllTags(Set<String> tags, Pageable pageRequest) {
        return postRepository.findByAllTags(tags, tags.size(), pageRequest).getContent();
    }

    public Post findById(long id) {
        return postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
    }

    @Transactional
    public Post create(Post post) {
        Set<Tag> verifiedTags = post.getTags().stream().map(tagService::enrichTagWithId).collect(Collectors.toSet());
        post.setTags(verifiedTags);

        Post createdPost = postRepository.save(post);
        LOGGER.info("Saved new post with id '{}' and title '{}'", createdPost.getId(), createdPost.getTitle());

        return createdPost;
    }

    @Transactional
    public Post updateTags(long id, Set<Tag> tags) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));
        Set<Tag> oldTags = post.getTags();

        Set<Tag> verifiedTags = tags.stream().map(tagService::enrichTagWithId).collect(Collectors.toSet());

        if (verifiedTags.equals(oldTags)) {
            LOGGER.info("Post with id '{}' already has these tags, not updated", post.getId());

            return post;
        }

        post.setTags(verifiedTags);
        Post updatedPost = postRepository.saveAndFlush(post);

        tagService.removeTagIfNoMorePostsAssociated(oldTags);

        LOGGER.info("Updated tags of post with id '{}'. It now has total of {} tags", updatedPost.getId(), verifiedTags.size());

        return updatedPost;
    }

    @Transactional
    public void delete(long id) {
        Post post = postRepository.findById(id).orElseThrow(() -> new PostNotFoundException(id));

        postRepository.deleteById(id);
        tagService.removeTagIfNoMorePostsAssociated(post.getTags());

        LOGGER.info("Deleted post with id '{}' and title '{}'", post.getId(), post.getTitle());
    }
}
