package org.example;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT a FROM Article a WHERE a.id = :id")
    Optional<Article> findByIdWithLock(Long id);

    Page<Article> findAll(Pageable pageable);
}