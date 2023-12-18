package com.epam.unit.service;

import com.epam.model.Post;
import com.epam.model.Tag;
import com.epam.repository.TagRepository;
import com.epam.service.TagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {
    @InjectMocks
    TagService tagService;

    @Mock
    TagRepository tagRepository;

    @Test
    void testEnrichTagWithId_returnsExistingTag() {
        Tag tag = new Tag(0, " NamE ", null);
        String normalizedName = tag.getName().trim().toLowerCase();
        Tag expected = getTestTag();

        when(tagRepository.findByName(normalizedName)).thenReturn(Optional.of(expected));

        Tag actual = tagService.enrichTagWithId(tag);

        assertThat(actual).isEqualTo(expected);

        verify(tagRepository).findByName(normalizedName);
        verifyNoMoreInteractions(tagRepository);
    }

    @Test
    void testEnrichTagWithId_returnsCreatedTag() {
        Tag tag = new Tag(0, " NamE ", null);
        String normalizedName = tag.getName().trim().toLowerCase();
        Tag expected = getTestTag();

        when(tagRepository.save(any(Tag.class))).thenReturn(expected);
        when(tagRepository.findByName(normalizedName)).thenReturn(Optional.empty());

        Tag actual = tagService.enrichTagWithId(tag);

        assertThat(actual).isEqualTo(expected);

        verify(tagRepository).save(any(Tag.class));
        verify(tagRepository).findByName(normalizedName);
    }

    @Test
    void testRemoveTagIfNoMorePostsAssociated() {
        Set<Tag> tags = getTagsForDeletion();

        doNothing().when(tagRepository).delete(any(Tag.class));

        tagService.removeTagIfNoMorePostsAssociated(tags);

        verify(tagRepository, times(2)).delete(any(Tag.class));
    }

    private Tag getTestTag() {
        return new Tag(1, "name", new HashSet<>());
    }

    private Set<Tag> getTagsForDeletion() {
        return Set.of(
                new Tag(1, "Tag 1", new HashSet<>()),
                new Tag(2, "Tag 2", new HashSet<>()),
                new Tag(3, "Tag 3", Set.of(new Post()))
        );
    }
}
