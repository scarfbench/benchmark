package org.example.realworldapi.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "TAG_RELATIONSHIP")
public class TagRelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(updatable = false)
    private ArticleEntity article;

    @ManyToOne
    @JoinColumn(updatable = false)
    private TagEntity tag;

    public TagRelationshipEntity(ArticleEntity article, TagEntity tag) {
        this.article = article;
        this.tag = tag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TagRelationshipEntity that = (TagRelationshipEntity) o;
        return Objects.equals(article, that.article) && Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(article, tag);
    }
}
