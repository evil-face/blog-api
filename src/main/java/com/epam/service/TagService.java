package com.epam.service;

import com.epam.model.Tag;
import com.epam.repository.TagRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class TagService {
    private final TagRepository tagRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(TagService.class);

    @Autowired
    public TagService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
    }

    @Transactional
    public Tag enrichTagWithId(Tag tag) {
        String normalizedName = tag.getName().trim().toLowerCase();
        tag.setName(normalizedName);

        return tagRepository.findByName(normalizedName).orElseGet(() -> tagRepository.save(tag));
    }

    @Transactional
    public void removeTagIfNoMorePostsAssociated(Set<Tag> tags) {
        tags.stream()
                .filter(tag -> tag.getPosts().isEmpty())
                .forEach(tag -> {
                    tagRepository.delete(tag);
                    LOGGER.info("Tag '{}' became unassociated and deleted", tag.getName());
                });
    }
}
