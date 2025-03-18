package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ArticleRepository articleRepository;

    public CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setContent(comment.getContent());
        dto.setAuthor(comment.getAuthor());
        dto.setDate(comment.getDate());
        dto.setLikes(comment.getLikes());
        return dto;
    }

    @Transactional
    public CommentDTO addComment(Long articleId, CommentDTO commentDTO) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Artykuł nie znaleziony"));

        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        comment.setAuthor(commentDTO.getAuthor());
        comment.setDate(commentDTO.getDate() != null ? commentDTO.getDate() : LocalDateTime.now());
        comment.setArticle(article);

        comment = commentRepository.save(comment);
        return toDTO(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentDTO> getCommentsForArticle(Long articleId, Pageable pageable) {
        return commentRepository.findByArticleId(articleId, pageable)
                .map(this::toDTO);
    }

    @Transactional
    public CommentDTO likeComment(Long id) {
        int maxRetries = 3;
        int retryCount = 0;

        while (retryCount < maxRetries) {
            try {
                Comment comment = commentRepository.findByIdWithLock(id)
                        .orElseThrow(() -> new RuntimeException("Komentarz nie znaleziony"));

                comment.incrementLikes();
                comment = commentRepository.save(comment);

                return toDTO(comment);
            } catch (OptimisticLockingFailureException e) {
                retryCount++;
                if (retryCount >= maxRetries) {
                    throw new RuntimeException("Nie można polubić komentarza po wielu próbach");
                }
            }
        }

        throw new RuntimeException("Nie można polubić komentarza");
    }
}
