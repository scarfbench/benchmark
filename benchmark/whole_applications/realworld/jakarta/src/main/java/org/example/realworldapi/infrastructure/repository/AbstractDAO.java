package org.example.realworldapi.infrastructure.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.infrastructure.repository.entity.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractDAO<ENTITY, ID> {

    @PersistenceContext
    protected EntityManager em;

    protected UserEntity findUserEntityById(UUID id) {
        return em.find(UserEntity.class, id);
    }

    protected TagEntity findTagEntityById(UUID id) {
        return em.find(TagEntity.class, id);
    }

    protected ArticleEntity findArticleEntityById(UUID id) {
        return em.find(ArticleEntity.class, id);
    }

    protected CommentEntity findCommentEntityById(UUID id) {
        return em.find(CommentEntity.class, id);
    }

    protected FavoriteRelationshipEntity findFavoriteRelationshipEntityByKey(
            FavoriteRelationship favoriteRelationship) {

        Query query = em.createNamedQuery("FVREfindByArticleAndUserID");
        query.setParameter("articleId", favoriteRelationship.getArticle().getId());
        query.setParameter("currentUserId", favoriteRelationship.getUser().getId());

        List<FavoriteRelationshipEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            return (resultList.get(0));
        } else {
            return null;
        }
    }

    protected boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    protected List<String> toUpperCase(List<String> subjectList) {
        return subjectList.stream().map(String::toUpperCase).collect(Collectors.toList());
    }
}
