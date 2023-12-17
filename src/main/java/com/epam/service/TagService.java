package com.epam.service;

import com.epam.model.Tag;
import com.epam.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
public class TagService {
    private final TagRepository tagRepository;

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
                .forEach(tagRepository::delete);
    }
}
