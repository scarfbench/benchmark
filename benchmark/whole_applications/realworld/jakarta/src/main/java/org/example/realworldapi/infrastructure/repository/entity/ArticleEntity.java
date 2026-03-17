package org.example.realworldapi.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.realworldapi.domain.model.article.Article;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "ARTICLES")
@NamedQueries({
        @NamedQuery(name = "ArticleEntity.findBySlugIgnoreCaseAndAuthor_Id", query = "select a from ArticleEntity a where upper(a.slug) = :slug and a.author.id = :authorId")})
public class ArticleEntity {

    @Id
    @Column(nullable = false, columnDefinition = "uuid")
    private UUID id;

    private String slug;
    private String title;
    private String description;
    private String body;
    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "author_id", referencedColumnName = "id", nullable = false)
    private UserEntity author;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<CommentEntity> comments;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<TagRelationshipEntity> tags;

    @OneToMany(mappedBy = "article", fetch = FetchType.LAZY)
    private List<FavoriteRelationshipEntity> favorites;

    public ArticleEntity(Article article, UserEntity author) {
        this.id = article.getId();
        this.createdAt = article.getCreatedAt();
        this.author = author;
        update(article);
    }

    public void update(Article article) {
        this.slug = article.getSlug();
        this.title = article.getTitle();
        this.description = article.getDescription();
        this.body = article.getBody();
        this.updatedAt = article.getUpdatedAt();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ArticleEntity that = (ArticleEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
