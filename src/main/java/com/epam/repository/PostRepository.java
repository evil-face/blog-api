package com.epam.repository;

import com.epam.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
           SELECT p FROM Post p JOIN p.tags t
           WHERE t.name IN :tagNames
           GROUP BY p
           HAVING COUNT(DISTINCT t) = :tagCount
           """)
    Page<Post> findByAllTags(@Param("tagNames") Set<String> tagNames,
                             @Param("tagCount") int tagCount,
                             Pageable pageable);
}
