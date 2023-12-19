package com.epam.unit.service;

import com.epam.exception.PostNotFoundException;
import com.epam.model.Post;
import com.epam.model.Tag;
import com.epam.repository.PostRepository;
import com.epam.service.PostService;
import com.epam.service.TagService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {
    @InjectMocks
    PostService postService;

    @Mock
    TagService tagService;

    @Mock
    PostRepository postRepository;


    @Test
    void testFindByAllTags_returnsListOfPosts() {
        Set<String> tags = Set.of("tag1", "tag2");

        when(postRepository.findByAllTags(tags, tags.size(), Pageable.unpaged())).thenReturn(Page.empty());

        List<Post> actual = postService.findByAllTags(tags, Pageable.unpaged());

        assertThat(actual).isNotNull();

        verify(postRepository).findByAllTags(tags, tags.size(), Pageable.unpaged());
    }

    @Test
    void testFindById_returnsPost() {
        long testId = 1;
        Post expected = getTestPost();

        when(postRepository.findById(testId)).thenReturn(Optional.of(expected));

        Post actual = postService.findById(testId);

        assertThat(actual).isEqualTo(expected);

        verify(postRepository).findById(testId);
    }

    @Test
    void testFindById_throwsPostNotFoundException() {
        long testId = 1;

        when(postRepository.findById(testId)).thenReturn(Optional.empty());

        Assertions.assertThrows(PostNotFoundException.class, () -> postService.findById(testId));

        verify(postRepository).findById(testId);
    }

    @Test
    void testCreate_returnsPost() {
        Post request = new Post(0, "Title", "Content", Set.of(new Tag("Tag")));
        Post expected = getTestPost();

        when(tagService.enrichTagWithId(any(Tag.class))).thenReturn(new Tag(1, "Tag", new HashSet<>()));
        when(postRepository.save(any(Post.class))).thenReturn(expected);

        Post actual = postService.create(request);

        assertThat(actual).isEqualTo(expected);

        verify(tagService).enrichTagWithId(any(Tag.class));
        verify(postRepository).save(request);
    }

    @Test
    void testUpdateTagsWithSameTags_returnsExistingPost() {
        long testId = 1;
        Post expected = getTestPost();
        Set<Tag> tagsForUpdate = expected.getTags();

        when(postRepository.findById(testId)).thenReturn(Optional.of(expected));
        when(tagService.enrichTagWithId(any(Tag.class))).thenReturn(expected.getTags().stream().findAny().get());

        Post actual = postService.updateTags(testId, tagsForUpdate);

        assertThat(actual).isEqualTo(expected);

        verify(postRepository).findById(testId);
        verifyNoMoreInteractions(tagService);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void testUpdateTagsWithNewTags_returnsUpdatedPost() {
        long testId = 1;
        Post existingPost = getTestPost();
        Post expected = getUpdatedPost();
        Set<Tag> tagsForUpdate = getTagsForUpdate();

        when(postRepository.findById(testId)).thenReturn(Optional.of(existingPost));
        when(postRepository.saveAndFlush(existingPost)).thenReturn(expected);
        doNothing().when(tagService).removeTagIfNoMorePostsAssociated(any(Set.class));

        Post actual = postService.updateTags(testId, tagsForUpdate);

        assertThat(actual).isEqualTo(expected);

        verify(postRepository).findById(testId);
        verify(tagService, times(3)).enrichTagWithId(any(Tag.class));
        verify(postRepository).saveAndFlush(existingPost);
        verify(tagService).removeTagIfNoMorePostsAssociated(any(Set.class));
    }

    @Test
    void testUpdateTagsNoExistingPost_throwsPostNotFoundException() {
        long testId = 1;
        Set<Tag> tagsForUpdate = getTagsForUpdate();

        when(postRepository.findById(testId)).thenReturn(Optional.empty());

        Assertions.assertThrows(PostNotFoundException.class, () -> postService.updateTags(testId, getTagsForUpdate()));

        verifyNoInteractions(tagService);
        verifyNoMoreInteractions(postRepository);
    }

    @Test
    void testDelete_Success() {
        long testId = 1;
        Post existingPost = getTestPost();

        when(postRepository.findById(testId)).thenReturn(Optional.of(existingPost));

        doNothing().when(postRepository).deleteById(testId);
        doNothing().when(tagService).removeTagIfNoMorePostsAssociated(any(Set.class));

        postService.delete(testId);

        verify(postRepository).findById(testId);
        verify(postRepository).deleteById(testId);
        verify(tagService).removeTagIfNoMorePostsAssociated(any(Set.class));
    }

    @Test
    void testDelete_throwsPostNotFoundException() {
        long testId = 1;

        when(postRepository.findById(testId)).thenReturn(Optional.empty());

        Assertions.assertThrows(PostNotFoundException.class, () -> postService.delete(testId));

        verifyNoInteractions(tagService);
        verifyNoMoreInteractions(postRepository);
    }

    private Post getTestPost() {
        return new Post(1, "Title", "Content", Set.of(
                new Tag(1, "Tag", new HashSet<>())));
    }

    private Post getUpdatedPost() {
        return new Post(1, "Title", "Content", getTagsForUpdate());
    }

    private Set<Tag> getTagsForUpdate() {
        return Set.of(
                new Tag(0, "Tag 1", new HashSet<>()),
                new Tag(0, "Tag 2", new HashSet<>()),
                new Tag(0, "Tag 3", new HashSet<>())
        );
    }
}
