package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @PostMapping("/articles/{articleId}/comments")
    public ResponseEntity<CommentDTO> addComment(
            @PathVariable Long articleId,
            @RequestBody CommentDTO commentDTO) {

        CommentDTO createdComment = commentService.addComment(articleId, commentDTO);
        return new ResponseEntity<>(createdComment, HttpStatus.CREATED);
    }

    @GetMapping("/articles/{articleId}/comments")
    public ResponseEntity<Page<CommentDTO>> getCommentsForArticle(
            @PathVariable Long articleId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<CommentDTO> comments = commentService.getCommentsForArticle(articleId, pageable);
        return ResponseEntity.ok(comments);
    }

    @PostMapping("/comments/{id}/likes")
    public ResponseEntity<CommentDTO> likeComment(@PathVariable Long id) {
        CommentDTO likedComment = commentService.likeComment(id);
        return ResponseEntity.ok(likedComment);
    }
}
