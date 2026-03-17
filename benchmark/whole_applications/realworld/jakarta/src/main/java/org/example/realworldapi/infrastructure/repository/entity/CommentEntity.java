package org.example.realworldapi.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.realworldapi.domain.model.comment.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "COMMENTS")
@NamedQueries({
        @NamedQuery(name = "CommentEntity.findByArticle_Id", query = "select c from CommentEntity c where c.article.id = :articleId"),
        @NamedQuery(name = "CommentEntity.findByIdAndAuthor_Id", query = "select c from CommentEntity c where c.id = :commentId and c.author.id = :authorId")
})
public class CommentEntity {

    @Id
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID id;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private String body;

    @ManyToOne
    @JoinColumn(name = "article_id", referencedColumnName = "id", nullable = false)
    private ArticleEntity article;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
    private UserEntity author;

    public CommentEntity(UserEntity author, ArticleEntity article, Comment comment) {
        this.id = comment.getId();
        this.body = comment.getBody();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.article = article;
        this.author = author;
    }
}
