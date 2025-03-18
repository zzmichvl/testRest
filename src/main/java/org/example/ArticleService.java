package org.example;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    public ArticleDTO toDTO(Article article) {
        ArticleDTO dto = new ArticleDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setContent(article.getContent());
        dto.setAuthor(article.getAuthor());
        dto.setDate(article.getDate());
        dto.setTopic(article.getTopic());
        dto.setLikes(article.getLikes());
        dto.setCommentCount(article.getCommentCount());
        return dto;
    }

    public Article fromDTO(ArticleDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setAuthor(dto.getAuthor());
        article.setDate(dto.getDate() != null ? dto.getDate() : LocalDateTime.now());
        article.setTopic(dto.getTopic());
        return article;
    }

    @Transactional
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        Article article = fromDTO(articleDTO);
        article = articleRepository.save(article);
        return toDTO(article);
    }

    @Transactional(readOnly = true)
    public Page<ArticleDTO> getArticles(Pageable pageable) {
        return articleRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Transactional(readOnly = true)
    public ArticleDTO getArticle(Long id) {
        return articleRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Artykuł nie znaleziony"));
    }

    @Transactional
    public ArticleDTO likeArticle(Long id) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Article article = articleRepository.findByIdWithLock(id)
                        .orElseThrow(() -> new RuntimeException("Artykuł nie znaleziony"));

                article.incrementLikes();
                article = articleRepository.save(article);

                return toDTO(article);
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Nie można polubić artykułu po wielu próbach");
                }
            }
        }

        throw new RuntimeException("Nie można polubić artykułu");
    }
}
